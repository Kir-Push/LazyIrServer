package com.push.lazyir.modules.notifications.sms;
import lombok.Data;

@Data
public class Sms {
    private String number;
    private String name;
    private String text;
    private String icon;
    private String picture;
    private String type = "SMS";
    private long date;
    private String id;

    public Sms(String name,String number, String text) {
        this.name = name;
        this.text = text;
        this.number = number;
    }

}
