package com.push.lazyir.modules.dbus;

/**
 * Created by buhalo on 15.04.17.
 */

public class Player {
    private String name;
    private String playbackStatus;
    private String title;
    private int lenght;
    private int volume;
    private int currTime;
    private String readyTimeString;
    public Player(String name, String playbackStatus, String title, int lenght, int volume, int currTime, String readyTimeString) {
        this.name = name;
        this.playbackStatus = playbackStatus;
        this.title = title;
        this.lenght = lenght;
        this.volume = volume;
        this.currTime = currTime;
        this.readyTimeString = readyTimeString;
    }

    public Player() {
    }

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

    public int getLenght() {
        return lenght;
    }

    public void setLenght(int lenght) {
        this.lenght = lenght;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public int getCurrTime() {
        return currTime;
    }

    public void setCurrTime(int currTime) {
        this.currTime = currTime;
    }

    public String getReadyTimeString() {
        return readyTimeString;
    }

    public void setReadyTimeString(String readyTimeString) {
        this.readyTimeString = readyTimeString;
    }
}
