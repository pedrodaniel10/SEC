package pt.ulisboa.tecnico.sec.server.client;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Scanner;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import pt.ulisboa.tecnico.sec.library.HdsProperties;
import pt.ulisboa.tecnico.sec.library.crypto.CryptoUtils;
import pt.ulisboa.tecnico.sec.library.data.Good;
import pt.ulisboa.tecnico.sec.library.data.Transaction;
import pt.ulisboa.tecnico.sec.library.data.User;
import pt.ulisboa.tecnico.sec.library.exceptions.InvalidSignatureException;
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
    private static RSAPublicKey serverPublicKey;
    private static PublicKey notaryPublicKey;

    private static HdsNotaryService hdsNotaryService;

    public static void main(String[] args) {
        String username;
        String password;
        if (args.length == 2) {
            username = args[0];
            password = args[1];
        } else {
            System.out.print("Enter username: ");
            System.out.flush();
            username = new Scanner(System.in).nextLine();
            System.out.print("Enter password: ");
            System.out.flush();
            password = new Scanner(System.in).nextLine();
        }

        // Get user
        User user = null;
        try {
            user = HdsProperties.getUser(username);
        } catch (UserNotFoundException e) {
            logger.error(e);
            System.exit(1);
        }

        //Get Server Public Key
        serverPublicKey = CryptoUtils.getPublicKey(HdsProperties.getServerPublicKey());

        // Get private key
        privateKey = CryptoUtils.getPrivateKey(HdsProperties.getClientPrivateKey(user.getName()), password);

        try {
            hdsNotaryService = (HdsNotaryService) Naming.lookup(HdsProperties.getServerUri());

            // Setup P2P service
            ClientService clientService = new ClientServiceImpl(hdsNotaryService, privateKey);

            final int registryPort = HdsProperties.getClientPort(username);
            final Registry reg = LocateRegistry.createRegistry(registryPort);

            reg.rebind("ClientService", clientService);

            logger.info("ClientService up at port " + registryPort);

            ImmutablePair<PublicKey, byte[]> requestNotaryKey = hdsNotaryService.getNotaryPublicKey();
            notaryPublicKey = requestNotaryKey.getLeft();

            if (!CryptoUtils.verifyDigitalSignature(serverPublicKey, requestNotaryKey.getRight(),
                new String(notaryPublicKey.getEncoded()))) {
                logger.error("Notary Public Key signature doesn't match.");
                System.exit(1);
            }

            while (true) {
                System.out.println("HDS Notary Service =======================");
                System.out.println("1) Get state of good.");
                System.out.println("2) Intention to sell good.");
                System.out.println("3) Buy good.");
                System.out.println("4) Exit.");
                System.out.println("==========================================");

                String nonce = hdsNotaryService.getNonce(user.getUserId());

                String option = new Scanner(System.in).nextLine();
                try {
                    switch (option) {
                        case "1":
                            getStateOfGood(user, nonce);
                            break;
                        case "2":
                            intentionToSell(user, nonce);
                            break;
                        case "3":
                            buyGood(user, nonce);
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

        } catch (NotBoundException | MalformedURLException | RemoteException | ServerException | NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            logger.error(e);
        }
    }

    private static void buyGood(User user, String nonce)
        throws RemoteException, ServerException, NoSuchAlgorithmException, InvalidKeyException,
               SignatureException, NotBoundException, MalformedURLException {

        //Get State of Good
        System.out.print("Enter goodId to buy: ");
        System.out.flush();
        String goodId = new Scanner(System.in).nextLine();
        byte[] signature = CryptoUtils.makeDigitalSignature(privateKey, user.getUserId(), goodId, nonce);

        ImmutablePair<Good, byte[]> response = hdsNotaryService.getStateOfGood(user.getUserId(), goodId, nonce,
            signature);

        Good good = response.getLeft();

        // Verify Signature
        if (!CryptoUtils.verifyDigitalSignature(serverPublicKey, response.getRight(),
            goodId, Boolean.toString(good.isOnSale()), nonce)) {
            throw new InvalidSignatureException(
                "Server has signature invalid.");
        }

        if (!good.isOnSale()) {
            System.out.println("The good with id " + goodId + " is not on sale.");
            return;
        }

        // Intention to buy
        nonce = hdsNotaryService.getNonce(user.getUserId());
        signature = CryptoUtils.makeDigitalSignature(privateKey, good.getOwnerId(), user.getUserId(),
            good.getGoodId(), nonce);

        final Transaction transactionResponse = hdsNotaryService.intentionToBuy(
            good.getOwnerId(),
            user.getUserId(),
            good.getGoodId(),
            nonce,
            signature);

        // Verify Signature
        if (!CryptoUtils.verifyDigitalSignature(serverPublicKey, transactionResponse.getNotarySignature(),
            transactionResponse.getTransactionId(), nonce)) {
            throw new InvalidSignatureException(
                "IntentionToBuy: Server has signature invalid.");
        }

        // Buy good
        ClientService clientServiceSeller =
            (ClientService) Naming.lookup(HdsProperties.getClientUri(good.getOwnerId()));

        signature = CryptoUtils.makeDigitalSignature(privateKey,
            transactionResponse.getTransactionId(),
            transactionResponse.getSellerId(),
            transactionResponse.getBuyerId(),
            transactionResponse.getGoodId());

        Transaction transaction = clientServiceSeller.buy(
            transactionResponse.getTransactionId(),
            transactionResponse.getSellerId(),
            transactionResponse.getBuyerId(),
            transactionResponse.getGoodId(),
            signature);

        // Verify Signature
        if (!CryptoUtils.verifyDigitalSignature(notaryPublicKey, transaction.getNotarySignature(),
            transaction.getTransactionId(),
            transaction.getSellerId(), transaction.getBuyerId(), new String(transaction.getSellerSignature()),
            new String(transaction.getBuyerSignature()))) {
            throw new InvalidSignatureException(
                "BuyGood: Seller has signature invalid.");
        }

        System.out.println("Good with id " + goodId + " bought!");
        System.out.println("Transaction Id: " + transaction.getTransactionId());
        System.out.println("Seller Id: " + transaction.getSellerId());
        System.out.println("Buyer Id: " + transaction.getBuyerId());
        System.out.println("Seller Signature: " + Base64.encodeBase64String(transaction.getSellerSignature()));
        System.out.println("Buyer Signature: " + Base64.encodeBase64String(transaction.getBuyerSignature()));
        System.out.println("Notary Signature: " + Base64.encodeBase64String(transaction.getNotarySignature()));
    }

    private static void intentionToSell(User user, String nonce)
        throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, RemoteException,
               ServerException {

        System.out.print("Enter goodId to sell: ");
        System.out.flush();
        String goodId = new Scanner(System.in).nextLine();
        byte[] signature =
            CryptoUtils.makeDigitalSignature(privateKey, user.getUserId(), goodId,
                nonce);

        ImmutablePair<Boolean, byte[]> response = hdsNotaryService.intentionToSell(user.getUserId(), goodId, nonce,
            signature);

        // Verify Signature
        if (!CryptoUtils.verifyDigitalSignature(serverPublicKey, response.getRight(), goodId,
            Boolean.toString(response.getLeft()), nonce)) {
            throw new InvalidSignatureException(
                "Server has signature invalid.");
        }

        if (response.getLeft()) {
            System.out.println("The request was successful.");
        }
    }

    private static void getStateOfGood(User user, String nonce)
        throws RemoteException, ServerException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {

        System.out.print("Enter goodId: ");
        System.out.flush();
        String goodId = new Scanner(System.in).nextLine();
        byte[] signature = CryptoUtils.makeDigitalSignature(privateKey, user.getUserId(), goodId, nonce);

        ImmutablePair<Good, byte[]> response = hdsNotaryService.getStateOfGood(user.getUserId(), goodId, nonce,
            signature);
        Good good = response.getLeft();

        // Verify Signature
        if (!CryptoUtils.verifyDigitalSignature(serverPublicKey, response.getRight(), goodId,
            Boolean.toString(good.isOnSale()), nonce)) {
            throw new InvalidSignatureException("Server has signature invalid.");
        }

        System.out.println("Owner's id: " + response.getLeft().getOwnerId());
        System.out.println("On Sale: " + response.getLeft().isOnSale());
    }
}
