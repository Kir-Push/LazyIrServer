package com.push.lazyir.modules.reminder;

/**
 * Created by buhalo on 14.01.18.
 */

public class MissedCall {
    public MissedCall(String number, String name, int count, long date, String picture, String id) {
        this.number = number;
        this.name = name;
        this.count = count;
        this.date = date;
        this.picture = picture;
        this.id = id;
    }

    private String number;
    private String name;
    private int count;
    private long date;
    private String picture;
    private String id;

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
