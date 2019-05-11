package pt.ulisboa.tecnico.sec.services.crypto;

import java.security.InvalidAlgorithmParameterException;
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
import java.util.UUID;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

public final class CryptoUtils {

    private static final Logger logger = Logger.getLogger(CryptoUtils.class);

    /**
     * Digital signature algorithm.
     */
    private static final String SIGNATURE_ALGO = "SHA256withRSA";
    private static final String PASSWORD_ALGO = "PBKDF2WithHmacSHA512";
    private static final String SYM_CIPHER = "AES/CBC/PKCS5Padding";
    private static final IvParameterSpec IV = new IvParameterSpec(
        new byte[]{0x59, (byte) 0xee, 0x74, 0x00, 0x0a, (byte) 0xe1, (byte) 0xe9, 0x16, (byte) 0xb0, (byte) 0xaa, 0x00,
            (byte) 0x81, (byte) 0xd2, 0x33, (byte) 0xc3, 0x3a});
    private static final String SALT = "N4V1fQkbwYKDL5bn";

    private static final int ITERATION_COUNT = 40000;

    private CryptoUtils() {
    }

    public static String generateNonce() {
        return UUID.randomUUID().toString();
    }

    /**
     * Decodes the plain key into RSAPrivateKey using key derivation (password-based)
     *
     * @param key      encoded key.
     * @param password password to encrypt the keys
     * @return the RSAPrivateKey decoded key.
     */
    public static RSAPrivateKey getPrivateKey(String key, String password) {
        try {
            SecretKeySpec secretKey = createSecretKey(password);
            String decryptedKey = decryptPrivateKey(key, secretKey);
            return getPrivateKey(decryptedKey);

        } catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException | InvalidKeyException
            | BadPaddingException | IllegalBlockSizeException | InvalidAlgorithmParameterException e) {
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
     * @param privateKey private key used to sign
     * @param message    the message in order
     */
    public static String makeDigitalSignature(PrivateKey privateKey, String... message)
        throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        String messageConcat = String.join("", message);
        return CryptoUtils.makeDigitalSignature(messageConcat.getBytes(), privateKey);
    }

    /**
     * Verifies the digital signature of the plain text
     *
     * @param publicKey    public key to check signature
     * @param cipherDigest the signature
     * @param plainText    the plain text
     */
    public static boolean verifyDigitalSignature(PublicKey publicKey, String cipherDigest, String... plainText) {
        return CryptoUtils.verifyDigitalSignature(publicKey,Base64.decodeBase64(cipherDigest), plainText);
    }

    /**
     * Makes the digital signature of the plain text
     *
     * @param bytes      plain text.
     * @param privatekey the private key.
     * @return the generated signature.
     */
    private static String makeDigitalSignature(byte[] bytes, PrivateKey privatekey)
        throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {

        Signature sig = Signature.getInstance(SIGNATURE_ALGO);
        sig.initSign(privatekey);
        sig.update(bytes);
        return Base64.encodeBase64String(sig.sign());
    }

    /**
     * Creates the secret key derived from the privatekey stored in the file
     *
     * @param password password used to encrypt the privatekey
     */
    public static SecretKeySpec createSecretKey(String password)
        throws NoSuchAlgorithmException, InvalidKeySpecException {

        byte[] salt = SALT.getBytes();

        /* Generate secret key derived from password */
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(PASSWORD_ALGO);
        PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, 256);
        SecretKey secretKey = keyFactory.generateSecret(keySpec);

        /* Encode the secret key into a AES key */
        return new SecretKeySpec(secretKey.getEncoded(), "AES");
    }

    /**
     * Decrypts the stored private key with a secret key
     *
     * @param encryptedKey stored private key
     * @param secretKey    used to encrypt the key
     */
    public static String decryptPrivateKey(String encryptedKey, SecretKeySpec secretKey)
        throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
               BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {

        byte[] encodedKey = Base64.decodeBase64(encryptedKey);
        Cipher cipher = Cipher.getInstance(SYM_CIPHER);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, IV);
        return new String(cipher.doFinal(encodedKey));
    }

    /**
     * @param decryptedKey plain key
     * @param secretKey    used to encrypt the key
     */
    public static String encryptPrivateKey(String decryptedKey, SecretKeySpec secretKey)
        throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
               BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {

        Cipher cipher = Cipher.getInstance(SYM_CIPHER);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, IV);
        byte[] encryptedKey = cipher.doFinal(decryptedKey.getBytes());
        return Base64.encodeBase64String(encryptedKey);
    }

    /**
     * Decodes the key into RSAPrivateKey
     *
     * @param key encoded key.
     * @return the RSAPrivateKey decoded key.
     */
    private static RSAPrivateKey getPrivateKey(String key) {
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
     * Verifies the signature with the provided PublicKey
     *
     * @param cipherDigest The received signature
     * @param bytes        the plain text
     * @param publicKey    the public key to test the signature.
     * @return true if the signature matches and false otherwise or an error occurs.
     */
    private static boolean verifyDigitalSignature(byte[] cipherDigest, byte[] bytes, PublicKey publicKey) {
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
     * Verifies the digital signature of the plain text
     *
     * @param publicKey    public key to check signature
     * @param cipherDigest the signature
     * @param plainText    the plain text
     */
    private static boolean verifyDigitalSignature(PublicKey publicKey, byte[] cipherDigest, String... plainText) {
        String messageConcat = String.join("", plainText);
        return CryptoUtils.verifyDigitalSignature(cipherDigest, messageConcat.getBytes(), publicKey);
    }


}
