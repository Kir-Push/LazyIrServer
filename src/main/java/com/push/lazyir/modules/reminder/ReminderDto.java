package com.push.lazyir.modules.reminder;

import com.push.lazyir.api.Dto;
import com.push.lazyir.modules.notifications.notifications.Notification;
import com.push.lazyir.modules.notifications.sms.Sms;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ReminderDto implements Dto {
    private String command;
    private String missedCallStr;
    private List<MissedCall> missedCalls;
    private List<Notification> notifications;
    private List<Sms> smsList;

    public ReminderDto(String command) {
        this.command = command;
    }

    public ReminderDto(String command, String missedCallStr) {
        this.command = command;
        this.missedCallStr = missedCallStr;
    }

    public ReminderDto(String command, List<MissedCall> missedCalls) {
        this.command = command;
        this.missedCalls = missedCalls;
    }

    public ReminderDto(String command, List<Notification> notifications, List<Sms> smsList) {
        this.command = command;
        this.notifications = notifications;
        this.smsList = smsList;
    }
}
