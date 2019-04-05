package pt.ulisboa.tecnico.sec.library.interfaces.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * HdsNotaryService world!
 */
public interface HdsNotaryService extends Remote {

    String hello(String name) throws RemoteException;
}
