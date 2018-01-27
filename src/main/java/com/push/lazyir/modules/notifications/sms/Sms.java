package com.push.lazyir.modules.notifications.sms;

/**
 * Created by buhalo on 26.03.17.
 */

public class Sms {
    private String number;
    private String name;
    private String text;
    private String icon;
    private String picture;
    private String type = "sms";
    private long date; // todo add to server
    private String id;

    public Sms() {
    }

    public Sms(String name,String number, String text,String icon,String picture) {
        this.name = name;
        this.text = text;
        this.picture = picture;
        this.icon = icon;
        this.number = number;
    }


    public Sms(String number, String text,String icon,String picture) {
        this.number = number;
        this.text = text;
        this.picture = picture;
        this.icon = icon;
    }

    public Sms(String number, String text,Long date,String icon,String picture) {
        this.number = number;
        this.text = text;
        this.picture = picture;
        this.icon = icon;
        this.date = date;
    }

    public Sms(String name,String number, String text,Long date,String icon,String picture){
        this(name,number,text,icon,picture);
        this.date = date;
    }

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

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }


    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "Sms{" +
                "number='" + number + '\'' +
                ", name='" + name + '\'' +
                ", text='" + text + '\'' +
                ", icon='" + icon + '\'' +
                ", picture='" + picture + '\'' +
                '}';
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
