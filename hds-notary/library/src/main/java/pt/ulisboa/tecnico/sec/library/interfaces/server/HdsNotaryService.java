package pt.ulisboa.tecnico.sec.library.interfaces.server;

import org.apache.commons.lang3.tuple.ImmutablePair;
import pt.ulisboa.tecnico.sec.library.data.Good;
import pt.ulisboa.tecnico.sec.library.data.Transaction;
import pt.ulisboa.tecnico.sec.library.exceptions.ServerException;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SignatureException;

/**
 * HdsNotaryService, RMI interface of the server (Notary)
 */
public interface HdsNotaryService extends Remote {

    String getNonce(String userId) throws RemoteException, ServerException;

    ImmutablePair<Boolean, byte[]> intentionToSell(String sellerId, String goodId, String nonce,
                                                   byte[] signature)
            throws RemoteException, ServerException;

    Transaction intentionToBuy(String sellerId, String buyerId, String goodId, String nonce,
                               byte[] signature)
            throws RemoteException, ServerException;

    ImmutablePair<Good, byte[]> getStateOfGood(String userId, String goodId, String nonce, byte[] signature)
            throws RemoteException, ServerException;

    Transaction transferGood(String transactionId,
                             String sellerId,
                             String buyerId,
                             String goodId,
                             byte[] sellerSignature,
                             byte[] buyerSignature)
            throws RemoteException, ServerException;

    ImmutablePair<PublicKey, byte[]> getNotaryPublicKey() throws RemoteException, NoSuchAlgorithmException, InvalidKeyException, SignatureException;
}
