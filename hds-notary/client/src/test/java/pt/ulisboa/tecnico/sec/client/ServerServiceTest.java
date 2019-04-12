package pt.ulisboa.tecnico.sec.client;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import org.apache.commons.lang3.tuple.ImmutablePair;
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
import pt.ulisboa.tecnico.sec.library.interfaces.client.ClientService;
import pt.ulisboa.tecnico.sec.library.interfaces.server.HdsNotaryService;

/**
 * Unit test for ServerServiceTest.
 */
public class ServerServiceTest {

    private static RSAPrivateKey alicePrivateKey;
    private static RSAPrivateKey bobPrivateKey;
    private static RSAPrivateKey charliePrivateKey;
    private static RSAPublicKey serverPublicKey;

    private static HdsNotaryService hdsNotaryService;

    @BeforeClass
    public static void setupServer()
        throws RemoteException, NotBoundException, MalformedURLException, InterruptedException {

        //new Thread(() -> ClientApplication.main(new String[]{"alice", "Uvv1j7a60q2q0a4"})).start();
        //new Thread(() -> ClientApplication.main(new String[]{"bob", "JNTpC0SE9Hzb3SG"})).start();
        //new Thread(() -> ClientApplication.main(new String[]{"charlie", "9QrKUNt9HAXPKG9"})).start();

        alicePrivateKey = CryptoUtils.getPrivateKey(HdsProperties.getClientPrivateKey("alice"), "Uvv1j7a60q2q0a4");
        bobPrivateKey = CryptoUtils.getPrivateKey(HdsProperties.getClientPrivateKey("bob"), "JNTpC0SE9Hzb3SG");
        charliePrivateKey = CryptoUtils.getPrivateKey(HdsProperties.getClientPrivateKey("charlie"), "9QrKUNt9HAXPKG9");
        serverPublicKey = CryptoUtils.getPublicKey(HdsProperties.getServerPublicKey());
        hdsNotaryService = (HdsNotaryService) Naming.lookup(HdsProperties.getServerUri());
    }

    // Status of Good
    @Test
    public void testOkStatusOfGood()
        throws ServerException, RemoteException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        String userId = "0";
        String goodId = "0";
        String nonce = hdsNotaryService.getNonce(userId);

        ImmutablePair<Good, byte[]> response = hdsNotaryService.getStateOfGood(userId, goodId, nonce,
            CryptoUtils.makeDigitalSignature(alicePrivateKey, userId, goodId, nonce));

        Good good = response.getLeft();

        Assert.assertEquals("House", good.getName());
        Assert.assertEquals("2", good.getOwnerId());
        Assert.assertEquals("0", good.getGoodId());
        Assert.assertFalse(good.isOnSale());

