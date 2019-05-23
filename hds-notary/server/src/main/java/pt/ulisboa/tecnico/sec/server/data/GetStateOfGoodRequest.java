package pt.ulisboa.tecnico.sec.server.data;

import java.io.Serializable;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

public class GetStateOfGoodRequest extends Request implements Serializable {

    private String userId;
    private String goodId;
    private int readId;

    public GetStateOfGoodRequest() {
    }

    public GetStateOfGoodRequest(String userId, String goodId, int readId) {
        this.userId = userId;
        this.goodId = goodId;
        this.readId = readId;
        this.setClientId(userId);
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getGoodId() {
        return goodId;
    }

    public void setGoodId(String goodId) {
        this.goodId = goodId;
    }

    public int getReadId() {
        return readId;
    }

    public void setReadId(int readId) {
        this.readId = readId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof GetStateOfGoodRequest) {
            GetStateOfGoodRequest request = (GetStateOfGoodRequest) obj;
            return StringUtils.equals(this.getUserId(), request.getUserId()) &&
                StringUtils.equals(this.getGoodId(), request.getGoodId()) &&
                this.getReadId() == request.getReadId() &&
                StringUtils.equals(this.getClientId(), request.getClientId());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUserId(), getGoodId(), getReadId(), getClientId());
    }

    @Override
    public String toString() {
        return "GetStateOfGoodRequest{" + "userId='" + userId + ", goodId='" + goodId + ", readId=" + readId
            + ", clientId=" + getClientId() + '}';
    }
}
