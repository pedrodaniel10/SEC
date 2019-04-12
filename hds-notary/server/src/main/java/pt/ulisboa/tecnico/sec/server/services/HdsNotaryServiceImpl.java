package pt.ulisboa.tecnico.sec.server.services;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SignatureException;
import org.apache.commons.lang3.tuple.ImmutablePair;
import pt.ulisboa.tecnico.sec.library.data.Good;
import pt.ulisboa.tecnico.sec.library.data.Transaction;
import pt.ulisboa.tecnico.sec.library.exceptions.ServerException;
import pt.ulisboa.tecnico.sec.library.interfaces.server.HdsNotaryService;
import pt.ulisboa.tecnico.sec.server.utils.PersistenceUtils;

/**
 * Service wrapper for RMI. This class only calls the function of serverState.
 */
public class HdsNotaryServiceImpl extends UnicastRemoteObject implements HdsNotaryService,
    Serializable {

    private HdsNotaryState serverState;

    public HdsNotaryServiceImpl() throws RemoteException {
        this.serverState = PersistenceUtils.getServerState();
    }

    public HdsNotaryState getServerState() {
        return this.serverState;
    }


    @Override
    public String getNonce(String userId) throws ServerException {
        return serverState.getNonce(userId);
    }

    @Override
    public ImmutablePair<Boolean, byte[]> intentionToSell(String sellerId, String goodId, String nonce,
        byte[] signature)
        throws ServerException {
        return this.serverState.intentionToSell(sellerId, goodId, nonce, signature);
    }

    @Override
    public Transaction intentionToBuy(String sellerId,
        String buyerId,
        String goodId,
        String nonce,
        byte[] signature) throws RemoteException, ServerException {
        return this.serverState.intentionToBuy(sellerId, buyerId, goodId, nonce, signature);
    }

    @Override
    public ImmutablePair<Good, byte[]> getStateOfGood(String userId, String goodId, String nonce, byte[] signature)
        throws RemoteException, ServerException {
        return this.serverState.getStateOfGood(userId, goodId, nonce, signature);
    }

    @Override
    public Transaction transferGood(String transactionId,
        String sellerId,
        String buyerId,
        String goodId,
        byte[] sellerSignature,
        byte[] buyerSignature) throws RemoteException, ServerException {
        return this.serverState
            .transferGood(transactionId, sellerId, buyerId, goodId, sellerSignature,
                buyerSignature);
    }

    @Override
    public ImmutablePair<PublicKey, byte[]> getNotaryPublicKey()
        throws RemoteException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        return this.serverState.getNotaryPublicKey();
    }
}
