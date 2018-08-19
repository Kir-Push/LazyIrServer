package com.push.lazyir.modules.dbus;

import com.push.lazyir.devices.NetworkPackage;
import com.push.lazyir.modules.dbus.strategies.win.Strategy;
import com.push.lazyir.modules.dbus.strategies.win.Vlc;
import com.push.lazyir.service.managers.settings.SettingManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static com.push.lazyir.modules.dbus.Mpris.*;

/**
 * Created by buhalo on 16.08.17.
 */
public class Win implements OsStrategy {
    private ConcurrentHashMap<String,Strategy> strategies;
    private ConcurrentHashMap<String,String> pausedPlayersWin = new ConcurrentHashMap<>();
    private SettingManager settingManager;

    public Win(SettingManager settingManager) {
        this.settingManager = settingManager;
            strategies = new ConcurrentHashMap<>();
            strategies.put("vlc",new Vlc(settingManager));
    }

    @Override
    public void seek(NetworkPackage np) {
        setPosition(np);
    }

    @Override
    public void stop(NetworkPackage np) {
        String npValue = np.getValue(player);
        Strategy strategy = strategies.get(npValue.substring(0, 3));
        strategy.stop(npValue);
    }

    @Override
    public void next(NetworkPackage np) {
        String npValue = np.getValue(player);
        Strategy strategy = strategies.get(npValue.substring(0, 3));
        strategy.next(npValue);
    }

    @Override
    public void previous(NetworkPackage np) {
        String npValue = np.getValue(player);
        Strategy strategy = strategies.get(npValue.substring(0, 3));
        strategy.previous(npValue);
    }

    @Override
    public void playPause(NetworkPackage np) {
        String npValue = np.getValue(player);
        Strategy strategy = strategies.get(npValue.substring(0, 3));
        strategy.playPause(npValue);
    }

    @Override
    public void openUri(NetworkPackage np) {
        String npValue = np.getValue(player);
        Strategy strategy = strategies.get(npValue.substring(0, 3));
        strategy.openUri(npValue,np.getValue(openUri));
    }

    @Override
    public void setPosition(NetworkPackage np) {
        String npValue = np.getValue(player);
        Strategy strategy = strategies.get(npValue.substring(0, 3));
        strategy.seek(npValue,np.getValue(seek));
    }

    @Override
    public void setVolume(NetworkPackage np) {
        String npValue = np.getValue(player);
        Strategy strategy = strategies.get(npValue.substring(0, 3));
        strategy.setVolume(npValue,np.getValue(volume));
    }

    @Override
    public void loop(NetworkPackage np) {
        //todo
    }

    @Override
    public List<Player> getAllPlayers() {
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
        return playerList;
    }

    @Override
    public void playAll(String id) {
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

    @Override
    public void pauseAll(String id) {
        for (Strategy strategy : strategies.values()) {
            if (!strategy.checkStatus()) {
                strategy.initiate();
                List<Player> playerList = strategy.getGetAll().getPlayerList();
                for (Player pl : playerList) {
                    if (pl.getPlaybackStatus().equalsIgnoreCase("playing") || pl.getPlaybackStatus().equalsIgnoreCase("play"))
                    {
                        pausedPlayersWin.put(pl.getName(), pl.getPlaybackStatus());
                        strategy.playPause(pl.getName());
                    }
                }
            }
        }
    }

    @Override
    public void endWork() {
        for (Strategy strategy : strategies.values()) {
            strategy.stopConnection();
        }
        strategies.clear();
        pausedPlayersWin.clear();
    }
}
