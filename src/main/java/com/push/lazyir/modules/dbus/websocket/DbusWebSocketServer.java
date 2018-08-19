package com.push.lazyir.modules.dbus.websocket;

import com.push.lazyir.Loggout;
import com.push.lazyir.devices.NetworkPackage;
import com.push.lazyir.modules.dbus.Player;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class DbusWebSocketServer extends WebSocketServer {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(DbusWebSocketServer.class);

    public ConcurrentHashMap<InetSocketAddress, List<Player>> getPlayersHashMap() {
        return playersHashMap;
    }

    private ConcurrentHashMap<InetSocketAddress,List<Player>> playersHashMap = new ConcurrentHashMap<>();

    public DbusWebSocketServer(InetSocketAddress address) {
        super(address);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println(handshake.getResourceDescriptor());
        Loggout.logInfo(log,"new connection to " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        Loggout.logInfo(log,"Closed connection to " + conn.getRemoteSocketAddress() + "  Reason: " + reason);
        removeConnection(conn.getRemoteSocketAddress());
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        Loggout.logInfo(log,"Message from " + conn.getRemoteSocketAddress() + ": " + message);
        parseMessage(conn.getRemoteSocketAddress(),message);
    }
    @Override
    public void onMessage( WebSocket conn, ByteBuffer message ) {
        System.out.println("received ByteBuffer from "	+ conn.getRemoteSocketAddress());
    }


    @Override
    public void onError(WebSocket conn, Exception ex) {
        if(conn != null) {
            Loggout.logInfo(log, "Error in connection to " + conn.getRemoteSocketAddress(), ex);
            removeConnection(conn.getRemoteSocketAddress());
        }
    }

    @Override
    public void onStart() {
        Loggout.logInfo(log,"WebSocket server started");
    }

    private void removeConnection(InetSocketAddress address){
        playersHashMap.remove(address);
    }


    private void parseMessage(InetSocketAddress conn, String message) {
        List<Player> players = createPlayers(new NetworkPackage(message),conn);
        if(players != null && players.size() > 0)
        playersHashMap.put(conn,players);
        else
         removeConnection(conn);
    }

    private List<Player> createPlayers(NetworkPackage np, InetSocketAddress remoteSocketAddress) {
        List<Player> players = new ArrayList<>();
        String type = np.getValue("type");
        if(type.equals("getInfo")) {  // if message contain one player
            players.add(createPlayer(np,remoteSocketAddress,""));
        }
        else if(type.equals("getInfoMultiple")) { // if message contain many players
            int count = np.getIntValue("numberOfVideos");
            for(int i = 0;i<count;i++){
                players.add(createPlayer( np,remoteSocketAddress,String.valueOf(i)));
            }
        }
        return players;
    }

    private Player createPlayer(NetworkPackage np, InetSocketAddress remoteSocketAddress, String number) {
        String title = np.getValue("title"+number);
        String status = np.getValue("status" + number);
        double localId = np.getDouble("localId" + number);
        String src = np.getValue("videoSrc" + number);
        String url = np.getValue("url" + number);
        int volume = (int) (np.getDouble("volume"+number)*100);
        int fullDuration = np.getIntValue("duration"+number);
        int fullTime = np.getIntValue("time"+number);
        int mins = np.getIntValue("time"+number) / 60;
        int secs = np.getIntValue("time"+number) % 60;
        int minsDuration =   np.getIntValue("duration"+number) / 60;
        int secsDuration =   np.getIntValue("duration"+number) % 60;
        String readyTime = (mins < 10 ? "0" : "") + mins + ":" +  (secs < 10 ? "0" : "") + secs +
                " / "
                + (minsDuration < 10 ? "0" : "") + minsDuration + ":" + (secsDuration < 10 ? "0" : "") + secsDuration;
        Player player = new Player(title,status,title,fullDuration,volume,fullTime,readyTime,"browser",remoteSocketAddress+":::wbmpl:::"+localId,src,url,String.valueOf(localId));
        return player;
    }
}
