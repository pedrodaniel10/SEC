package pt.ulisboa.tecnico.sec.services.interfaces.client;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.List;
import pt.ulisboa.tecnico.sec.services.data.Transaction;
import pt.ulisboa.tecnico.sec.services.exceptions.ServerException;

/**
 * ClientService, RMI interface of the P2P
 */
public interface ClientService extends Remote {

    List<Transaction> buy(List<Transaction> transactions)
        throws RemoteException, ServerException, NoSuchAlgorithmException, InvalidKeyException, SignatureException,
               InterruptedException;
}
