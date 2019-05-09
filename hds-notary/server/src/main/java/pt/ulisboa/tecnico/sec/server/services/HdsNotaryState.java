package pt.ulisboa.tecnico.sec.server.services;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import pt.ulisboa.tecnico.sec.library.crypto.CryptoUtils;
import pt.ulisboa.tecnico.sec.library.data.Good;
import pt.ulisboa.tecnico.sec.library.data.Transaction;
import pt.ulisboa.tecnico.sec.library.data.User;
import pt.ulisboa.tecnico.sec.library.exceptions.GoodIsNotOnSaleException;
import pt.ulisboa.tecnico.sec.library.exceptions.GoodNotFoundException;
import pt.ulisboa.tecnico.sec.library.exceptions.GoodWrongOwnerException;
import pt.ulisboa.tecnico.sec.library.exceptions.InvalidNonceException;
import pt.ulisboa.tecnico.sec.library.exceptions.InvalidSignatureException;
import pt.ulisboa.tecnico.sec.library.exceptions.ServerException;
import pt.ulisboa.tecnico.sec.library.exceptions.TransactionDoesntExistsException;
import pt.ulisboa.tecnico.sec.library.exceptions.UserNotFoundException;
import pt.ulisboa.tecnico.sec.library.interfaces.server.HdsNotaryService;
import pt.ulisboa.tecnico.sec.server.utils.CcUtils;
import pt.ulisboa.tecnico.sec.server.utils.PersistenceUtils;
import sun.security.pkcs11.wrapper.PKCS11Exception;

/**
 * Logic of the service.
 */
public final class HdsNotaryState implements HdsNotaryService, Serializable {


    @JsonIgnore
    RSAPrivateKey serverPrivateKey;
    @JsonIgnore
    RSAPublicKey notaryPublicKey;
    private Map<String, Transaction> transactions = new ConcurrentHashMap<>();
    private Map<String, User> users = new ConcurrentHashMap<>();
    private Map<String, Good> goods = new ConcurrentHashMap<>();
    private Map<String, List<Transaction>> pendingTransactions = new ConcurrentHashMap<>();
    // Get private key
    private String privateKey;

    public HdsNotaryState() {
        // Basic Constructor for Jackson's instantiation.
    }

    @Override
    public String getNonce(String userId) throws ServerException {
        return getUserById(userId).getNonce();
    }


