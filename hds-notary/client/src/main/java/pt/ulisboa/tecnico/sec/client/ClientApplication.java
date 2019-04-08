package pt.ulisboa.tecnico.sec.client;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;
import org.apache.log4j.Logger;
import pt.ulisboa.tecnico.sec.client.services.ClientServiceImpl;
import pt.ulisboa.tecnico.sec.library.HdsProperties;
import pt.ulisboa.tecnico.sec.library.data.Good;
import pt.ulisboa.tecnico.sec.library.data.Transaction;
import pt.ulisboa.tecnico.sec.library.data.User;
import pt.ulisboa.tecnico.sec.library.exceptions.ServerException;
import pt.ulisboa.tecnico.sec.library.exceptions.UserNotFoundException;
import pt.ulisboa.tecnico.sec.library.interfaces.client.ClientService;
import pt.ulisboa.tecnico.sec.library.interfaces.server.HdsNotaryService;

/**
 * ClientApplication main class
 */
public class ClientApplication {
    private static final Logger logger = Logger.getLogger(ClientApplication.class);

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

        try {
            HdsNotaryService hdsNotaryService = (HdsNotaryService) Naming.lookup(HdsProperties.getServerUri());

            // Setup P2P service
            ClientService clientService = new ClientServiceImpl(hdsNotaryService);

            final int registryPort = HdsProperties.getClientPort(username);
            final Registry reg = LocateRegistry.createRegistry(registryPort);

            reg.rebind("ClientService", clientService);

            logger.info("ClientService up at port " + registryPort);

            while (true) {
                System.out.println("HDS Notary Service =======================");
                System.out.println("1) Get state of good.");
                System.out.println("2) Intention to sell good.");
                System.out.println("3) Buy good.");
                System.out.println("3) Transfer good.");
                System.out.println("4) Exit.");
                System.out.println("==========================================");

                String option = new Scanner(System.in).nextLine();
                try {
                    switch (option) {
                        case "1": {
                            System.out.print("Enter goodId: ");
                            String goodId = new Scanner(System.in).nextLine();
                            Good good = hdsNotaryService.getStateOfGood(goodId);
                            System.out.println("Owner's id: " + good.getOwnerId());
                            System.out.println("On Sale: " + good.isOnSale());
                            break;
                        }
                        case "2": {
                            System.out.print("Enter goodId to sell: ");
                            String goodId = new Scanner(System.in).nextLine();
                            if (hdsNotaryService.intentionToSell(user.getUserId(), goodId)) {
                                System.out.println("The request was successful.");
                            }
                            break;
                        }
                        case "3": {
                            System.out.print("Enter goodId to buy: ");
                            String goodId = new Scanner(System.in).nextLine();
                            Good good = hdsNotaryService.getStateOfGood(goodId);
                            if (!good.isOnSale()) {
                                break;
                            }
                            final Transaction transactionRequest = hdsNotaryService.intentionToBuy(good.getOwnerId(),
                                                                                                   user.getUserId(),
                                                                                                   good.getGoodId());
                            ClientService clientServiceSeller =
                                    (ClientService) Naming.lookup(HdsProperties.getClientUri(good.getOwnerId()));

                            Transaction transaction = clientServiceSeller.buy(transactionRequest.getTransactionId(),
                                                                              transactionRequest.getSellerId(),
                                                                              transactionRequest.getBuyerId(),
                                                                              transactionRequest.getGoodId());
                            System.out.println("Good with id " + goodId + " bought!");
                            System.out.println("Transaction Id: " + transaction.getTransactionId());
                            System.out.println("Seller Id: " + transaction.getSellerId());
                            System.out.println("Buyer Id: " + transaction.getBuyerId());
                            break;
                        }
                        case "4":
                            System.exit(1);
                        default:
                            System.out.println("Unknown command.");
                    }
                } catch (ServerException e) {
                    System.out.println(e.getMessage());
                }
            }

        } catch (NotBoundException | MalformedURLException | RemoteException | UserNotFoundException e) {
            logger.error(e);
        }
    }
}
