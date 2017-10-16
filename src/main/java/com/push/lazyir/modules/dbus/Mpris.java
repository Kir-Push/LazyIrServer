package com.push.lazyir.modules.dbus;

import com.push.lazyir.Loggout;
import com.push.lazyir.MainClass;
import com.push.lazyir.devices.Device;
import com.push.lazyir.devices.NetworkPackage;
import com.push.lazyir.modules.Module;
import com.push.lazyir.modules.dbus.strategies.win.Strategy;
import com.push.lazyir.modules.dbus.strategies.win.Vlc;
import com.push.lazyir.modules.dbus.websocket.BrowserServer;
import com.push.lazyir.modules.dbus.websocket.PopupEndpoint;
import com.push.lazyir.service.BackgroundService;
import com.push.lazyir.utils.SettableFuture;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.push.lazyir.MainClass.*;

/**
 * Created by buhalo on 22.07.17.
 */
public class Mpris extends Module {
     static final String seek = "seek";
     static final String next = "next";
     static final String previous = "previous";
     static final String stop = "stop";
     static final String playPause = "playPause";
     static final String player = "player";
     static final String openUri = "openUri";
     static final String setPosition = "setPosition";
     static final String volume = "volume";
     public static final String allPlayers = "allPlayers";

     static final String[] getAllMpris = {"/bin/sh", "-c", DbusCommandFabric.getGetAll()};

    private volatile static BrowserServer browserServer;
    private static Lock lock = new ReentrantLock();
    private volatile static int callersCount = 0;

    private volatile static OsStrategy strategy;
    private volatile static OsStrategy browserStrategy;

    public Mpris() {
        super();
        lock.lock();
        try {
            if (browserServer == null) {
                browserServer = new BrowserServer();
                        try {
                            browserServer.start();
                        } catch (Exception e) {
                            Loggout.e("Mpris", "Constructor-BrowserServer Start",e);
                        }
                    }
            if (strategy == null && isUnix()) {
                strategy = new Nix();
            } else if ( strategy == null && isWindows()) {
                strategy = new Win();
            }
            if (browserStrategy == null) {
                browserStrategy = new HtmlVid();
            }
        }finally {
            lock.unlock();
        }
    }


    @Override
    public void endWork() {
       lock.lock();
       try{
            if( Device.getConnectedDevices().size() == 0) {
                if (browserServer != null) {
                    browserServer.stop();
                }
                strategy.endWork();
                browserStrategy.endWork();
            }}finally {
           lock.unlock();
       }
    }

    @Override
    public void execute(NetworkPackage np) {
        determine(np);
    }

    private void determine(NetworkPackage np) {
        try {
            OsStrategy tempStrat;
            String data = np.getData();
            if(data.equals(allPlayers))
            {
                getAllPlayers();
                return;
            }
            if(browserCheck(np.getValue(player)))
            {
                tempStrat =  browserStrategy;
            }
            else
                tempStrat = strategy;

                switch (data) {
                    case seek:
                        tempStrat.seek(np);
                        break;
                    case next:
                        tempStrat.next(np);
                        break;
                    case stop:
                        tempStrat.stop(np);
                        break;
                    case previous:
                        tempStrat.previous(np);
                        break;
                    case playPause:
                        tempStrat.playPause(np);
                        break;
                    case openUri:
                        tempStrat.openUri(np);
                        break;
                    case setPosition:
                        tempStrat.setPosition(np);
                        break;
                    case volume:
                        tempStrat.setVolume(np);
                        break;
                    default:
                            break;
                }

        }catch (Exception e)
        {
            Loggout.e("Mpris","Determine",e);
        }
    }



    private void getAllPlayers() {
        executorService.submit(()->{
            NetworkPackage np =  NetworkPackage.Cacher.getOrCreatePackage(Mpris.class.getSimpleName(),allPlayers);
            List<Player> playerList = new ArrayList<>();
            playerList.addAll(strategy.getAllPlayers());
            playerList.addAll(browserStrategy.getAllPlayers());
            Players players = new Players(playerList);
            if(players.getPlayerList().size()> 0)
            {
                np.setObject(allPlayers, players);
                sendAnswer(np.getMessage());
            }
        });
    }

    private boolean browserCheck(String player)
    {
        return player.startsWith("js9876528:");
    }

    private void sendAnswer(String message)
    {
        BackgroundService.getTcp().sendCommandToServer(device.getId(),message);
    }

    public void pauseAll(String id) {
        lock.lock();
        try {
            callersCount++;
           strategy.pauseAll(id);
           browserStrategy.pauseAll(id);
        }finally {
            lock.unlock();
        }
    }

    public void playAll(String id) {
        lock.lock();
        try {
            callersCount--;
            if (callersCount > 0)
               return;
            strategy.playAll(id);
            browserStrategy.playAll(id);
        }finally {
            lock.unlock();
        }
    }

}
