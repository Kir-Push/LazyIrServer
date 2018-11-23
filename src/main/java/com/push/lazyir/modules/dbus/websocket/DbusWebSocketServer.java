package com.push.lazyir.modules.dbus.websocket;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import com.push.lazyir.api.MessageFactory;
import com.push.lazyir.api.NetworkPackage;
import com.push.lazyir.modules.dbus.MprisDto;
import com.push.lazyir.modules.dbus.Player;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import java.net.InetSocketAddress;
import java.util.List;

@Slf4j
@Getter
public class DbusWebSocketServer extends WebSocketServer {

    private ListMultimap<InetSocketAddress,Player> playersHashMap =  MultimapBuilder.hashKeys().arrayListValues().build();
    private MessageFactory messageFactory;

    DbusWebSocketServer(InetSocketAddress address,MessageFactory messageFactory) {
        super(address);
        this.messageFactory = messageFactory;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake){
        // don't need logging, and nothing to implement now, maybe later
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        removeConnection(conn.getRemoteSocketAddress());
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        parseMessage(conn.getRemoteSocketAddress(),message);
    }
    @Override
    public void onError(WebSocket conn, Exception ex) {
        if(conn != null) {
            log.error("Error in connection to " + conn.getRemoteSocketAddress(), ex);
            removeConnection(conn.getRemoteSocketAddress());
        }
    }

    @Override
    public void onStart() {
        log.info("WebSocket server started");
    }

    private void removeConnection(InetSocketAddress address){
        playersHashMap.removeAll(address);
    }


    private void parseMessage(InetSocketAddress ip,String message) {
        NetworkPackage np = messageFactory.parseMessage(message);
        List<Player> players = ((MprisDto)np.getData()).getPlayers();
        if(players != null && !players.isEmpty()) {
            players.forEach(player -> player.setIp(ip.toString()));
            playersHashMap.putAll(ip,players);
        }
        else {
            removeConnection(ip);
        }
    }
}
