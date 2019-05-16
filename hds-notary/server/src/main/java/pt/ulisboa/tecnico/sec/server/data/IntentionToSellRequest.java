package pt.ulisboa.tecnico.sec.server.data;

import java.io.Serializable;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

public class IntentionToSellRequest extends Request implements Serializable {

    private String sellerId;
    private String goodId;
    private int timeStamp;

    public IntentionToSellRequest() {

    }

    public IntentionToSellRequest(String sellerId, String goodId, int timeStamp) {
        this.sellerId = sellerId;
        this.goodId = goodId;
        this.timeStamp = timeStamp;
        setClientId(sellerId);
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

    public int getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(int timeStamp) {
        this.timeStamp = timeStamp;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IntentionToSellRequest) {
            IntentionToSellRequest request = (IntentionToSellRequest) obj;
            return StringUtils.equals(this.getSellerId(), request.getSellerId()) &&
                StringUtils.equals(this.getGoodId(), request.getGoodId()) &&
                this.getTimeStamp() == request.getTimeStamp() &&
                StringUtils.equals(this.getClientId(), request.getClientId());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSellerId(), getGoodId(), getTimeStamp(), getClientId());
    }

    @Override
    public String toString() {
        return "IntentionToSellRequest{" + "sellerId='" + sellerId + ", goodId='" + goodId + ", timeStamp=" + timeStamp
            + ", clientId=" + getClientId() + '}';
    }
}
