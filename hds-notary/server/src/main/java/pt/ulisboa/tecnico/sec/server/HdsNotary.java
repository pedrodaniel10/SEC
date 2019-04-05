package pt.ulisboa.tecnico.sec.server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import org.apache.log4j.Logger;
import pt.ulisboa.tecnico.sec.server.services.HdsNotaryServiceImpl;

/**
 * HdsNotaryService world!
 */
public class HdsNotary {
    private static final Logger logger = Logger.getLogger(HdsNotary.class);
    // TODO: Change this hard-coded registry port to dynamic.
    private static final int registryPort = 1099;

    public static void main(String[] args) {
        try {
            HdsNotaryServiceImpl hello = new HdsNotaryServiceImpl();

            final Registry reg = LocateRegistry.createRegistry(registryPort);

            reg.rebind("HdsNotaryService", hello);

            logger.info("Server up");

            System.in.read();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
