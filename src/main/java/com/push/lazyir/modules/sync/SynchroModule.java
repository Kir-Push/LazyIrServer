package com.push.lazyir.modules.sync;


import com.push.lazyir.devices.NetworkPackage;
import com.push.lazyir.gui.GuiCommunicator;
import com.push.lazyir.modules.Module;
import com.push.lazyir.pojo.Command;
import com.push.lazyir.pojo.CommandsList;
import com.push.lazyir.service.BackgroundService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by buhalo on 09.03.17.
 */

public class SynchroModule extends Module {
    private final static String DELETE_COMMANDS = "delete_cmds";
    private final static String ADD_COMMAND = "add_cmd";
    private final static String GET_ALL_COMMANDS = "all_cmd";
    private final static String UPDATE_COMMANDS = "update_cmd";

    private boolean comparing = false;
    private boolean receivingCommands;
    private boolean endReceivingCommands;
    private List<Command> commands = new ArrayList<>();
    @Override

    public  void execute(NetworkPackage np) {
        String data = np.getData();
        switch (data)
        {
            case GET_ALL_COMMANDS:
                receiveAllCommands(np);
                break;
        }

    }

    private void receiveAllCommands(NetworkPackage np) {
        CommandsList cmds = np.getObject("cmds", CommandsList.class);
        GuiCommunicator.receiveCommands(cmds,device.getId());

    }

    public static void sendUpdateCommands(String id,List<Command> commands){
        CommandsList commandsList = new CommandsList(commands);
        NetworkPackage orCreatePackage = NetworkPackage.Cacher.getOrCreatePackage(SynchroModule.class.getSimpleName(), UPDATE_COMMANDS);
        orCreatePackage.setObject("cmdsU",commandsList);
        BackgroundService.sendToDevice(id,orCreatePackage.getMessage());
    }

    public static void sendDeleteCommands(String id,List<Command> commands){
        CommandsList commandsList = new CommandsList(commands);
        NetworkPackage orCreatePackage = NetworkPackage.Cacher.getOrCreatePackage(SynchroModule.class.getSimpleName(), DELETE_COMMANDS);
        orCreatePackage.setObject("cmdsD",commandsList);
        BackgroundService.sendToDevice(id,orCreatePackage.getMessage());
    }

    public static void sendAddCommands(String id,List<Command> commands){
        CommandsList commandsList = new CommandsList(commands);
        NetworkPackage orCreatePackage = NetworkPackage.Cacher.getOrCreatePackage(SynchroModule.class.getSimpleName(), ADD_COMMAND);
        orCreatePackage.setObject("cmdsA",commandsList);
        BackgroundService.sendToDevice(id,orCreatePackage.getMessage());
    }

    public static void sendGetAllCommands(String id){
        NetworkPackage orCreatePackage = NetworkPackage.Cacher.getOrCreatePackage(SynchroModule.class.getSimpleName(), GET_ALL_COMMANDS);
        BackgroundService.sendToDevice(id,orCreatePackage.getMessage());
    }

    @Override
    public void endWork() {
        
    }


    private void compareMethod() {

    }
}
