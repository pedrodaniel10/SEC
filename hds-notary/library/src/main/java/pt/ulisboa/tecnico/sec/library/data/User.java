package pt.ulisboa.tecnico.sec.library.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import pt.ulisboa.tecnico.sec.library.crypto.CryptoUtils;

import java.io.Serializable;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

public class User implements Serializable {

    private String name;
    private String userId;
    private String publicKeyS;
    private String nonce;

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

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
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

    public void generateNonce() {
        this.nonce = CryptoUtils.generateNonce();
    }
}
