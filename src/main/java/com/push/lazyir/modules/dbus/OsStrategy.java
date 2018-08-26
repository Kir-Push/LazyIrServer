package com.push.lazyir.modules.dbus;
import com.push.lazyir.api.Dto;

import java.util.List;


public interface OsStrategy {
    void seek(MprisDto dto);
    void stop(MprisDto dto);
    void next(MprisDto dto);
    void previous(MprisDto dto);
    void playPause(MprisDto dto);
    void openUri(MprisDto dto);
    void setPosition(MprisDto dto);
    void setVolume(MprisDto dto);
    List<Player> getAllPlayers();
    void playAll();
    void pauseAll();
    void endWork();
    void loop(MprisDto dto);
}
