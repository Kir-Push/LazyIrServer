package com.push.lazyir.modules.notifications;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by buhalo on 26.03.17.
 */

public class Notifications {

    private List<Notification> notifications;

    public Notifications() {
        notifications = new ArrayList<>();
    }

    public Notifications(List<Notification> notifications) {
        this.notifications = notifications;
    }

    public void addNotification(Notification notification)
    {
        notifications.add(notification);
    }

    public List<Notification> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications;
    }
}
