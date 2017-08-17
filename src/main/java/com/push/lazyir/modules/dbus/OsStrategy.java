package com.push.lazyir.modules.dbus;

import com.push.lazyir.devices.NetworkPackage;

import java.util.List;


public interface OsStrategy {

    void seek(NetworkPackage np);
    void stop(NetworkPackage np);
    void next(NetworkPackage np);
    void previous(NetworkPackage np);
    void playPause(NetworkPackage np);
    void openUri(NetworkPackage np);
    void setPosition(NetworkPackage np);
    void setVolume(NetworkPackage np);
    List<Player> getAllPlayers();
    void playAll(String id);
    void pauseAll(String id);
    void endWork();
}
