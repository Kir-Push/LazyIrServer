package com.push.lazyir.modules.share;

import com.push.lazyir.api.Dto;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ShareModuleDto implements Dto {
    private String command;
    private String userName;
    private String password;
    private String mountPoint;
    private String osType;
    private int port;
    private PathWrapper externalMountPoint;

    public ShareModuleDto(String command) {
        this.command = command;
    }

    public ShareModuleDto(String command, String osType) {
        this.command = command;
        this.osType = osType;
    }
}
