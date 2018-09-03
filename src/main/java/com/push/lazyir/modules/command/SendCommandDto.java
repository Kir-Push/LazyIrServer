package com.push.lazyir.modules.command;

import com.push.lazyir.api.Dto;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

@Data
@AllArgsConstructor
public class SendCommandDto extends Dto {
    private String command;
    private String id;
    private Set<Command> commands;

    public SendCommandDto(String command, Set<Command> commands) {
        this.command = command;
        this.commands = commands;
    }
}
