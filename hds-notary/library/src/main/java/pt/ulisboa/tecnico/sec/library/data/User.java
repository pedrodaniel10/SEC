package pt.ulisboa.tecnico.sec.library.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import pt.ulisboa.tecnico.sec.library.crypto.CryptoUtils;

public class User implements Serializable {

    private String name;
    private String userId;
    private String publicKeyS;
    private int requestNumber;

    @JsonIgnore
    private RSAPrivateKey privateKey;

    @JsonIgnore
    private RSAPublicKey publicKey;

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

    public String getPublicKeyS() {
        return publicKeyS;
    }

    public void setPublicKeyS(String publicKeyS) {
        this.publicKeyS = publicKeyS;
        this.publicKey = CryptoUtils.getPublicKey(publicKeyS);
    }

    public int getRequestNumber() {
        return requestNumber;
    }

    public void setRequestNumber(int requestNumber) {
        this.requestNumber = requestNumber;
    }

    public RSAPrivateKey getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(RSAPrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    public RSAPublicKey getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(RSAPublicKey publicKey) {
        this.publicKey = publicKey;
    }

    public void incrementRequestNumber() {
        this.requestNumber++;
    }
}
