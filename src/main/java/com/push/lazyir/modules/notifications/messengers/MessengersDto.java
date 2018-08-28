package com.push.lazyir.modules.notifications.messengers;

import com.push.lazyir.api.Dto;
import com.push.lazyir.modules.notifications.notifications.Notification;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MessengersDto implements Dto {
    private String command;
    private String text;
    private String typeName;
    private Notification notification;
}
