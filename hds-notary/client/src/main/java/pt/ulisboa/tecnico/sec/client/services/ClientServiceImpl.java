package pt.ulisboa.tecnico.sec.client.services;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import pt.ulisboa.tecnico.sec.library.data.Transaction;
import pt.ulisboa.tecnico.sec.library.exceptions.ServerException;
import pt.ulisboa.tecnico.sec.library.interfaces.client.ClientService;
import pt.ulisboa.tecnico.sec.library.interfaces.server.HdsNotaryService;

public class ClientServiceImpl extends UnicastRemoteObject implements ClientService, Serializable {
    private static HdsNotaryService hdsNotaryService;

    public ClientServiceImpl(HdsNotaryService hdsNotaryService) throws RemoteException {
        super();
        ClientServiceImpl.hdsNotaryService = hdsNotaryService;
    }

    @Override
    public Transaction buy(String transactionId, String sellerId, String buyerId, String goodId)
            throws RemoteException, ServerException {
        return hdsNotaryService.transferGood(transactionId, sellerId, buyerId, goodId);
    }
}
