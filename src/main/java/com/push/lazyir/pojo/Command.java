package com.push.lazyir.pojo;

/**
 * Created by buhalo on 26.02.17.
 */

public class Command {

    private String producer;
    private String device;
    private String command_name;
    private String command;
    private String owner_id;
    private String type;

    public static String pc = "pc";
    public static String ir = "ir";

    public Command() {
    }

    public Command(String command_name, String command, String owner_id, String type) {
        this.producer = "";
        this.device = "";
        this.command_name = command_name;
        this.command = command;
        this.owner_id = owner_id;
        this.type = type;
    }

    public Command(String producer, String device, String command_name, String command, String owner_id, String type) {
        this.producer = producer;
        this.device = device;
        this.command_name = command_name;
        this.command = command;
        this.owner_id = owner_id;
        this.type = type;
    }


    public String getCommand_name() {
        return command_name;
    }

    public void setCommand_name(String command_name) {
        this.command_name = command_name;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getOwner_id() {
        return owner_id;
    }

    public void setOwner_id(String owner_id) {
        this.owner_id = owner_id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Command command = (Command) o;

        return command_name.equals(command.command_name);

    }

    @Override
    public int hashCode() {
        return command_name.hashCode();
    }

    @Override
    public String toString() {
        return "Command{" +
                "command_name='" + command_name + '\'' +
                '}';
    }

    public String getProducer() {
        return producer;
    }

    public void setProducer(String producer) {
        this.producer = producer;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }
}
