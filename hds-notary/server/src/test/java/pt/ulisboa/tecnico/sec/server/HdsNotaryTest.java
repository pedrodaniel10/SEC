package pt.ulisboa.tecnico.sec.server;

import static org.junit.Assert.assertTrue;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import org.junit.Before;
import org.junit.Test;
import pt.ulisboa.tecnico.sec.library.HdsProperties;
import pt.ulisboa.tecnico.sec.library.crypto.CryptoUtils;
import pt.ulisboa.tecnico.sec.library.exceptions.UserNotFoundException;
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
        alicePrivateKey = CryptoUtils.getPrivateKey(HdsProperties.getClientPrivateKey("alice"));
        bobPrivateKey = CryptoUtils.getPrivateKey(HdsProperties.getClientPrivateKey("bob"));
        charliePrivateKey = CryptoUtils.getPrivateKey(HdsProperties.getClientPrivateKey("charlie"));

        final HdsNotaryState serverState = PersistenceUtils.getServerState();
        alicePublicKey = serverState.getUserById("0").getPublicKey();
        bobPublicKey = serverState.getUserById("1").getPublicKey();
        charliePublicKey = serverState.getUserById("2").getPublicKey();
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
