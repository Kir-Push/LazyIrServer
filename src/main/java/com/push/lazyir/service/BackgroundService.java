package com.push.lazyir.service;

import com.push.lazyir.devices.Device;
import com.push.lazyir.gui.Communicator;
import com.push.lazyir.managers.CommandManager;
import com.push.lazyir.managers.SettingManager;
import com.push.lazyir.managers.TcpConnectionManager;
import com.push.lazyir.managers.UdpBroadcastManager;
import com.push.lazyir.modules.clipboard.ClipboardJni;

/**
 * Created by buhalo on 12.03.17.
 */
public class BackgroundService {



    private static BackgroundService instance;

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
    private CommandManager commandManager;
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
        tcp.startServer(5667);
        tcp.startListening(5667);
    }

    public synchronized void eraseTcpConnections()
    {
        for (Device device : Device.getConnectedDevices().values()) {
            tcp.StopListening(device.getId());
        }

    }

    public static UdpBroadcastManager getUdp() {
        return getInstance().udp;
    }

    public void setUdp(UdpBroadcastManager udp) {
        this.udp = udp;
    }

    public static TcpConnectionManager getTcp() {
        return getInstance().tcp;
    }
    public void setCommandManager(CommandManager commandManager){this.commandManager = commandManager;}

    public static CommandManager getCommandManager(){return getInstance().commandManager;}

    public void setSettingManager(SettingManager settingManager){this.settingManager = settingManager;}

    public static SettingManager getSettingManager(){return getInstance().settingManager;}

    public void setTcp(TcpConnectionManager tcp) {
        this.tcp = tcp;
    }

}
