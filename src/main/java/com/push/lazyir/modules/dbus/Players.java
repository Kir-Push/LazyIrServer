package com.push.lazyir.modules.dbus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by buhalo on 15.04.17.
 */

public class Players {

    private List<Player> playerList;

    public Players(List<Player> playerList) {
        this.playerList = playerList;
    }

    public Players() {
        playerList = new ArrayList<>();
    }

    public List<Player> getPlayerList() {
        return playerList;
    }

    public void setPlayerList(List<Player> playerList) {
        this.playerList = playerList;
    }
}
