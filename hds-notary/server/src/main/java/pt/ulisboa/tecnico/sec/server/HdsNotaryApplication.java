package pt.ulisboa.tecnico.sec.server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import pt.ulisboa.tecnico.sec.server.services.HdsNotaryServiceImpl;
import pt.ulisboa.tecnico.sec.server.utils.CcUtils;
import pt.ulisboa.tecnico.sec.server.utils.PersistenceUtils;
import pt.ulisboa.tecnico.sec.services.properties.HdsProperties;
import pteidlib.PteidException;
import pteidlib.pteid;

/**
 * HdsNotaryService world!
 */
public class HdsNotaryApplication {

    private static final Logger logger = Logger.getLogger(HdsNotaryApplication.class);

    public static boolean signWithCC = true;
    public static String serverPassword;
    public static String notaryPassword;

    public static void main(String[] args) {
        // create the command line parser
        CommandLineParser parser = new DefaultParser();

        // create the Options
        Options options = new Options();
        options.addOption(new Option("help", "Prints this message"));
        options.addOption(
            new Option("noCC", "noCitizenCard", false, "Disables signature using the Portuguese Citizen Card"));
        options.addOption(Option.builder("p")
            .longOpt("server-password").argName("password")
            .desc("The server's private key password.")
            .hasArg()
            .required()
            .build());
        options.addOption(Option.builder("np")
            .longOpt("notary-password")
            .argName("password")
            .desc("The Notary's private key password in case Citizen Card is disabled. It is required in that case.")
            .hasArg()
            .build());
        options.addOption(Option.builder("sid")
            .longOpt("server-id")
            .argName("id")
            .desc("The server's identifier.")
            .hasArg()
            .required()
            .build());

        // automatically generate the help statement
        HelpFormatter formatter = new HelpFormatter();

        String serverId = "";
        try {
            CommandLine line = parser.parse(options, args);

            // Help
            if (line.hasOption("help")) {
                formatter.printHelp("HDS-Server", options, true);
                System.exit(0);
            }

            if (line.hasOption("noCC")) {
                signWithCC = false;
                if (!line.hasOption("np")) {
                    System.out.println("Missing required option: np");
                    formatter.printHelp("HDS-Server", options, true);
                    System.exit(0);
                }
                notaryPassword = line.getOptionValue("np");
            }

            serverId = line.getOptionValue("sid");
            serverPassword = line.getOptionValue("p");

            // Start server from file
            PersistenceUtils.init(serverId);

        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("HDS-Server", options, true);
            System.exit(1);
        }

        int registryPort = HdsProperties.getServerPort(serverId);

        try {
            if (signWithCC) {
                CcUtils.init();
            }

            //Bind RMI service
            HdsNotaryServiceImpl server = new HdsNotaryServiceImpl();

            final Registry reg = LocateRegistry.createRegistry(registryPort);

            reg.rebind("HdsNotaryService", server);

            logger.info("Server up at port " + registryPort);

            System.in.read();

            if (signWithCC) {
                pteid.Exit(pteid.PTEID_EXIT_LEAVE_CARD);
            }

        } catch (Exception e) {
            logger.error(e);
            if (signWithCC) {
                try {
                    pteid.Exit(pteid.PTEID_EXIT_LEAVE_CARD);
                } catch (PteidException e1) {
                    System.exit(1);
                }
            }

            System.exit(1);
        }
    }
}
