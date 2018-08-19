package com.push.lazyir.modules.dbus;

import com.push.lazyir.Loggout;
import com.push.lazyir.devices.Cacher;
import com.push.lazyir.devices.Device;
import com.push.lazyir.devices.NetworkPackage;
import com.push.lazyir.modules.Module;
import com.push.lazyir.modules.dbus.websocket.ServerController;
import com.push.lazyir.service.main.BackgroundService;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.push.lazyir.service.main.MainClass.*;

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
     public static final String ALL_PLAYERS = "allPlayers";

     static final String[] getAllMpris = {"/bin/sh", "-c", DbusCommandFabric.getGetAll()};

    private static Lock lock = new ReentrantLock();
    private volatile static int callersCount = 0;

    private volatile static OsStrategy strategy;
    private volatile static OsStrategy browserStrategy;
    private ServerController serverController;

    @Inject
    public Mpris(BackgroundService backgroundService, Cacher cacher, ServerController serverController) {
        super(backgroundService, cacher);
        this.serverController = serverController;
        lock.lock();
        try {
            serverController.startServer();
            if (strategy == null && isUnix()) {
                strategy = new Nix();
            } else if ( strategy == null && isWindows()) {
                strategy = new Win(backgroundService.getSettingManager());
            }
            if (browserStrategy == null) {
                browserStrategy = new HtmlVid(serverController);
            }
        }finally {
            lock.unlock();
        }
    }

    @Override
    public void endWork() {
       lock.lock();
       try{
            if( backgroundService.getConnectedDevices().size() == 0) {
               serverController.stopServer();
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
            if(data.equals(ALL_PLAYERS))
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
        backgroundService.submitNewTask(()->{
            NetworkPackage np =  cacher.getOrCreatePackage(Mpris.class.getSimpleName(), ALL_PLAYERS);
            List<Player> playerList = new ArrayList<>();
            playerList.addAll(strategy.getAllPlayers());
            playerList.addAll(browserStrategy.getAllPlayers());
            Players players = new Players(playerList);
            if(players.getPlayerList().size()> 0)
            {
                np.setObject(ALL_PLAYERS, players);
                sendAnswer(np.getMessage());
            }
        });
    }

    private boolean browserCheck(String player)
    {
        return player.startsWith("js9876528:");
    }

    private void sendAnswer(String message) {
        backgroundService.sendToDevice(device.getId(),message);
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
