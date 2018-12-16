package com.push.lazyir.modules.command;
import lombok.Data;

@Data
public class Command implements Comparable<Command> {
    private String commandName;
    private String cmd;
    private int id;

    public Command(String commandName, String cmd, int id) {
        this.commandName = commandName;
        this.cmd = cmd;
        this.id = id;
    }

    @Override
    public int compareTo(Command o) {
        return Integer.compare(getId(),o.getId());
    }
}
