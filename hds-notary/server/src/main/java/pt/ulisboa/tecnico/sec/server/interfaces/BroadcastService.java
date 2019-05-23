package pt.ulisboa.tecnico.sec.server.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import pt.ulisboa.tecnico.sec.server.data.Request;
import pt.ulisboa.tecnico.sec.services.exceptions.InvalidSignatureException;

/**
 * BroadcastService, RMI interface of the server broadcast
 */
public interface BroadcastService extends Remote {

    void echo(String serverId, Request request, String signature) throws RemoteException, InvalidSignatureException;

    void ready(String serverId, Request request, String signature) throws RemoteException, InvalidSignatureException;

}
