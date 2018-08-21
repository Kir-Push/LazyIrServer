package com.push.gui.entity;

import lombok.*;

@Data
public class CommandGuiEntity implements Comparable<CommandGuiEntity> {

    private String id;
    private String producer;
    private String device;
    private String commandName;
    private String command;
    private String ownerId;
    private String type;
    private boolean active;
    private boolean edited;
    private boolean updated;

    public CommandGuiEntity(String id, String producer, String device, String commandName, String command, String ownerId, String type) {
        this.id = id;
        this.producer = producer;
        this.device = device;
        this.commandName = commandName;
        this.command = command;
        this.ownerId = ownerId;
        this.type = type;
    }

    @Override
    public int compareTo(CommandGuiEntity o) {
        return this.getCommandName().compareTo(o.getCommandName());
    }
}
