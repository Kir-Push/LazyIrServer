package com.push.lazyir.modules.command;

import com.push.lazyir.api.Dto;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

@Data
@AllArgsConstructor
public class SendCommandDto implements Dto {
    private String command;
    private Set<Command> commands;
}
