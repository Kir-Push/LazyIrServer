package com.push.lazyir.modules.dbus.websocket;

import com.push.lazyir.Loggout;
import com.push.lazyir.devices.NetworkPackage;
import com.push.lazyir.modules.dbus.Player;
import com.push.lazyir.modules.dbus.Players;
import com.push.lazyir.utils.CollectingFuture;
import com.push.lazyir.utils.SettableFuture;
import com.push.lazyir.utils.exceptions.FutureCollectionAlreadySetted;
import org.glassfish.tyrus.core.MaxSessions;


import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.StampedLock;

/**
 * Created by buhalo on 29.06.17.
 */
// Че я тут понаписал, мать моя женщина
@MaxSessions(100)
@ServerEndpoint("/v1")
public class PopupEndpoint {

    private volatile static CollectingFuture<Player,Collection<Player>> getAllFuture;
    private static StampedLock lock = new StampedLock();
    private static int expectedCount;

    private final static ConcurrentHashMap<String,Session> connectedSessions = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session)
    {
        long writeLock = lock.writeLock();
        connectedSessions.put(session.getId(), session);
        lock.unlockWrite(writeLock);
    }

    @OnClose
    public void onClose(Session session)
    {
        System.out.println("CLOSED SESSION " + session.getId());
       connectedSessions.remove(session.getId());
    //   players.remove(session.getId());
    }

    @OnError
    public void onError(Session session, Throwable t)
    {
        System.out.println("ERROR SESSION " + session.getId());
        connectedSessions.remove(session.getId());
    }

    @OnMessage
    public void onMessage(Session session, String msg) {
        long stamp = lock.writeLock();
        try {
            NetworkPackage np = NetworkPackage.Cacher.getOrCreatePackage(msg);
            String type = np.getValue("type");
            boolean onePlayer = type.equals("getInfo");
            boolean manyPlayers = type.equals("getInfoMultiple");
            if (onePlayer || manyPlayers) {
                Player player = null;
                Players players = null;

                //todo check correspond in android (status and other names)
                if (onePlayer) {
                    String title = np.getValue("title");
                    String readyTime = ((int) np.getDouble("time")) / 60 + ":" + ((int) np.getDouble("time")) % 60 + " / " + ((int) np.getDouble("duration")) / 60 + ":" + (int) np.getDouble("duration") % 60;
                    player = new Player(title, np.getValue("status"), title, (int) np.getDouble("duration"),
                            (int) (np.getDouble("volume") * 100), (int) np.getDouble("time"), readyTime, "browser", session.getId(),np.getValue("videoSrc"),np.getValue("url"), null);
                } else if (manyPlayers) {
                    int count = Integer.parseInt(np.getValue("numberOfVideos"));
                    players = new Players();
                    for (int i = 0; i < count; i++) {
                        String title = np.getValue("title" + i);
                        String readyTime = ((int) np.getDouble("time" + i)) / 60 + ":" + ((int) np.getDouble("time" + i)) % 60 + " / " + ((int) np.getDouble("duration" + i)) / 60 + ":" + (int) np.getDouble("duration" + i) % 60;
                        Player tmp = new Player(title, np.getValue("status+i"), title, (int) np.getDouble("duration" + i),
                                (int) (np.getDouble("volume" + i) * 100), (int) np.getDouble("time" + i), readyTime, "browser", session.getId() + ":::wbmpl:::" + np.getValue("localId"),np.getValue("videoSrc")+i,np.getValue("url")+i, np.getValue("localId" + i));
                        players.addTo(tmp);
                    }
                }
                try {
                    if (onePlayer) {
                        getAllFuture.putItem(player);
                    }
                    else if (manyPlayers)
                        for (Player player1 : players.getPlayerList())
                            getAllFuture.putItem(player1);
                    if (expectedCount <= getAllFuture.getCollectedSize()) {
                        getAllFuture.completeWithCollected();
                    }

                } catch (Exception e) {
                    Loggout.e("Websocket", "Onmessage Error", e);
                }
            }
        }finally {
            lock.unlock(stamp);
        }
    }


    // sending message to browser script
    // first check if id arg contain localId for one of many page html5 vid's(only need when on page many of vids)
    // if yes add to message - multipleVids key with value true and lazyIrId with localId
    // It done via recreating NetworkPackage what is cost operation, need to rewrite
    // get stored session from hashMap and send message.
    // todo android version id handle
    public static void sendMessage(String msg,String id)
    {
        String[] splittedId = checkIfMultiple(id);
        String localId;
        String lclMsg;
        if(splittedId == null) {
            localId = id;
            lclMsg = msg;
        } else{
            localId = splittedId[0];
            NetworkPackage pckg = NetworkPackage.Cacher.getOrCreatePackage(msg);
            pckg.setValue("multipleVids","true");
            pckg.setValue("lazyIrId",splittedId[1]);
            lclMsg = pckg.getMessage();
        }
        try {
            Session session = connectedSessions.get(localId);
            if(session != null) {
                RemoteEndpoint.Basic basicRemote = session.getBasicRemote();
                if(basicRemote != null) {
                    basicRemote.sendText(lclMsg);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String[] checkIfMultiple(String id) {
        String[] split = id.split(":::wbmpl:::");
        if( split.length == 2)
            return split;
        else
            return null;
    }

    //play or pause
    public static void sendStatus(String id,String status)
    {
        NetworkPackage np =  NetworkPackage.Cacher.getOrCreatePackage("Web","JS");
        np.setValue("command",status);
        sendMessage(np.getMessage(),id);
    }

    public static void sendVolume(String id,String volume)
    {
        NetworkPackage np =  NetworkPackage.Cacher.getOrCreatePackage("Web","JS");
        np.setValue("command","setVolume");
        np.setValue("volume",volume);
        sendMessage(np.getMessage(),id);
    }

    public static void sendTime(String id,String time)
    {
        NetworkPackage np =  NetworkPackage.Cacher.getOrCreatePackage("Web","JS");
        np.setValue("command","setTime");
        np.setValue("time",time);
        sendMessage(np.getMessage(),id);
    }

    public static void sendGetInfo()
    {
        NetworkPackage np =  NetworkPackage.Cacher.getOrCreatePackage("Web","JS");
        np.setValue("command","getInfo");
        expectedCount = connectedSessions.size();
        for (Session session : connectedSessions.values()) {
            sendMessage(np.getMessage(),session.getId());
        }
    }

    public static CollectingFuture<Player,Collection<Player>> getAll() throws InterruptedException,FutureCollectionAlreadySetted
    {
        long stamp = lock.readLock();
        try {
            if (getAllFuture == null || getAllFuture.isDone()) {
                stamp = lock.tryConvertToWriteLock(stamp);
                if (stamp != 0L) {
                    getAllFuture = new CollectingFuture<>();
                    getAllFuture.setCollection( new ConcurrentSkipListSet<>());
                    System.out.println("Initiate collecton");
                    sendGetInfo();
                }
            }
            return getAllFuture;
        } finally {
            lock.unlock(stamp);
        }
    }
}