package com.push.lazyir.modules.dbus.strategies.win;

import com.push.lazyir.modules.dbus.Player;
import com.push.lazyir.modules.dbus.Players;

/**
 * Created by buhalo on 31.07.17.
 */
public interface Strategy {

    boolean initiate();

    boolean Tryinitiate();


     void stopConnection();

     boolean checkStatus();

     Players getGetAll();

     Player getOnePlayer();

    void seek(String player,String arg);

    void next(String player);

    void previous(String player);

    void stop(String player);

    void playPause(String player);

    void openUri(String player,String uri);

    void setPosition(String player,String path,String position);

    void setVolume(String player,String value);

    double getVolume(String player);

    String getMetadata(String player);

    String getPlaybackstatus(String player);

    double getPosition(String player);

    String getPlayerName();
}
