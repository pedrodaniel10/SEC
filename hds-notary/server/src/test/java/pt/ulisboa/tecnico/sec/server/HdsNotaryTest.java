package pt.ulisboa.tecnico.sec.server;

import static org.junit.Assert.assertTrue;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import org.junit.Before;
import org.junit.Test;
import pt.ulisboa.tecnico.sec.services.crypto.CryptoUtils;
import pt.ulisboa.tecnico.sec.services.properties.HdsProperties;
import pt.ulisboa.tecnico.sec.services.exceptions.UserNotFoundException;
import pt.ulisboa.tecnico.sec.server.services.HdsNotaryState;
import pt.ulisboa.tecnico.sec.server.utils.PersistenceUtils;

/**
 * Unit test for simple HdsNotaryApplication.
 */
public class HdsNotaryTest {

    private static RSAPrivateKey alicePrivateKey;
    private static RSAPrivateKey bobPrivateKey;
    private static RSAPrivateKey charliePrivateKey;

    private static RSAPublicKey alicePublicKey;
    private static RSAPublicKey bobPublicKey;
    private static RSAPublicKey charliePublicKey;

    @Before
    public void setup() throws UserNotFoundException {
        alicePrivateKey = HdsProperties.getClientPrivateKey("alice", "Uvv1j7a60q2q0a4");
        bobPrivateKey = HdsProperties.getClientPrivateKey("bob", "JNTpC0SE9Hzb3SG");
        charliePrivateKey = HdsProperties.getClientPrivateKey("charlie", "9QrKUNt9HAXPKG9");

        final HdsNotaryState serverState = PersistenceUtils.getServerState();
        alicePublicKey = HdsProperties.getClientPublicKey("alice");
        bobPublicKey = HdsProperties.getClientPublicKey("bob");
        charliePublicKey = HdsProperties.getClientPublicKey("charlie");
    }

    @Test
    public void testKeyPairs()
        throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        String plainText = "test";

        byte[] aliceSignature = CryptoUtils.makeDigitalSignature(alicePrivateKey, plainText);
        byte[] bobSignature = CryptoUtils.makeDigitalSignature(bobPrivateKey, plainText);
        byte[] charlieSignature = CryptoUtils.makeDigitalSignature(charliePrivateKey, plainText);

        assertTrue(CryptoUtils.verifyDigitalSignature(alicePublicKey, aliceSignature, plainText));
        assertTrue(CryptoUtils.verifyDigitalSignature(bobPublicKey, bobSignature, plainText));
        assertTrue(
            CryptoUtils.verifyDigitalSignature(charliePublicKey, charlieSignature, plainText));
    }
}
