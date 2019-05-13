package pt.ulisboa.tecnico.sec.services.interfaces.server;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SignatureException;
import org.apache.commons.lang3.tuple.ImmutablePair;
import pt.ulisboa.tecnico.sec.services.data.Transaction;
import pt.ulisboa.tecnico.sec.services.exceptions.ServerException;
import pt.ulisboa.tecnico.sec.services.exceptions.UserNotFoundException;

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

    ImmutablePair<Transaction, String> intentionToBuy(String sellerId, String buyerId, String goodId, String nonce,
        String signature)
        throws RemoteException, ServerException;

    void getStateOfGood(String userId,
        String goodId,
        String nonce,
        int readId,
        String signature)
        throws RemoteException, ServerException, NotBoundException, MalformedURLException, NoSuchAlgorithmException,
               InvalidKeyException, SignatureException;

    void completeGetStateOfGood(String userId, String goodId) throws RemoteException, UserNotFoundException;

    Transaction transferGood(Transaction transaction, int timeStamp)
        throws RemoteException, ServerException, NoSuchAlgorithmException;

    ImmutablePair<PublicKey, String> getNotaryPublicKey()
        throws RemoteException, NoSuchAlgorithmException, InvalidKeyException, SignatureException;
}
