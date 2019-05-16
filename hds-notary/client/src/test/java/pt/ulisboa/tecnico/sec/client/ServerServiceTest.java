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
import java.util.Optional;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import pt.ulisboa.tecnico.sec.server.client.services.HdsNotaryClient;
import pt.ulisboa.tecnico.sec.services.crypto.CryptoUtils;
import pt.ulisboa.tecnico.sec.services.data.Good;
import pt.ulisboa.tecnico.sec.services.data.Transaction;
import pt.ulisboa.tecnico.sec.services.data.User;
import pt.ulisboa.tecnico.sec.services.exceptions.GoodWrongOwnerException;
import pt.ulisboa.tecnico.sec.services.exceptions.InvalidNonceException;
import pt.ulisboa.tecnico.sec.services.exceptions.InvalidSignatureException;
import pt.ulisboa.tecnico.sec.services.exceptions.ServerException;
import pt.ulisboa.tecnico.sec.services.interfaces.server.HdsNotaryService;
import pt.ulisboa.tecnico.sec.services.properties.HdsProperties;

/**
 * Unit test for ServerServiceTest.
 */
public class ServerServiceTest {

    private static RSAPrivateKey alicePrivateKey;
    private static RSAPrivateKey bobPrivateKey;
    private static RSAPrivateKey charliePrivateKey;
    private static RSAPublicKey serverPublicKey_0;

    private static HdsNotaryService hdsNotaryService_0;

    @BeforeClass
    public static void setupServer()
        throws RemoteException, NotBoundException, MalformedURLException, InterruptedException {

        //new Thread(() -> ClientApplication.main(new String[]{"alice", "Uvv1j7a60q2q0a4"})).start();
        //new Thread(() -> ClientApplication.main(new String[]{"bob", "JNTpC0SE9Hzb3SG"})).start();
        //new Thread(() -> ClientApplication.main(new String[]{"charlie", "9QrKUNt9HAXPKG9"})).start();

        alicePrivateKey = HdsProperties.getClientPrivateKey("alice", "Uvv1j7a60q2q0a4");
        bobPrivateKey = HdsProperties.getClientPrivateKey("bob", "JNTpC0SE9Hzb3SG");
        charliePrivateKey = HdsProperties.getClientPrivateKey("charlie", "9QrKUNt9HAXPKG9");
        serverPublicKey_0 = HdsProperties.getServerPublicKey("0");
        hdsNotaryService_0 = (HdsNotaryService) Naming.lookup(HdsProperties.getServerUri("0"));
    }

    // Status of Good
    @Test
    public void testOkStatusOfGood()
        throws ServerException, InterruptedException, RemoteException {
        String goodId = "0";
        User user = HdsProperties.getUser("alice");

        Good good = HdsNotaryClient.getStateOfGood(user, goodId).get();

        Assert.assertEquals("House", good.getName());
        Assert.assertEquals("2", good.getOwnerId());
        Assert.assertEquals("0", good.getGoodId());
        Assert.assertFalse(good.isOnSale());
    }

    @Test
    public void testOkTwoFollowedRequestsStatusOfGood()
        throws ServerException, InterruptedException {
        String goodId = "0";
        User user = HdsProperties.getUser("alice");

        Good good = HdsNotaryClient.getStateOfGood(user, goodId).get();
        Assert.assertNotNull(good);

        good = HdsNotaryClient.getStateOfGood(user, goodId).get();
        Assert.assertNotNull(good);
    }

    @Test(expected = InvalidSignatureException.class)
    public void testNOkSpoofingSignatureStateOfGood()
        throws ServerException, RemoteException, NoSuchAlgorithmException, InvalidKeyException, SignatureException,
               MalformedURLException, NotBoundException, InterruptedException {
        String userId = "0";
        String goodId = "0";

        String nonce = hdsNotaryService_0.getNonce(userId);

        hdsNotaryService_0.getStateOfGood(userId, goodId, nonce, 0,
            CryptoUtils.makeDigitalSignature(bobPrivateKey, userId, goodId, nonce, "0"));
    }

