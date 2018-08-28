package com.push.lazyir.modules.ping;

import com.push.lazyir.api.Dto;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PingDto implements Dto {
    String command;
}
