package pt.ulisboa.tecnico.sec.library;

import org.junit.Assert;
import org.junit.Test;
import pt.ulisboa.tecnico.sec.library.crypto.CryptoUtils;
import pt.ulisboa.tecnico.sec.library.data.User;
import pt.ulisboa.tecnico.sec.library.exceptions.UserNotFoundException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

/**
 * Library tests
 */
public class LibraryTest {

    @Test
    public void checkUsers() throws UserNotFoundException {
        User alice = HdsProperties.getUser("alice");
        User bob = HdsProperties.getUser("bob");
        User charlie = HdsProperties.getUser("charlie");

        Assert.assertEquals("0", alice.getUserId());
        Assert.assertEquals("1", bob.getUserId());
        Assert.assertEquals("2", charlie.getUserId());

    }

    @Test
    public void generateKeys() throws NoSuchAlgorithmException,
            IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchPaddingException,
            InvalidAlgorithmParameterException, InvalidKeySpecException {

        String key = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAN+wIrgV+9at9M6B axYH/wzo7EYpYkSYd17OiDNECLqak";
        String password = "password";

        SecretKeySpec secretKey = CryptoUtils.createSecretKey(password);
        String encodedKey = CryptoUtils.encryptPrivateKey(key, secretKey);
        String decodedKey = CryptoUtils.decryptPrivateKey(encodedKey, secretKey);
        Assert.assertEquals(key, decodedKey);
    }
}
