package com.push.lazyir.modules.dbus.websocket;

import com.push.lazyir.api.MessageFactory;
import com.push.lazyir.modules.dbus.MprisDto;
import com.push.lazyir.modules.dbus.Player;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.WebSocket;
import javax.inject.Inject;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ServerController{

    private DbusWebSocketServer dbusWebSocketServer;
    private boolean working;
    private MessageFactory messageFactory;

    @Inject
    public ServerController(MessageFactory messageFactory) {
        this.messageFactory = messageFactory;
    }

    @Synchronized
    public void startServer(){
        if (dbusWebSocketServer == null && notWorking()) {
                dbusWebSocketServer = new DbusWebSocketServer(new InetSocketAddress("localhost", 11520),messageFactory);
                dbusWebSocketServer.setReuseAddr(true);
                dbusWebSocketServer.start();
                working = true;
        }
    }

    @Synchronized
    public void stopServer(){
        try{
            working = false;
            dbusWebSocketServer.stop();
            dbusWebSocketServer = null;
        } catch (IOException | InterruptedException e){
            log.error("stopServer interrupt AAA",e);
            if(Thread.interrupted()){
                Thread.currentThread().interrupt();
            }
        }
    }

    public List<Player> getAll(){
        return new ArrayList<>( dbusWebSocketServer.getPlayersHashMap().values());
    }


    private void sendMessage(String id,String msg) {
        for (WebSocket webSocket : dbusWebSocketServer.getConnections()) {
            if(webSocket.getRemoteSocketAddress().toString().equals(id)) {
                webSocket.send(msg);
                break;
            }
        }
    }

    public void sendTime(String ip,double time,String jsId) {
        String message = messageFactory.createMessage("Web", true, new MprisDto("setTime", jsId, time));
        sendMessage(ip,message);
        sendGetInfo();
    }

    public void sendStatus(String ip, String status,String jsId) {
        String message = messageFactory.createMessage("Web", true, new MprisDto(status, jsId));
        sendMessage(ip,message);
        sendGetInfo();
    }

    public void sendVolume(String ip, double volume, String jsId) {
        String message = messageFactory.createMessage("Web", true, new MprisDto("setVolume", jsId,volume));
        sendMessage(ip,message);
        sendGetInfo();
    }


    public void sendGetInfo() {
        String message = messageFactory.getCachedMessage("GETINFO");
        dbusWebSocketServer.broadcast(message);
    }

    public boolean notWorking() {
        return !working;
    }
}
