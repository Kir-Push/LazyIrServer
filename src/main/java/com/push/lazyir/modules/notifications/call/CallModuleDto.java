package com.push.lazyir.modules.notifications.call;

import com.push.lazyir.api.Dto;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CallModuleDto extends Dto {
    private String command;
    private String callType;
    private String text;
    private String name;
    private String number;
    private String icon;

    CallModuleDto(String command) {
        this.command = command;
    }

    CallModuleDto(String command, String number) {
        this.command = command;
        this.number = number;
    }
}
