package com.push.lazyir.modules.dbus;

/**
 * Created by buhalo on 15.04.17.
 */

public class Player implements Comparable<Player> {
    private String name;
    private String playbackStatus;
    private String title;
    private double lenght;
    private double volume;
    private double currTime;
    private String readyTimeString;
    private String type;
    private String id;

    public Player(String name, String playbackStatus, String title, double lenght, double volume, double currTime, String readyTimeString, String type, String id) {
        this.name = "js9876528:" + id; //i'm lazy
        this.playbackStatus = playbackStatus;
        this.title = title;
        this.lenght = lenght;
        this.volume = volume;
        this.currTime = currTime;
        this.readyTimeString = readyTimeString;
        this.type = type;
        this.id = id;
    }

    public Player(String name, String playbackStatus, String title, double lenght, double volume, double currTime, String readyTimeString) {
        this.name = name; // android use name as identificator
        this.playbackStatus = playbackStatus;
        this.title = title;
        this.lenght = lenght;
        this.volume = volume;
        this.currTime = currTime;
        this.readyTimeString = readyTimeString;
        this.type = "dbus";
        this.id = "-1";
    }

    public Player() {
    }
    public void setType(String type) {this.type = type;}

    public String getType() {return type;}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPlaybackStatus() {
        return playbackStatus;
    }

    public void setPlaybackStatus(String playbackStatus) {
        this.playbackStatus = playbackStatus;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getLenght() {
        return lenght;
    }

    public void setLenght(double lenght) {
        this.lenght = lenght;
    }

    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }

    public double getCurrTime() {
        return currTime;
    }

    public void setCurrTime(double currTime) {
        this.currTime = currTime;
    }

    public String getReadyTimeString() {
        return readyTimeString;
    }

    public void setReadyTimeString(String readyTimeString) {
        this.readyTimeString = readyTimeString;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Player player = (Player) o;

        if (name != null ? !name.equals(player.name) : player.name != null) return false;
        if (title != null ? !title.equals(player.title) : player.title != null) return false;
        if (type != null ? !type.equals(player.type) : player.type != null) return false;
        return id != null ? id.equals(player.id) : player.id == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (id != null ? id.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Player{" +
                "name='" + name + '\'' +
                ", playbackStatus='" + playbackStatus + '\'' +
                ", title='" + title + '\'' +
                ", lenght=" + lenght +
                ", volume=" + volume +
                ", currTime=" + currTime +
                ", readyTimeString='" + readyTimeString + '\'' +
                ", type='" + type + '\'' +
                ", id='" + id + '\'' +
                '}';
    }

    @Override
    public int compareTo(Player o) {
        if(getTitle().equals(o.getTitle())){
            return o.getId().compareTo(getId());
        }
        else
        return o.getTitle().compareTo(getTitle());
    }
}
