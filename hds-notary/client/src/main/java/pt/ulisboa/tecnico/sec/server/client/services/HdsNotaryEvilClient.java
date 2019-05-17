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
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.log4j.Logger;
import pt.ulisboa.tecnico.sec.services.crypto.CryptoUtils;
import pt.ulisboa.tecnico.sec.services.data.Good;
import pt.ulisboa.tecnico.sec.services.data.Transaction;
import pt.ulisboa.tecnico.sec.services.data.User;
import pt.ulisboa.tecnico.sec.services.exceptions.InvalidSignatureException;
import pt.ulisboa.tecnico.sec.services.exceptions.ServerException;
import pt.ulisboa.tecnico.sec.services.interfaces.client.ClientService;
import pt.ulisboa.tecnico.sec.services.interfaces.client.ReadBonarService;
import pt.ulisboa.tecnico.sec.services.interfaces.server.HdsNotaryService;
import pt.ulisboa.tecnico.sec.services.properties.HdsProperties;
import pt.ulisboa.tecnico.sec.services.utils.Constants;

public class HdsNotaryEvilClient extends UnicastRemoteObject implements ReadBonarService, Serializable {

    private static final Logger logger = Logger.getLogger(HdsNotaryEvilClient.class);

    private static RSAPrivateKey privateKey;
    private static Map<String, RSAPublicKey> serverPublicKey = new HashMap<>();
    private static Map<String, PublicKey> notaryPublicKey = new HashMap<>();
    private static Map<String, HdsNotaryService> hdsNotaryService = new HashMap<>();
    private static int readId = 0;
    private static Map<Integer, Map<String, Good>> answers = new ConcurrentHashMap<>();
    private static CountDownLatch latch;
    private static Good lastGoodRead;

    protected HdsNotaryEvilClient() throws RemoteException {
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
            reg.rebind("ReadBonarService", new HdsNotaryEvilClient());

            logger.info("ClientService up at port " + registryPort);
        } catch (RemoteException e) {
            logger.error(e);
        }

    }

    public static Optional<Transaction> buyGood(User user, String goodId) {
        return Optional.empty();
    }

    public static List<Transaction> intentionToBuy(User user, Good good) throws InterruptedException {
        return new ArrayList<>();
    }

    public static boolean intentionToSell(User user, String realGoodId) throws InterruptedException {
        String fakeGoodId = realGoodId.equals("0") ? "3" : "0";

        ConcurrentLinkedDeque<Boolean> returnValue = new ConcurrentLinkedDeque<>();
        CountDownLatch latch = new CountDownLatch(Constants.N);

        final Optional<Good> stateOfGood = getStateOfGood(user, realGoodId);

        for (Map.Entry<String, HdsNotaryService> entry : hdsNotaryService.entrySet()) {
            CompletableFuture.runAsync(() -> {
                try {

                    String nonce = entry.getValue().getNonce(user.getUserId());
                    int timeStamp = stateOfGood.get().getTimeStamp() + 1;

                    AtomicReference<String> goodId = new AtomicReference<>("");
                    if (NumberUtils.toInt(entry.getKey()) % 2 == 0) {
                        goodId.set(fakeGoodId);
                    } else {
                        goodId.set(realGoodId);
                    }

                    String signature = CryptoUtils.makeDigitalSignature(privateKey, user.getUserId(), goodId.get(),
                        nonce,
                        Integer.toString(timeStamp));

                    ImmutablePair<Boolean, String> response = entry.getValue().intentionToSell(user.getUserId(),
                        goodId.get(),
                        nonce, timeStamp, signature);

                    // Verify Signature
                    if (!CryptoUtils.verifyDigitalSignature(serverPublicKey.get(entry.getKey()), response.getRight(),
                        goodId.get(),
                        Boolean.toString(response.getLeft()), nonce)) {
                        throw new InvalidSignatureException("Server has signature invalid.");
                    }

                    if (response.getLeft()) {
                        returnValue.add(response.getLeft());
                    }
                } catch (RemoteException | NoSuchAlgorithmException | InvalidKeyException | SignatureException | ServerException | InterruptedException e) {
                    logger.error("Error on server id " + entry.getKey() + " - " + e.getMessage());
                }
                latch.countDown();
            });
        }

        boolean responses;
        do {
            responses = latch.await(2, TimeUnit.SECONDS);
        } while (!responses && returnValue.size() <= (Constants.N + Constants.F) / 2);

        return (returnValue.size() > (Constants.N + Constants.F) / 2);
    }

    public static Optional<Good> getStateOfGood(User user, String goodId) throws InterruptedException {
        CountDownLatch latchResponses = new CountDownLatch(Constants.N);
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
                } catch (RemoteException | NoSuchAlgorithmException | InvalidKeyException | SignatureException | ServerException | NotBoundException | MalformedURLException | InterruptedException e) {
                    logger.error("Error on server id " + entry.getKey());
                }
                latchResponses.countDown();
            }).exceptionally(e -> {
                e.printStackTrace();
                return null;
            });
        }

        boolean responses;
        Optional<Good> quorum;
        do {
            responses = latchResponses.await(2, TimeUnit.SECONDS);
            quorum = existsQuorumGood(answers);
        } while (!responses && !quorum.isPresent());

        if (successfulRequests.get() <= (Constants.N + Constants.F) / 2) {
            completeGetStateOfGood(user, goodId);
            return Optional.empty();
        }

        if (quorum.isPresent()) {
            completeGetStateOfGood(user, goodId);
            return quorum;
        }

        completeGetStateOfGood(user, goodId);
        return Optional.of(lastGoodRead);
    }

    private static void completeGetStateOfGood(User user, String goodId) {
        for (Map.Entry<String, HdsNotaryService> serviceEntry : hdsNotaryService.entrySet()) {
            CompletableFuture.runAsync(() -> {
                try {
                    serviceEntry.getValue().completeGetStateOfGood(user.getUserId(), goodId);
                } catch (RemoteException | ServerException e) {
                    logger.error("Error on server id " + serviceEntry.getKey());
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

    private static Optional<Good> existsQuorumGood(Map<Integer, Map<String, Good>> responses) {
        for (Map.Entry<Integer, Map<String, Good>> entry : responses.entrySet()) {
            final Collection<Good> goods = entry.getValue().values();
            Set<Good> uniqueSet = new HashSet<>(goods);
            for (Good temp : uniqueSet) {
                if (temp == null) {
                    continue;
                }
                if (Collections.frequency(goods, temp) > (Constants.N + Constants.F) / 2) {
                    return Optional.of(temp);
                }
            }
        }
        return Optional.empty();
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
