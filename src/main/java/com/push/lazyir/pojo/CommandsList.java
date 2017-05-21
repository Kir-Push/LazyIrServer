package com.push.lazyir.pojo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by buhalo on 25.03.17.
 */
public class CommandsList {

    public CommandsList() {
        this.commands = new ArrayList<>();
    }

    public CommandsList(List<Command> commands) {
        this.commands = commands;
    }

    public void addCommand(Command cmd)
    {
        commands.add(cmd);
    }

    private List<Command> commands;

    public List<Command> getCommands() {
        return commands;
    }

    public void setCommands(List<Command> commands) {
        this.commands = commands;
    }
}
