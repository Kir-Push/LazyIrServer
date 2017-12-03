package com.push.lazyir.modules.notifications;

/**
 * Created by buhalo on 26.03.17.
 */

public class Sms {
    private String number;
    private String name;
    private String text;
    private String icon;
    private String picture;

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
}
