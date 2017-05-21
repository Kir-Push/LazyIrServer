package com.push.lazyir.service;

import com.push.lazyir.devices.Device;
import com.push.lazyir.managers.TcpConnectionManager;
import com.push.lazyir.managers.UdpBroadcastManager;
import com.push.lazyir.modules.clipBoard.BoardListener;

/**
 * Created by buhalo on 12.03.17.
 */
public class BackgroundService {

    private static BackgroundService instance;

    public static BackgroundService getInstance()
    {
        if(instance == null)
        {
            instance = new BackgroundService(TcpConnectionManager.getInstance(),UdpBroadcastManager.getInstance());
        }
        return instance;
    }


    private TcpConnectionManager tcp;
    private UdpBroadcastManager udp;

    private BoardListener clipBoard;

    private BackgroundService(TcpConnectionManager tcp, UdpBroadcastManager udp) {
        this.tcp = tcp;
        this.udp = udp;
        this.clipBoard = new BoardListener();
    }



    public synchronized void startUdpListening()
    {
        udp.startUdpListener(5667);
    }

    public synchronized void connectCached()
    {
        udp.connectRecconect("null");
    }

    public synchronized void startClipBoardListener()
    {
        clipBoard.run();
    }

    public synchronized void stopUdpListening()
    {
        udp.stopUdpListener();
    }

    public synchronized void stopUdpSending()
    {
        udp.stopSending();
    }

    public synchronized void startTcpListening()
    {
        tcp.startListening(5667);
    }

    public synchronized void eraseTcpConnections()
    {
        for (Device device : Device.getConnectedDevices().values()) {
            tcp.StopListening(device.getId());
        }

    }

    public UdpBroadcastManager getUdp() {
        return udp;
    }

    public void setUdp(UdpBroadcastManager udp) {
        this.udp = udp;
    }

    public TcpConnectionManager getTcp() {
        return tcp;
    }

    public void setTcp(TcpConnectionManager tcp) {
        this.tcp = tcp;
    }
}
