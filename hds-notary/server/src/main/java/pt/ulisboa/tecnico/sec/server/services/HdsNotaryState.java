package pt.ulisboa.tecnico.sec.server.services;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.log4j.Logger;
import pt.ulisboa.tecnico.sec.server.HdsNotaryApplication;
import pt.ulisboa.tecnico.sec.server.data.GetStateOfGoodRequest;
import pt.ulisboa.tecnico.sec.server.data.IntentionToBuyRequest;
import pt.ulisboa.tecnico.sec.server.data.IntentionToSellRequest;
import pt.ulisboa.tecnico.sec.server.data.Request;
import pt.ulisboa.tecnico.sec.server.data.TransferGoodRequest;
import pt.ulisboa.tecnico.sec.server.utils.CcUtils;
import pt.ulisboa.tecnico.sec.server.utils.PersistenceUtils;
import pt.ulisboa.tecnico.sec.services.crypto.CryptoUtils;
import pt.ulisboa.tecnico.sec.services.data.Good;
import pt.ulisboa.tecnico.sec.services.data.Transaction;
import pt.ulisboa.tecnico.sec.services.data.User;
import pt.ulisboa.tecnico.sec.services.exceptions.GoodIsNotOnSaleException;
import pt.ulisboa.tecnico.sec.services.exceptions.GoodNotFoundException;
import pt.ulisboa.tecnico.sec.services.exceptions.GoodWrongOwnerException;
import pt.ulisboa.tecnico.sec.services.exceptions.InvalidNonceException;
import pt.ulisboa.tecnico.sec.services.exceptions.InvalidProofOfWorkException;
import pt.ulisboa.tecnico.sec.services.exceptions.InvalidSignatureException;
import pt.ulisboa.tecnico.sec.services.exceptions.ServerException;
import pt.ulisboa.tecnico.sec.services.exceptions.TransactionDoesntExistsException;
import pt.ulisboa.tecnico.sec.services.exceptions.UserNotFoundException;
import pt.ulisboa.tecnico.sec.services.exceptions.WrongTimeStampException;
import pt.ulisboa.tecnico.sec.services.interfaces.client.ReadBonarService;
import pt.ulisboa.tecnico.sec.services.interfaces.server.HdsNotaryService;
import pt.ulisboa.tecnico.sec.services.properties.HdsProperties;
import sun.security.pkcs11.wrapper.PKCS11Exception;

/**
 * Logic of the service.
 */
public final class HdsNotaryState implements HdsNotaryService, Serializable {

    private static final Logger logger = Logger.getLogger(HdsNotaryState.class);

    private transient RSAPrivateKey serverPrivateKey;
    private transient RSAPrivateKey notaryPrivateKey;

    private Map<String, Transaction> transactions = new ConcurrentHashMap<>();
    private Map<String, User> users = new ConcurrentHashMap<>();
    private Map<String, Good> goods = new ConcurrentHashMap<>();
    private Map<String, List<Transaction>> pendingTransactions = new ConcurrentHashMap<>();

    public HdsNotaryState() {
    }

    @Override
    public String getNonce(String userId) throws ServerException {
        return getUserById(userId).generateNonce();
    }


    /**
     * Sets the the good available to sell.
     *
     * @param sellerId  the userId of the seller.
     * @param goodId    the id of the good.
     * @param nonce     the server nonce.
     * @param timeStamp BONAR time stamp.
     * @param signature the signature of the parameters.
     * @return true if no error occurs.
     * @throws GoodNotFoundException   if the good doesn't exist.
     * @throws GoodWrongOwnerException if the good doesn't belong to the provided sellerId.
     */
    @Override
    public ImmutablePair<Boolean, String> intentionToSell(String sellerId,
        String goodId,
        String nonce,
        int timeStamp,
        String signature)
        throws GoodNotFoundException, GoodWrongOwnerException, UserNotFoundException, InvalidNonceException,
               InvalidSignatureException, WrongTimeStampException, RemoteException, InterruptedException {

        User user = getUserById(sellerId);

        // Verify Signature
        if (!CryptoUtils.verifyDigitalSignature(HdsProperties.getClientPublicKey(user.getName()), signature,
            sellerId, goodId, nonce, Integer.toString(timeStamp))) {
            throw new InvalidSignatureException(
                "Seller with id " + sellerId + " has signature invalid.");
        }

        // Verify nonce
        if (!StringUtils.equals(nonce, user.getNonce())) {
            throw new InvalidNonceException("IntentionToSell: Wrong request number.");
        }

        Good good = this.getGood(goodId);
        if (!StringUtils.equals(good.getOwnerId(), sellerId)) {
            throw new GoodWrongOwnerException(
                "The good with id " + goodId + " doesn't belong to the user with id " + sellerId
                    + ".");
        }

        if (timeStamp <= good.getTimeStamp()) {
            throw new WrongTimeStampException("The timestamp is below or equal.");
        }

        //Broadcast
        Request request = new IntentionToSellRequest(sellerId, goodId, timeStamp);
        BroadcastServiceImpl.sendBroadcast(request);

        good.setOnSale(true);
        good.setTimeStamp(timeStamp);
        multicastWriteForReaders(good);

        user.generateNonce();

        // Save State
        PersistenceUtils.save();

        // Generate signature
        try {
            String serverSignature = CryptoUtils.makeDigitalSignature(getServerPrivateKey(), goodId,
                Boolean.toString(true),
                nonce);
            return new ImmutablePair<>(true, serverSignature);
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            throw new InvalidSignatureException("Server was unable to sign the message.");
        }
    }

