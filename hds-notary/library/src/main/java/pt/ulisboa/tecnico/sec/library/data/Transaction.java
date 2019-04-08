package pt.ulisboa.tecnico.sec.library.data;

import java.io.Serializable;

public class Transaction implements Serializable {
    private String transactionId;
    private String buyerId;
    private String sellerId;
    private String goodId;

    public Transaction() {
    }

    public Transaction(String transactionId, String sellerId, String buyerId, String goodId) {
        this.transactionId = transactionId;
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.goodId = goodId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(String buyerId) {
        this.buyerId = buyerId;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getGoodId() {
        return goodId;
    }

    public void setGoodId(String goodId) {
        this.goodId = goodId;
    }
}
