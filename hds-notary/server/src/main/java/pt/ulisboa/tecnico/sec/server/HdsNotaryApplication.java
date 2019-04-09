package pt.ulisboa.tecnico.sec.server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import org.apache.log4j.Logger;
import pt.ulisboa.tecnico.sec.library.HdsProperties;
import pt.ulisboa.tecnico.sec.server.services.HdsNotaryServiceImpl;

/**
 * HdsNotaryService world!
 */
public class HdsNotaryApplication {

    private static final Logger logger = Logger.getLogger(HdsNotaryApplication.class);

    public static void main(String[] args) {
        int registryPort = HdsProperties.getServerPort();

        // Bind RMI service
        try {
            HdsNotaryServiceImpl server = new HdsNotaryServiceImpl();

            final Registry reg = LocateRegistry.createRegistry(registryPort);

            reg.rebind("HdsNotaryService", server);

            logger.info("Server up at port " + registryPort);

            System.in.read();

        } catch (Exception e) {
            logger.error(e);
            System.exit(1);
        }
    }
}
