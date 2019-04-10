package pt.ulisboa.tecnico.sec.client;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateKey;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import pt.ulisboa.tecnico.sec.library.HdsProperties;
import pt.ulisboa.tecnico.sec.library.crypto.CryptoUtils;
import pt.ulisboa.tecnico.sec.library.data.Good;
import pt.ulisboa.tecnico.sec.library.data.Transaction;
import pt.ulisboa.tecnico.sec.library.exceptions.GoodWrongOwnerException;
import pt.ulisboa.tecnico.sec.library.exceptions.InvalidNonceException;
import pt.ulisboa.tecnico.sec.library.exceptions.InvalidSignatureException;
import pt.ulisboa.tecnico.sec.library.exceptions.ServerException;
import pt.ulisboa.tecnico.sec.library.interfaces.server.HdsNotaryService;
import pt.ulisboa.tecnico.sec.server.HdsNotaryApplication;
import pt.ulisboa.tecnico.sec.server.client.ClientApplication;

/**
 * Unit test for ServerServiceTest.
 */
public class ServerServiceTest {

    private static RSAPrivateKey alicePrivateKey;
    private static RSAPrivateKey bobPrivateKey;
    private static RSAPrivateKey charliePrivateKey;

    private static HdsNotaryService hdsNotaryService;

    @BeforeClass
    public static void setupServer()
        throws RemoteException, NotBoundException, MalformedURLException {
        new Thread(() -> HdsNotaryApplication.main(new String[]{})).start();

        new Thread(() -> ClientApplication.main(new String[]{"alice", "-Dpassword=password"})).start();
        new Thread(() -> ClientApplication.main(new String[]{"bob", "-Dpassword=password"})).start();
        new Thread(() -> ClientApplication.main(new String[]{"charlie", "-Dpassword=password"})).start();

        alicePrivateKey = CryptoUtils.getPrivateKey(HdsProperties.getClientPrivateKey("alice"), "password");
        bobPrivateKey = CryptoUtils.getPrivateKey(HdsProperties.getClientPrivateKey("bob"), "password");
        charliePrivateKey = CryptoUtils.getPrivateKey(HdsProperties.getClientPrivateKey("charlie"), "password");

        hdsNotaryService = (HdsNotaryService) Naming.lookup(HdsProperties.getServerUri());
    }

    // Status of Good
    @Test
    public void testStatusOfGood() throws ServerException, RemoteException {
        Good good = hdsNotaryService.getStateOfGood("0");
        Assert.assertEquals("House", good.getName());
        Assert.assertEquals("2", good.getOwnerId());
        Assert.assertEquals("0", good.getGoodId());
        Assert.assertFalse(good.isOnSale());
    }

    @Test(expected = ServerException.class)
    public void testWrongStatusOfGood() throws ServerException, RemoteException {
        hdsNotaryService.getStateOfGood("This id does not exist.");
    }

    @Test(expected = ServerException.class)
    public void testNullStatusOfGood() throws ServerException, RemoteException {
        hdsNotaryService.getStateOfGood(null);
    }

    @Test(expected = ServerException.class)
    public void testEmptyStatusOfGood() throws ServerException, RemoteException {
        hdsNotaryService.getStateOfGood("     ");
    }

    // IntentionToSell
    @Test
    public void testOkIntentionToSell()
        throws ServerException, RemoteException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        String userId = "0";
        String goodId = "1";
        String nonce = hdsNotaryService.getNonce(userId);

