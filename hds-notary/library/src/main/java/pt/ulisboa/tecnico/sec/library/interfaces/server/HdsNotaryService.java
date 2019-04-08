package pt.ulisboa.tecnico.sec.library.interfaces.server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import pt.ulisboa.tecnico.sec.library.data.Good;
import pt.ulisboa.tecnico.sec.library.data.Transaction;
import pt.ulisboa.tecnico.sec.library.exceptions.ServerException;

/**
 * HdsNotaryService, RMI interface of the server (Notary)
 */
public interface HdsNotaryService extends Remote {

    boolean intentionToSell(String sellerId, String goodId)
            throws RemoteException, ServerException;

    Transaction intentionToBuy(String sellerId, String buyerId, String goodId)
            throws RemoteException, ServerException;

    Good getStateOfGood(String goodId) throws RemoteException, ServerException;

    Transaction transferGood(String transactionId, String sellerId, String buyerId, String goodId)
            throws RemoteException, ServerException;
}
