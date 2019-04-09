package pt.ulisboa.tecnico.sec.server.services;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.StringUtils;
import pt.ulisboa.tecnico.sec.library.crypto.CryptoUtils;
import pt.ulisboa.tecnico.sec.library.data.Good;
import pt.ulisboa.tecnico.sec.library.data.Transaction;
import pt.ulisboa.tecnico.sec.library.data.User;
import pt.ulisboa.tecnico.sec.library.exceptions.GoodIsNotOnSaleException;
import pt.ulisboa.tecnico.sec.library.exceptions.GoodNotFoundException;
import pt.ulisboa.tecnico.sec.library.exceptions.GoodWrongOwnerException;
import pt.ulisboa.tecnico.sec.library.exceptions.InvalidRequestNumberException;
import pt.ulisboa.tecnico.sec.library.exceptions.InvalidSignatureException;
import pt.ulisboa.tecnico.sec.library.exceptions.ServerException;
import pt.ulisboa.tecnico.sec.library.exceptions.TransactionDoesntExistsException;
import pt.ulisboa.tecnico.sec.library.exceptions.UserNotFoundException;
import pt.ulisboa.tecnico.sec.library.interfaces.server.HdsNotaryService;
import pt.ulisboa.tecnico.sec.server.utils.PersistenceUtils;

/**
 * Logic of the service.
 */
public final class HdsNotaryState implements HdsNotaryService, Serializable {

    private Map<String, Transaction> transactions = new ConcurrentHashMap<>();
    private Map<String, User> users = new ConcurrentHashMap<>();
    private Map<String, Good> goods = new ConcurrentHashMap<>();
    private Map<String, List<Transaction>> pendingTransactions = new ConcurrentHashMap<>();

    public HdsNotaryState() {
        // Basic Constructor for Jackson's instantiation.
    }

    @Override
    public int getRequestNumber(String userId) throws ServerException {
        return getUserById(userId).getRequestNumber();
    }


    /**
     * Sets the the good available to sell.
     *
     * @param sellerId the userId of the seller.
     * @param goodId the id of the good.
     * @param requestNumber the server request number.
     * @param signature the signature of the parameters.
     * @return true if no error occurs.
     * @throws GoodNotFoundException if the good doesn't exist.
     * @throws GoodWrongOwnerException if the good doesn't belong to the provided sellerId.
     */
    @Override
    public boolean intentionToSell(String sellerId, String goodId, int requestNumber,
        byte[] signature)
        throws GoodNotFoundException, GoodWrongOwnerException, UserNotFoundException, InvalidRequestNumberException,
        InvalidSignatureException {

        User user = getUserById(sellerId);

        // Verify Signature
        if (!CryptoUtils.verifyDigitalSignature(user.getPublicKey(), signature,
            sellerId, goodId, String.valueOf(requestNumber))) {
            throw new InvalidSignatureException(
                "Seller with id " + sellerId + " has signature invalid.");
        }

        // Verify requestNumber
        if (requestNumber != user.getRequestNumber()) {
            throw new InvalidRequestNumberException("IntentionToSell: Wrong request number.");
        }

        Good good = this.getStateOfGood(goodId);
        if (!StringUtils.equals(good.getOwnerId(), sellerId)) {
            throw new GoodWrongOwnerException(
                "The good with id " + goodId + " doesn't belong to the user with id " + sellerId
                    + ".");
        }

        good.setOnSale(true);
        user.incrementRequestNumber();

        // Save State
        PersistenceUtils.save();

        return true;
    }

    /**
     * Returns a pending transaction.
     *
     * @param sellerId the userId of the seller.
     * @param buyerId the userId of the buyer.
     * @param goodId the id of the good.
     * @param requestNumber the server request number.
     * @param signature the signature of the parameters.
     * @return The pending transaction.
     * @throws GoodNotFoundException if the good doesn't exist.
     * @throws GoodIsNotOnSaleException if the good is not on sale.
     * @throws GoodWrongOwnerException if the good doesn't belong to the provided sellerId.
     */
    @Override
    public Transaction intentionToBuy(String sellerId,
        String buyerId,
        String goodId,
        int requestNumber,
        byte[] signature)
        throws GoodNotFoundException, GoodIsNotOnSaleException, GoodWrongOwnerException, UserNotFoundException,
        InvalidSignatureException, InvalidRequestNumberException {

        User user = getUserById(buyerId);

        // Verify Signature
        if (!CryptoUtils
            .verifyDigitalSignature(user.getPublicKey(), signature,
                sellerId, buyerId, goodId, String.valueOf(requestNumber))) {
            throw new InvalidSignatureException("IntentionToBuy: Signature is invalid.");
        }

        // Verify requestNumber
        if (requestNumber != user.getRequestNumber()) {
            throw new InvalidRequestNumberException("Wrong request number.");
        }
        Good good = this.getStateOfGood(goodId);

        // Basic checks.
        checkGood(sellerId, goodId, good);

        List<Transaction> pendingGoodTransactions = this.getGoodsPendingTransaction(good);

        final Optional<Transaction> transaction = pendingGoodTransactions.stream()
            .filter(t -> StringUtils.equals(t.getBuyerId(), buyerId))
            .findFirst();

        // It already exists, just return
        if (transaction.isPresent()) {
            user.incrementRequestNumber();
            return transaction.get();
        }

        // Doesn't exist, create
        String transactionId = UUID.randomUUID().toString();
        Transaction newTransaction = new Transaction(transactionId, sellerId, buyerId, goodId);
        pendingGoodTransactions.add(newTransaction);

        user.incrementRequestNumber();

        // Save State
        PersistenceUtils.save();

        return newTransaction;
    }

    /**
     * Returns the good provided the goodId.
     *
     * @param goodId the id of the good.
     * @return the good with the provided goodId.
     * @throws GoodNotFoundException if the good doesn't exist.
     */
    @Override
    public Good getStateOfGood(String goodId) throws GoodNotFoundException {
        Good good = this.goods.get(goodId);

        if (good == null) {
            throw new GoodNotFoundException("Good with id " + goodId + " not found.");
        }

        return good;
    }

    @Override
    public Transaction transferGood(String transactionId,
        String sellerId,
        String buyerId,
        String goodId,
        byte[] sellerSignature,
        byte[] buyerSignature)
        throws GoodNotFoundException, TransactionDoesntExistsException, GoodWrongOwnerException, GoodIsNotOnSaleException,
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

        Good good = this.getStateOfGood(goodId);

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
        return transaction.get();
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

    private List<Transaction> getGoodsPendingTransaction(Good good) {
        return this.pendingTransactions.computeIfAbsent(good.getGoodId(), k -> new ArrayList<>());
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