        boolean serverValidation = CryptoUtils.verifyDigitalSignature(serverPublicKey, response.getRight(),
            goodId, Boolean.toString(good.isOnSale()), nonce);
        Assert.assertTrue(serverValidation);
    }

    @Test
    public void testOkTwoFollowedRequestsStatusOfGood()
        throws ServerException, RemoteException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        String userId = "0";
        String goodId = "0";
        String nonce = hdsNotaryService.getNonce(userId);

        Good good = hdsNotaryService.getStateOfGood(userId, goodId, nonce,
            CryptoUtils.makeDigitalSignature(alicePrivateKey, userId, goodId, nonce)).getLeft();
        Assert.assertNotNull(good);

        nonce = hdsNotaryService.getNonce(userId);
        good = hdsNotaryService.getStateOfGood(userId, goodId, nonce,
            CryptoUtils.makeDigitalSignature(alicePrivateKey, userId, goodId, nonce)).getLeft();

        Assert.assertNotNull(good);
    }

    @Test(expected = InvalidSignatureException.class)
    public void testNOkSpoofingSignatureStateOfGood()
        throws ServerException, RemoteException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        String userId = "0";
        String goodId = "0";

        String nonce = hdsNotaryService.getNonce(userId);

        hdsNotaryService.getStateOfGood(userId, goodId, nonce,
            CryptoUtils.makeDigitalSignature(bobPrivateKey, userId, goodId, nonce));
    }

    @Test(expected = InvalidSignatureException.class)
    public void testNOkSpoofingStateOfGood()
        throws ServerException, RemoteException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        String userId = "1";
        String goodId = "0";

        String nonce = hdsNotaryService.getNonce(userId);

        hdsNotaryService.getStateOfGood(userId, goodId, nonce,
            CryptoUtils.makeDigitalSignature(alicePrivateKey, userId, goodId, nonce));
    }

    @Test(expected = InvalidSignatureException.class)
    public void testNOkTamperingStateOfGood()
        throws ServerException, RemoteException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        String userId = "0";
        String goodId = "0";

        String nonce = hdsNotaryService.getNonce(userId);

        hdsNotaryService.getStateOfGood(userId, "different", nonce,
            CryptoUtils.makeDigitalSignature(alicePrivateKey, userId, goodId, nonce));
    }

    @Test(expected = InvalidNonceException.class)
    public void testNOkReplayAttackStateOfGood()
        throws ServerException, RemoteException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        String userId = "0";
        String goodId = "1";
        String nonce = hdsNotaryService.getNonce(userId);

        hdsNotaryService.getStateOfGood(userId, goodId, nonce,
            CryptoUtils.makeDigitalSignature(alicePrivateKey, userId, goodId, nonce));

        hdsNotaryService.getStateOfGood(userId, goodId, nonce,
            CryptoUtils.makeDigitalSignature(alicePrivateKey, userId, goodId, nonce));
    }

    @Test
    public void testNOkServerSpoofingStateOfGood()
        throws ServerException, RemoteException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        String userId = "0";
        String goodId = "1";
        String nonce = hdsNotaryService.getNonce(userId);

        ImmutablePair<Good, byte[]> response = hdsNotaryService.getStateOfGood(userId, goodId, nonce,
            CryptoUtils.makeDigitalSignature(alicePrivateKey, userId, goodId, nonce));

        byte[] spoofedSignature = CryptoUtils.makeDigitalSignature(bobPrivateKey, userId, goodId, nonce);
        boolean serverValidation = CryptoUtils.verifyDigitalSignature(serverPublicKey, spoofedSignature,
            goodId, Boolean.toString(response.getLeft().isOnSale()), nonce);

        Assert.assertFalse(serverValidation);
    }

    @Test(expected = ServerException.class)
    public void testWrongStatusOfGood() throws ServerException, RemoteException {
        hdsNotaryService.getStateOfGood("userId", "This id does not exist.", "", null);
    }

    @Test(expected = ServerException.class)
    public void testNullStatusOfGood() throws ServerException, RemoteException {
        hdsNotaryService.getStateOfGood("userId", null, "", null);
    }

    @Test(expected = ServerException.class)
    public void testEmptyStatusOfGood() throws ServerException, RemoteException {
        hdsNotaryService.getStateOfGood("userId", "     ", "", null);
    }

    // IntentionToSell
    @Test
    public void testOkIntentionToSell()
        throws ServerException, RemoteException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        String userId = "0";
        String goodId = "1";
        String nonce = hdsNotaryService.getNonce(userId);

        ImmutablePair<Boolean, byte[]> response = hdsNotaryService.intentionToSell(userId, goodId, nonce,
            CryptoUtils.makeDigitalSignature(alicePrivateKey,
                userId, goodId, nonce));

        Assert.assertTrue(response.getLeft());

        boolean serverValidation = CryptoUtils.verifyDigitalSignature(serverPublicKey, response.getRight(),
            goodId, Boolean.toString(response.getLeft()), nonce);
        Assert.assertTrue(serverValidation);
    }

    @Test
    public void testOkTwoFollowedRequestsIntentionToSell()
        throws ServerException, RemoteException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        String userId = "0";
        String goodId = "1";
        String nonce = hdsNotaryService.getNonce(userId);

        boolean success = hdsNotaryService.intentionToSell(userId, goodId, nonce,
            CryptoUtils.makeDigitalSignature(alicePrivateKey,
                userId, goodId, nonce)).getLeft();
        Assert.assertTrue(success);

        nonce = hdsNotaryService.getNonce(userId);
        success = hdsNotaryService.intentionToSell(userId, goodId, nonce,
            CryptoUtils.makeDigitalSignature(alicePrivateKey,
                userId, goodId, nonce)).getLeft();
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

    @Test
    public void testNOkServerSpoofingIntentionToSell()
        throws ServerException, RemoteException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        String userId = "0";
        String goodId = "1";
        String nonce = hdsNotaryService.getNonce(userId);

        ImmutablePair<Boolean, byte[]> response = hdsNotaryService.intentionToSell(userId, goodId, nonce,
            CryptoUtils.makeDigitalSignature(alicePrivateKey,
                userId, goodId, nonce));

        byte[] spoofedSignature = CryptoUtils.makeDigitalSignature(bobPrivateKey, userId, goodId, nonce);
        boolean serverValidation = CryptoUtils.verifyDigitalSignature(serverPublicKey, spoofedSignature,
            userId, goodId, nonce);

        Assert.assertFalse(serverValidation);
    }

    // IntentionToBuy
    @Test
    public void testOkIntentionToBuy()
        throws ServerException, RemoteException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        String userId = "0";
        String goodId = "2";
        String ownerId = "1";
        String nonce = hdsNotaryService.getNonce(userId);

        Transaction response = hdsNotaryService.intentionToBuy(ownerId,
            userId,
            goodId,
            nonce,
            CryptoUtils.makeDigitalSignature(alicePrivateKey, ownerId, userId,
                goodId, nonce));

        boolean serverValidation = CryptoUtils.verifyDigitalSignature(serverPublicKey, response.getNotarySignature(),
            response.getTransactionId(), nonce);

        Assert.assertTrue(serverValidation);
        Assert.assertNotNull(response);


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
        String goodId = "1";
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

    @Test
    public void testNOkServerSpoofingIntentionToBuy()
        throws ServerException, RemoteException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        String userId = "0";
        String goodId = "2";
        String ownerId = "1";
        String nonce = hdsNotaryService.getNonce(userId);

        Transaction response = hdsNotaryService.intentionToBuy(ownerId,
            userId,
            goodId,
            nonce,
            CryptoUtils.makeDigitalSignature(alicePrivateKey, ownerId, userId,
                goodId, nonce));

        byte[] spoofedSignature = CryptoUtils.makeDigitalSignature(bobPrivateKey, ownerId, userId, goodId, nonce);
        boolean serverValidation = CryptoUtils.verifyDigitalSignature(serverPublicKey, spoofedSignature,
            response.getTransactionId(), nonce);

        Assert.assertFalse(serverValidation);
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

    @Test
    public void verifyNotaryPublicKey()
        throws RemoteException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        ImmutablePair<PublicKey, byte[]> requestNotaryKey = hdsNotaryService.getNotaryPublicKey();
        PublicKey notaryPublicKey = requestNotaryKey.getLeft();

        Boolean validation = CryptoUtils.verifyDigitalSignature(serverPublicKey, requestNotaryKey.getRight(),
            new String(notaryPublicKey.getEncoded()));
        Assert.assertTrue(validation);
    }

    @Test
    public void testOkBuyGood()
        throws ServerException, RemoteException, NoSuchAlgorithmException, InvalidKeyException, SignatureException,
               MalformedURLException, NotBoundException {
        String buyerId = "0";
        String sellerId = "1";
        String goodId = "2";

        // Intention to buy
        String nonce = hdsNotaryService.getNonce(buyerId);
        byte[] signature = CryptoUtils.makeDigitalSignature(alicePrivateKey, sellerId, buyerId,
            goodId, nonce);

        final Transaction transactionResponse = hdsNotaryService.intentionToBuy(
            sellerId,
            buyerId,
            goodId,
            nonce,
            signature);

        // Verify Signature
        Boolean serverValidation = CryptoUtils.verifyDigitalSignature(serverPublicKey,
            transactionResponse.getNotarySignature(),
            transactionResponse.getTransactionId(), nonce);
        Assert.assertTrue(serverValidation);

        // Buy good
        ClientService clientServiceSeller =
            (ClientService) Naming.lookup(HdsProperties.getClientUri(sellerId));

        byte[] buyer_signature = CryptoUtils.makeDigitalSignature(alicePrivateKey,
            transactionResponse.getTransactionId(),
            sellerId,
            buyerId,
            goodId);

        Transaction transaction = clientServiceSeller.buy(
            transactionResponse.getTransactionId(),
            sellerId,
            buyerId,
            goodId,
            buyer_signature);

        // Verify Signature
        ImmutablePair<PublicKey, byte[]> requestNotaryKey = hdsNotaryService.getNotaryPublicKey();
        PublicKey notaryPublicKey = requestNotaryKey.getLeft();

        serverValidation = CryptoUtils.verifyDigitalSignature(notaryPublicKey, transaction.getNotarySignature(),
            transaction.getTransactionId(), transaction.getSellerId(), transaction.getBuyerId(),
            new String(transaction.getSellerSignature()),
            new String(transaction.getBuyerSignature()));
        Assert.assertTrue(serverValidation);

        byte[] seller_signature = CryptoUtils.makeDigitalSignature(bobPrivateKey,
            transactionResponse.getTransactionId(),
            sellerId, buyerId, goodId);

        // Validate Transaction
        Assert.assertEquals(buyerId, transaction.getBuyerId());
        Assert.assertEquals(sellerId, transaction.getSellerId());
        Assert.assertEquals(goodId, transaction.getGoodId());
        Assert.assertEquals(new String(buyer_signature), new String(transaction.getBuyerSignature()));
        Assert.assertEquals(new String(seller_signature), new String(transaction.getSellerSignature()));
    }
    
}
