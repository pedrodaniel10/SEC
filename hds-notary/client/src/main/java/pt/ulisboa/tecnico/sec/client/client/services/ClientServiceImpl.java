package pt.ulisboa.tecnico.sec.client.client.services;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateKey;
import pt.ulisboa.tecnico.sec.client.library.crypto.CryptoUtils;
import pt.ulisboa.tecnico.sec.client.library.data.Transaction;
import pt.ulisboa.tecnico.sec.client.library.exceptions.ServerException;
import pt.ulisboa.tecnico.sec.client.library.interfaces.client.ClientService;
import pt.ulisboa.tecnico.sec.client.library.interfaces.server.HdsNotaryService;

public class ClientServiceImpl extends UnicastRemoteObject implements ClientService, Serializable {

    private HdsNotaryService hdsNotaryService;
    private RSAPrivateKey privateKey;

    public ClientServiceImpl(HdsNotaryService hdsNotaryService, RSAPrivateKey privateKey)
        throws RemoteException {
        super();
        this.hdsNotaryService = hdsNotaryService;
        this.privateKey = privateKey;
    }

    @Override
    public Transaction buy(String transactionId,
        String sellerId,
        String buyerId,
        String goodId,
        byte[] buyerSignature)
        throws RemoteException, ServerException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {

        byte[] signature = CryptoUtils
            .makeDigitalSignature(privateKey, transactionId, sellerId, buyerId, goodId);
        return hdsNotaryService
            .transferGood(transactionId, sellerId, buyerId, goodId, signature, buyerSignature);
    }
}
