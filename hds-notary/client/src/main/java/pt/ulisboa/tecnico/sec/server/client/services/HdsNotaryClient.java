package pt.ulisboa.tecnico.sec.server.client.services;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.log4j.Logger;
import pt.ulisboa.tecnico.sec.services.crypto.CryptoUtils;
import pt.ulisboa.tecnico.sec.services.data.Good;
import pt.ulisboa.tecnico.sec.services.data.Transaction;
import pt.ulisboa.tecnico.sec.services.data.User;
import pt.ulisboa.tecnico.sec.services.exceptions.GoodIsNotOnSaleException;
import pt.ulisboa.tecnico.sec.services.exceptions.InvalidSignatureException;
import pt.ulisboa.tecnico.sec.services.exceptions.ServerException;
import pt.ulisboa.tecnico.sec.services.interfaces.client.ClientService;
import pt.ulisboa.tecnico.sec.services.interfaces.client.ReadBonarService;
import pt.ulisboa.tecnico.sec.services.interfaces.server.HdsNotaryService;
import pt.ulisboa.tecnico.sec.services.properties.HdsProperties;
import pt.ulisboa.tecnico.sec.services.utils.Constants;

public class HdsNotaryClient extends UnicastRemoteObject implements ReadBonarService, Serializable {

    private static final Logger logger = Logger.getLogger(HdsNotaryClient.class);

    private static RSAPrivateKey privateKey;
    private static Map<String, RSAPublicKey> serverPublicKey = new HashMap<>();
    private static Map<String, PublicKey> notaryPublicKey = new HashMap<>();
    private static Map<String, HdsNotaryService> hdsNotaryService = new HashMap<>();
    private static int readId = 0;
    private static Map<Integer, Map<String, Good>> answers = new ConcurrentHashMap<>();
    private static CountDownLatch latch;
    private static Good lastGoodRead;

    protected HdsNotaryClient() throws RemoteException {
    }

    public static void init(User user, String username, String password) {
        // Get user private key
        privateKey = HdsProperties.getClientPrivateKey(user.getName(), password);

        // Fill servers public keys
        fillServerPublicKey();

        // Fill services RMI
        fillHdsNotaryService();

        // Fill notary public keys
        fillNotaryPublicKey();

        // Setup P2P service && ReadBonar
        try {
            ClientService clientService = new ClientServiceImpl(hdsNotaryService, privateKey);
            final int registryPort = HdsProperties.getClientPort(username);
            final Registry reg = LocateRegistry.createRegistry(registryPort);

            reg.rebind("ClientService", clientService);
            reg.rebind("ReadBonarService", new HdsNotaryClient());

            logger.info("ClientService up at port " + registryPort);
        } catch (RemoteException e) {
            logger.error(e);
        }

    }

    public static Optional<Transaction> buyGood(User user, String goodId)
        throws RemoteException, ServerException, NoSuchAlgorithmException, InvalidKeyException,
               SignatureException, NotBoundException, MalformedURLException, InterruptedException {

        Good good = getStateOfGood(user, goodId).get();

        if (!good.isOnSale()) {
            throw new GoodIsNotOnSaleException("The good with id " + goodId + " is not on sale.");
        }

        // Intention to buy
        final List<Transaction> transactionResponse = intentionToBuy(user, good);

        // Buy good
        ClientService clientServiceSeller =
            (ClientService) Naming.lookup(HdsProperties.getClientUri(good.getOwnerId()));

        for (Transaction transaction : transactionResponse) {
            transaction.setBuyerSignature(CryptoUtils.makeDigitalSignature(privateKey,
                transaction.getTransactionId(),
                transaction.getSellerId(),
                transaction.getBuyerId(),
                transaction.getGoodId()));
        }

        List<Transaction> transactions = clientServiceSeller.buy(goodId, transactionResponse);

        // Verify Signatures
        for (Transaction transaction : transactions) {
            if (!CryptoUtils.verifyDigitalSignature(notaryPublicKey.get(transaction.getServerId()),
                transaction.getNotarySignature(),
                transaction.getTransactionId(),
                transaction.getSellerId(),
                transaction.getBuyerId(),
                transaction.getSellerSignature(),
                transaction.getBuyerSignature())) {
                throw new InvalidSignatureException("BuyGood (server " + transaction.getServerId() + "): Transaction has signature invalid.");
            }
        }

        return transactions.stream().findFirst();
    }