    @Test(expected = InvalidSignatureException.class)
    public void testNOkSpoofingStateOfGood()
        throws ServerException, RemoteException, NoSuchAlgorithmException, InvalidKeyException, SignatureException,
               MalformedURLException, NotBoundException, InterruptedException {
        String userId = "1";
        String goodId = "0";

        String nonce = hdsNotaryService_0.getNonce(userId);

        hdsNotaryService_0.getStateOfGood(userId, goodId, nonce, 0,
            CryptoUtils.makeDigitalSignature(alicePrivateKey, userId, goodId, nonce, "0"));
    }

    @Test(expected = InvalidSignatureException.class)
    public void testNOkTamperingStateOfGood()
        throws ServerException, RemoteException, NoSuchAlgorithmException, InvalidKeyException, SignatureException,
               MalformedURLException, NotBoundException, InterruptedException {
        String userId = "0";
        String goodId = "0";

        String nonce = hdsNotaryService_0.getNonce(userId);

        hdsNotaryService_0.getStateOfGood(userId, "different", nonce, 0,
            CryptoUtils.makeDigitalSignature(alicePrivateKey, userId, goodId, nonce, "0"));
    }

    @Test(expected = InvalidNonceException.class)
    public void testNOkReplayAttackStateOfGood()
        throws ServerException, RemoteException, NoSuchAlgorithmException, InvalidKeyException, SignatureException,
               MalformedURLException, NotBoundException, InterruptedException {
        String userId = "0";
        String goodId = "1";
        String nonce = hdsNotaryService_0.getNonce(userId);

        hdsNotaryService_0.getStateOfGood(userId, goodId, nonce, 0,
            CryptoUtils.makeDigitalSignature(alicePrivateKey, userId, goodId, nonce, "0"));

        hdsNotaryService_0.getStateOfGood(userId, goodId, nonce, 0,
            CryptoUtils.makeDigitalSignature(alicePrivateKey, userId, goodId, nonce, "0"));
    }

//    @Test
//    public void testNOkServerSpoofingStateOfGood()
//        throws ServerException, RemoteException, NoSuchAlgorithmException, InvalidKeyException, SignatureException,
//               MalformedURLException, NotBoundException {
//        String userId = "0";
//        String goodId = "1";
//        String nonce = hdsNotaryService_0.getNonce(userId);
//
//        hdsNotaryService_0.getStateOfGood(userId, goodId, nonce, 0,
//            CryptoUtils.makeDigitalSignature(alicePrivateKey, userId, goodId, nonce, "0"));
//
//        String spoofedSignature = CryptoUtils.makeDigitalSignature(bobPrivateKey, userId, goodId, nonce);
//        boolean serverValidation = CryptoUtils.verifyDigitalSignature(serverPublicKey_0, spoofedSignature,
//            goodId, Boolean.toString(response.getLeft().isOnSale()), nonce);
//
//        Assert.assertFalse(serverValidation);
//    }

    @Test(expected = ServerException.class)
    public void testWrongStatusOfGood()
        throws ServerException, RemoteException, MalformedURLException, SignatureException, NoSuchAlgorithmException,
               InvalidKeyException, NotBoundException, InterruptedException {
        hdsNotaryService_0.getStateOfGood("userId", "This id does not exist.", "", 0, null);
    }

    @Test(expected = ServerException.class)
    public void testNullStatusOfGood()
        throws ServerException, RemoteException, MalformedURLException, SignatureException, NoSuchAlgorithmException,
               InvalidKeyException, NotBoundException, InterruptedException {
        hdsNotaryService_0.getStateOfGood("userId", null, "", 0, null);
    }

