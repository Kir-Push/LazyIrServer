package com.push.lazyir.modules.dbus.websocket;

import com.push.lazyir.devices.NetworkPackage;
import com.push.lazyir.modules.dbus.Player;
import org.java_websocket.WebSocket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ServerController{
    private static DbusWebSocketServer dbusWebSocketServer;
    private static Lock lock = new ReentrantLock();
    private static boolean working = false;

    public static void startServer(){
        lock.lock();
        try {
            if (dbusWebSocketServer == null && !working) {
                dbusWebSocketServer = new DbusWebSocketServer(new InetSocketAddress("localhost", 11520));
                working = true;
                dbusWebSocketServer.start();
            }
        }finally {
            lock.unlock();
        }
    }

    public static void stopServer(){
        lock.lock();
        try {
            dbusWebSocketServer.stop();
            dbusWebSocketServer = null;
            working = false;
        } catch (IOException | InterruptedException e) {
                e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    public static List<Player> getAll(){
        ConcurrentHashMap<InetSocketAddress, List<Player>> playersHashMap = dbusWebSocketServer.getPlayersHashMap();
        List<Player> players = new ArrayList<>();
        for (List<Player> playerList : playersHashMap.values()) {
            players.addAll(playerList);
        }
        return players;
    }


        private static String[] checkIfMultiple(String id) {
        String[] split = id.split(":::wbmpl:::");
        if( split.length == 2)
            return split;
        else
            return null;
    }

    private static void sendMessage(NetworkPackage np, String id) {
        String[] splittedId = checkIfMultiple(id);
        String localId, lclMsg;
        if (splittedId == null) {
            localId = id;
            lclMsg = np.getMessage();
        }else{
            localId = splittedId[0];
            np.setValue("multipleVids","true");
            np.setValue("lazyIrId",splittedId[1]);
            lclMsg = np.getMessage();
        }
        for (WebSocket webSocket : dbusWebSocketServer.getConnections()) {
            if(webSocket.getRemoteSocketAddress().toString().equals(localId)) {
                webSocket.send(lclMsg);
                break;
            }
        }
    }

    public static void sendTime(String id,String time) {
        NetworkPackage np =  NetworkPackage.Cacher.getOrCreatePackage("Web","JS");
        np.setValue("command","setTime");
        np.setValue("time",time);
        sendMessage(np,id);
        sendGetInfo();
    }

    public static void sendStatus(String id, String status) {
        NetworkPackage np =  NetworkPackage.Cacher.getOrCreatePackage("Web","JS");
        np.setValue("command",status);
        sendMessage(np,id);
        sendGetInfo();
    }

    public static void sendVolume(String id,String volume) {
        NetworkPackage np =  NetworkPackage.Cacher.getOrCreatePackage("Web","JS");
        np.setValue("command","setVolume");
        np.setValue("volume",volume);
        sendMessage(np,id);
        sendGetInfo();
    }


    public static void sendGetInfo()
    {
        NetworkPackage np =  NetworkPackage.Cacher.getOrCreatePackage("Web","JS");
        np.setValue("command","getInfo");
        dbusWebSocketServer.broadcast(np.getMessage());
    }
}
