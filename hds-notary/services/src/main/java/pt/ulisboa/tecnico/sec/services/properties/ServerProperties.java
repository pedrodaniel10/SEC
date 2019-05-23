package pt.ulisboa.tecnico.sec.services.properties;

import java.io.Serializable;

class ServerProperties implements Serializable {

    private String id;
    private String publicKey;
    private String privateKey;
    private String host;
    private int port;
    private String notarySignaturePublicKey;
    private String notarySignaturePrivateKey;

    public ServerProperties() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getNotarySignaturePublicKey() {
        return notarySignaturePublicKey;
    }

    public void setNotarySignaturePublicKey(String notarySignaturePublicKey) {
        this.notarySignaturePublicKey = notarySignaturePublicKey;
    }

    public String getNotarySignaturePrivateKey() {
        return notarySignaturePrivateKey;
    }

    public void setNotarySignaturePrivateKey(String notarySignaturePrivateKey) {
        this.notarySignaturePrivateKey = notarySignaturePrivateKey;
    }
}
