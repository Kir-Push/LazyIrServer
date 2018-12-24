package com.push.lazyir.service.main;


import com.google.gson.JsonSyntaxException;
import com.push.lazyir.api.MessageFactory;
import com.push.lazyir.api.NetworkPackage;
import com.push.lazyir.gui.GuiCommunicator;
import com.push.lazyir.utils.exceptions.UdpRuntimeException;
import lombok.Getter;
import lombok.Setter;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.*;


@Slf4j
public class UdpBroadcastManager  {
    public enum api{
        BROADCAST_INTRODUCE,
        INTRODUCE
    }

    private DatagramSocket socket;
    private  DatagramSocket server;
    @Getter @Setter
    public int port;
    @Getter @Setter
    private boolean listening;
    private BackgroundService backgroundService;
    private GuiCommunicator guiCommunicator; // not used now, but maybe helpful in future
    private MessageFactory messageFactory;

    UdpBroadcastManager(BackgroundService backgroundService, GuiCommunicator guiCommunicator, MessageFactory messageFactory) {
        this.backgroundService = backgroundService;
        this.guiCommunicator = guiCommunicator;
        this.messageFactory = messageFactory;
    }

    void configureManager() {
        try {
            socket = new DatagramSocket();
            socket.setReuseAddress(true);
        } catch (IOException e) {
            log.error("error in udp configure method",e);
        }
    }

    void startUdpListener(int port)
    {
            if (isListening()) {
                log.info("udp listener already running - port: " + port);
                return;
            }
            configServer(port);
            setListening(true);
            backgroundService.submitNewTask(() -> {
                final int bufferSize = 1024 * 5;
                byte[] data = new byte[bufferSize];
                try {
                    while (isListening()) {
                        DatagramPacket packet = new DatagramPacket(data, bufferSize);
                        server.receive(packet);
                        broadcastReceived(packet);
                        data = new byte[bufferSize];
                    }
                } catch (Exception e) {
                    log.error("udpReceive exception",e);
                    throw new UdpRuntimeException("Something goes wrong in udpBroadcastListener",e);
                } finally {
                    log.info("stopping udp listener");
                    stopUdpListener();
                }
            });
    }

    private void configServer(int port) {
        try {
            this.port = port;
            server = new DatagramSocket(port);
            server.setReuseAddress(true);
        } catch (SocketException e) {
            log.error("error in startUdpListener", e);
        }
    }


    private void broadcastReceived(DatagramPacket packet) throws UnknownHostException {
        try {
            String pck = new String(packet.getData(), packet.getOffset(), packet.getLength());
            NetworkPackage np = messageFactory.parseMessage(pck);
            String receivedId = np.getId();
            String myId = getMyId();
            if (np.getType().equals(api.BROADCAST_INTRODUCE.name()) && !receivedId.equals(myId)
                    && !backgroundService.getConnectedDevices().containsKey(receivedId) && !backgroundService.getNeighbours().contains(packet.getAddress())) {
                sendUdp(packet.getAddress(), port);
            }
        }catch (JsonSyntaxException e){
            log.error("error in parseMessage",e);
        }
    }

    private void sendUdp(InetAddress address, int port) {
        String message = messageFactory.createMessage(api.INTRODUCE.name(), false, null);
        byte[] bytes = message.getBytes();
        DatagramPacket dp = new DatagramPacket(bytes, bytes.length, address, port);
        try {
            socket.send(dp);
        } catch (IOException e) {
            log.error("exception in sendUdp  - address: " + address + " port: " + port, e);
        }
    }

    private String getMyId() throws UnknownHostException {
        return InetAddress.getLocalHost().getHostName();
    }


    @Synchronized
    void stopUdpListener() {
        setListening(false);
        if (server != null) {
            server.close();
        }
        if(socket != null){
            socket.close();
        }
        socket = null;
        server = null;
    }

    void connectRecconect()  {
        for (String allCachedThing : backgroundService.getSettingManager().getAllCachedThings()) {
            try {
                sendUdp(InetAddress.getByName(allCachedThing),port);
            } catch (UnknownHostException e) {
                log.error("error in udp connectRecconect method",e);
            }
        }
    }

}
