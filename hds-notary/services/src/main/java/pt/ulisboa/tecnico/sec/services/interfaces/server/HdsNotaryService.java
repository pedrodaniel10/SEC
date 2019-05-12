package pt.ulisboa.tecnico.sec.services.interfaces.server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SignatureException;
import org.apache.commons.lang3.tuple.ImmutablePair;
import pt.ulisboa.tecnico.sec.services.data.Good;
import pt.ulisboa.tecnico.sec.services.data.Transaction;
import pt.ulisboa.tecnico.sec.services.exceptions.ServerException;

/**
 * HdsNotaryService, RMI interface of the server (Notary)
 */
public interface HdsNotaryService extends Remote {

    String getNonce(String userId) throws RemoteException, ServerException;

    ImmutablePair<Boolean, String> intentionToSell(String sellerId,
        String goodId,
        String nonce,
        int timeStamp,
        String signature)
        throws RemoteException, ServerException;

    ImmutablePair<Transaction, String> intentionToBuy(String sellerId, String buyerId, String goodId, String nonce, String signature)
        throws RemoteException, ServerException;

    ImmutablePair<Good, String> getStateOfGood(String userId,
        String goodId,
        String nonce,
        int readId,
        String signature)
        throws RemoteException, ServerException;

    Transaction transferGood(Transaction transaction, int timeStamp, String signature)
        throws RemoteException, ServerException;

    ImmutablePair<PublicKey, String> getNotaryPublicKey()
        throws RemoteException, NoSuchAlgorithmException, InvalidKeyException, SignatureException;
}
