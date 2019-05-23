package pt.ulisboa.tecnico.sec.server.data;

import java.io.Serializable;

public abstract class Request implements Serializable {

    private String clientId;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

}
