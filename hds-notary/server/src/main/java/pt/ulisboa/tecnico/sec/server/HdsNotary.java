package pt.ulisboa.tecnico.sec.server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import pt.ulisboa.tecnico.sec.server.services.HdsNotaryServiceImpl;

/**
 * HdsNotaryService world!
 */
public class HdsNotary {
    public static final int registryPort = 1099;

    public static void main(String[] args) {
        try {
            HdsNotaryServiceImpl hello = new HdsNotaryServiceImpl();

            final Registry reg = LocateRegistry.createRegistry(registryPort);

            reg.rebind("HdsNotaryService", hello);

            System.err.println("Server up");

            System.in.read();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
