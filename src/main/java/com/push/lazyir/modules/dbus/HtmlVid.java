package com.push.lazyir.modules.dbus;

import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;
import com.push.lazyir.modules.dbus.websocket.ServerController;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * strategy for htmlVideo in browser
 *  work though websocket
 */
@Slf4j
public class HtmlVid implements OsStrategy {
    private SetMultimap<String, String> pausedPlayersBrowser = MultimapBuilder.hashKeys().hashSetValues().build();
    private ServerController serverController;

    HtmlVid(ServerController serverController) {
        this.serverController = serverController;
    }



    @Override
    public void seek(MprisDto dto) {
        serverController.sendTime(dto.getJsIp(),dto.getDValue(),dto.getJsId());
    }

    @Override
    public void stop(MprisDto dto) {
        serverController.sendStatus(dto.getJsIp(),"pause",dto.getJsId());
    }

    @Override
    public void next(MprisDto dto) {
        serverController.sendStatus(dto.getJsIp(),"next",dto.getJsId());
    }

    @Override
    public void previous(MprisDto dto) {
        serverController.sendStatus(dto.getJsIp(),"previous",dto.getJsId());
    }

    @Override
    public void playPause(MprisDto dto) {
        serverController.sendStatus(dto.getJsIp(),"playPause",dto.getJsId());
    }

    @Override
    public void loop(MprisDto dto) {
        serverController.sendStatus(dto.getJsIp(),"loop",dto.getJsId());
    }

    @Override
    public void openUri(MprisDto dto) {
        //todo
    }

    @Override
    public void setPosition(MprisDto dto) {
        seek(dto);
    }

    @Override
    public void setVolume(MprisDto dto) {
        serverController.sendVolume(dto.getJsIp(),dto.getDValue(),dto.getJsId());
    }

    @Override
    public List<Player> getAllPlayers() {
        serverController.sendGetInfo();
       return serverController.getAll();
    }

    @Override
    public void playAll() {
       pausedPlayersBrowser.entries().forEach(pl -> serverController.sendStatus(pl.getKey(),"play",pl.getValue()));
        pausedPlayersBrowser.clear();
    }

    @Override
    public void pauseAll() {
        new Thread(()->{
            int count = 10;
            List<Player> allPlayers = new ArrayList<>();
            try {
                serverController.sendGetInfo();
                Thread.sleep(100);
                allPlayers = serverController.getAll();
            while(count > 0 && allPlayers.isEmpty()){
                serverController.sendGetInfo();
                Thread.sleep(100);
                allPlayers = serverController.getAll();
                count--;
            }
            } catch (InterruptedException e) {
                log.error("error in pauseAll",e);
                Thread.currentThread().interrupt();

            }finally {
                allPlayers.stream()
                        .filter(pl -> pl.getStatus().equalsIgnoreCase("playing"))
                        .forEach(pl -> {
                            serverController.sendStatus(pl.getIp(), "pause", pl.getId());
                            pausedPlayersBrowser.put(pl.getIp(), pl.getId());
                        });
            }
        }).start();
    }

    @Override
    public void endWork() {
        pausedPlayersBrowser.clear();
    }

}
