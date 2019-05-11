package pt.ulisboa.tecnico.sec.services.interfaces.client;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import pt.ulisboa.tecnico.sec.services.data.Transaction;
import pt.ulisboa.tecnico.sec.services.exceptions.ServerException;

/**
 * ClientService, RMI interface of the P2P
 */
public interface ClientService extends Remote {

    Transaction buy(String transactionId,
        String sellerId,
        String buyerId,
        String goodId,
        String buyerSignature)
        throws RemoteException, ServerException, NoSuchAlgorithmException, InvalidKeyException, SignatureException;
}
