package com.push.lazyir.modules.command;
import lombok.Data;

@Data
public class Command implements Comparable<Command> {
    private String producer;
    private String device;
    private String commandName;
    private String cmd;
    private String ownerId;
    private String type;
    private transient boolean active;
    private transient boolean edited;
    private transient boolean updated;

    public Command(String producer, String device, String commandName, String cmd, String ownerId, String type) {
        this.producer = producer;
        this.device = device;
        this.commandName = commandName;
        this.cmd = cmd;
        this.ownerId = ownerId;
        this.type = type;
    }

    @Override
    public int compareTo(Command o) {
        return this.getCommandName().compareTo(o.getCommandName());
    }
}
