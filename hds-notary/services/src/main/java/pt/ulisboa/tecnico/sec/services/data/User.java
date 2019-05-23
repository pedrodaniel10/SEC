package pt.ulisboa.tecnico.sec.services.data;

import java.io.Serializable;
import pt.ulisboa.tecnico.sec.services.crypto.CryptoUtils;

public class User implements Serializable {

    private String name;
    private String userId;
    private String nonce;

    public User() {
    }

    public User(String name, String userId) {
        this.name = name;
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public String generateNonce() {
        this.nonce = CryptoUtils.generateNonce();
        return this.nonce;
    }
}
