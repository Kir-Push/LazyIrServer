package com.push.lazyir.modules.dbus.websocket;

import com.push.lazyir.Loggout;
import com.push.lazyir.devices.NetworkPackage;
import com.push.lazyir.modules.dbus.Player;
import com.push.lazyir.modules.dbus.Players;
import com.push.lazyir.utils.SettableFuture;


import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.StampedLock;

/**
 * Created by buhalo on 29.06.17.
 */
// Че я тут понаписал, мать моя женщина
@ServerEndpoint("/v1")
public class PopupEndpoint {

    private volatile static SettableFuture<List<Player>> getAllFuture;
    private static final Semaphore semaphore = new Semaphore(1);
    private static StampedLock lock = new StampedLock();
    private static int expectedCount;

    private final static ConcurrentHashMap<String,Session> connectedSessions = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String,Player> players = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session)
    {
        long writeLock = lock.writeLock();
        if (!connectedSessions.containsKey(session.getId())) {
            connectedSessions.put(session.getId(), session);
        }
        lock.unlockWrite(writeLock);
    }

    @OnClose
    public void onClose(Session session)
    {
       connectedSessions.remove(session.getId());
    //   players.remove(session.getId());
    }

    @OnError
    public void onError(Session session, Throwable t)
    {
  //      connectedSessions.remove(session.getId());
    }

    @OnMessage
    public void onMessage(Session session, String msg) {
           NetworkPackage np =  NetworkPackage.Cacher.getOrCreatePackage(msg);
           if(np.getValue("type").equals("getInfo"))
           {
               try {
                   //todo check correspond in android (status and other names)
                   String title = np.getValue("title");
                   String readyTime = ((int)np.getDouble("time"))/60 + ":" + ((int)np.getDouble("time")) % 60 + " / " + ((int)np.getDouble("duration"))/60 + ":" + (int)np.getDouble("duration")%60;
                   Player player = new Player(title, np.getValue("status"), title, (int)np.getDouble("duration"),
                           (int)(np.getDouble("volume")*100), (int)np.getDouble("time"), readyTime, "browser", session.getId());

                   //todo add many player's per session !!
                   long stamp = lock.writeLock();
                   try {
                       players.put(session.getId(), player);
                       if (expectedCount <= players.size()) {
                           getAllFuture.complete(new ArrayList<>(players.values()));
                       }
                   }finally {
                       lock.unlock(stamp);
                   }
               }catch (Exception e)
               {
                   Loggout.e("Websocket","Onmessage Error",e);
               }
           }
    }


    public static void sendMessage(String msg,String id)
    {
        try {
            Session session = connectedSessions.get(id);
            if(session != null) {
                RemoteEndpoint.Basic basicRemote = session.getBasicRemote();
                if(basicRemote != null)
                basicRemote.sendText(msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public static SettableFuture<List<Player>> getAll() throws InterruptedException
    {
        long stamp = lock.readLock();
        try {
            if (getAllFuture == null || getAllFuture.isDone()) {
                stamp = lock.tryConvertToWriteLock(stamp);
                if (stamp != 0L) {
                    getAllFuture = new SettableFuture<>();
                    players.clear();
                    sendGetInfo();
                }
            }
            return getAllFuture;
        }finally {
            lock.unlock(stamp);
        }
    }
}