package com.push.lazyir.modules.battery;

import com.push.lazyir.api.Dto;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BatteryDto implements Dto {
    private String percentage;
    private String status;
}
