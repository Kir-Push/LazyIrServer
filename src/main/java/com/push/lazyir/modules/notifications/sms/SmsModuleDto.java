package com.push.lazyir.modules.notifications.sms;

import com.push.lazyir.api.Dto;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SmsModuleDto implements Dto {
    private String command;
    private Sms sms;
}
