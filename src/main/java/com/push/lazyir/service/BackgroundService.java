package com.push.lazyir.service;

import com.push.lazyir.devices.Device;
import com.push.lazyir.managers.settings.SettingManager;
import com.push.lazyir.managers.tcp.TcpConnectionManager;
import com.push.lazyir.managers.udp.UdpBroadcastManager;

/**
 * Created by buhalo on 12.03.17.
 */
public class BackgroundService {



    private volatile static BackgroundService instance;

    public static BackgroundService getInstance()
    {
        if(instance == null)
        {
            instance = new BackgroundService();
        }
        return instance;
    }

    private TcpConnectionManager tcp;
    private UdpBroadcastManager udp;
    private SettingManager settingManager;


    private BackgroundService() {
    }



    public synchronized void startUdpListening()
    {
        udp.configureManager();
        udp.startUdpListener(5667);
    }

    public synchronized void connectCached()
    {
        udp.connectRecconect("null");
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
        tcp.startServer();
        tcp.startListening();
    }

    public synchronized void eraseTcpConnections()
    {
        for (Device device : Device.getConnectedDevices().values()) {
            tcp.StopListening(device.getId());
        }

    }

    public synchronized static UdpBroadcastManager getUdp() {
        return getInstance().udp;
    }

    public synchronized void setUdp(UdpBroadcastManager udp) {
        this.udp = udp;
    }

    public synchronized  static TcpConnectionManager getTcp() {
        return getInstance().tcp;
    }


    public void setSettingManager(SettingManager settingManager){this.settingManager = settingManager;}

    public static SettingManager getSettingManager(){return getInstance().settingManager;}

    public synchronized void setTcp(TcpConnectionManager tcp) {
        this.tcp = tcp;
    }

    public static void sendToDevice(String id, String msg) {
        // todo something like android version
    }
}
