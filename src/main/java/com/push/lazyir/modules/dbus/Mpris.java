package com.push.lazyir.modules.dbus;

import com.push.lazyir.Loggout;
import com.push.lazyir.devices.NetworkPackage;
import com.push.lazyir.managers.TcpConnectionManager;
import com.push.lazyir.modules.Module;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Created by buhalo on 09.04.17.
 */
public class Mpris extends Module {

    public final static String FIRST_PART_DEST = "dbus-send --session --dest=";
    public final static String FREEDESKTOP_DBUS = "org.freedesktop.DBus";
    public final static String MEDIUM_PART = "--type=method_call --print-reply /org/mpris/MediaPlayer2 ";
    public final static String GET_ALL_MPRIS = FIRST_PART_DEST +  FREEDESKTOP_DBUS + " --type=method_call --print-reply /org/freedesktop/DBus org.freedesktop.DBus.ListNames | grep org.mpris.MediaPlayer2";
    public final static String PLAYER_INTERFACE = "org.mpris.MediaPlayer2.Player";
    public final static String SEEK = ".Seek int64:";
    public final static String NEXT = ".Next";
    public final static String PREVIOUS = ".Previous";
    public final static String PLAYPAUSE = ".PlayPause";
    public final static String STOP = ".Stop";
    public final static String OPENURI = ".OpenUri string:";
    public final static String SETPOSITION = ".SetPosition Object Path:";

    public final static String VOLUME = ".Volume double:";
    public final static String PLAYBACKSTATUS = ".PlaybackStatus string:";

    public final static String MONITOR = "dbus-monitor --session \"path=/org/mpris/MediaPlayer2,member=PropertiesChanged\" --monitor ";

    public final static String PLAYER = "player";

    public final static String GET_ALL_INFO = "allInfo";

    public final static String ALL_PLAYERS = "allPlayers";

    private final static String SETVOLUME = " --type=method_call --print-reply /org/mpris/MediaPlayer2  org.freedesktop.DBus.Properties.Set string:org.mpris.MediaPlayer2.Player string:Volume variant:double:";

    public static Map<String,String> players = new HashMap<>();

    private static Set<String> Paused = new HashSet<>();

    private static int NUMBER_OF_CALLERS = 0;

    private static Set<String> callers = new HashSet<>();

    @Override
    public void execute(NetworkPackage np) {
        String data = np.getData();
        switch (data)
        {
            case SEEK:
                seek(np);
                break;
            case NEXT:
                next(np);
                break;
            case PREVIOUS:
                previous(np);
                break;
            case PLAYPAUSE:
                playPause(np);
                break;
            case STOP:
                stop(np);
                break;
            case OPENURI:
                openUri(np);
                break;
            case SETPOSITION:
                setPostion(np);
                break;
            case VOLUME:
                setVolume(np);
                break;
            case PLAYBACKSTATUS:
                sendPlaybackStatus(np);
                break;
            case ALL_PLAYERS:
                sendMpris();
                break;
            case GET_ALL_INFO:
                sendAllInfo(np);
                break;
            default:
                break;

        }
    }

