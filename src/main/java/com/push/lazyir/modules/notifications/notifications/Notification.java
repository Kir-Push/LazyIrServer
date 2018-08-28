package com.push.lazyir.modules.notifications.notifications;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Notification {
    private String text;
    private String title;
    private String pack;
    private String ticker;
    private String id;
    private String icon;
    private String picture;
    private String type;

    public Notification(String id) {
        this.id = id;
    }
}