        boolean success = hdsNotaryService.intentionToSell(userId, goodId, nonce,
            CryptoUtils.makeDigitalSignature(alicePrivateKey,
                userId, goodId, nonce));
        Assert.assertTrue(success);
    }

    @Test
    public void testOkTwoFollowedRequestsIntentionToSell()
        throws ServerException, RemoteException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        String userId = "0";
        String goodId = "1";
        String nonce = hdsNotaryService.getNonce(userId);

        boolean success = hdsNotaryService.intentionToSell(userId, goodId, nonce,
            CryptoUtils.makeDigitalSignature(alicePrivateKey,
                userId, goodId, nonce));
        Assert.assertTrue(success);

        nonce = hdsNotaryService.getNonce(userId);
        success = hdsNotaryService.intentionToSell(userId, goodId, nonce,
            CryptoUtils.makeDigitalSignature(alicePrivateKey,
                userId, goodId, nonce));
        Assert.assertTrue(success);
    }

    @Test(expected = GoodWrongOwnerException.class)
    public void testNOkWrongOwnerIntentionToSell()
        throws ServerException, RemoteException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        String userId = "0";
        String goodId = "0";

        String nonce = hdsNotaryService.getNonce(userId);

        hdsNotaryService.intentionToSell(userId, goodId, nonce,
            CryptoUtils.makeDigitalSignature(alicePrivateKey,
                userId, goodId, nonce));
    }

    @Test(expected = InvalidSignatureException.class)
    public void testNOkSpoofingSignatureIntentionToSell()
        throws ServerException, RemoteException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        String userId = "0";
        String goodId = "1";

        String nonce = hdsNotaryService.getNonce(userId);

        hdsNotaryService.intentionToSell(userId, goodId, nonce,
            CryptoUtils.makeDigitalSignature(bobPrivateKey,
                userId, goodId, nonce));
    }

    @Test(expected = InvalidSignatureException.class)
    public void testNOkSpoofingIntentionToSell()
        throws ServerException, RemoteException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        String userId = "1";
        String goodId = "1";

        String nonce = hdsNotaryService.getNonce(userId);

        hdsNotaryService.intentionToSell(userId, goodId, nonce,
            CryptoUtils.makeDigitalSignature(alicePrivateKey,
                userId, goodId, nonce));
    }

    @Test(expected = InvalidSignatureException.class)
    public void testNOkTamperingIntentionToSell()
        throws ServerException, RemoteException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        String userId = "0";
        String goodId = "0";

        String nonce = hdsNotaryService.getNonce(userId);

        hdsNotaryService.intentionToSell(userId, "different", nonce,
            CryptoUtils.makeDigitalSignature(alicePrivateKey,
                userId, goodId, nonce));
    }

    @Test(expected = InvalidNonceException.class)
    public void testNOkReplayAttackIntentionToSell()
        throws ServerException, RemoteException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        String userId = "0";
        String goodId = "1";
        String nonce = hdsNotaryService.getNonce(userId);

        hdsNotaryService.intentionToSell(userId, goodId, nonce,
            CryptoUtils.makeDigitalSignature(alicePrivateKey,
                userId, goodId, nonce));

        hdsNotaryService.intentionToSell(userId, goodId, nonce,
            CryptoUtils.makeDigitalSignature(alicePrivateKey,
                userId, goodId, nonce));
    }

    // IntentionToBuy
    @Test
    public void testOkIntentionToBuy()
        throws ServerException, RemoteException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        String userId = "0";
        String goodId = "2";
        String ownerId = "1";
        String nonce = hdsNotaryService.getNonce(userId);

        final Transaction transactionRequest = hdsNotaryService.intentionToBuy(ownerId,
            userId,
            goodId,
            nonce,
            CryptoUtils.makeDigitalSignature(alicePrivateKey, ownerId, userId,
                goodId, nonce));

        Assert.assertNotNull(transactionRequest);
    }

    @Test
    public void testOkTwoFollowedRequestsIntentionToBuy()
        throws ServerException, RemoteException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        String userId = "0";
        String goodId = "2";
        String ownerId = "1";
        String nonce = hdsNotaryService.getNonce(userId);

        Transaction transactionRequest = hdsNotaryService.intentionToBuy(ownerId,
            userId,
            goodId,
            nonce,
            CryptoUtils.makeDigitalSignature(alicePrivateKey, ownerId, userId,
                goodId, nonce));

        Assert.assertNotNull(transactionRequest);

        nonce = hdsNotaryService.getNonce(userId);
        Transaction transactionRequest2 = hdsNotaryService.intentionToBuy(ownerId,
            userId,
            goodId,
            nonce,
            CryptoUtils.makeDigitalSignature(alicePrivateKey, ownerId, userId,
                goodId, nonce));

        Assert.assertNotNull(transactionRequest2);
        Assert.assertEquals(transactionRequest.getTransactionId(),
            transactionRequest2.getTransactionId());
    }

    @Test(expected = GoodWrongOwnerException.class)
    public void testNOkWrongOwnerIntentionToBuy()
        throws ServerException, RemoteException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        String userId = "0";
        String goodId = "2";
        String ownerId = "2";
        String nonce = hdsNotaryService.getNonce(userId);

        hdsNotaryService.intentionToBuy(ownerId,
            userId,
            goodId,
            nonce,
            CryptoUtils.makeDigitalSignature(alicePrivateKey, ownerId, userId,
                goodId, nonce));
    }

    @Test(expected = InvalidSignatureException.class)
    public void testNOkSpoofingSignatureIntentionToBuy()
        throws ServerException, RemoteException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        String userId = "0";
        String goodId = "2";
        String ownerId = "1";
        String nonce = hdsNotaryService.getNonce(userId);

        hdsNotaryService.intentionToBuy(ownerId,
            userId,
            goodId,
            nonce,
            CryptoUtils.makeDigitalSignature(bobPrivateKey, ownerId, userId,
                goodId, nonce));
    }

    @Test(expected = InvalidSignatureException.class)
    public void testNOkSpoofingIntentionToBuy()
        throws ServerException, RemoteException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        String userId = "2";
        String goodId = "2";
        String ownerId = "1";
        String nonce = hdsNotaryService.getNonce(userId);

        hdsNotaryService.intentionToBuy(ownerId,
            userId,
            goodId,
            nonce,
            CryptoUtils.makeDigitalSignature(alicePrivateKey, ownerId, userId,
                goodId, nonce));
    }

    @Test(expected = InvalidSignatureException.class)
    public void testNOkTamperingIntentionToBuy()
        throws ServerException, RemoteException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        String userId = "0";
        String goodId = "2";
        String ownerId = "1";
        String nonce = hdsNotaryService.getNonce(userId);

        hdsNotaryService.intentionToBuy(ownerId,
            userId,
            "different",
            nonce,
            CryptoUtils.makeDigitalSignature(alicePrivateKey, ownerId, userId,
                goodId, nonce));
    }

    @Test(expected = InvalidNonceException.class)
    public void testNOkReplayAttackIntentionToBuy()
        throws ServerException, RemoteException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        String userId = "0";
        String goodId = "2";
        String ownerId = "1";
        String nonce = hdsNotaryService.getNonce(userId);

        hdsNotaryService.intentionToBuy(ownerId,
            userId,
            goodId,
            nonce,
            CryptoUtils.makeDigitalSignature(alicePrivateKey, ownerId, userId,
                goodId, nonce));

        hdsNotaryService.intentionToBuy(ownerId,
            userId,
            goodId,
            nonce,
            CryptoUtils.makeDigitalSignature(alicePrivateKey, ownerId, userId,
                goodId, nonce));
    }

    // TODO: Transaction tests with citizen card.
}
