package pt.ulisboa.tecnico.sec.services.data;

import java.io.Serializable;

public class Transaction implements Serializable {

    private String transactionId;
    private String serverId;
    private String buyerId;
    private String sellerId;
    private String goodId;

    private String sellerSignature;
    private String buyerSignature;
    private String notarySignature;
    private String proofOfWork;

    public Transaction() {
    }

    public Transaction(String transactionId, String serverId, String sellerId, String buyerId, String goodId) {
        this.transactionId = transactionId;
        this.serverId = serverId;
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

    public String getSellerSignature() {
        return sellerSignature;
    }

    public void setSellerSignature(String sellerSignature) {
        this.sellerSignature = sellerSignature;
    }

    public String getBuyerSignature() {
        return buyerSignature;
    }

    public void setBuyerSignature(String buyerSignature) {
        this.buyerSignature = buyerSignature;
    }

    public String getNotarySignature() {
        return notarySignature;
    }

    public void setNotarySignature(String notarySignature) {
        this.notarySignature = notarySignature;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getProofOfWork() {
        return proofOfWork;
    }

    public void setProofOfWork(String proofOfWork) {
        this.proofOfWork = proofOfWork;
    }
}
