package pt.ulisboa.tecnico.sec.server.services;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import pt.ulisboa.tecnico.sec.library.interfaces.server.HdsNotaryService;

public class HdsNotaryServiceImpl extends UnicastRemoteObject implements HdsNotaryService {

    public HdsNotaryServiceImpl() throws RemoteException {
    }

    @Override
    public String hello(String name) throws RemoteException {
        return "Hello " + name;
    }
}
