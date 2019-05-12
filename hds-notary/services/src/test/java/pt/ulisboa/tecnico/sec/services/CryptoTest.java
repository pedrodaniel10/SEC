package pt.ulisboa.tecnico.sec.services;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import org.junit.Assert;
import org.junit.Test;
import pt.ulisboa.tecnico.sec.services.crypto.CryptoUtils;
import pt.ulisboa.tecnico.sec.services.properties.HdsProperties;

public class CryptoTest {

    private static final String PLAIN_TEXT = "Test Text to verify signature.";

    @Test
    public void generateKeys() throws NoSuchAlgorithmException,
                                      IllegalBlockSizeException, InvalidKeyException, BadPaddingException,
                                      NoSuchPaddingException,
                                      InvalidAlgorithmParameterException, InvalidKeySpecException {

        String key = "key";
        String password = "admin";

        SecretKeySpec secretKey = CryptoUtils.createSecretKey(password);
        String encodedKey = CryptoUtils.encryptPrivateKey(key, secretKey);
        String decodedKey = CryptoUtils.decryptPrivateKey(encodedKey, secretKey);
        Assert.assertEquals(key, decodedKey);
    }

    @Test
    public void checkSignatureAlice() throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        PrivateKey privateKey = HdsProperties.getClientPrivateKey("alice", "Uvv1j7a60q2q0a4");
        PublicKey publicKey = HdsProperties.getClientPublicKey("alice");

        String signature = CryptoUtils.makeDigitalSignature(privateKey, PLAIN_TEXT);
        Assert.assertTrue(CryptoUtils.verifyDigitalSignature(publicKey, signature, PLAIN_TEXT));
    }

    @Test
    public void checkSignatureBob() throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        PrivateKey privateKey = HdsProperties.getClientPrivateKey("bob", "JNTpC0SE9Hzb3SG");
        PublicKey publicKey = HdsProperties.getClientPublicKey("bob");

        String signature = CryptoUtils.makeDigitalSignature(privateKey, PLAIN_TEXT);
        Assert.assertTrue(CryptoUtils.verifyDigitalSignature(publicKey, signature, PLAIN_TEXT));
    }

    @Test
    public void checkSignatureCharlie() throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        PrivateKey privateKey = HdsProperties.getClientPrivateKey("charlie", "9QrKUNt9HAXPKG9");
        PublicKey publicKey = HdsProperties.getClientPublicKey("charlie");

        String signature = CryptoUtils.makeDigitalSignature(privateKey, PLAIN_TEXT);
        Assert.assertTrue(CryptoUtils.verifyDigitalSignature(publicKey, signature, PLAIN_TEXT));
    }

    @Test
    public void checkSignatureServer0() throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        PrivateKey privateKey = HdsProperties.getServerPrivateKey("0", "admin");
        PublicKey publicKey = HdsProperties.getServerPublicKey("0");

        String signature = CryptoUtils.makeDigitalSignature(privateKey, PLAIN_TEXT);
        Assert.assertTrue(CryptoUtils.verifyDigitalSignature(publicKey, signature, PLAIN_TEXT));
    }

    @Test
    public void checkSignatureNotary0() throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        PrivateKey privateKey = HdsProperties.getNotarySignaturePrivateKey("0", "admin");
        PublicKey publicKey = HdsProperties.getNotarySignaturePublicKey("0");

        String signature = CryptoUtils.makeDigitalSignature(privateKey, PLAIN_TEXT);
        Assert.assertTrue(CryptoUtils.verifyDigitalSignature(publicKey, signature, PLAIN_TEXT));
    }

    @Test
    public void checkSignatureServer1() throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        PrivateKey privateKey = HdsProperties.getServerPrivateKey("1", "admin");
        PublicKey publicKey = HdsProperties.getServerPublicKey("1");

        String signature = CryptoUtils.makeDigitalSignature(privateKey, PLAIN_TEXT);
        Assert.assertTrue(CryptoUtils.verifyDigitalSignature(publicKey, signature, PLAIN_TEXT));
    }

    @Test
    public void checkSignatureNotary1() throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        PrivateKey privateKey = HdsProperties.getNotarySignaturePrivateKey("1", "admin");
        PublicKey publicKey = HdsProperties.getNotarySignaturePublicKey("1");

        String signature = CryptoUtils.makeDigitalSignature(privateKey, PLAIN_TEXT);
        Assert.assertTrue(CryptoUtils.verifyDigitalSignature(publicKey, signature, PLAIN_TEXT));
    }

    @Test
    public void checkSignatureServer2() throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        PrivateKey privateKey = HdsProperties.getServerPrivateKey("2", "admin");
        PublicKey publicKey = HdsProperties.getServerPublicKey("2");

        String signature = CryptoUtils.makeDigitalSignature(privateKey, PLAIN_TEXT);
        Assert.assertTrue(CryptoUtils.verifyDigitalSignature(publicKey, signature, PLAIN_TEXT));
    }

    @Test
    public void checkSignatureNotary2() throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        PrivateKey privateKey = HdsProperties.getNotarySignaturePrivateKey("2", "admin");
        PublicKey publicKey = HdsProperties.getNotarySignaturePublicKey("2");

        String signature = CryptoUtils.makeDigitalSignature(privateKey, PLAIN_TEXT);
        Assert.assertTrue(CryptoUtils.verifyDigitalSignature(publicKey, signature, PLAIN_TEXT));
    }

    @Test
    public void checkSignatureServer3() throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        PrivateKey privateKey = HdsProperties.getServerPrivateKey("3", "admin");
        PublicKey publicKey = HdsProperties.getServerPublicKey("3");

        String signature = CryptoUtils.makeDigitalSignature(privateKey, PLAIN_TEXT);
        Assert.assertTrue(CryptoUtils.verifyDigitalSignature(publicKey, signature, PLAIN_TEXT));
    }

    @Test
    public void checkSignatureNotary3() throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        PrivateKey privateKey = HdsProperties.getNotarySignaturePrivateKey("3", "admin");
        PublicKey publicKey = HdsProperties.getNotarySignaturePublicKey("3");

        String signature = CryptoUtils.makeDigitalSignature(privateKey, PLAIN_TEXT);
        Assert.assertTrue(CryptoUtils.verifyDigitalSignature(publicKey, signature, PLAIN_TEXT));
    }
}