    @Test(expected = ServerException.class)
    public void testEmptyStatusOfGood()
        throws ServerException, RemoteException, MalformedURLException, SignatureException, NoSuchAlgorithmException,
               InvalidKeyException, NotBoundException, InterruptedException {
        hdsNotaryService_0.getStateOfGood("userId", "     ", "", 0, null);
    }

    // IntentionToSell
    @Test
    public void testOkIntentionToSell()
        throws ServerException, RemoteException, NoSuchAlgorithmException, InvalidKeyException, SignatureException,
               InterruptedException {
        String userId = "0";
        String goodId = "1";
        String nonce = hdsNotaryService_0.getNonce(userId);

        ImmutablePair<Boolean, String> response = hdsNotaryService_0.intentionToSell(userId, goodId, nonce, 0,
            CryptoUtils.makeDigitalSignature(alicePrivateKey,
                userId, goodId, nonce));

        Assert.assertTrue(response.getLeft());

        boolean serverValidation = CryptoUtils.verifyDigitalSignature(serverPublicKey_0, response.getRight(),
            goodId, Boolean.toString(response.getLeft()), nonce);
        Assert.assertTrue(serverValidation);
    }

    @Test
    public void testOkTwoFollowedRequestsIntentionToSell()
        throws ServerException, RemoteException, NoSuchAlgorithmException, InvalidKeyException, SignatureException,
               InterruptedException {
        String userId = "0";
        String goodId = "1";
        String nonce = hdsNotaryService_0.getNonce(userId);

        boolean success = hdsNotaryService_0.intentionToSell(userId, goodId, nonce, 0,
            CryptoUtils.makeDigitalSignature(alicePrivateKey,
                userId, goodId, nonce)).getLeft();
        Assert.assertTrue(success);

        nonce = hdsNotaryService_0.getNonce(userId);
        success = hdsNotaryService_0.intentionToSell(userId, goodId, nonce, 0,
            CryptoUtils.makeDigitalSignature(alicePrivateKey,
                userId, goodId, nonce)).getLeft();
        Assert.assertTrue(success);
    }

    @Test(expected = GoodWrongOwnerException.class)
    public void testNOkWrongOwnerIntentionToSell()
        throws ServerException, RemoteException, NoSuchAlgorithmException, InvalidKeyException, SignatureException,
               InterruptedException {
        String userId = "0";
        String goodId = "0";

        String nonce = hdsNotaryService_0.getNonce(userId);

        hdsNotaryService_0.intentionToSell(userId, goodId, nonce, 0,
            CryptoUtils.makeDigitalSignature(alicePrivateKey,
                userId, goodId, nonce));
    }

    @Test(expected = InvalidSignatureException.class)
    public void testNOkSpoofingSignatureIntentionToSell()
        throws ServerException, RemoteException, NoSuchAlgorithmException, InvalidKeyException, SignatureException,
               InterruptedException {
        String userId = "0";
        String goodId = "1";

        String nonce = hdsNotaryService_0.getNonce(userId);

        hdsNotaryService_0.intentionToSell(userId, goodId, nonce, 0,
            CryptoUtils.makeDigitalSignature(bobPrivateKey,
                userId, goodId, nonce));
    }

    @Test(expected = InvalidSignatureException.class)
    public void testNOkSpoofingIntentionToSell()
        throws ServerException, RemoteException, NoSuchAlgorithmException, InvalidKeyException, SignatureException,
               InterruptedException {
        String userId = "1";
        String goodId = "1";

        String nonce = hdsNotaryService_0.getNonce(userId);

        hdsNotaryService_0.intentionToSell(userId, goodId, nonce, 0, CryptoUtils.makeDigitalSignature(alicePrivateKey,
            userId, goodId, nonce));
    }

