package com.push.lazyir.modules.dbus;

import com.push.lazyir.api.Dto;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class MprisDto implements Dto {
    private String command;
    private String player;
    private String jsIp;
    private String jsId;
    private String playerType;
    private String value;
    private double dValue;
    private List<Player> players;

    public MprisDto(String command, List<Player> players) {
        this.command = command;
        this.players = players;
    }

    public MprisDto(String command, String jsId, double dValue) {
        this.command = command;
        this.jsId = jsId;
        this.dValue = dValue;
    }

    public MprisDto(String command, String jsId) {
        this.command = command;
        this.jsId = jsId;
    }
}
