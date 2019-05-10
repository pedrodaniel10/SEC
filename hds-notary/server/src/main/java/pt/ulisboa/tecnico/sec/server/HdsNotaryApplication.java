package pt.ulisboa.tecnico.sec.server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import org.apache.log4j.Logger;
import pt.ulisboa.tecnico.sec.library.HdsProperties;
import pt.ulisboa.tecnico.sec.server.services.HdsNotaryServiceImpl;
import pt.ulisboa.tecnico.sec.server.utils.CcUtils;
import pteidlib.PteidException;
import pteidlib.pteid;

/**
 * HdsNotaryService world!
 */
public class HdsNotaryApplication {

    private static final Logger logger = Logger.getLogger(HdsNotaryApplication.class);

    public static void main(String[] args) {
        int registryPort = HdsProperties.getServerPort();

        try {
            CcUtils.init();
            //Bind RMI service
            HdsNotaryServiceImpl server = new HdsNotaryServiceImpl();

            final Registry reg = LocateRegistry.createRegistry(registryPort);

            reg.rebind("HdsNotaryService", server);

            logger.info("Server up at port " + registryPort);

            System.in.read();

            pteid.Exit(pteid.PTEID_EXIT_LEAVE_CARD);

        } catch (Exception e) {
            logger.error(e);
            try {
                pteid.Exit(pteid.PTEID_EXIT_LEAVE_CARD);
            } catch (PteidException e1) {
                System.exit(1);
            }
            System.exit(1);
        }
    }
}