    /**
     * Returns a pending transaction.
     *
     * @param sellerId  the userId of the seller.
     * @param buyerId   the userId of the buyer.
     * @param goodId    the id of the good.
     * @param nonce     the server nonce.
     * @param signature the signature of the parameters.
     * @return The pending transaction.
     * @throws GoodNotFoundException    if the good doesn't exist.
     * @throws GoodIsNotOnSaleException if the good is not on sale.
     * @throws GoodWrongOwnerException  if the good doesn't belong to the provided sellerId.
     */
    @Override
    public ImmutablePair<Transaction, String> intentionToBuy(String sellerId,
        String buyerId,
        String goodId,
        String nonce,
        String signature)
        throws GoodNotFoundException, GoodIsNotOnSaleException, GoodWrongOwnerException, UserNotFoundException,
               InvalidSignatureException, InvalidNonceException, RemoteException, InterruptedException {

        User user = getUserById(buyerId);

        // Verify Signature
        if (!CryptoUtils
            .verifyDigitalSignature(HdsProperties.getClientPublicKey(user.getName()), signature,
                sellerId, buyerId, goodId, nonce)) {
            throw new InvalidSignatureException("IntentionToBuy: Signature is invalid.");
        }

        // Verify nonce
        if (!StringUtils.equals(nonce, user.getNonce())) {
            throw new InvalidNonceException("Wrong nonce.");
        }
        Good good = this.getGood(goodId);

        // Basic checks.
        checkGood(sellerId, goodId, good);

        //Broadcast
        Request request = new IntentionToBuyRequest(sellerId, buyerId, goodId);
        BroadcastServiceImpl.sendBroadcast(request);

        List<Transaction> pendingGoodTransactions = this.getGoodsPendingTransaction(good);

        final Optional<Transaction> existingTransaction = pendingGoodTransactions.stream()
            .filter(t -> StringUtils.equals(t.getBuyerId(), buyerId))
            .findFirst();

        user.generateNonce();

        // If transaction doesn't exist, create new one
        Transaction transaction;
        if (existingTransaction.isPresent()) {
            transaction = existingTransaction.get();
        } else {
            String transactionId = UUID.randomUUID().toString();
            transaction = new Transaction(transactionId, HdsNotaryApplication.serverId, sellerId, buyerId, goodId);
            pendingGoodTransactions.add(transaction);

            // Save State
            PersistenceUtils.save();
        }

        // Generate signature
        try {
            String serverSignature = CryptoUtils.makeDigitalSignature(getServerPrivateKey(),
                transaction.getTransactionId(),
                nonce);
            return new ImmutablePair<>(transaction, serverSignature);
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            throw new InvalidSignatureException("Server was unable to sign the message.");
        }
    }

    /**
     * Returns the good provided the goodId.
     *
     * @param goodId the id of the good.
     * @return the good with the provided goodId.
     * @throws GoodNotFoundException if the good doesn't exist.
     */
    @Override
    public void getStateOfGood(String userId,
        String goodId,
        String nonce,
        int readId,
        String signature)
        throws GoodNotFoundException, InvalidSignatureException, UserNotFoundException, InvalidNonceException,
               RemoteException, NotBoundException, MalformedURLException, NoSuchAlgorithmException, InvalidKeyException,
               SignatureException, InterruptedException {

        User user = getUserById(userId);

        // Verify nonce
        if (!StringUtils.equals(nonce, user.getNonce())) {
            throw new InvalidNonceException("GetStateOfGood: Wrong nonce.");
        }

        // Verify Signature
        if (!CryptoUtils
            .verifyDigitalSignature(HdsProperties.getClientPublicKey(user.getName()), signature, userId, goodId,
                nonce, Integer.toString(readId))) {
            throw new InvalidSignatureException("GetStateOfGood: Signature is invalid.");
        }
        Good good = this.goods.get(goodId);

        if (good == null) {
            throw new GoodNotFoundException("Good with id " + goodId + " not found.");
        }

        //Broadcast
        Request request = new GetStateOfGoodRequest(userId, goodId, readId);
        BroadcastServiceImpl.sendBroadcast(request);

        good.getListening().put(userId, readId);
        user.generateNonce();

        setStateOfGood(userId, readId, good);
        return;
    }

