package com.push.lazyir.service.dto;

import com.push.lazyir.api.Dto;
import com.push.lazyir.devices.ModuleSetting;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class TcpDto implements Dto {
    private String command;
    private String data;
    private String result;
    private String icon;
    private List<ModuleSetting> moduleSettings;

    public TcpDto(String command, String data) {
        this.command = command;
        this.data = data;
    }

    public TcpDto(String command) {
        this.command = command;
    }

    public TcpDto(String command, String data, String result) {
        this.command = command;
        this.data = data;
        this.result = result;
    }
}