    public static List<Transaction> intentionToBuy(User user, Good good) throws InterruptedException {
        ConcurrentLinkedDeque<Transaction> transactionResponse = new ConcurrentLinkedDeque<>();
        CountDownLatch latch = new CountDownLatch((Constants.N + Constants.F) / 2 + 1);

        for (Map.Entry<String, HdsNotaryService> entry : hdsNotaryService.entrySet()) {
            CompletableFuture.runAsync(() -> {
                try {
                    String nonce = entry.getValue().getNonce(user.getUserId());
                    String signature = CryptoUtils.makeDigitalSignature(privateKey, good.getOwnerId(), user.getUserId(),
                        good.getGoodId(), nonce);

                    final ImmutablePair<Transaction, String> response = entry.getValue()
                        .intentionToBuy(
                            good.getOwnerId(),
                            user.getUserId(),
                            good.getGoodId(),
                            nonce,
                            signature);

                    // Verify Signature
                    if (!CryptoUtils.verifyDigitalSignature(serverPublicKey.get(entry.getKey()),
                        response.getRight(),
                        response.getLeft().getTransactionId(), nonce)) {
                        throw new InvalidSignatureException("IntentionToBuy: Server has signature invalid.");
                    }

                    transactionResponse.add(response.getLeft());
                } catch (RemoteException | NoSuchAlgorithmException | InvalidKeyException | SignatureException | ServerException e) {
                    logger.error("Error on server id " + entry.getKey(), e);
                }
                latch.countDown();
            });

        }

        latch.await();
        final ArrayList<Transaction> transactions = new ArrayList<>(transactionResponse);
        if (transactions.size() <= (Constants.N + Constants.F) / 2) {
            return new ArrayList<>();
        }
        return transactions;
    }

    public static boolean intentionToSell(User user, String goodId) throws InterruptedException {
        ConcurrentLinkedDeque<Boolean> returnValue = new ConcurrentLinkedDeque<>();
        CountDownLatch latch = new CountDownLatch((Constants.N + Constants.F) / 2 + 1);

        final Optional<Good> stateOfGood = getStateOfGood(user, goodId);

        for (Map.Entry<String, HdsNotaryService> entry : hdsNotaryService.entrySet()) {
            CompletableFuture.runAsync(() -> {
                try {

                    String nonce = entry.getValue().getNonce(user.getUserId());
                    int timeStamp = stateOfGood.get().getTimeStamp() + 1;

                    String signature = CryptoUtils.makeDigitalSignature(privateKey, user.getUserId(), goodId, nonce, Integer.toString(timeStamp));

                    ImmutablePair<Boolean, String> response = entry.getValue().intentionToSell(user.getUserId(), goodId,
                        nonce, timeStamp, signature);

                    // Verify Signature
                    if (!CryptoUtils.verifyDigitalSignature(serverPublicKey.get(entry.getKey()), response.getRight(),
                        goodId,
                        Boolean.toString(response.getLeft()), nonce)) {
                        throw new InvalidSignatureException("Server has signature invalid.");
                    }

                    if (response.getLeft()) {
                        returnValue.add(response.getLeft());
                    }
                } catch (RemoteException | NoSuchAlgorithmException | InvalidKeyException | SignatureException | ServerException e) {
                    logger.error("Error on server id " + entry.getKey(), e);
                }
                latch.countDown();
            });
        }
        latch.await();

        return (returnValue.size() > (Constants.N + Constants.F) / 2);
    }