    private void setStateOfGood(String userId, int readId, Good good)
        throws NotBoundException, MalformedURLException, RemoteException, UserNotFoundException,
               NoSuchAlgorithmException, InvalidKeyException, SignatureException, InvalidSignatureException {
        String signature;
        ReadBonarService readBonarService = (ReadBonarService) Naming.lookup(
            HdsProperties.getClientBonarUri(userId));

        int timeStamp = good.getTimeStamp();
        signature = CryptoUtils.makeDigitalSignature(getServerPrivateKey(),
            HdsNotaryApplication.serverId,
            good.toString(),
            "" + readId,
            "" + timeStamp);

        readBonarService.setStateOfGood(HdsNotaryApplication.serverId, good, readId, good.getTimeStamp(), signature);
    }

    @Override
    public void completeGetStateOfGood(String userId, String goodId) throws RemoteException, UserNotFoundException {
        Good good = this.goods.get(goodId);

        good.getListening().remove(userId);
    }

    @Override
    public Transaction transferGood(Transaction transaction, int timeStamp)
        throws GoodNotFoundException, TransactionDoesntExistsException, GoodWrongOwnerException,
               GoodIsNotOnSaleException,
               UserNotFoundException, InvalidSignatureException, WrongTimeStampException, NoSuchAlgorithmException,
               InvalidProofOfWorkException, RemoteException, InterruptedException {

        String transactionId = transaction.getTransactionId();
        String sellerId = transaction.getSellerId();
        String buyerId = transaction.getBuyerId();
        String goodId = transaction.getGoodId();
        String proofOfWork = transaction.getProofOfWork();

        String sellerSignature = transaction.getSellerSignature();
        String buyerSignature = transaction.getBuyerSignature();

        User userSeller = getUserById(sellerId);
        User userBuyer = getUserById(buyerId);

        // Verify Signature
        if (!CryptoUtils
            .verifyDigitalSignature(HdsProperties.getClientPublicKey(userBuyer.getName()),
                transaction.getBuyerSignature(),
                transactionId,
                sellerId,
                buyerId,
                goodId)) {
            throw new InvalidSignatureException("Buyer signature is invalid.");
        }

        if (!CryptoUtils
            .verifyDigitalSignature(HdsProperties.getClientPublicKey(userSeller.getName()), sellerSignature,
                transactionId, sellerId, buyerId, goodId, proofOfWork, buyerSignature)) {
            throw new InvalidSignatureException("Seller signature is invalid.");
        }

        Good good = this.getGood(goodId);

        if (timeStamp <= good.getTimeStamp()) {
            throw new WrongTimeStampException("The timestamp is below or equal.");
        }

        final List<Transaction> pendingGoodTransactions = this.getGoodsPendingTransaction(good);

        final Optional<Transaction> transactionResponse = pendingGoodTransactions.stream()
            .filter(t -> StringUtils.equals(t.getTransactionId(), transactionId))
            .findFirst();

        if (!transactionResponse.isPresent()) {
            throw new TransactionDoesntExistsException(
                "The transaction with id " + transactionId + " doesn't exist.");
        }

        // Check Transaction
        // Check proof of work
        byte[] hash = CryptoUtils.computeSHA256Hash(transaction.getTransactionId(),
            transaction.getSellerId(),
            transaction.getBuyerId(),
            transaction.getGoodId(),
            transaction.getBuyerSignature(),
            transaction.getProofOfWork());

        if (hash == null || hash[0] != 0) {
            throw new InvalidProofOfWorkException("The proof of work is invalid with this transaction.");
        }

        this.checkGood(sellerId, goodId, good);
        if (!StringUtils.equals(transactionResponse.get().getBuyerId(), buyerId)) {
            throw new TransactionDoesntExistsException(
                "The Transaction doesn't refer to the buyer with id " + buyerId + ".");
        }

        //Broadcast
        Request request = new TransferGoodRequest(transaction, timeStamp);
        BroadcastServiceImpl.sendBroadcast(request);

        // Transfer good
        synchronized (pendingGoodTransactions) {
            this.transactions.put(transactionId, transaction);
            pendingGoodTransactions.clear();
            good.setOwnerId(buyerId);
            good.setOnSale(false);
            good.setTimeStamp(timeStamp);
        }

        multicastWriteForReaders(good);

        // Generate signature
        try {
            String notarySignature;
            if (HdsNotaryApplication.signWithCC) {
                notarySignature = CcUtils.signMessage(
                    transactionId,
                    sellerId,
                    buyerId,
                    sellerSignature,
                    buyerSignature);
            } else {
                notarySignature = CryptoUtils.makeDigitalSignature(getNotaryPrivateKey(),
                    transactionId,
                    sellerId,
                    buyerId,
                    sellerSignature,
                    buyerSignature);
            }
            transaction.setNotarySignature(notarySignature);

            // Save State
            PersistenceUtils.save();

            return transaction;
        } catch (PKCS11Exception | NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            e.printStackTrace();
            throw new InvalidSignatureException("Notary was unable to sign the message.");
        }

    }