    /**
     * Sets the the good available to sell.
     *
     * @param sellerId  the userId of the seller.
     * @param goodId    the id of the good.
     * @param nonce     the server nonce.
     * @param signature the signature of the parameters.
     * @return true if no error occurs.
     * @throws GoodNotFoundException   if the good doesn't exist.
     * @throws GoodWrongOwnerException if the good doesn't belong to the provided sellerId.
     */
    @Override
    public ImmutablePair<Boolean, byte[]> intentionToSell(String sellerId, String goodId, String nonce,
        byte[] signature)
        throws GoodNotFoundException, GoodWrongOwnerException, UserNotFoundException, InvalidNonceException,
               InvalidSignatureException {

        User user = getUserById(sellerId);

        // Verify Signature
        if (!CryptoUtils.verifyDigitalSignature(user.getPublicKey(), signature,
            sellerId, goodId, nonce)) {
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

        good.setOnSale(true);
        user.generateNonce();

        // Save State
        PersistenceUtils.save();

        // Generate signature
        try {
            byte[] serverSignature = CryptoUtils.makeDigitalSignature(serverPrivateKey, goodId, Boolean.toString(true),
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
    public Transaction intentionToBuy(String sellerId,
        String buyerId,
        String goodId,
        String nonce,
        byte[] signature)
        throws GoodNotFoundException, GoodIsNotOnSaleException, GoodWrongOwnerException, UserNotFoundException,
               InvalidSignatureException, InvalidNonceException {

        User user = getUserById(buyerId);

        // Verify Signature
        if (!CryptoUtils
            .verifyDigitalSignature(user.getPublicKey(), signature,
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
            transaction = new Transaction(transactionId, sellerId, buyerId, goodId);
            pendingGoodTransactions.add(transaction);

            // Save State
            PersistenceUtils.save();
        }

        // Generate signature
        try {
            byte[] serverSignature = CryptoUtils.makeDigitalSignature(serverPrivateKey, transaction.getTransactionId(),
                nonce);
            transaction.setNotarySignature(serverSignature);
            return transaction;
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
    public ImmutablePair<Good, byte[]> getStateOfGood(String userId, String goodId, String nonce, byte[] signature)
        throws GoodNotFoundException, InvalidSignatureException, UserNotFoundException, InvalidNonceException {

        User user = getUserById(userId);

        // Verify nonce
        if (!StringUtils.equals(nonce, user.getNonce())) {
            throw new InvalidNonceException("GetStateOfGood: Wrong nonce.");
        }

        // Verify Signature
        if (!CryptoUtils
            .verifyDigitalSignature(user.getPublicKey(), signature, userId, goodId, nonce)) {
            throw new InvalidSignatureException("GetStateOfGood: Signature is invalid.");
        }
        Good good = this.goods.get(goodId);

        if (good == null) {
            throw new GoodNotFoundException("Good with id " + goodId + " not found.");
        }

        user.generateNonce();

        // Generate signature
        try {
            byte[] serverSignature = CryptoUtils.makeDigitalSignature(serverPrivateKey, goodId,
                Boolean.toString(good.isOnSale()), nonce);
            return new ImmutablePair<>(good, serverSignature);
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            throw new InvalidSignatureException("Server was unable to sign the message.");
        }
    }

    @Override
    public Transaction transferGood(String transactionId,
        String sellerId,
        String buyerId,
        String goodId,
        byte[] sellerSignature,
        byte[] buyerSignature)
        throws GoodNotFoundException, TransactionDoesntExistsException, GoodWrongOwnerException,
               GoodIsNotOnSaleException,
               UserNotFoundException, InvalidSignatureException {

        User userSeller = getUserById(sellerId);
        User userBuyer = getUserById(buyerId);

        // Verify Signature
        if (!CryptoUtils
            .verifyDigitalSignature(userBuyer.getPublicKey(), buyerSignature,
                transactionId, sellerId, buyerId, goodId)) {
            throw new InvalidSignatureException("Buyer signature is invalid.");
        }

        if (!CryptoUtils
            .verifyDigitalSignature(userSeller.getPublicKey(), sellerSignature,
                transactionId, sellerId, buyerId, goodId)) {
            throw new InvalidSignatureException("Seller signature is invalid.");
        }

        Good good = this.getGood(goodId);

        final List<Transaction> pendingGoodTransactions = this.getGoodsPendingTransaction(good);

        final Optional<Transaction> transaction = pendingGoodTransactions.stream()
            .filter(t -> StringUtils.equals(t.getTransactionId(), transactionId))
            .findFirst();

        if (!transaction.isPresent()) {
            throw new TransactionDoesntExistsException(
                "The transaction with id " + transactionId + " doesn't exist.");
        }

        // Check Transaction
        this.checkGood(sellerId, goodId, good);
        if (!StringUtils.equals(transaction.get().getBuyerId(), buyerId)) {
            throw new TransactionDoesntExistsException(
                "The Transaction doesn't refer to the buyer with id " + buyerId + ".");
        }

        // Transfer good
        synchronized (pendingGoodTransactions) {
            this.transactions.put(transactionId, transaction.get());
            transaction.get().setBuyerSignature(buyerSignature);
            transaction.get().setSellerSignature(sellerSignature);
            pendingGoodTransactions.clear();
            good.setOwnerId(buyerId);
            good.setOnSale(false);
        }

        // Save State
        PersistenceUtils.save();

        // Generate signature
        try {
            byte[] notarySignature = CcUtils.signMessage(transactionId, sellerId,
                buyerId, new String(sellerSignature), new String(buyerSignature));
            transaction.get().setNotarySignature(notarySignature);
            return transaction.get();
        } catch (PKCS11Exception e) {
            e.printStackTrace();
            throw new InvalidSignatureException("Notary was unable to sign the message.");
        }
    }

    @Override
    public ImmutablePair<PublicKey, byte[]> getNotaryPublicKey()
        throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        byte[] signature = CryptoUtils.makeDigitalSignature(this.serverPrivateKey,
            new String(CcUtils.getNotaryPublicKey().getEncoded()));

        return new ImmutablePair<>(CcUtils.getNotaryPublicKey(), signature);
    }

    public void setNotaryPublicKey(RSAPublicKey rsa) {
    }


    public Map<String, Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(Map<String, Transaction> transactions) {
        this.transactions = transactions;
    }

    public Map<String, User> getUsers() {
        return users;
    }

    public void setUsers(Map<String, User> users) {
        this.users = users;
    }

    public Map<String, Good> getGoods() {
        return goods;
    }

    public void setGoods(Map<String, Good> goods) {
        this.goods = goods;
    }

    public Map<String, List<Transaction>> getPendingTransactions() {
        return pendingTransactions;
    }

    public void setPendingTransactions(
        Map<String, List<Transaction>> pendingTransactions) {
        this.pendingTransactions = pendingTransactions;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        String password = "admin";
        this.privateKey = privateKey;
        this.serverPrivateKey = CryptoUtils.getPrivateKey(privateKey, password);
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
