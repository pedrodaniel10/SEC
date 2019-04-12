package pt.ulisboa.tecnico.sec.library;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import org.junit.Assert;
import org.junit.Test;
import pt.ulisboa.tecnico.sec.library.crypto.CryptoUtils;
import pt.ulisboa.tecnico.sec.library.data.User;
import pt.ulisboa.tecnico.sec.library.exceptions.UserNotFoundException;

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
                                      IllegalBlockSizeException, InvalidKeyException, BadPaddingException,
                                      NoSuchPaddingException,
                                      InvalidAlgorithmParameterException, InvalidKeySpecException {

        String key = "MIICeAIBADANBgkqhkiG9w0BAQEFAASCAmIwggJeAgEAAoGBAMPRspUaC5h32Ql6 2LjRAGsQrE+hrM2WKVsYlQRkFo6mn9GVoHyx61/Ptu0DZkd4oQo+gP73V17bTGWl ZiP6jTNHhJggz2HwsT/vzQMjKO25fpipOe/Wzns5DsanFx4TiTSGAPTz9UQk2ZVF DwVekHGEl9UwN/+qZELKvrvov6IzAgMBAAECgYB29Twc0h67OAt0c9mWpPkxEYbs NVZp6lAjVBKrATam4Fh0lQZS2i8YHHKPF6KZxpFmTMRGn/HG4UhO86TSNJJzxU/y bT/ye1udT99qCVktvADX3ohCGsgS+N6k6Yo8eoqH1MJ7gEySNCtWpq2Yd/ezWlAZ zw/NMfU6d0IcQtw/MQJBAOdT4duE2Jrp7OT5VMcoH4yB0ydMMLw7r/ZHXq6yFkiO 7dBSNA7vl5IIuAvcfvOq9pO8Jq7uJY3PMuHj4Ed3fesCQQDYtExmAUE59igZZi8m Cmrzyc0eSaQNYluhAevMV2K/XWmY60CT5moTUtYMIKK9BSIkQdKIGA/4rJFWT2Sc GjLZAkEAvVsOkHCoFfbSMYRe/z86w/spawuVASAio4g8WufwEajdxh7j+i3pdmKo tRzi1nblrHzhdWP/XZtz3TB5UEbhzQJBANXIALp8sGlK0sJD0W2Yx2wbf/RKN8JQ bw6Gg6WB69PXho4qPvnpTGolxS4PoBwTDVxxZw2Fl3P+Yh6gkiOBoPkCQQCaNtKS 273TKrrSKKuNxv4OQdBXqGxje/Jp8d5TLK1rm9ypxwuB0FotMAoN6uob3kzVQryp TbT/4W7tSp4tEeTT";
        String password = "admin";

        SecretKeySpec secretKey = CryptoUtils.createSecretKey(password);
        String encodedKey = CryptoUtils.encryptPrivateKey(key, secretKey);
        String decodedKey = CryptoUtils.decryptPrivateKey(encodedKey, secretKey);
        Assert.assertEquals(key, decodedKey);
    }
}
