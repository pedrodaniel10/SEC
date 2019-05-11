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

        String key = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBALXWtAJRIQT/oUF6SQFATc9T2OAv+0jb1ap5UKHlHJJxa3dt2NQfaY+g7he7iM10TKk3Vp6c3JotUv8L/NoTuMDcX+lomwNzsnbPkMgAGLmr6e5RBCSvmZ7eRKZyyp/LrB9Oh4Dmc3T67FzqtCESp2lRWwJVGDpj3dkPGQJTShPJAgMBAAECgYBIexN3lPUPdAHIAsFU4Vfim25oNlf4e8AYWpD8Z0HTUahfi4aRTxAZszEUfqkSFBh2nttFEAuS3RwnE5UvLAtJEK6CPJY1jU2EYHFRrG0+1a6cdjxewwn8H/ErP1nRQl+uEapZ4FeabCPjZpjbbzGcwwJ/8lSlJHZBgaJ/xzgoXQJBAOAMepoQgpqZU5Goxg45UnJjXGovBw1Cg9QETMBFVlQWRiZu5qtFvi/rujS0jYoSBd4swHg0j4t4lHnvpRYXV9cCQQDPxTqVMDJlSxmjb2P+hmYgulI74vP9pbzQ1N1IrCWgnh/v6ObMyfvdS6/ZAq66OpWCzw1+kgBE4BlMTNDZl/1fAkEAw0+9lQ6te+4Lnt/c/8tSpysdBefHIvUIDMxOyBHpxtXAA4MMDOR4fjfllEqNVH6PpPWoN2HCfNf5vy584Hwq8wJAWgEQXaxhSGv+EUShxpKmDytnkDXCiHCRmM19hou8SRX2s9DqmnAtQTIWXSXCVz4lomLbb6cDotKJYvorxvcGdQJBAIUPqZ/XYoGeMtR3dsJEZLqgVnJU6+cOSMzvhybAIj0XR7kMLoK6DSnbwVQgA7sp/1gorf1OunPOuQ3C1IKrkm4=";
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

        byte[] signature = CryptoUtils.makeDigitalSignature(privateKey, PLAIN_TEXT);
        Assert.assertTrue(CryptoUtils.verifyDigitalSignature(publicKey, signature, PLAIN_TEXT));
    }

    @Test
    public void checkSignatureBob() throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        PrivateKey privateKey = HdsProperties.getClientPrivateKey("bob", "JNTpC0SE9Hzb3SG");
        PublicKey publicKey = HdsProperties.getClientPublicKey("bob");

        byte[] signature = CryptoUtils.makeDigitalSignature(privateKey, PLAIN_TEXT);
        Assert.assertTrue(CryptoUtils.verifyDigitalSignature(publicKey, signature, PLAIN_TEXT));
    }

    @Test
    public void checkSignatureCharlie() throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        PrivateKey privateKey = HdsProperties.getClientPrivateKey("charlie", "9QrKUNt9HAXPKG9");
        PublicKey publicKey = HdsProperties.getClientPublicKey("charlie");

        byte[] signature = CryptoUtils.makeDigitalSignature(privateKey, PLAIN_TEXT);
        Assert.assertTrue(CryptoUtils.verifyDigitalSignature(publicKey, signature, PLAIN_TEXT));
    }

    @Test
    public void checkSignatureServer0() throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        PrivateKey privateKey = HdsProperties.getServerPrivateKey("0", "admin");
        PublicKey publicKey = HdsProperties.getServerPublicKey("0");

        byte[] signature = CryptoUtils.makeDigitalSignature(privateKey, PLAIN_TEXT);
        Assert.assertTrue(CryptoUtils.verifyDigitalSignature(publicKey, signature, PLAIN_TEXT));
    }

    @Test
    public void checkSignatureNotary0() throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        PrivateKey privateKey = HdsProperties.getNotarySignaturePrivateKey("0", "admin");
        PublicKey publicKey = HdsProperties.getNotarySignaturePublicKey("0");

        byte[] signature = CryptoUtils.makeDigitalSignature(privateKey, PLAIN_TEXT);
        Assert.assertTrue(CryptoUtils.verifyDigitalSignature(publicKey, signature, PLAIN_TEXT));
    }

}