    @Test(expected = InvalidSignatureException.class)
    public void testNOkTamperingIntentionToSell()
        throws ServerException, RemoteException, NoSuchAlgorithmException, InvalidKeyException, SignatureException,
               InterruptedException {
        String userId = "0";
        String goodId = "0";

        String nonce = hdsNotaryService_0.getNonce(userId);

        hdsNotaryService_0.intentionToSell(userId, "different", nonce, 0,
            CryptoUtils.makeDigitalSignature(alicePrivateKey,
                userId, goodId, nonce));
    }

    @Test(expected = InvalidNonceException.class)
    public void testNOkReplayAttackIntentionToSell()
        throws ServerException, RemoteException, NoSuchAlgorithmException, InvalidKeyException, SignatureException,
               InterruptedException {
        String userId = "0";
        String goodId = "1";
        String nonce = hdsNotaryService_0.getNonce(userId);

        hdsNotaryService_0.intentionToSell(userId, goodId, nonce, 0,
            CryptoUtils.makeDigitalSignature(alicePrivateKey,
                userId, goodId, nonce));

        hdsNotaryService_0.intentionToSell(userId, goodId, nonce, 0,
            CryptoUtils.makeDigitalSignature(alicePrivateKey,
                userId, goodId, nonce));
    }

//    @Test
//    public void testNOkServerSpoofingIntentionToSell()
//        throws ServerException, RemoteException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
//        String userId = "0";
//        String goodId = "1";
//        String nonce = hdsNotaryService_0.getNonce(userId);
//
//        ImmutablePair<Boolean, String> response = hdsNotaryService_0.intentionToSell(userId, goodId, nonce, 0,
//            CryptoUtils.makeDigitalSignature(alicePrivateKey,
//                userId, goodId, nonce));
//
//        String spoofedSignature = CryptoUtils.makeDigitalSignature(bobPrivateKey, userId, goodId, nonce);
//        boolean serverValidation = CryptoUtils.verifyDigitalSignature(serverPublicKey_0, spoofedSignature,
//            userId, goodId, nonce);
//
//        Assert.assertFalse(serverValidation);
//    }

    // IntentionToBuy
    @Test
    public void testOkIntentionToBuy()
        throws ServerException, RemoteException, NoSuchAlgorithmException, InvalidKeyException, SignatureException,
               InterruptedException {
        String userId = "0";
        String goodId = "2";
        String ownerId = "1";
        String nonce = hdsNotaryService_0.getNonce(userId);

        ImmutablePair<Transaction, String> response = hdsNotaryService_0.intentionToBuy(ownerId,
            userId,
            goodId,
            nonce,
            CryptoUtils.makeDigitalSignature(alicePrivateKey, ownerId, userId, goodId, nonce));

        boolean serverValidation = CryptoUtils.verifyDigitalSignature(serverPublicKey_0, response.getRight(),
            response.getLeft().getTransactionId(), nonce);

        Assert.assertTrue(serverValidation);
        Assert.assertNotNull(response);


    }

    @Test
    public void testOkTwoFollowedRequestsIntentionToBuy()
        throws ServerException, RemoteException, NoSuchAlgorithmException, InvalidKeyException, SignatureException,
               InterruptedException {
        String userId = "0";
        String goodId = "2";
        String ownerId = "1";
        String nonce = hdsNotaryService_0.getNonce(userId);

        ImmutablePair<Transaction, String> transactionRequest = hdsNotaryService_0.intentionToBuy(ownerId,
            userId,
            goodId,
            nonce,
            CryptoUtils.makeDigitalSignature(alicePrivateKey, ownerId, userId, goodId, nonce));

        Assert.assertNotNull(transactionRequest);

        nonce = hdsNotaryService_0.getNonce(userId);
        ImmutablePair<Transaction, String> transactionRequest2 = hdsNotaryService_0.intentionToBuy(ownerId,
            userId,
            goodId,
            nonce,
            CryptoUtils.makeDigitalSignature(alicePrivateKey, ownerId, userId,
                goodId, nonce));

        Assert.assertNotNull(transactionRequest2);
        Assert.assertEquals(transactionRequest.getLeft().getTransactionId(),
            transactionRequest2.getLeft().getTransactionId());
    }

