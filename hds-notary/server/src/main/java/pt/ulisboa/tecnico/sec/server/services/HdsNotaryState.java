package pt.ulisboa.tecnico.sec.server.services;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.StringUtils;
import pt.ulisboa.tecnico.sec.library.data.Good;
import pt.ulisboa.tecnico.sec.library.data.Transaction;
import pt.ulisboa.tecnico.sec.library.data.User;
import pt.ulisboa.tecnico.sec.library.exceptions.GoodIsNotOnSale;
import pt.ulisboa.tecnico.sec.library.exceptions.GoodNotFoundException;
import pt.ulisboa.tecnico.sec.library.exceptions.GoodWrongOwner;
import pt.ulisboa.tecnico.sec.library.exceptions.TransactionDoesntExistsException;
import pt.ulisboa.tecnico.sec.library.interfaces.server.HdsNotaryService;
import pt.ulisboa.tecnico.sec.server.utils.PersistenceUtils;

/**
 * Logic of the service.
 */
public final class HdsNotaryState implements HdsNotaryService, Serializable {
    private Map<String, Transaction> transactions = new ConcurrentHashMap<>();
    private Map<String, User> users = new ConcurrentHashMap<>();
    private Map<String, Good> goods = new ConcurrentHashMap<>();
    private Map<Good, List<Transaction>> pendingTransactions = new ConcurrentHashMap<>();

    public HdsNotaryState() {
    }

    /**
     * Sets the the good available to sell.
     *
     * @param sellerId the userId of the seller.
     * @param goodId   the id of the good.
     * @return true if no error occurs.
     * @throws GoodNotFoundException if the good doesn't exist.
     * @throws GoodWrongOwner        if the good doesn't belong to the provided sellerId.
     */
    @Override
    public boolean intentionToSell(String sellerId, String goodId) throws GoodNotFoundException, GoodWrongOwner {
        Good good = this.getStateOfGood(goodId);

        if (!StringUtils.equals(good.getOwnerId().toString(), sellerId.toString())) {
            throw new GoodWrongOwner(
                    "The good with id " + goodId + " doesn't belong to the user with id " + sellerId + ".");
        }

        good.setOnSale(true);

        // Save State
        PersistenceUtils.save();

        return true;
    }

    /**
     * Returns a pending transaction.
     *
     * @param sellerId the userId of the seller.
     * @param buyerId  the userId of the buyer.
     * @param goodId   the id of the good.
     * @return The pending transaction.
     * @throws GoodNotFoundException if the good doesn't exist.
     * @throws GoodIsNotOnSale       if the good is not on sale.
     * @throws GoodWrongOwner        if the good doesn't belong to the provided sellerId.
     */
    @Override
    public Transaction intentionToBuy(String sellerId, String buyerId, String goodId) throws GoodNotFoundException,
            GoodIsNotOnSale, GoodWrongOwner {
        Good good = this.getStateOfGood(goodId);

        // Basic checks.
        checkGood(sellerId, goodId, good);

        List<Transaction> pendingGoodTransactions = this.getGoodsPendingTransaction(good);

        final Optional<Transaction> transaction = pendingGoodTransactions.stream()
                .filter(t -> t.getBuyerId() == buyerId)
                .findFirst();

        // It already exists, just return
        if (transaction.isPresent()) {
            return transaction.get();
        }

        // Doesn't exist, create
        String transactionId = UUID.randomUUID().toString();
        Transaction newTransaction = new Transaction(transactionId, sellerId, buyerId, goodId);
        pendingGoodTransactions.add(newTransaction);

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
    public Transaction transferGood(String transactionId, String sellerId, String buyerId, String goodId)
            throws GoodNotFoundException, TransactionDoesntExistsException, GoodWrongOwner, GoodIsNotOnSale {
        Good good = this.getStateOfGood(goodId);

        final List<Transaction> pendingGoodTransactions = this.getGoodsPendingTransaction(good);

        final Optional<Transaction> transaction = pendingGoodTransactions.stream()
                .filter(t -> StringUtils.equals(t.getTransactionId(), transactionId))
                .findFirst();

        if (!transaction.isPresent()) {
            throw new TransactionDoesntExistsException("The transaction with id " + transactionId + " doesn't exist.");
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

    private List<Transaction> getGoodsPendingTransaction(Good good) {
        return this.pendingTransactions.computeIfAbsent(good, k -> new ArrayList<>());
    }

    private void checkGood(String sellerId, String goodId, Good good) throws GoodIsNotOnSale, GoodWrongOwner {
        if (!good.isOnSale()) {
            throw new GoodIsNotOnSale("The good with id " + goodId + " is not for sale.");
        } else if (!StringUtils.equals(good.getOwnerId(), sellerId)) {
            throw new GoodWrongOwner(
                    "The good with id " + goodId + " doesn't belong to the user with id " + sellerId + ".");
        }
    }


}
