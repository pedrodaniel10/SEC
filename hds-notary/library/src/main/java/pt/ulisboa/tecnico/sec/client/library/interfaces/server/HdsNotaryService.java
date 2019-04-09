package pt.ulisboa.tecnico.sec.client.library.interfaces.server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import pt.ulisboa.tecnico.sec.client.library.data.Good;
import pt.ulisboa.tecnico.sec.client.library.data.Transaction;
import pt.ulisboa.tecnico.sec.client.library.exceptions.ServerException;

/**
 * HdsNotaryService, RMI interface of the server (Notary)
 */
public interface HdsNotaryService extends Remote {

    int getRequestNumber(String userId) throws RemoteException, ServerException;

    boolean intentionToSell(String sellerId, String goodId, int requestNumber, byte[] signature)
        throws RemoteException, ServerException;

    Transaction intentionToBuy(String sellerId, String buyerId, String goodId, int requestNumber,
        byte[] signature)
        throws RemoteException, ServerException;

    Good getStateOfGood(String goodId) throws RemoteException, ServerException;

    Transaction transferGood(String transactionId,
        String sellerId,
        String buyerId,
        String goodId,
        byte[] sellerSignature,
        byte[] buyerSignature)
        throws RemoteException, ServerException;
}