    @Test(expected = GoodWrongOwnerException.class)
    public void testNOkWrongOwnerIntentionToBuy()
        throws ServerException, RemoteException, NoSuchAlgorithmException, InvalidKeyException, SignatureException,
               InterruptedException {
        String userId = "0";
        String goodId = "1";
        String ownerId = "2";
        String nonce = hdsNotaryService_0.getNonce(userId);

        hdsNotaryService_0.intentionToBuy(ownerId,
            userId,
            goodId,
            nonce,
            CryptoUtils.makeDigitalSignature(alicePrivateKey, ownerId, userId,
                goodId, nonce));
    }

    @Test(expected = InvalidSignatureException.class)
    public void testNOkSpoofingSignatureIntentionToBuy()
        throws ServerException, RemoteException, NoSuchAlgorithmException, InvalidKeyException, SignatureException,
               InterruptedException {
        String userId = "0";
        String goodId = "2";
        String ownerId = "1";
        String nonce = hdsNotaryService_0.getNonce(userId);

        hdsNotaryService_0.intentionToBuy(ownerId,
            userId,
            goodId,
            nonce,
            CryptoUtils.makeDigitalSignature(bobPrivateKey, ownerId, userId,
                goodId, nonce));
    }

    @Test
    public void testNOkServerSpoofingIntentionToBuy()
        throws ServerException, RemoteException, NoSuchAlgorithmException, InvalidKeyException, SignatureException,
               InterruptedException {
        String userId = "0";
        String goodId = "2";
        String ownerId = "1";
        String nonce = hdsNotaryService_0.getNonce(userId);

        ImmutablePair<Transaction, String> response = hdsNotaryService_0.intentionToBuy(ownerId,
            userId,
            goodId,
            nonce,
            CryptoUtils.makeDigitalSignature(alicePrivateKey, ownerId, userId, goodId, nonce));

        String spoofedSignature = CryptoUtils.makeDigitalSignature(bobPrivateKey, ownerId, userId, goodId, nonce);
        boolean serverValidation = CryptoUtils.verifyDigitalSignature(serverPublicKey_0, spoofedSignature,
            response.getLeft().getTransactionId(), nonce);

        Assert.assertFalse(serverValidation);
    }

    @Test(expected = InvalidSignatureException.class)
    public void testNOkSpoofingIntentionToBuy()
        throws ServerException, RemoteException, NoSuchAlgorithmException, InvalidKeyException, SignatureException,
               InterruptedException {
        String userId = "2";
        String goodId = "2";
        String ownerId = "1";
        String nonce = hdsNotaryService_0.getNonce(userId);

        hdsNotaryService_0.intentionToBuy(ownerId,
            userId,
            goodId,
            nonce,
            CryptoUtils.makeDigitalSignature(alicePrivateKey, ownerId, userId,
                goodId, nonce));
    }

    @Test(expected = InvalidSignatureException.class)
    public void testNOkTamperingIntentionToBuy()
        throws ServerException, RemoteException, NoSuchAlgorithmException, InvalidKeyException, SignatureException,
               InterruptedException {
        String userId = "0";
        String goodId = "2";
        String ownerId = "1";
        String nonce = hdsNotaryService_0.getNonce(userId);

        hdsNotaryService_0.intentionToBuy(ownerId,
            userId,
            "different",
            nonce,
            CryptoUtils.makeDigitalSignature(alicePrivateKey, ownerId, userId,
                goodId, nonce));
    }

