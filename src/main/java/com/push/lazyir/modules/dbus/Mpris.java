package com.push.lazyir.modules.dbus;

import com.push.lazyir.Loggout;
import com.push.lazyir.MainClass;
import com.push.lazyir.devices.NetworkPackage;
import com.push.lazyir.managers.TcpConnectionManager;
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
    private static final String seek = "seek";
    private static final String next = "next";
    private static final String previous = "previous";
    private static final String stop = "stop";
    private static final String playPause = "playPause";
    private static final String player = "player";
    private static final String openUri = "openUri";
    private static final String setPosition = "setPosition";
    private static final String volume = "volume";
    private static final String allPlayers = "allPlayers";

    private static final String[] getAllMpris = {"/bin/sh", "-c", DbusCommandFabric.getGetAll()};

    private volatile static BrowserServer browserServer;
    private volatile static ConcurrentHashMap<String,Strategy> strategies;
    private static Lock lock = new ReentrantLock();
    private volatile static ConcurrentHashMap<String,String> pausedPlayers = new ConcurrentHashMap<>();
    private volatile static ConcurrentHashMap<String,String> pausedPlayersBrowser = new ConcurrentHashMap<>();
    private volatile static ConcurrentHashMap<String,String> pausedPlayersWin = new ConcurrentHashMap<>();
    private volatile static int callersCount = 0;

    public Mpris() {
        super();
            BrowserServer localInstance = browserServer;
            if(localInstance == null) {
                synchronized (BrowserServer.class) {
                    localInstance = browserServer;
                    if (localInstance == null) {
                        localInstance = browserServer = new BrowserServer();
                        try {
                            browserServer.start();
                        } catch (Exception e) {
                            Loggout.e("Mpris",e.toString());
                        }
                        }
                    }
                }
                if(isWindows())
                {
                    if(strategies == null)
                    {
                        strategies = new ConcurrentHashMap<>();
                    }
                    if(strategies.size() == 0)
                    {
                        strategies.put("vlc",new Vlc());
                    }
                }
    }

    @Override
    public void execute(NetworkPackage np) {
        if(isUnix()){
            isDbus(np);
        }
        else if(isWindows())
        {
            nonDbus(np);
        }
    }

    private void isDbus(NetworkPackage np) {
        try {
            String data = np.getData();
            switch (data) {
                case seek:
                    seek(np);
                    break;
                case next:
                    next(np);
                    break;
                case stop:
                    stop(np);
                    break;
                case previous:
                    previous(np);
                    break;
                case playPause:
                    playPause(np);
                    break;
                case openUri:
                    openUri(np);
                    break;
                case setPosition:
                    setPosition(np);
                    break;
                case volume:
                    setVolume(np);
                    break;
                case allPlayers:
                    getAllPlayers();
                    break;
            }
        }catch (Exception e)
        {
            Loggout.e("Mpris",e.toString());
        }
    }

    private void setVolume(NetworkPackage np) {
        String playerValue = np.getValue(player);
        String volumeVal = np.getValue(volume);
        if(browserCheck(playerValue))
            PopupEndpoint.sendVolume(playerValue.substring(10),volumeVal);
        else
        dbusSend(DbusCommandFabric.setVolume(playerValue,volumeVal));
    }

    private void setPosition(NetworkPackage np) {
        String playerValue = np.getValue(player);
        String pos = np.getValue("position");
        String path = np.getValue("path");
        if(browserCheck(playerValue))
            PopupEndpoint.sendTime(playerValue,pos);
        else
        dbusSend(DbusCommandFabric.setPosition(playerValue,path,pos));
    }

    private void openUri(NetworkPackage np) {
        String playerValue = np.getValue(player);
        String uri = np.getValue(openUri);
        if(!browserCheck(playerValue))
            dbusSend(DbusCommandFabric.openUri(playerValue,uri));
    }

    private void playPause(NetworkPackage np) {
        String playerValue = np.getValue(player);
        if(browserCheck(playerValue))
            PopupEndpoint.sendStatus(playerValue.substring(10),playPause);
        else
        dbusSend(DbusCommandFabric.playPause(playerValue));
    }

    private void previous(NetworkPackage np) {
        String playerValue = np.getValue(player);
        if(!browserCheck(playerValue))
            dbusSend(DbusCommandFabric.previous(playerValue));
    }

    private void stop(NetworkPackage np) {
        String playerValue = np.getValue(player);
        if(!browserCheck(playerValue))
            dbusSend(DbusCommandFabric.stop(playerValue));
        else
            PopupEndpoint.sendStatus(playerValue.substring(10),"pause");
    }

    private void next(NetworkPackage np) {
        String playerValue = np.getValue(player);
        if(browserCheck(playerValue))
            PopupEndpoint.sendStatus(playerValue.substring(10),"next");
        else
        dbusSend(DbusCommandFabric.next(playerValue));
    }

    private void seek(NetworkPackage np) {
        String playerValue = np.getValue(player);
        String seekValue = np.getValue(seek);
        if(browserCheck(playerValue))
            PopupEndpoint.sendTime(playerValue.substring(10), seekValue);
        else {
            Long ss = Long.parseLong(seekValue);
            ss *= 1000000;
            seekValue = Long.toString(ss);
            dbusSend(DbusCommandFabric.seek(playerValue, seekValue));
        }
    }

    private void getAllPlayers() {
        executorService.submit(()->{
            NetworkPackage np = new NetworkPackage(Mpris.class.getSimpleName(),allPlayers);
            List<Player> playerList = new ArrayList<>();
            List<Player> playersFromBrowser = null;
            SettableFuture<List<Player>> browsr = null;
            try {
                Process exec = Runtime.getRuntime().exec(getAllMpris);
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(exec.getInputStream()))) {
                    String temp;
                    while((temp = reader.readLine()) != null)
                    {
                        String org = temp.substring(temp.indexOf("org"), temp.length() - 1);
                        playerList.add(fillPlayer(org));
                    }
                }
                try {
                    browsr = PopupEndpoint.getAll();
                    playerList.addAll(browsr.get(200, TimeUnit.MILLISECONDS)); // from browser
                    System.out.println(Thread.currentThread());
                } catch (Exception e) {
                    Loggout.e("Mpris",e.toString());
                    browsr.cancel(true);
                }
                Players players = new Players(playerList);
                if(players.getPlayerList().size()> 0)
                {
                    np.setObject(allPlayers, players);
                    sendAnswer(np.getMessage());
                }
            } catch (Exception e) {
                Loggout.e("Mpris",e.toString());
            }
        });
    }

    private Player fillPlayer(String org) {
        try {
            String status = getPlayStatus(DbusCommandFabric.getPlaybackstatus(org));
            HashMap<String, String> metadata = getMetadata(DbusCommandFabric.getMetadata(org));
            String volumeStr = getVolume(DbusCommandFabric.getVolume(org));
            String currTimeStr = getTime(DbusCommandFabric.getPosition(org));
            String lenghtStr = metadata.get("lenght");
            double volume = volumeStr == null ? 0 : (Double.parseDouble(volumeStr) * 100);
            double currTime = currTimeStr == null ? 0 : ((currTime = Double.parseDouble(currTimeStr)) < 1000000 ? 0 : currTime / 1000000);
            double lenght = lenghtStr == null ? 0 : ((lenght = Double.parseDouble(lenghtStr)) < 1000000 ? 0 : lenght / 1000000);
            String readyTime = ((int)currTime) / 60 + ":" + ((int)currTime) % 60 + " / " + ((int)lenght)/60 + ":" + ((int)lenght) % 60;
            return new Player(org, status, metadata.get("title"), lenght, volume, currTime, readyTime);
        }catch (Exception e){
            return new Player(org,"","",0,0,0,"0/0");
        }
    }

    private String getTime(String org) {
        StringBuilder result = new StringBuilder();
        getResultFromexec(org).forEach(result::append);
        return result.substring(result.indexOf("int64 ") + 6);
    }

    private String getVolume(String org) {
        StringBuilder result = new StringBuilder();
        getResultFromexec(org).forEach(result::append);
        return result.substring(result.indexOf("double ") + 7);
    }

    private HashMap<String, String> getMetadata(String metadata) {
        HashMap<String,String> result = new HashMap<>();
        result.put("artist","");
        List<String> resultFromexec = getResultFromexec(metadata);
        for(int i=0;i<resultFromexec.size();i++)
        {
           if(resultFromexec.get(i).contains("mpris:length") && ++i < resultFromexec.size())
           {
               String temp = resultFromexec.get(i);
               int indx = temp.indexOf("int64 ");
               if(indx != -1)
                   result.put("lenght",temp.substring(indx+6));
               else if((indx = temp.indexOf("double ")) != -1)
               {
                   result.put("lenght",temp.substring(indx+7));
               }
           } else if(resultFromexec.get(i).contains("mpris:trackid") && ++i < resultFromexec.size())
           {
               String temp = resultFromexec.get(i);
               result.put("object path",temp.substring(temp.indexOf("object path ")+12));
           } else if(resultFromexec.get(i).contains("title") && ++i < resultFromexec.size())
           {
               String temp = resultFromexec.get(i);
               result.put("title",temp.substring(temp.indexOf("string ")+7));
           } else if(resultFromexec.get(i).contains("artist") && (i+=2) < resultFromexec.size())
           {
               String temp = resultFromexec.get(i);
               try {
                   result.put("artist",temp.trim().substring(temp.indexOf("string ") + 7));
               }catch (NullPointerException e) {
                   result.put("artist","");
               }
           }
        }
        return result;
    }


    private String getPlayStatus(String org) {
        StringBuilder result = new StringBuilder();
        getResultFromexec(org).forEach(result::append);
        return result.substring(result.indexOf("string ") + 7); //magic number is double string lenght
    }

    private List<String> getResultFromexec(String org)
    {
        List<String> result = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(dbusSend(org).getInputStream()))) {
            String temp;
            while ((temp = reader.readLine()) != null) {
                result.add(temp);
            }
        }catch (IOException e) {
            Loggout.e("Mpris",e.toString());
        }
        return result;
    }

    private boolean browserCheck(String player)
    {
        return player.startsWith("js9876528:");
    }

    private void sendAnswer(String message)
    {
        BackgroundService.getTcp().sendCommandToServer(device.getId(),message);
    }


    private Process dbusSend(String message)
    {
        try {
          return Runtime.getRuntime().exec(message);
        } catch (Exception e) {
            Loggout.e("Mpris",e.toString());
            return null;
        }
    }

    private void nonDbus(NetworkPackage np) {
        String data = np.getData();
        switch (data) {
            case seek:
                seekWin(np);
                break;
            case next:
                nextWin(np);
                break;
            case stop:
                stopWin(np);
                break;
            case previous:
                previousWin(np);
                break;
            case playPause:
                playPauseWin(np);
                break;
            case openUri:
                openUriWin(np);
                break;
            case setPosition:
                setPositionWin(np);
                break;
            case volume:
                setVolumeWin(np);
                break;
            case allPlayers:
                getAllWin();
                break;
        }

    }

    private void seekWin(NetworkPackage np) {
        setPositionWin(np);
    }

    private void nextWin(NetworkPackage np) {
        String npValue = np.getValue(player);
        Strategy strategy = strategies.get(npValue.substring(0, 3));
        strategy.next(npValue);
    }

    private void stopWin(NetworkPackage np) {
        String npValue = np.getValue(player);
        Strategy strategy = strategies.get(npValue.substring(0, 3));
        strategy.stop(npValue);
    }

    private void previousWin(NetworkPackage np) {
        String npValue = np.getValue(player);
        Strategy strategy = strategies.get(npValue.substring(0, 3));
        strategy.previous(npValue);
    }

    private void playPauseWin(NetworkPackage np) {
        String npValue = np.getValue(player);
        Strategy strategy = strategies.get(npValue.substring(0, 3));
        strategy.playPause(npValue);
    }

    private void openUriWin(NetworkPackage np) {
        String npValue = np.getValue(player);
        Strategy strategy = strategies.get(npValue.substring(0, 3));
        strategy.openUri(npValue,np.getValue(openUri));
    }

    private void setPositionWin(NetworkPackage np) {
        String npValue = np.getValue(player);
        Strategy strategy = strategies.get(npValue.substring(0, 3));
        strategy.seek(npValue,np.getValue(seek));
    }

    private void setVolumeWin(NetworkPackage np) {
        String npValue = np.getValue(player);
        Strategy strategy = strategies.get(npValue.substring(0, 3));
        strategy.setVolume(npValue,np.getValue(volume));
    }

    private void getAllWin()
    {
        executorService.submit(()->{
            NetworkPackage np = new NetworkPackage(Mpris.class.getSimpleName(),allPlayers);
            List<Player> playerList = new ArrayList<>();
            for (Strategy strategy : strategies.values()) {
                if (!strategy.checkStatus()) {
                    strategy.Tryinitiate();
                }
                else
                {
                    playerList.addAll(strategy.getGetAll().getPlayerList());
                }
            }
            SettableFuture<List<Player>>  browsr = null;
            try {
                browsr = PopupEndpoint.getAll();
                playerList.addAll(browsr.get(200, TimeUnit.MILLISECONDS)); // from browser
            } catch (Exception e) {
                Loggout.e("Mpris",e.toString());
                if(browsr!= null)
                browsr.cancel(true);
            }
            Players players = new Players(playerList);
            if(players.getPlayerList().size()> 0)
            {

                np.setObject(allPlayers, players);
                sendAnswer(np.getMessage());
            }
        });
    }

    public void pauseAll(String id) {
        lock.lock();
        try {
            callersCount++;
            if (MainClass.isUnix()) {
                try {
                    Process exec = Runtime.getRuntime().exec(getAllMpris);
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(exec.getInputStream()))) {
                        String temp;
                        while ((temp = reader.readLine()) != null) {
                            String org = temp.substring(temp.indexOf("org"), temp.length() - 1);
                            Player player = fillPlayer(org);
                            if (player.getPlaybackStatus().equalsIgnoreCase("playing")) {
                                dbusSend(DbusCommandFabric.playPause(player.getName()));
                                pausedPlayers.put(player.getName(), player.getPlaybackStatus());
                            }
                        }
                    }

                } catch (IOException e) {
                    Loggout.e("Mpris", e.toString());
                }
            } else if (MainClass.isWindows()) {
                for (Strategy strategy : strategies.values()) {
                    if (!strategy.checkStatus()) {
                        strategy.initiate();
                        List<Player> playerList = strategy.getGetAll().getPlayerList();
                        for (Player pl : playerList) {
                            if (pl.getPlaybackStatus().equalsIgnoreCase("playing")) //todo проовервь везде ли playing статус а не play например и pause тоже (может быть paused например)
                            {
                                pausedPlayersWin.put(pl.getName(), pl.getPlaybackStatus());
                                strategy.playPause(pl.getName());
                            }
                        }
                    }

                }
            }
            try {
                List<Player> playerList = PopupEndpoint.getAll().get(200, TimeUnit.MILLISECONDS);
                if (playerList != null) {
                    for (Player pl : playerList) {
                        if (pl.getPlaybackStatus().equalsIgnoreCase("playing")) {
                            String substring = pl.getName().substring(10);
                            PopupEndpoint.sendStatus(substring, "pause");
                            pausedPlayersBrowser.put(substring, pl.getPlaybackStatus());
                        }
                    }
                }
            } catch (Exception e) {
                Loggout.e("Mpris", e.toString());
            }
        }finally {
            lock.unlock();
        }
    }

    public void playAll(String id) {
        lock.lock();
        try {
            callersCount--;
            if (callersCount > 0) {
               return;

            }
            if(MainClass.isWindows())
            {
                for (String s : pausedPlayersWin.keySet()) {
                    String strategyType = s.substring(0, 2);
                    Strategy strategy = strategies.get(strategyType);
                    if (!strategy.checkStatus())
                        strategy.initiate();
                    String playbackStatus = strategy.getOnePlayer().getPlaybackStatus();
                    if(playbackStatus.equalsIgnoreCase("pause"))
                    strategy.playPause(s);
                }
                pausedPlayersWin.clear();
            }
            else if(MainClass.isUnix())
            {
                for(String player : pausedPlayers.keySet())
                {
                    String playStatus = getPlayStatus(player);
                    if(playStatus.equalsIgnoreCase("pause"))
                    {
                        dbusSend(DbusCommandFabric.playPause(player));
                    }
                }
                pausedPlayers.clear();
            }

            for (String s : pausedPlayersBrowser.keySet()) {
                PopupEndpoint.sendStatus(s,"play");
            }

        }finally {
            lock.unlock();
        }
    }
}
