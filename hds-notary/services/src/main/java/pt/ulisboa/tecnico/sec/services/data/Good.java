package pt.ulisboa.tecnico.sec.services.data;

import java.io.Serializable;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class Good implements Serializable {

    private String name;
    private String goodId;
    private String ownerId;
    private boolean onSale;
    private int timeStamp;
    private Map<String, Integer> listening;

    public Good() {
    }

    public Good(Good good) {
        this.name = good.getName();
        this.goodId = good.getGoodId();
        this.ownerId = good.getOwnerId();
        this.onSale = good.isOnSale();
        this.timeStamp = good.getTimeStamp();
    }


    public String getGoodId() {
        return goodId;
    }

    public void setGoodId(String goodId) {
        this.goodId = goodId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isOnSale() {
        return onSale;
    }

    public void setOnSale(boolean onSale) {
        this.onSale = onSale;
    }

    public int getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(int timeStamp) {
        this.timeStamp = timeStamp;
    }

    public Map<String, Integer> getListening() {
        return listening;
    }

    public void setListening(Map<String, Integer> listening) {
        this.listening = listening;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Good) {
            Good good = (Good) obj;
            return StringUtils.equals(this.getName(), good.getName()) &&
                StringUtils.equals(this.getGoodId(), good.getGoodId()) &&
                StringUtils.equals(this.ownerId, good.getOwnerId()) &&
                this.isOnSale() == good.isOnSale();
        }
        return false;
    }

    @Override
    public String toString() {
        return this.name + this.goodId + this.ownerId + this.onSale;
    }
}