    public static Optional<Good> getStateOfGood(User user, String goodId) throws InterruptedException {
        CountDownLatch latchResponses = new CountDownLatch((Constants.N + Constants.F) / 2 + 1);
        AtomicInteger successfulRequests = new AtomicInteger();
        latch = new CountDownLatch(1);
        // Clear answers and increment readId
        answers.clear();
        readId++;

        for (Map.Entry<String, HdsNotaryService> entry : hdsNotaryService.entrySet()) {
            CompletableFuture.runAsync(() -> {
                try {
                    String nonce = entry.getValue().getNonce(user.getUserId());

                    String signature = CryptoUtils.makeDigitalSignature(privateKey, user.getUserId(), goodId, nonce,
                        Integer.toString(readId));

                    entry.getValue().getStateOfGood(
                        user.getUserId(),
                        goodId,
                        nonce,
                        readId,
                        signature);

                    successfulRequests.getAndIncrement();
                } catch (RemoteException | NoSuchAlgorithmException | InvalidKeyException | SignatureException | ServerException | NotBoundException | MalformedURLException e) {
                    logger.error("Error on server id " + entry.getKey(), e);
                }
                latchResponses.countDown();
            }).exceptionally(e -> {
                logger.error(e);
                return null;
            });
        }
        // Wait responses
        latchResponses.await();

        if (successfulRequests.get() <= (Constants.N + Constants.F) / 2) {
            completeGetStateOfGood(user, goodId);
            return Optional.empty();
        }

        // Wait for a response with quorum
        while (true) {
            // Wait for 1 second at max and test if exists.
            if (latch.await(1, TimeUnit.SECONDS)) {
                completeGetStateOfGood(user, goodId);
                return Optional.of(lastGoodRead);
            }
            for (Map.Entry<Integer, Map<String, Good>> entry : answers.entrySet()) {
                final Collection<Good> goods = entry.getValue().values();
                Set<Good> uniqueSet = new HashSet<>(goods);
                for (Good temp : uniqueSet) {
                    if (temp == null) {
                        continue;
                    }
                    if (Collections.frequency(goods, temp) > (Constants.N + Constants.F) / 2) {
                        completeGetStateOfGood(user, goodId);
                        return Optional.of(temp);
                    }
                }
            }
        }
    }

    private static void completeGetStateOfGood(User user, String goodId) {
        for (Map.Entry<String, HdsNotaryService> serviceEntry : hdsNotaryService.entrySet()) {
            CompletableFuture.runAsync(() -> {
                try {
                    serviceEntry.getValue().completeGetStateOfGood(user.getUserId(), goodId);
                } catch (RemoteException | ServerException e) {
                    logger.error("Error on server id " + serviceEntry.getKey(), e);
                }
            });
        }
    }

    private static void fillServerPublicKey() {
        for (int i = 0; i < Constants.N; i++) {
            String id = "" + i;
            serverPublicKey.put(id, HdsProperties.getServerPublicKey(id));
        }
    }

    private static void fillHdsNotaryService() {
        for (int i = 0; i < Constants.N; i++) {
            String id = "" + i;
            try {
                hdsNotaryService.put(id, (HdsNotaryService) Naming.lookup(HdsProperties.getServerUri(id)));
            } catch (NotBoundException | MalformedURLException | RemoteException e) {
                logger.error(e);
            }
        }
    }

    private static void fillNotaryPublicKey() {
        hdsNotaryService.forEach((id, service) -> {
            try {
                ImmutablePair<PublicKey, String> requestNotaryKey = service.getNotaryPublicKey();

                final PublicKey key = requestNotaryKey.getLeft();

                if (CryptoUtils.verifyDigitalSignature(serverPublicKey.get(id),
                    requestNotaryKey.getRight(),
                    new String(key.getEncoded()))) {
                    notaryPublicKey.put(id, key);
                } else {
                    logger.error("Notary Public Key " + "(id=" + id + ") signature doesn't match.");
                }
            } catch (RemoteException | NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
                logger.error(e);
            }
        });

    }

    @Override
    public synchronized void setStateOfGood(String serverId, Good good, int readId, int timeStamp, String signature)
        throws InvalidSignatureException {
        // Verify Signature
        if (!CryptoUtils.verifyDigitalSignature(serverPublicKey.get(serverId),
            signature,
            serverId,
            good.toString(),
            "" + readId,
            "" + timeStamp)) {
            throw new InvalidSignatureException("Server has signature invalid.");
        }

        answers.putIfAbsent(timeStamp, new ConcurrentHashMap<>());
        answers.get(timeStamp).put(serverId, good);

        // Count answers
        for (Map.Entry<Integer, Map<String, Good>> entry : answers.entrySet()) {
            final Collection<Good> goods = entry.getValue().values();
            Set<Good> uniqueSet = new HashSet<>(goods);
            for (Good temp : uniqueSet) {
                if (temp == null) {
                    continue;
                }
                if (Collections.frequency(goods, temp) > (Constants.N + Constants.F) / 2) {
                    lastGoodRead = temp;
                    latch.countDown();
                    return;
                }
            }
        }

    }
}