    public synchronized static void pauseAll(String id)
    {
        List<String> mpris = new ArrayList<>();
        try {
            String[] cmd = {
                    "/bin/sh",
                    "-c",
                    GET_ALL_MPRIS};
            Process exec = Runtime.getRuntime().exec(cmd);
            System.out.println(cmd);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(exec.getInputStream()))) {
                String temp;
                while((temp = reader.readLine()) != null)
                {
                    String org = temp.substring(temp.indexOf("org"), temp.length() - 1);
                    mpris.add(org);
                }

            }

        } catch (Exception e) {
            Loggout.e("Mpris",e.toString());

        }
        callers.add(id);
        for(String name : mpris)
        {
            String getPlayback = FIRST_PART_DEST + name + " --type=method_call --print-reply /org/mpris/MediaPlayer2 org.freedesktop.DBus.Properties.Get string:org.mpris.MediaPlayer2.Player string:PlaybackStatus";
            String playback = getPlaybackStatus(getPlayback);
            if(playback.equals("\"Playing\"") || playback.equals("Playing")) {
                Paused.add(name);
                String result = FIRST_PART_DEST + name + " " + MEDIUM_PART + PLAYER_INTERFACE + ".Pause";
                dbusSend(result);
            }
        }
    }

    public synchronized static void playAll(String id)
    {
       callers.remove(id);
      if(callers.size() == 0) {
          for (String name : Paused) {
              String result = FIRST_PART_DEST + name + " " + MEDIUM_PART + PLAYER_INTERFACE + ".Play";
              dbusSend(result);
          }
          Paused.clear();

      }
    }

    private void sendAllInfo(NetworkPackage np) {
        NetworkPackage infopck = new NetworkPackage(Mpris.class.getSimpleName(),GET_ALL_INFO);
        String getVolume = FIRST_PART_DEST + np.getValue(PLAYER) + " --type=method_call --print-reply /org/mpris/MediaPlayer2 org.freedesktop.DBus.Properties.Get string:org.mpris.MediaPlayer2.Player string:Volume";
        String volumeFromDbus = getVolumeFromDbus(getVolume);
        infopck.setValue(VOLUME,volumeFromDbus);
        String metadata = FIRST_PART_DEST + np.getValue(PLAYER) + " --type=method_call --print-reply /org/mpris/MediaPlayer2 org.freedesktop.DBus.Properties.Get string:org.mpris.MediaPlayer2.Player string:Metadata";
        infopck.setValue(PLAYER,np.getValue(PLAYER));
        getMetadata(infopck,metadata);
        String getPlayback = FIRST_PART_DEST + np.getValue(PLAYER) + " --type=method_call --print-reply /org/mpris/MediaPlayer2 org.freedesktop.DBus.Properties.Get string:org.mpris.MediaPlayer2.Player string:PlaybackStatus";
        String playback = getPlaybackStatus(getPlayback);
        infopck.setValue("playbackStatus",playback);
        String currTime = FIRST_PART_DEST + np.getValue(PLAYER) + " --type=method_call --print-reply /org/mpris/MediaPlayer2 org.freedesktop.DBus.Properties.Get string:org.mpris.MediaPlayer2.Player string:Position";
        String time = getTime(currTime);
        infopck.setValue("currTime",time);
        sendAnswer(infopck.getMessage());
    }

    private NetworkPackage getAllInfo(String player)
    {
        NetworkPackage infopck = new NetworkPackage(Mpris.class.getSimpleName(),GET_ALL_INFO);
        String getVolume = FIRST_PART_DEST + player + " --type=method_call --print-reply /org/mpris/MediaPlayer2 org.freedesktop.DBus.Properties.Get string:org.mpris.MediaPlayer2.Player string:Volume";
        String volumeFromDbus = getVolumeFromDbus(getVolume);
        infopck.setValue(VOLUME,volumeFromDbus);
        String metadata = FIRST_PART_DEST + player + " --type=method_call --print-reply /org/mpris/MediaPlayer2 org.freedesktop.DBus.Properties.Get string:org.mpris.MediaPlayer2.Player string:Metadata";
        infopck.setValue(PLAYER,player);
        getMetadata(infopck,metadata);
        String getPlayback = FIRST_PART_DEST + player + " --type=method_call --print-reply /org/mpris/MediaPlayer2 org.freedesktop.DBus.Properties.Get string:org.mpris.MediaPlayer2.Player string:PlaybackStatus";
        String playback = getPlaybackStatus(getPlayback);
        infopck.setValue("playbackStatus",playback);
        String currTime = FIRST_PART_DEST + player + " --type=method_call --print-reply /org/mpris/MediaPlayer2 org.freedesktop.DBus.Properties.Get string:org.mpris.MediaPlayer2.Player string:Position";
        String time = getTime(currTime);
        infopck.setValue("currTime",time);
        return infopck;
    }

    private String getTime(String currTime) {
        String result = "";
        try {
            Process exec = Runtime.getRuntime().exec(currTime);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(exec.getInputStream()))) {
                String temp;
                while((temp = reader.readLine()) != null)
                {
                    result += temp;
                }
            }
            result = result.substring(result.indexOf("int64 ")+6, result.length()); //magic number is double string lenght
        } catch (IOException e) {
            Loggout.e("Mpris",e.toString());
        }
        return result;
    }

    private static synchronized String getPlaybackStatus(String np) {
        String volume = "";
        try {
            String result = "";
            Process exec = Runtime.getRuntime().exec(np);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(exec.getInputStream()))) {
                String temp;
                while((temp = reader.readLine()) != null)
                {
                    result += temp;
                }
            }
            volume = result.substring(result.indexOf("string ")+7, result.length()); //magic number is double string lenght
        } catch (IOException e) {
            Loggout.e("Mpris",e.toString());
        }
        return volume;
    }

    private void getMetadata(NetworkPackage np, String metadata) {
     //   np.setValue(PLAYER,"org.mpris.MediaPlayer2.amarok");
        List<String> metaList = new ArrayList<>();
        try {
            String commad = FIRST_PART_DEST + np.getValue(PLAYER) + " --print-reply /org/mpris/MediaPlayer2 org.freedesktop.DBus.Properties.Get string:org.mpris.MediaPlayer2.Player string:Metadata";

            Process exec = Runtime.getRuntime().exec(commad);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(exec.getInputStream()))) {
                String temp;
                while((temp = reader.readLine()) != null)
                {
                    metaList.add(temp);
                }
            }
            np.setValue("artist","");
            for(int i = 0;i<metaList.size();i++)
            {
                String m = metaList.get(i);
                if(m.contains("mpris:length"))
                {
                    i++;
                    String m2 = metaList.get(i);
                    if(m2.contains("int64"))
                    m2 = m2.substring(m2.indexOf("int64 ")+6,m2.length());
                    else if(m2.contains("double")) {
                        m2 = m2.substring(m2.indexOf("double ") + 7,m2.length());
                    }
                    np.setValue("lenght",m2);
                }
                else if(m.contains("mpris:trackid"))
                {
                    i++;
                    String m2 = metaList.get(i);
                    m2 = m2.substring(m2.indexOf("object path ")+12,m2.length());
                    np.setValue("object path",m2);
                }
                else if(m.contains("title"))
                {
                    i++;
                    String m2 = metaList.get(i);
                    m2 = m2.substring(m2.indexOf("string ")+7,m2.length());
                    np.setValue("title",m2);
                }

                if(m.contains("artist"))
                {
                    i++;
                    i++;
                    String m2 = metaList.get(i);
                    try {
                        m2 = m2.trim();
                        m2 = m2.substring(m2.indexOf("string ") + 7, m2.length());
                    }catch (NullPointerException e)
                    {

                        m2 = "";
                    }
                    np.setValue("artist",m2);
                }
            }
        } catch (IOException e) {
            Loggout.e("Mpris",e.toString());
        }
    }

    private String getVolumeFromDbus(String command) {
        String volume = "";
        try {
            String result = "";
            Process exec = Runtime.getRuntime().exec(command);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(exec.getInputStream()))) {
                String temp;
                while((temp = reader.readLine()) != null)
                {
                    result += temp;
                }
            }
            volume = result.substring(result.indexOf("double ")+7, result.length()); //magic number is double string lenght
        } catch (IOException e) {
            Loggout.e("Mpris",e.toString());
        }
        return volume;
    }

    public void sendMpris()
    {
        List<String> mpris = new ArrayList<>();
        try {
            String[] cmd = {
                    "/bin/sh",
                    "-c",
            GET_ALL_MPRIS};
            Process exec = Runtime.getRuntime().exec(cmd);
            System.out.println(cmd);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(exec.getInputStream()))) {
                String temp;
                while((temp = reader.readLine()) != null)
                {
                    String org = temp.substring(temp.indexOf("org"), temp.length() - 1);
                    mpris.add(org);
                }

            }

        } catch (Exception e) {
            Loggout.e("Mpris",e.toString());

        }
        NetworkPackage np = new NetworkPackage(Mpris.class.getSimpleName(),ALL_PLAYERS);
        List<Player> playerList = new ArrayList<>();
        for(String mp : mpris)
        {
            NetworkPackage data = getAllInfo(mp);
            fillPlayer(data,mp);
            playerList.add(fillPlayer(data,mp));
        }
        Players players = new Players(playerList);
        if(players.getPlayerList().size()> 0) {
            np.setObject(ALL_PLAYERS, players);
            String message = np.getMessage();
            sendAnswer(message);
        }

    }

    private Player fillPlayer(NetworkPackage data,String name) {
        try {

            String status = data.getValue("playbackStatus");
            String titleArtist = data.getValue("artist") + " : " + data.getValue("title");
            int lenght = extractLenght(data.getValue("lenght"));
            int volume = extractVolume(data.getValue(VOLUME));
            int currTime = extractCurrTime(data.getValue("currTime"));

            StringBuilder readyTime = new StringBuilder();
            readyTime.append(currTime / 60);
            readyTime.append(":");
            readyTime.append(currTime % 60);
            readyTime.append(" / ");
            readyTime.append(lenght / 60);
            readyTime.append(":");
            readyTime.append(lenght % 60);
            return new Player(name, status, titleArtist, lenght, volume, currTime, readyTime.toString());
        }
        catch (Exception e)
        {
            return new Player(name,"","",0,0,0,"0/0");
        }
    }

    private int extractCurrTime(String currTime) {
      return extractLenght(currTime);
    }

    private int extractVolume(String value) {
        if(value == null)
        {
            return 0;
        }
        return (int)(Double.parseDouble(value)*100);
    }

    private int extractLenght(String lenght) {
        if(lenght == null)
        {
            return 0;
        }

      double d = Double.parseDouble(lenght);
        if(d < 1000000)
        {
            return 0;
        }
        else
        {
            return (int)(d / 1000000);
        }

    }


    public void sendPlaybackStatus(NetworkPackage np) {

        //hz
    }

    public void setVolume(NetworkPackage np) {
        String destination = players.get(np.getValue(PLAYER));
        String volume = np.getValue(VOLUME);

        dbusSend(FIRST_PART_DEST + np.getValue(PLAYER) + SETVOLUME+volume);
    }

    public void setPostion(NetworkPackage np) {
        String destination = players.get(np.getValue(PLAYER));
        String path = np.getValue("path");
        String pos = np.getValue("position");
        String result = FIRST_PART_DEST + destination +  MEDIUM_PART + PLAYER_INTERFACE + SETPOSITION + path + " int64:" + pos;
        dbusSend(result);
    }

    public void openUri(NetworkPackage np) {
        String destination = players.get(np.getValue(PLAYER));
        String uri = np.getValue(OPENURI);
        String result = FIRST_PART_DEST + destination +  MEDIUM_PART + PLAYER_INTERFACE + OPENURI + uri;
        dbusSend(result);
    }

    public void stop(NetworkPackage np) {
        String destination = players.get(np.getValue(PLAYER));
        String result = FIRST_PART_DEST + destination +  MEDIUM_PART + PLAYER_INTERFACE + STOP;
        dbusSend(result);
    }

    public void playPause(NetworkPackage np) {
        String destination = players.get(np.getValue(PLAYER));
        String result = FIRST_PART_DEST + np.getValue(PLAYER) + " " +  MEDIUM_PART + PLAYER_INTERFACE + PLAYPAUSE; //aatention
        dbusSend(result);
    }

    public void previous(NetworkPackage np) {
        String destination = players.get(np.getValue(PLAYER));
        String result = FIRST_PART_DEST + np.getValue(PLAYER)  +" "+  MEDIUM_PART + PLAYER_INTERFACE + PREVIOUS;
        dbusSend(result);
    }

    public void next(NetworkPackage np) {
        String destination = players.get(np.getValue(PLAYER));
        String result = FIRST_PART_DEST + np.getValue(PLAYER)  +" " +  MEDIUM_PART + PLAYER_INTERFACE + NEXT;
        dbusSend(result);
    }

    public void seek(NetworkPackage np) {
        String destination = players.get(np.getValue(PLAYER));
        String sec = np.getValue(SEEK);
        Long ss = Long.parseLong(sec);
        ss *= 1000000;
        sec = Long.toString(ss);
        String result = FIRST_PART_DEST + np.getValue(PLAYER) +" "+  MEDIUM_PART + PLAYER_INTERFACE + SEEK + sec;
       dbusSend(result);
    }


    public void sendAnswer(String message)
    {
        TcpConnectionManager.getInstance().sendCommandToServer(device.getId(),message);
    }

    public static void dbusSend(String message)
    {
        try {
            Runtime.getRuntime().exec(message);
        } catch (Exception e) {
            Loggout.e("Mpris",e.toString());
        }
    }

    public static void main(String args[])
    {


    }


}
