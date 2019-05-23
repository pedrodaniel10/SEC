package pt.ulisboa.tecnico.sec.services.interfaces.client;

import java.rmi.Remote;
import java.rmi.RemoteException;
import pt.ulisboa.tecnico.sec.services.data.Good;
import pt.ulisboa.tecnico.sec.services.exceptions.InvalidSignatureException;

/**
 * ReadBonarService, RMI interface of the getStateOfgood
 */
public interface ReadBonarService extends Remote {

    void setStateOfGood(String serverId, Good good, int readId, int timeStamp, String signature)
        throws RemoteException, InvalidSignatureException;
}
