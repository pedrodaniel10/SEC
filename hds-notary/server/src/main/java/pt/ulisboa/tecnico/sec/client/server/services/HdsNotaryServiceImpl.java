package pt.ulisboa.tecnico.sec.client.server.services;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import pt.ulisboa.tecnico.sec.client.library.data.Good;
import pt.ulisboa.tecnico.sec.client.library.data.Transaction;
import pt.ulisboa.tecnico.sec.client.library.exceptions.ServerException;
import pt.ulisboa.tecnico.sec.client.library.interfaces.server.HdsNotaryService;
import pt.ulisboa.tecnico.sec.client.server.utils.PersistenceUtils;

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
    public int getRequestNumber(String userId) throws ServerException {
        return serverState.getRequestNumber(userId);
    }

    @Override
    public boolean intentionToSell(String sellerId, String goodId, int requestNumber,
        byte[] signature)
        throws ServerException {
        return this.serverState.intentionToSell(sellerId, goodId, requestNumber, signature);
    }

    @Override
    public Transaction intentionToBuy(String sellerId,
        String buyerId,
        String goodId,
        int requestNumber,
        byte[] signature) throws RemoteException, ServerException {
        return this.serverState.intentionToBuy(sellerId, buyerId, goodId, requestNumber, signature);
    }

    @Override
    public Good getStateOfGood(String goodId) throws RemoteException, ServerException {
        return this.serverState.getStateOfGood(goodId);
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
}
