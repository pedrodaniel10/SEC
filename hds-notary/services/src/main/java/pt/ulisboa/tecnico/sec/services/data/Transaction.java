package pt.ulisboa.tecnico.sec.services.data;

import java.io.Serializable;

public class Transaction implements Serializable {

    private String transactionId;
    private String buyerId;
    private String sellerId;
    private String goodId;

    private byte[] sellerSignature;
    private byte[] buyerSignature;
    private byte[] notarySignature;

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

    public byte[] getSellerSignature() {
        return sellerSignature;
    }

    public void setSellerSignature(byte[] sellerSignature) {
        this.sellerSignature = sellerSignature;
    }

    public byte[] getBuyerSignature() {
        return buyerSignature;
    }

    public void setBuyerSignature(byte[] buyerSignature) {
        this.buyerSignature = buyerSignature;
    }

    public byte[] getNotarySignature() {
        return notarySignature;
    }

    public void setNotarySignature(byte[] notarySignature) {
        this.notarySignature = notarySignature;
    }
}
