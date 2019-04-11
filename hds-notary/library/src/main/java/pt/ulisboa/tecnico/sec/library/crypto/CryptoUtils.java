package pt.ulisboa.tecnico.sec.library.crypto;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

public final class CryptoUtils {

    private static final Logger logger = Logger.getLogger(CryptoUtils.class);

    /**
     * Digital signature algorithm.
     */
    private static final String SIGNATURE_ALGO = "SHA256withRSA";

    private CryptoUtils() {
    }

    /**
     * Decodes the key into RSAPrivateKey
     *
     * @param key encoded key.
     * @return the RSAPrivateKey decoded key.
     */
    public static RSAPrivateKey getPrivateKey(String key) {
        try {
            byte[] encoded = Base64.decodeBase64(key);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
            return (RSAPrivateKey) kf.generatePrivate(keySpec);

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            logger.error(e);
        }
        return null;
    }

    /**
     * Decodes the key into RSAPublicKey
     *
     * @param key encoded key.
     * @return the RSAPublicKey decoded key
     */
    public static RSAPublicKey getPublicKey(String key) {
        try {
            byte[] encoded = Base64.decodeBase64(key);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return (RSAPublicKey) kf.generatePublic(new X509EncodedKeySpec(encoded));

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            logger.error(e);
        }
        return null;
    }

    /**
     * Returns the digital signature of the message
     *
     * @param privatekey private key used to sign
     * @param message the message in order
     */
    public static byte[] makeDigitalSignature(PrivateKey privatekey, String... message)
        throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        String messageConcat = String.join("", message);
        return CryptoUtils.makeDigitalSignature(messageConcat.getBytes(), privatekey);
    }

    /**
     * Verifies the digital signature of the plain text
     *
     * @param publicKey public key to check signature
     * @param cipherDigest the signature
     * @param plainText the plain text
     */
    public static boolean verifyDigitalSignature(PublicKey publicKey, byte[] cipherDigest,
        String... plainText) {
        String messageConcat = String.join("", plainText);
        return CryptoUtils
            .verifyDigitalSignature(cipherDigest, messageConcat.getBytes(), publicKey);
    }

    /**
     * Verifies the signature with the provided PublicKey
     *
     * @param cipherDigest The received signature
     * @param bytes the plain text
     * @param publicKey the public key to test the signature.
     * @return true if the signature matches and false otherwise or an error occurs.
     */
    private static boolean verifyDigitalSignature(byte[] cipherDigest, byte[] bytes,
        PublicKey publicKey) {
        try {
            Signature sig = Signature.getInstance(SIGNATURE_ALGO);
            sig.initVerify(publicKey);
            sig.update(bytes);
            return sig.verify(cipherDigest);
        } catch (SignatureException | InvalidKeyException | NoSuchAlgorithmException e) {
            logger.error(e);
            return false;
        }
    }

    /**
     * Makes the digital signature of the plain text
     *
     * @param bytes plain text.
     * @param privatekey the private key.
     * @return the generated signature.
     */
    private static byte[] makeDigitalSignature(byte[] bytes, PrivateKey privatekey)
        throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {

        Signature sig = Signature.getInstance(SIGNATURE_ALGO);
        sig.initSign(privatekey);
        sig.update(bytes);
        return sig.sign();
    }
}
