package pt.ulisboa.tecnico.sec.library.data;

import java.io.Serializable;

public class Good implements Serializable {

    private String name;
    private String goodId;
    private String ownerId;
    private boolean onSale;

    public Good() {
    }

    public Good(String name, String goodId, String ownerId) {
        this.name = name;
        this.goodId = goodId;
        this.ownerId = ownerId;
        this.onSale = false;
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
}
