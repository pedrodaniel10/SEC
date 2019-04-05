package pt.ulisboa.tecnico.sec.client;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import org.apache.log4j.Logger;
import pt.ulisboa.tecnico.sec.library.interfaces.server.HdsNotaryService;

/**
 * Client main class
 */
public class Client {
    private static final Logger logger = Logger.getLogger(Client.class);

    public static void main(String[] args) {

        try {
            HdsNotaryService hello = (HdsNotaryService) Naming.lookup("//localhost:1099/HdsNotaryService");
            logger.info(hello.hello("Pedro"));
        } catch (NotBoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
