package com.push.lazyir.modules.dbus;

import com.push.lazyir.modules.dbus.strategies.win.Strategy;
import com.push.lazyir.modules.dbus.strategies.win.Vlc;
import com.push.lazyir.service.managers.settings.SettingManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Win implements OsStrategy {
    private ConcurrentHashMap<String,Strategy> strategies;
    private ConcurrentHashMap<String,String> pausedPlayersWin = new ConcurrentHashMap<>();

    public Win(SettingManager settingManager) {
        strategies = new ConcurrentHashMap<>();
        strategies.put("vlc",new Vlc(settingManager));
    }

    @Override
    public void seek(MprisDto dto) {
        setPosition(dto);
    }

    @Override
    public void stop(MprisDto dto) {
        String player = dto.getPlayer();
        getStrategy(player).stop(player);
    }

    @Override
    public void next(MprisDto dto) {
        String player = dto.getPlayer();
        getStrategy(player).next(player);
    }

    @Override
    public void previous(MprisDto dto) {
        String player = dto.getPlayer();
        getStrategy(player).previous(player);
    }

    @Override
    public void playPause(MprisDto dto) {
        String player = dto.getPlayer();
        getStrategy(player).playPause(player);
    }

    @Override
    public void openUri(MprisDto dto) {
        String player = dto.getPlayer();
        getStrategy(player).openUri(player,dto.getValue());
    }

    @Override
    public void setPosition(MprisDto dto) {
        String player = dto.getPlayer();
        getStrategy(player).seek(player,Double.toString(dto.getDValue()));
    }

    @Override
    public void setVolume(MprisDto dto) {
        String player = dto.getPlayer();
        getStrategy(player).setVolume(player,dto.getDValue());
    }

    @Override
    public void loop(MprisDto dto) {
        //todo
    }

    @Override
    public List<Player> getAllPlayers() {
        List<Player> playerList = new ArrayList<>();
        strategies.values().forEach(strategy -> {
            if (!strategy.checkStatus()) {
                strategy.initiate();
            }else{
                playerList.addAll(strategy.getGetAll());
            }
        });
        return playerList;
    }

    @Override
    public void playAll() {
        pausedPlayersWin.keySet().forEach(player -> {
            Strategy strategy = strategies.get(player.substring(0, 2));
            if(!strategy.checkStatus()){
                strategy.initiate();
            }
            if(strategy.getOnePlayer().getStatus().equalsIgnoreCase("pause")){
                strategy.playPause(player);
            }
        });
        pausedPlayersWin.clear();
    }

    @Override
    public void pauseAll() {
        strategies.values()
                .forEach(strategy -> {
                    if(!strategy.checkStatus()) {
                        strategy.initiate();
                    }
                    strategy.getGetAll()
                            .stream()
                            .filter(player -> {
                                String status = player.getStatus();
                                return  (status.equalsIgnoreCase("play") || status.equalsIgnoreCase("playing"));
                            })
                            .forEach(player -> {
                                String name = player.getName();
                                pausedPlayersWin.put(name, player.getStatus());
                                strategy.playPause(name);
                            });
                });
    }

    private Strategy getStrategy(String player){
        return strategies.get(player.substring(0, 3));
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
