package pt.ulisboa.tecnico.sec.library.interfaces.client;

import pt.ulisboa.tecnico.sec.library.data.Transaction;
import pt.ulisboa.tecnico.sec.library.exceptions.ServerException;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

/**
 * ClientService, RMI interface of the P2P
 */
public interface ClientService extends Remote {

    Transaction buy(String transactionId,
                    String sellerId,
                    String buyerId,
                    String goodId,
                    byte[] buyerSignature)
            throws RemoteException, ServerException, NoSuchAlgorithmException, InvalidKeyException, SignatureException;
}
