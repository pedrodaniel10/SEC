package pt.ulisboa.tecnico.sec.server.client;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.security.SignatureException;
import java.util.Optional;
import java.util.Scanner;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import pt.ulisboa.tecnico.sec.server.client.services.HdsNotaryEvilClient;
import pt.ulisboa.tecnico.sec.services.data.Good;
import pt.ulisboa.tecnico.sec.services.data.Transaction;
import pt.ulisboa.tecnico.sec.services.data.User;
import pt.ulisboa.tecnico.sec.services.exceptions.UserNotFoundException;
import pt.ulisboa.tecnico.sec.services.properties.HdsProperties;

/**
 * ClientApplication main class
 */
public class EvilClientApplication {

    private static final Logger logger = Logger.getLogger(EvilClientApplication.class);

    public static String username = "";
    public static String userId;

    public static void main(String[] args)
        throws NotBoundException, MalformedURLException, RemoteException, SignatureException {
        // create the command line parser
        CommandLineParser parser = new DefaultParser();

        // create the Options
        Options options = new Options();
        options.addOption(new Option("help", "Prints this message"));
        options.addOption(Option.builder("u").longOpt("username").argName("username").desc(
            "The name of the user to login.").hasArg().build());
        options.addOption(Option.builder("p").longOpt("password").argName("password").desc(
            "The password of the user to login.").hasArg().build());

        // automatically generate the help statement
        HelpFormatter formatter = new HelpFormatter();

        String password = "";
        try {
            CommandLine line = parser.parse(options, args);

            // Help
            if (line.hasOption("help")) {
                formatter.printHelp("HDS-Client", options, true);
                System.exit(0);
            }

            if (line.hasOption("username")) {
                username = line.getOptionValue("username");
            } else {
                System.out.print("Enter username: ");
                System.out.flush();
                username = new Scanner(System.in).nextLine();
            }

            if (line.hasOption("password")) {
                password = line.getOptionValue("password");
            } else {
                System.out.print("Enter password: ");
                System.out.flush();
                password = new Scanner(System.in).nextLine();
            }

        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("HDS-Client", options, true);
            System.exit(1);
        }

        // Get user
        User user = null;
        try {
            user = HdsProperties.getUser(username);
            userId = user.getUserId();
        } catch (UserNotFoundException e) {
            logger.error(e);
            System.exit(1);
        }

        HdsNotaryEvilClient.init(user, username, password);

        while (true) {
            System.out.println("EVIL HDS Notary Service =======================");
            System.out.println("1) Get state of good.");
            System.out.println("2) Intention to sell good.");
            System.out.println("3) Buy good.");
            System.out.println("4) Exit.");
            System.out.println("==========================================");

            String option = new Scanner(System.in).nextLine();
            try {
                switch (option) {
                    case "1": {
                        System.out.print("Enter goodId: ");
                        System.out.flush();
                        String goodId = new Scanner(System.in).nextLine();

                        final Optional<Good> response = HdsNotaryEvilClient.getStateOfGood(user, goodId);

                        if (response.isPresent()) {
                            System.out.println("Owner's id: " + response.get().getOwnerId());
                            System.out.println("On Sale: " + response.get().isOnSale());
                        }
                        break;
                    }
                    case "2": {
                        System.out.print("Enter goodId to sell: ");
                        System.out.flush();
                        String goodId = new Scanner(System.in).nextLine();

                        if (HdsNotaryEvilClient.intentionToSell(user, goodId)) {
                            System.out.println("The request was successful.");
                        } else {
                            System.out.println("The request was unsuccessful.");
                        }
                        break;
                    }
                    case "3": {
                        //Get State of Good
                        System.out.print("Enter goodId to buy: ");
                        System.out.flush();
                        String goodId = new Scanner(System.in).nextLine();

                        final Optional<Transaction> transaction = HdsNotaryEvilClient.buyGood(user, goodId);

                        if (transaction.isPresent()) {
                            System.out.println("Good with id " + goodId + " bought!");
                            System.out.println("Seller Id: " + transaction.get().getSellerId());
                            System.out.println("Buyer Id: " + transaction.get().getBuyerId());
                        } else {
                            System.out.println("The request was unsuccessful.");
                        }
                        break;
                    }
                    case "4":
                        System.exit(1);
                        break;
                    default:
                        System.out.println("Unknown command.");
                }
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
        }

    }
}
