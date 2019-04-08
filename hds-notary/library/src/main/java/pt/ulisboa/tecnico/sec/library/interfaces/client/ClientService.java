package pt.ulisboa.tecnico.sec.library.interfaces.client;

import java.rmi.Remote;
import java.rmi.RemoteException;
import pt.ulisboa.tecnico.sec.library.data.Transaction;
import pt.ulisboa.tecnico.sec.library.exceptions.GoodIsNotOnSale;
import pt.ulisboa.tecnico.sec.library.exceptions.GoodNotFoundException;
import pt.ulisboa.tecnico.sec.library.exceptions.GoodWrongOwner;
import pt.ulisboa.tecnico.sec.library.exceptions.ServerException;
import pt.ulisboa.tecnico.sec.library.exceptions.TransactionDoesntExistsException;

/**
 * ClientService, RMI interface of the P2P
 */
public interface ClientService extends Remote {

    Transaction buy(String transactionId, String sellerId, String buyerId, String goodId)
            throws RemoteException,
            ServerException;
}
