package com.push.lazyir.modules.notifications;

/**
 * Created by buhalo on 26.03.17.
 */

public class Sms {
    private int number;
    private String name;
    private String text;

    public Sms() {
    }

    public Sms(String name, String text) {
        this.name = name;
        this.text = text;
    }

    public Sms(int number, String text) {
        this.number = number;
        this.text = text;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
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
}
