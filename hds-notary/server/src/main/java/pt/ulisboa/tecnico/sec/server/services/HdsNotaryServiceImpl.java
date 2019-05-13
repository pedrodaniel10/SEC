package pt.ulisboa.tecnico.sec.server.services;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SignatureException;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.log4j.Logger;
import pt.ulisboa.tecnico.sec.server.utils.PersistenceUtils;
import pt.ulisboa.tecnico.sec.services.data.Transaction;
import pt.ulisboa.tecnico.sec.services.exceptions.ServerException;
import pt.ulisboa.tecnico.sec.services.exceptions.UserNotFoundException;
import pt.ulisboa.tecnico.sec.services.interfaces.server.HdsNotaryService;

/**
 * Service wrapper for RMI. This class only calls the function of serverState.
 */
public class HdsNotaryServiceImpl extends UnicastRemoteObject implements HdsNotaryService, Serializable {

    private static final Logger logger = Logger.getLogger(HdsNotaryServiceImpl.class);
    private HdsNotaryState serverState;

    public HdsNotaryServiceImpl() throws RemoteException {
        this.serverState = PersistenceUtils.getServerState();
    }

    public HdsNotaryState getServerState() {
        return this.serverState;
    }


    @Override
    public String getNonce(String userId) throws ServerException {
        logger.debug("Called GetNonce: " + userId);
        return serverState.getNonce(userId);
    }

    @Override
    public ImmutablePair<Boolean, String> intentionToSell(String sellerId,
        String goodId,
        String nonce,
        int timeStamp,
        String signature)
        throws ServerException {
        logger.debug("Called IntentionToSell: " + sellerId);
        return this.serverState.intentionToSell(sellerId, goodId, nonce, timeStamp, signature);
    }

    @Override
    public ImmutablePair<Transaction, String> intentionToBuy(String sellerId,
        String buyerId,
        String goodId,
        String nonce,
        String signature) throws ServerException {
        logger.debug("Called IntentionToBuy: " + buyerId);
        return this.serverState.intentionToBuy(sellerId, buyerId, goodId, nonce, signature);
    }

    @Override
    public void getStateOfGood(String userId, String goodId, String nonce, int readId,
        String signature)
        throws ServerException, RemoteException, InvalidKeyException, NoSuchAlgorithmException, SignatureException,
               NotBoundException, MalformedURLException {
        logger.debug("Called GetStateOfGood: " + userId);
        this.serverState.getStateOfGood(userId, goodId, nonce, readId, signature);
    }

    @Override
    public void completeGetStateOfGood(String userId, String goodId) throws RemoteException, UserNotFoundException {
        logger.debug("Called CompleteGetStateOfGood: " + userId);
        this.serverState.completeGetStateOfGood(userId, goodId);
    }

    @Override
    public Transaction transferGood(Transaction transaction, int timeStamp)
        throws ServerException, NoSuchAlgorithmException {
        logger.debug("Called TransferGood: " + transaction.getSellerId());
        return this.serverState.transferGood(transaction, timeStamp);
    }

    @Override
    public ImmutablePair<PublicKey, String> getNotaryPublicKey()
        throws RemoteException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        logger.debug("Called GetNotaryPublicKey: ");
        return this.serverState.getNotaryPublicKey();
    }
}
