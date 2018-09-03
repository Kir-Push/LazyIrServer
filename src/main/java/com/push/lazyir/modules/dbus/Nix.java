package com.push.lazyir.modules.dbus;

import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 *  strategy for nix which have dbus
 */
@Slf4j
public class Nix implements OsStrategy {
    private static final String LENGTH = "length";
    private static final String TITLE = "title";
    private final String[] getAllMpris = {"/bin/sh", "-c", DbusCommandFabric.getGetAll()};
    private ConcurrentHashMap<String,String> pausedPlayers = new ConcurrentHashMap<>();

    @Override
    public void seek(MprisDto dto){
        long seekValue = (long)dto.getDValue();
        seekValue *= 1000000;
        dbusSend(DbusCommandFabric.seek(dto.getPlayer(), Long.toString(seekValue)));
    }

    @Override
    public void stop(MprisDto dto) {
        dbusSend(DbusCommandFabric.stop(dto.getPlayer()));
    }

    @Override
    public void next(MprisDto dto) {
        dbusSend(DbusCommandFabric.next(dto.getPlayer()));
    }

    @Override
    public void previous(MprisDto dto) {
        dbusSend(DbusCommandFabric.previous(dto.getPlayer()));
    }

    @Override
    public void playPause(MprisDto dto) {
        dbusSend(DbusCommandFabric.playPause(dto.getPlayer()));
    }

    @Override
    public void openUri(MprisDto dto) {
        dbusSend(DbusCommandFabric.openUri(dto.getPlayer(),dto.getValue()));
    }

    @Override
    public void setPosition(MprisDto dto) {
        dbusSend(DbusCommandFabric.setPosition(dto.getPlayer(),dto.getValue(),Double.toString(dto.getDValue())));
    }

    @Override
    public void setVolume(MprisDto dto) {
        dbusSend(DbusCommandFabric.setVolume(dto.getPlayer(),Double.toString(dto.getDValue())));
    }

    @Override
    public void loop(MprisDto dto) {
        //todo
    }

    @Override
    public List<Player> getAllPlayers() {
        List<Player> playerList = new ArrayList<>();
        Process exec = null;
        try {
            exec = Runtime.getRuntime().exec(getAllMpris);
            @Cleanup BufferedReader reader = new BufferedReader(new InputStreamReader(exec.getInputStream()));
            String temp;
            while ((temp = reader.readLine()) != null) {
                String org = temp.substring(temp.indexOf("org"), temp.length() - 1);
                playerList.add(fillPlayer(org));
            }
        }catch (IOException e) {
            log.error("error while getAllPlayers",e);
            if(exec != null){
                exec.destroyForcibly();
            }
        }
        return playerList;
    }

    @Override
    public void playAll() {
        pausedPlayers.keySet().stream()
                .filter(player -> getPlayStatus(DbusCommandFabric.getPlaybackstatus(player)).equalsIgnoreCase("paused"))
                .forEach(player -> dbusSend(DbusCommandFabric.playPause(player)));
        pausedPlayers.clear();
    }

    @Override
    public void pauseAll() {
        List<Player> allPlayers = getAllPlayers();
        allPlayers.stream()
                .filter(player -> player.getStatus().equalsIgnoreCase("playing"))
                .forEach(player -> {
                    String name = player.getName();
                    dbusSend(DbusCommandFabric.playPause(name));
                    pausedPlayers.put(name, player.getStatus());
                });
    }
    /**
     *
     * @return process or null, be careful
     */
    private Process dbusSend(String message) {
        try {
            return Runtime.getRuntime().exec(message);
        } catch (IOException e) {
            log.error("dbusSend error",e);
            return null;
        }
    }

    private Player fillPlayer(String org) {
        String status = getPlayStatus(DbusCommandFabric.getPlaybackstatus(org));
        Map<String, String> metadata = getMetadata(DbusCommandFabric.getMetadata(org));
        double volume = getVolume(DbusCommandFabric.getVolume(org));
        double currTime = getTime(DbusCommandFabric.getPosition(org));
        String lengthStr = metadata.get(LENGTH);
        double length = lengthStr == null ? 0 : Double.parseDouble(lengthStr);
        length = length < 1000000 ? 0 : length / 1000000;
        return new Player(org, status, metadata.get(TITLE), length, volume, currTime);
    }

    private double getTime(String org) {
        StringBuilder result = new StringBuilder();
        getResultFromExec(org).forEach(result::append);
        String time = result.substring(result.indexOf("int64 ") + 6);
        double currTime = time == null ? 0 : Double.parseDouble(time);
        return currTime < 1000000 ? 0 : currTime / 1000000;
    }

    private double getVolume(String org) {
        StringBuilder result = new StringBuilder();
        getResultFromExec(org).forEach(result::append);
        String volume = result.substring(result.indexOf("double ") + 7);
        return volume == null ? 0 : (Double.parseDouble(volume) * 100);
    }

    private Map<String, String> getMetadata(String metadata) {
        HashMap<String,String> result = new HashMap<>();
        List<String> resultFromexec = getResultFromExec(metadata);
        int i = 1;
        while (i < resultFromexec.size()){
            if(resultFromexec.get(i-1).contains("mpris:length")) {
                result.put(LENGTH,extractLength(resultFromexec.get(i)));
                i++;
            } else if(resultFromexec.get(i-1).contains(TITLE)) {
                result.put(TITLE,extractTitle(resultFromexec.get(i)));
                i++;
            }
            i++;
        }
        return result;
    }

    private String extractTitle(String temp) {
        return temp.substring(temp.indexOf("string ")+8,temp.length()-1);
    }

    private String extractLength(String temp) {
        int indx = temp.indexOf("int64 ");
        if(indx != -1) {
            return temp.substring(indx+6);
        }
        indx = temp.indexOf("double ");
        if(indx != -1){
            return temp.substring(indx+7);
        }
        return null;
    }


    private String getPlayStatus(String org) {
        StringBuilder result = new StringBuilder();
        getResultFromExec(org).forEach(result::append);
        return result.substring(result.indexOf("string ") + 8,result.length()-1); //magic number is double string LENGTH
    }

    private List<String> getResultFromExec(String org) {
        List<String> result = new ArrayList<>();
        Process process = dbusSend(org);
        if(process == null) {
            return result;
        }
        try{
        @Cleanup BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String temp;
            while ((temp = reader.readLine()) != null) {
                result.add(temp);
            }
        }catch (IOException e) {
            log.error("error in getResultFromExec - " + org,e);
            process.destroyForcibly();
        }
        return result;
    }

    @Override
    public void endWork() {
        pausedPlayers.clear();
    }
}