    private void multicastWriteForReaders(Good good) {
        for (Map.Entry<String, Integer> entry : good.getListening().entrySet()) {
            CompletableFuture.runAsync(() -> {
                try {
                    setStateOfGood(entry.getKey(), entry.getValue(), good);
                } catch (NotBoundException | MalformedURLException | RemoteException | UserNotFoundException
                    | NoSuchAlgorithmException | InvalidKeyException | SignatureException | InvalidSignatureException e) {
                    logger.error(e);
                }
            });
        }
    }

    private void broadcastRequest(Request request) {

        /*
        CountDownLatch latch = new CountDownLatch((Constants.N + Constants.F) / 2 + 1);

        //Send echo to all processes
        if (!sentEcho) {
            sentEcho = true;

            for (Map.Entry<String, BroadcastService> entry : broadcastService.entrySet()) {
                CompletableFuture.runAsync(() -> {
                    try {
                        entry.getValue().echo(request);
                    } catch (RemoteException e) {
                        logger.error(e);
                    }
                });
            }
        }

        //Wait for quorum of echos with the same request
        CompletableFuture.runAsync(() -> {
            latch.countDown();
        });

        //When exists quorum
        if (!sentReady) {
            sentReady = true;

            for (Map.Entry<String, BroadcastService> entry : broadcastService.entrySet()) {
                CompletableFuture.runAsync(() -> {
                    try {
                        entry.getValue().ready(request);
                    } catch (RemoteException e) {
                        logger.error(e);
                    }
                });
            }
        }

        //Wait for quorum of echos with the same request
        CompletableFuture.runAsync(() -> {
            latch.countDown();
        });*/
    }

    @Override
    public ImmutablePair<PublicKey, String> getNotaryPublicKey()
        throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {

        PublicKey publicKey;
        if (HdsNotaryApplication.signWithCC) {
            publicKey = CcUtils.getNotaryPublicKey();
        } else {
            publicKey = HdsProperties.getNotarySignaturePublicKey(HdsNotaryApplication.serverId);
        }
        String encodedPublicKey = new String(publicKey.getEncoded());
        String signature = CryptoUtils.makeDigitalSignature(this.getServerPrivateKey(), encodedPublicKey);

        return new ImmutablePair<>(publicKey, signature);

    }

    public RSAPrivateKey getServerPrivateKey() {
        if (this.serverPrivateKey == null) {
            this.serverPrivateKey = HdsProperties.getServerPrivateKey(HdsNotaryApplication.serverId,
                HdsNotaryApplication.serverPassword);
        }
        return this.serverPrivateKey;
    }


    public RSAPrivateKey getNotaryPrivateKey() {
        if (this.notaryPrivateKey == null) {
            this.notaryPrivateKey = HdsProperties.getNotarySignaturePrivateKey(HdsNotaryApplication.serverId,
                HdsNotaryApplication.notaryPassword);
        }
        return this.notaryPrivateKey;
    }

    private List<Transaction> getGoodsPendingTransaction(Good good) {
        return this.pendingTransactions.computeIfAbsent(good.getGoodId(), k -> new ArrayList<>());
    }

    private Good getGood(String goodId) throws GoodNotFoundException {
        Good good = this.goods.get(goodId);

        if (good == null) {
            throw new GoodNotFoundException("Good with id " + goodId + " not found.");
        }

        return good;
    }

    private void checkGood(String sellerId, String goodId, Good good)
        throws GoodIsNotOnSaleException, GoodWrongOwnerException {
        if (!good.isOnSale()) {
            throw new GoodIsNotOnSaleException("The good with id " + goodId + " is not for sale.");
        } else if (!StringUtils.equals(good.getOwnerId(), sellerId)) {
            throw new GoodWrongOwnerException(
                "The good with id " + goodId + " doesn't belong to the user with id " + sellerId
                    + ".");
        }
    }

    public User getUserById(String userId) throws UserNotFoundException {
        User user = this.users.get(userId);

        if (user == null) {
            throw new UserNotFoundException("User with id " + userId + " was not found.");
        }
        return user;
    }
}
