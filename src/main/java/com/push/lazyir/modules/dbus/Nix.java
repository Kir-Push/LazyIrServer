package com.push.lazyir.modules.dbus;

import com.push.lazyir.Loggout;
import com.push.lazyir.devices.Device;
import com.push.lazyir.devices.NetworkPackage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static com.push.lazyir.modules.dbus.DbusCommandFabric.volume;
import static com.push.lazyir.modules.dbus.Mpris.*;

/**
 * Created by buhalo on 15.08.17.
 */
public class Nix implements OsStrategy {

    private ConcurrentHashMap<String,String> pausedPlayers = new ConcurrentHashMap<>();

    @Override
    public void seek(NetworkPackage np) {
        String playerValue = np.getValue(player);
        String seekValue = np.getValue(seek);
        Long ss = Long.parseLong(seekValue);
        ss *= 1000000;
        seekValue = Long.toString(ss);
        dbusSend(DbusCommandFabric.seek(playerValue, seekValue));

    }

    @Override
    public void stop(NetworkPackage np) {
        String playerValue = np.getValue(player);
        dbusSend(DbusCommandFabric.stop(playerValue));
    }

    @Override
    public void next(NetworkPackage np) {
        String playerValue = np.getValue(player);
        dbusSend(DbusCommandFabric.next(playerValue));
    }

    @Override
    public void previous(NetworkPackage np) {
        String playerValue = np.getValue(player);
        dbusSend(DbusCommandFabric.previous(playerValue));
    }

    @Override
    public void playPause(NetworkPackage np) {
        String playerValue = np.getValue(player);
        dbusSend(DbusCommandFabric.playPause(playerValue));
    }

    @Override
    public void openUri(NetworkPackage np) {
        String playerValue = np.getValue(player);
        String uri = np.getValue(openUri);
        dbusSend(DbusCommandFabric.openUri(playerValue,uri));
    }

    @Override
    public void setPosition(NetworkPackage np) {
        String playerValue = np.getValue(player);
        String pos = np.getValue("position");
        String path = np.getValue("path");
        dbusSend(DbusCommandFabric.setPosition(playerValue,path,pos));
    }

    @Override
    public void setVolume(NetworkPackage np) {
        String playerValue = np.getValue(player);
        String volumeVal = np.getValue(volume);
        dbusSend(DbusCommandFabric.setVolume(playerValue,volumeVal));
    }

    @Override
    public List<Player> getAllPlayers() {
        List<Player> playerList = new ArrayList<>();
        try {
            Process exec = Runtime.getRuntime().exec(getAllMpris);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(exec.getInputStream()))) {
                String temp;
                while ((temp = reader.readLine()) != null) {
                    String org = temp.substring(temp.indexOf("org"), temp.length() - 1);
                    playerList.add(fillPlayer(org));
                }
            }
        }catch (IOException e)
        {
            Loggout.e("Nix","getAllPlayers",e);
        }
        return playerList;
    }

    @Override
    public void playAll(String id) {
        for(String player : pausedPlayers.keySet())
        {
            String playStatus = getPlayStatus(DbusCommandFabric.getPlaybackstatus(player));
            if(playStatus.equalsIgnoreCase("paused"))
                dbusSend(DbusCommandFabric.playPause(player));
        }
        pausedPlayers.clear();
    }

    @Override
    public void pauseAll(String id) {
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
            Loggout.e("Nix", "pauseAll",e);
        }
    }


    private Process dbusSend(String message)
    {
        try {
            return Runtime.getRuntime().exec(message);
        } catch (Exception e) {
            Loggout.e("Nix","dbusSend",e);
            return null;
        }
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
                result.put("object path",temp.substring(temp.indexOf("object path ")+13,temp.length()-1)); // for delete quotes
            } else if(resultFromexec.get(i).contains("title") && ++i < resultFromexec.size())
            {
                String temp = resultFromexec.get(i);
                result.put("title",temp.substring(temp.indexOf("string ")+8,temp.length()-1));
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
        return result.substring(result.indexOf("string ") + 8,result.length()-1); //magic number is double string lenght
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
            Loggout.e("Nix","getResultFromExe",e);
        }
        return result;
    }

    @Override
    public void endWork() {
        pausedPlayers.clear();
    }
}
