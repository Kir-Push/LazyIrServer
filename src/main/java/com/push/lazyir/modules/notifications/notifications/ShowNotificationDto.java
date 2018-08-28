package com.push.lazyir.modules.notifications.notifications;

import com.push.lazyir.api.Dto;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ShowNotificationDto implements Dto {
    private String command;
    private List<Notification> notifications;
    private Notification notification;

    public ShowNotificationDto(String command) {
        this.command = command;
    }

    public ShowNotificationDto(String command, Notification notification) {
        this.command = command;
        this.notification = notification;
    }
}