    @Test(expected = InvalidNonceException.class)
    public void testNOkReplayAttackIntentionToBuy()
        throws ServerException, RemoteException, NoSuchAlgorithmException, InvalidKeyException, SignatureException,
               InterruptedException {
        String userId = "0";
        String goodId = "2";
        String ownerId = "1";
        String nonce = hdsNotaryService_0.getNonce(userId);

        hdsNotaryService_0.intentionToBuy(ownerId,
            userId,
            goodId,
            nonce,
            CryptoUtils.makeDigitalSignature(alicePrivateKey, ownerId, userId,
                goodId, nonce));

        hdsNotaryService_0.intentionToBuy(ownerId,
            userId,
            goodId,
            nonce,
            CryptoUtils.makeDigitalSignature(alicePrivateKey, ownerId, userId,
                goodId, nonce));
    }

    @Test
    public void verifyNotaryPublicKey()
        throws RemoteException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        ImmutablePair<PublicKey, String> requestNotaryKey = hdsNotaryService_0.getNotaryPublicKey();
        PublicKey notaryPublicKey = requestNotaryKey.getLeft();

        Boolean validation = CryptoUtils.verifyDigitalSignature(serverPublicKey_0, requestNotaryKey.getRight(),
            new String(notaryPublicKey.getEncoded()));
        Assert.assertTrue(validation);
    }

    @Test
    public void testOkBuyGood()
        throws ServerException, RemoteException, NoSuchAlgorithmException, InvalidKeyException, SignatureException,
               MalformedURLException, NotBoundException {
        //        String buyerId = "0";
        //        String sellerId = "1";
        //        String goodId = "2";
        //
        //        // Intention to buy
        //        String nonce = hdsNotaryService_0.getNonce(buyerId);
        //        String signature = CryptoUtils.makeDigitalSignature(alicePrivateKey, sellerId, buyerId,
        //            goodId, nonce);
        //
        //        final Transaction transactionResponse = hdsNotaryService_0.intentionToBuy(
        //            sellerId,
        //            buyerId,
        //            goodId,
        //            nonce,
        //            signature);
        //
        //        // Verify Signature
        //        Boolean serverValidation = CryptoUtils.verifyDigitalSignature(serverPublicKey_0,
        //            transactionResponse.getNotarySignature(),
        //            transactionResponse.getTransactionId(), nonce);
        //        Assert.assertTrue(serverValidation);
        //
        //        // Buy good
        //        ClientService clientServiceSeller =
        //            (ClientService) Naming.lookup(HdsProperties.getClientUri(sellerId));
        //
        //        String buyer_signature = CryptoUtils.makeDigitalSignature(alicePrivateKey,
        //            transactionResponse.getTransactionId(),
        //            sellerId,
        //            buyerId,
        //            goodId);
        //
        //
        //        Transaction transaction = clientServiceSeller.buy( new ArrayList<>().add(new Transaction())
        //            transactionResponse.getTransactionId(),
        //            sellerId,
        //            buyerId,
        //            goodId,
        //            buyer_signature);
        //
        //        // Verify Signature
        //        ImmutablePair<PublicKey, String> requestNotaryKey = hdsNotaryService_0.getNotaryPublicKey();
        //        PublicKey notaryPublicKey = requestNotaryKey.getLeft();
        //
        //        serverValidation = CryptoUtils.verifyDigitalSignature(notaryPublicKey, transaction.getNotarySignature(),
        //            transaction.getTransactionId(), transaction.getSellerId(), transaction.getBuyerId(),
        //            transaction.getSellerSignature(),
        //            transaction.getBuyerSignature());
        //        Assert.assertTrue(serverValidation);
        //
        //        String seller_signature = CryptoUtils.makeDigitalSignature(bobPrivateKey,
        //            transactionResponse.getTransactionId(),
        //            sellerId, buyerId, goodId);
        //
        //        // Validate Transaction
        //        Assert.assertEquals(buyerId, transaction.getBuyerId());
        //        Assert.assertEquals(sellerId, transaction.getSellerId());
        //        Assert.assertEquals(goodId, transaction.getGoodId());
        //        Assert.assertEquals(buyer_signature, transaction.getBuyerSignature());
        //        Assert.assertEquals(seller_signature, transaction.getSellerSignature());
    }

}
