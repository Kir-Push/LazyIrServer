package com.push.lazyir.modules.sync;


import com.push.lazyir.devices.Cacher;
import com.push.lazyir.devices.Device;
import com.push.lazyir.devices.NetworkPackage;
import com.push.lazyir.gui.GuiCommunicator;
import com.push.lazyir.modules.Module;
import com.push.lazyir.pojo.Command;
import com.push.lazyir.pojo.CommandsList;
import com.push.lazyir.service.main.BackgroundService;

import javax.inject.Inject;
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
    private GuiCommunicator guiCommunicator;

    @Inject
    public SynchroModule(BackgroundService backgroundService, Cacher cacher, GuiCommunicator guiCommunicator) {
        super(backgroundService, cacher);
        this.guiCommunicator = guiCommunicator;
    }

    @Override
    public void execute(NetworkPackage np) {
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
        guiCommunicator.receiveCommands(cmds,device.getId());

    }

    public void sendUpdateCommands(String id,List<Command> commands){
        CommandsList commandsList = new CommandsList(commands);
        NetworkPackage orCreatePackage = cacher.getOrCreatePackage(SynchroModule.class.getSimpleName(), UPDATE_COMMANDS);
        orCreatePackage.setObject("cmdsU",commandsList);
        backgroundService.sendToDevice(id,orCreatePackage.getMessage());
    }

    public void sendDeleteCommands(String id,List<Command> commands){
        CommandsList commandsList = new CommandsList(commands);
        NetworkPackage orCreatePackage = cacher.getOrCreatePackage(SynchroModule.class.getSimpleName(), DELETE_COMMANDS);
        orCreatePackage.setObject("cmdsD",commandsList);
        backgroundService.sendToDevice(id,orCreatePackage.getMessage());
    }

    public void sendAddCommands(String id,List<Command> commands){
        CommandsList commandsList = new CommandsList(commands);
        NetworkPackage orCreatePackage = cacher.getOrCreatePackage(SynchroModule.class.getSimpleName(), ADD_COMMAND);
        orCreatePackage.setObject("cmdsA",commandsList);
        backgroundService.sendToDevice(id,orCreatePackage.getMessage());
    }

    public void sendGetAllCommands(String id){
        NetworkPackage orCreatePackage = cacher.getOrCreatePackage(SynchroModule.class.getSimpleName(), GET_ALL_COMMANDS);
        backgroundService.sendToDevice(id,orCreatePackage.getMessage());
    }

    @Override
    public void endWork() {
        
    }


    private void compareMethod() {

    }
}
