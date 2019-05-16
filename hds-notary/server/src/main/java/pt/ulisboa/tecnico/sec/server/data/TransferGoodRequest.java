package pt.ulisboa.tecnico.sec.server.data;

import java.io.Serializable;
import java.util.Objects;
import pt.ulisboa.tecnico.sec.services.data.Transaction;

public class TransferGoodRequest extends Request implements Serializable {

    private Transaction transaction;
    private int timeStamp;

    public TransferGoodRequest() {

    }

    public TransferGoodRequest(Transaction transaction, int timeStamp) {
        this.transaction = transaction;
        this.timeStamp = timeStamp;
        setClientId(transaction.getSellerId());
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public int getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(int timeStamp) {
        this.timeStamp = timeStamp;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TransferGoodRequest) {
            TransferGoodRequest request = (TransferGoodRequest) obj;
            return this.getTransaction().equals(request.getTransaction()) &&
                this.getTimeStamp() == request.getTimeStamp();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTransaction().hashCode(), getTimeStamp(), getClientId());
    }

    @Override
    public String toString() {
        return "TransferGoodRequest{" + "transaction=" + transaction + ", timeStamp=" + timeStamp + ", clientId="
            + getClientId() + '}';
    }
}
