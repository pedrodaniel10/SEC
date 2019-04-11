package pt.ulisboa.tecnico.sec.server.client;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateKey;
import java.util.Scanner;
import org.apache.log4j.Logger;
import pt.ulisboa.tecnico.sec.library.HdsProperties;
import pt.ulisboa.tecnico.sec.library.crypto.CryptoUtils;
import pt.ulisboa.tecnico.sec.library.data.Good;
import pt.ulisboa.tecnico.sec.library.data.Transaction;
import pt.ulisboa.tecnico.sec.library.data.User;
import pt.ulisboa.tecnico.sec.library.exceptions.ServerException;
import pt.ulisboa.tecnico.sec.library.exceptions.UserNotFoundException;
import pt.ulisboa.tecnico.sec.library.interfaces.client.ClientService;
import pt.ulisboa.tecnico.sec.library.interfaces.server.HdsNotaryService;
import pt.ulisboa.tecnico.sec.server.client.services.ClientServiceImpl;

/**
 * ClientApplication main class
 */
public class ClientApplication {

    private static final Logger logger = Logger.getLogger(ClientApplication.class);

    private static RSAPrivateKey privateKey;

    private static HdsNotaryService hdsNotaryService;

    public static void main(String[] args) {
        String username;
        if (args.length == 1) {
            username = args[0];
        } else {
            System.out.print("Enter username: ");
            username = new Scanner(System.in).nextLine();
        }

        // Get user
        User user = null;
        try {
            user = HdsProperties.getUser(username);
        } catch (UserNotFoundException e) {
            logger.error(e);
            System.exit(1);
        }

        // Get private key
        privateKey = CryptoUtils.getPrivateKey(HdsProperties.getClientPrivateKey(user.getName()));

        try {
            hdsNotaryService = (HdsNotaryService) Naming.lookup(HdsProperties.getServerUri());

            // Setup P2P service
            ClientService clientService = new ClientServiceImpl(hdsNotaryService, privateKey);

            final int registryPort = HdsProperties.getClientPort(username);
            final Registry reg = LocateRegistry.createRegistry(registryPort);

            reg.rebind("ClientService", clientService);

            logger.info("ClientService up at port " + registryPort);

            while (true) {
                System.out.println("HDS Notary Service =======================");
                System.out.println("1) Get state of good.");
                System.out.println("2) Intention to sell good.");
                System.out.println("3) Buy good.");
                System.out.println("4) Exit.");
                System.out.println("==========================================");

                int requestNumber = hdsNotaryService.getRequestNumber(user.getUserId());

                String option = new Scanner(System.in).nextLine();
                try {
                    switch (option) {
                        case "1":
                            getStateOfGood();
                            break;
                        case "2":
                            intentionToSell(user, requestNumber);
                            break;
                        case "3":
                            buyGood(user, requestNumber);
                            break;
                        case "4":
                            System.exit(1);
                            break;
                        default:
                            System.out.println("Unknown command.");
                    }
                } catch (ServerException | NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
                    System.out.println(e.getMessage());
                }
            }

        } catch (NotBoundException | MalformedURLException | RemoteException | ServerException e) {
            logger.error(e);
        }
    }

    private static void buyGood(User user, int requestNumber)
        throws RemoteException, ServerException, NoSuchAlgorithmException, InvalidKeyException,
        SignatureException, NotBoundException, MalformedURLException {

        System.out.print("Enter goodId to buy: ");
        String goodId = new Scanner(System.in).nextLine();
        Good good = hdsNotaryService.getStateOfGood(goodId);
        if (!good.isOnSale()) {
            System.out.print("The good with id " + goodId + " is not on sale.");
            return;
        }

        // Intention to buy
        byte[] signature =
            CryptoUtils.makeDigitalSignature(privateKey, good.getOwnerId(), user.getUserId(),
                good.getGoodId(), String.valueOf(requestNumber));

        final Transaction transactionRequest = hdsNotaryService.intentionToBuy(good.getOwnerId(),
            user.getUserId(),
            good.getGoodId(),
            requestNumber,
            signature);
        ClientService clientServiceSeller =
            (ClientService) Naming.lookup(HdsProperties.getClientUri(good.getOwnerId()));

        // Buy good
        signature = CryptoUtils.makeDigitalSignature(privateKey,
            transactionRequest.getTransactionId(),
            transactionRequest.getSellerId(),
            transactionRequest.getBuyerId(),
            transactionRequest.getGoodId());

        Transaction transaction = clientServiceSeller.buy(transactionRequest.getTransactionId(),
            transactionRequest.getSellerId(),
            transactionRequest.getBuyerId(),
            transactionRequest.getGoodId(),
            signature);

        System.out.println("Good with id " + goodId + " bought!");
        System.out.println("Transaction Id: " + transaction.getTransactionId());
        System.out.println("Seller Id: " + transaction.getSellerId());
        System.out.println("Buyer Id: " + transaction.getBuyerId());
        System.out.println("Seller Signature: " + new String(transaction.getSellerSignature()));
        System.out.println("Buyer Signature: " + new String(transaction.getBuyerSignature()));
    }

    private static void intentionToSell(User user, int requestNumber)
        throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, RemoteException,
        ServerException {

        System.out.print("Enter goodId to sell: ");
        String goodId = new Scanner(System.in).nextLine();
        byte[] signature =
            CryptoUtils.makeDigitalSignature(privateKey, user.getUserId(), goodId,
                String.valueOf(requestNumber));

        if (hdsNotaryService.intentionToSell(user.getUserId(), goodId, requestNumber, signature)) {
            System.out.println("The request was successful.");
        }
    }

    private static void getStateOfGood()
        throws RemoteException, ServerException {
        System.out.print("Enter goodId: ");
        String goodId = new Scanner(System.in).nextLine();
        Good good = hdsNotaryService.getStateOfGood(goodId);
        System.out.println("Owner's id: " + good.getOwnerId());
        System.out.println("On Sale: " + good.isOnSale());
    }
}
