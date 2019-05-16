package pt.ulisboa.tecnico.sec.server.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import pt.ulisboa.tecnico.sec.server.data.Request;

/**
 * BroadcastService, RMI interface of the server broadcas
 */
public interface BroadcastService extends Remote {

    void echo(String serverId, Request request) throws RemoteException;

    void ready(String serverId, Request request) throws RemoteException;

}
