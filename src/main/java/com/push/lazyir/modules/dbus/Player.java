package com.push.lazyir.modules.dbus;
import lombok.Data;

import java.util.Objects;

@Data
public class Player implements Comparable<Player> {
    private String name;
    private String status;
    private String title;
    private double length;
    private double volume;
    private double currTime;
    private String id;
    private String url;
    private String ip;

    public Player(String status, String title, double length, double volume, double currTime, String id, String url,String ip) {
        this.status = status;
        this.title = title;
        this.length = length;
        this.volume = volume;
        this.currTime = currTime;
        this.id = id;
        this.url = url;
        this.ip = ip;
    }

    public Player(String name,String status, String title, double length, double volume, double currTime) {
        this.name = name;
        this.status = status;
        this.title = title;
        this.length = length;
        this.volume = volume;
        this.currTime = currTime;
        this.id = "-1"; // means dbus
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return Objects.equals(title, player.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title);
    }

    @Override
    public int compareTo(Player o) {
        if(getTitle().equals(o.getTitle())){
            return o.getId().compareTo(getId());
        }
        else {
            return o.getTitle().compareTo(getTitle());
        }
    }
}
