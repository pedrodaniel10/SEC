package pt.ulisboa.tecnico.sec.server.client.services;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CountDownLatch;
import org.apache.log4j.Logger;
import pt.ulisboa.tecnico.sec.services.crypto.CryptoUtils;
import pt.ulisboa.tecnico.sec.services.data.Transaction;
import pt.ulisboa.tecnico.sec.services.exceptions.ServerException;
import pt.ulisboa.tecnico.sec.services.interfaces.client.ClientService;
import pt.ulisboa.tecnico.sec.services.interfaces.server.HdsNotaryService;
import pt.ulisboa.tecnico.sec.services.utils.Constants;

public class ClientServiceImpl extends UnicastRemoteObject implements ClientService, Serializable {

    private static final Logger logger = Logger.getLogger(ClientServiceImpl.class);
    private final Map<String, HdsNotaryService> hdsNotaryService;
    private final RSAPrivateKey privateKey;

    public ClientServiceImpl(Map<String, HdsNotaryService> hdsNotaryService, RSAPrivateKey privateKey)
        throws RemoteException {
        super();
        this.hdsNotaryService = hdsNotaryService;
        this.privateKey = privateKey;
    }

    @Override
    public List<Transaction> buy(List<Transaction> transactions)
        throws RemoteException, ServerException, InterruptedException {
        ConcurrentLinkedDeque<Transaction> transactionResponse = new ConcurrentLinkedDeque<>();
        CountDownLatch latch = new CountDownLatch((Constants.N + Constants.F) / 2 + 1);

        for (Transaction transaction : transactions) {
            CompletableFuture.runAsync(() -> {
                try {
                    String sellerSignature = CryptoUtils.makeDigitalSignature(privateKey,
                        transaction.getTransactionId(),
                        transaction.getSellerId(),
                        transaction.getBuyerId(),
                        transaction.getGoodId());
                    transaction.setSellerSignature(sellerSignature);
                    transactionResponse.add(hdsNotaryService.get(transaction.getServerId()).transferGood(transaction, 0, ""));
                } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException | RemoteException | ServerException e) {
                    logger.error(e);
                }
                latch.countDown();
            });
        }
        latch.await();

        final ArrayList<Transaction> response = new ArrayList<>(transactionResponse);
        if (transactions.size() <= (Constants.N + Constants.F) / 2) {
            return new ArrayList<>();
        }
        return response;
    }
}
