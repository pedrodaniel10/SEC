package pt.ulisboa.tecnico.sec.server.data;

import java.io.Serializable;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

public class IntentionToBuyRequest extends Request implements Serializable {

    private String sellerId;
    private String buyerId;
    private String goodId;

    public IntentionToBuyRequest() {

    }

    public IntentionToBuyRequest(String sellerId, String buyerId, String goodId) {
        this.sellerId = sellerId;
        this.buyerId = buyerId;
        this.goodId = goodId;
        setClientId(buyerId);
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(String buyerId) {
        this.buyerId = buyerId;
    }

    public String getGoodId() {
        return goodId;
    }

    public void setGoodId(String goodId) {
        this.goodId = goodId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IntentionToBuyRequest) {
            IntentionToBuyRequest request = (IntentionToBuyRequest) obj;
            return StringUtils.equals(this.getSellerId(), request.getSellerId()) &&
                StringUtils.equals(this.getBuyerId(), request.getBuyerId()) &&
                StringUtils.equals(this.getGoodId(), request.getGoodId()) &&
                StringUtils.equals(this.getClientId(), request.getClientId());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSellerId(), getBuyerId(), getGoodId(), getClientId());
    }

    @Override
    public String toString() {
        return "IntentionToBuyRequest{" + "sellerId='" + sellerId + ", buyerId='" + buyerId + ", goodId='" + goodId
            + ", clientId='" + getClientId() + '}';
    }
}
