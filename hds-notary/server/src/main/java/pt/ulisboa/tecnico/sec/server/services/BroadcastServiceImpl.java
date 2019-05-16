package pt.ulisboa.tecnico.sec.server.services;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.log4j.Logger;
import pt.ulisboa.tecnico.sec.server.HdsNotaryApplication;
import pt.ulisboa.tecnico.sec.server.data.Request;
import pt.ulisboa.tecnico.sec.server.interfaces.BroadcastService;
import pt.ulisboa.tecnico.sec.services.properties.HdsProperties;
import pt.ulisboa.tecnico.sec.services.utils.Constants;


public class BroadcastServiceImpl extends UnicastRemoteObject implements BroadcastService, Serializable {

    private static final Logger logger = Logger.getLogger(BroadcastServiceImpl.class);
    private static final Object echoWait = new Object();
    private static final Object readyWait = new Object();
    private static Boolean newRequest = false;
    private static Map<String, Object> deliverWait = new ConcurrentHashMap<>();
    private static Map<String, BroadcastService> broadcastServices = new HashMap<>();
    private static Map<String, Boolean> sentEcho = new ConcurrentHashMap<>();
    private static Map<String, Boolean> sentReady = new ConcurrentHashMap<>();
    private static Map<String, Boolean> delivered = new ConcurrentHashMap<>();
    private static Map<String, Map<String, Request>> echos = new ConcurrentHashMap<>();
    private static Map<String, Map<String, Request>> readys = new ConcurrentHashMap<>();

    public BroadcastServiceImpl() throws RemoteException {
    }

    public static void init() {
        for (int i = 0; i < Constants.N; i++) {
            String id = "" + i;
            try {
                broadcastServices.put(id, (BroadcastService) Naming.lookup(HdsProperties.getServerBroadcastUri((id))));
            } catch (NotBoundException | MalformedURLException | RemoteException e) {
                logger.error(e);
            }
        }
        waitForQuorum();
    }

    public static void waitForQuorum() {
        //Thread waiting for quorum of echos
        CompletableFuture.runAsync(() -> {
            while (true) {
                if (newRequest) {
                    //Count echos
                    for (Map.Entry<String, Map<String, Request>> clientMap : echos.entrySet()) {
                        final Collection<Request> requests = clientMap.getValue().values();
                        Set<Request> uniqueSet = new HashSet<>(requests);
                        for (Request temp : uniqueSet) {
                            if (temp == null) {
                                continue;
                            }
                            //If we have a quorum of echos and sentready = false
                            if (Collections.frequency(requests, temp) > (Constants.N + Constants.F) / 2
                                && !sentReady.containsKey(clientMap.getKey())) {
                                sentReady.putIfAbsent(clientMap.getKey(), true);
                                synchronized (readyWait) {
                                    sendReadys(temp);
                                }
                            }
                        }
                    }
                }
                synchronized (echoWait) {
                    try {
                        echoWait.wait();
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }
        });

        //Waiting for quorum of readys
        CompletableFuture.runAsync(() -> {
            while (true) {
                if (newRequest) {
                    //Count Readys
                    for (Map.Entry<String, Map<String, Request>> clientMap : readys.entrySet()) {
                        final Collection<Request> requests = clientMap.getValue().values();
                        Set<Request> uniqueSet = new HashSet<>(requests);
                        for (Request temp : uniqueSet) {
                            if (temp == null) {
                                continue;
                            }
                            //If we have readys > f and sentready = false
                            if (Collections.frequency(requests, temp) > Constants.F
                                && !sentReady.containsKey(clientMap.getKey())) {
                                sentReady.putIfAbsent(clientMap.getKey(), true);
                                sendReadys(temp);
                            }

                            //If we have readys > 2f and delivered = false
                            if (Collections.frequency(requests, temp) > 2 * Constants.F
                                && !delivered.containsKey(clientMap.getKey())) {
                                delivered.putIfAbsent(clientMap.getKey(), true);
                                //Trigger deliver
                                synchronized (deliverWait.get(clientMap.getKey())) {
                                    deliverWait.get(clientMap.getKey()).notify();
                                }
                            }
                        }
                    }
                }
                synchronized (readyWait) {
                    try {
                        readyWait.wait();
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }
        });
    }

    public static void sendBroadcast(Request request) throws InterruptedException {
        newRequest = true;
        if (broadcastServices.isEmpty()) {
            init();
        }

        initClient(request.getClientId());

        deliverWait.putIfAbsent(request.getClientId(), new Object());

        if (!sentEcho.containsKey(request.getClientId())) { //if sentecho = false
            sentEcho.put(request.getClientId(), true);
            for (Map.Entry<String, BroadcastService> entry : broadcastServices.entrySet()) {
                CompletableFuture.runAsync(() -> {
                    try {
                        entry.getValue().echo(HdsNotaryApplication.serverId, request);
                    } catch (RemoteException e) {
                        logger.error("Error on server id " + entry.getKey());
                    }
                });
            }
        }

        while (!delivered.containsKey(request.getClientId())) {
            synchronized (deliverWait.get(request.getClientId())) {
                deliverWait.get(request.getClientId()).wait();
            }
        }

        cleanClient(request.getClientId(), request);
        newRequest = false;

    }

    static void sendReadys(Request request) {
        for (Map.Entry<String, BroadcastService> entry : broadcastServices.entrySet()) {
            CompletableFuture.runAsync(() -> {
                try {
                    entry.getValue().ready(HdsNotaryApplication.serverId, request);
                } catch (RemoteException e) {
                    logger.error("Error on server id " + entry.getKey());
                }
            });
        }
    }

    static void initClient(String clientId) {
        sentEcho.remove(clientId);
        sentReady.remove(clientId);
        delivered.remove(clientId);
    }

    static void cleanClient(String clientId, Request request) {
        echos.remove(clientId);
        readys.remove(clientId);
    }

    @Override
    public void echo(String serverId, Request request) throws RemoteException {
        echos.putIfAbsent(request.getClientId(), new ConcurrentHashMap<>()); //initialize list of echos.
        echos.get(request.getClientId()).put(serverId, request);
        synchronized (echoWait) {
            echoWait.notifyAll();
        }
    }

    @Override
    public void ready(String serverId, Request request) throws RemoteException {
        readys.putIfAbsent(request.getClientId(), new ConcurrentHashMap<>()); //initialize list of readys.
        readys.get(request.getClientId()).put(serverId, request);
        synchronized (readyWait) {
            readyWait.notifyAll();
        }
    }
}
