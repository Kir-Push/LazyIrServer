package com.push.lazyir.modules.dbus;

import com.push.lazyir.Loggout;
import com.push.lazyir.devices.NetworkPackage;
import com.push.lazyir.modules.dbus.websocket.PopupEndpoint;
import com.push.lazyir.utils.CollectingFuture;
import com.push.lazyir.utils.SettableFuture;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.push.lazyir.modules.dbus.Mpris.*;

/**
 * Created by buhalo on 16.08.17.
 */
// stategy for htmlVideo in browser
    // work though websocket
public class HtmlVid implements OsStrategy {
    private ConcurrentHashMap<String,String> pausedPlayersBrowser = new ConcurrentHashMap<>();

    @Override
    public void seek(NetworkPackage np) {
        String playerValue = np.getValue(player);
        String seekValue = np.getValue(seek);
        PopupEndpoint.sendTime(playerValue.substring(10), seekValue);
    }

    @Override
    public void stop(NetworkPackage np) {
        String playerValue = np.getValue(player);
        PopupEndpoint.sendStatus(playerValue.substring(10),"pause");
    }

    @Override
    public void next(NetworkPackage np) {
        String playerValue = np.getValue(player);
        PopupEndpoint.sendStatus(playerValue.substring(10),"next");
    }

    @Override
    public void previous(NetworkPackage np) {
        String playerValue = np.getValue(player);
        PopupEndpoint.sendStatus(playerValue.substring(10),"previous");
    }



    @Override
    public void playPause(NetworkPackage np) {
        System.out.println(np.getMessage());
        String playerValue = np.getValue(player);
        PopupEndpoint.sendStatus(playerValue.substring(10),playPause);
    }

    @Override
    public void openUri(NetworkPackage np) {
        String playerValue = np.getValue(player);
        String uri = np.getValue(openUri);
        //todo
    }

    @Override
    public void setPosition(NetworkPackage np) {
        String playerValue = np.getValue(player);
        String pos = np.getValue("position");
      //  String path = np.getValue("path");
        PopupEndpoint.sendTime(playerValue,pos);
    }

    @Override
    public void setVolume(NetworkPackage np) {
        String playerValue = np.getValue(player);
        String volumeVal = np.getValue(volume);
        PopupEndpoint.sendVolume(playerValue.substring(10),volumeVal);
    }

    @Override
    public List<Player> getAllPlayers() {
        CollectingFuture<Player,Collection<Player>> browsr = null;
        try {
            browsr = PopupEndpoint.getAll();
            return new ArrayList<>(browsr.getByTimerWhatHave(500, TimeUnit.MILLISECONDS)); // from browser
        } catch (Exception e) {
            Loggout.e("HtmlVid","getAllPlayers ",e);
            if(browsr!=null)
            browsr.cancel(true);
        }
        return new ArrayList<>();
    }

    @Override
    public void playAll(String id) {
        for (String s : pausedPlayersBrowser.keySet()) {
            PopupEndpoint.sendStatus(s,"play");
            pausedPlayersBrowser.clear();
        }
    }

    @Override
    public void pauseAll(String id) {
        try {
            List<Player> playerList = new ArrayList<>(PopupEndpoint.getAll().getByTimerWhatHave(1000, TimeUnit.MILLISECONDS));
            for (Player pl : playerList) {
                if (pl.getPlaybackStatus().equalsIgnoreCase("playing")) {
                    String substring = pl.getName().substring(10);
                    PopupEndpoint.sendStatus(substring, "pause");
                    pausedPlayersBrowser.put(substring, pl.getPlaybackStatus());
                }
            }
        } catch (Exception e) {
            Loggout.e("HtmlVid", "PauseAll",e);
        }
    }

    @Override
    public void endWork() {
        pausedPlayersBrowser.clear();
    }

    @Override
    public void loop(NetworkPackage np) {
        String playerValue = np.getValue(player);
        PopupEndpoint.sendStatus(playerValue.substring(10),"loop");
    }
}
