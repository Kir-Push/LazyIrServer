package com.push.lazyir.modules.sync;


import com.push.lazyir.devices.NetworkPackage;
import com.push.lazyir.modules.Module;
import com.push.lazyir.pojo.Command;
import com.push.lazyir.pojo.CommandsList;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by buhalo on 09.03.17.
 */

public class SynchroModule extends Module {
    private final static String DELETE_COMMANDS = "delete_cmds";
    private final static String ADD_COMMAND = "add_cmd";
    private final static String GET_ALL_COMMANDS = "all_cmd";

    private boolean comparing = false;
    private boolean receivingCommands;
    private boolean endReceivingCommands;
    private List<Command> commands = new ArrayList<>();
    @Override

    public  void execute(NetworkPackage np) {
        String data = np.getData();
        switch (data)
        {
            case DELETE_COMMANDS:
                deleteCommands(np);
                break;
            case ADD_COMMAND:
                addCommand(np);
                break;
            case GET_ALL_COMMANDS:
                sendAllCommands();
                break;
        }

    }

    @Override
    public void endWork() {
        
    }

    private void sendAllCommands() {

    }

    private void addCommand(NetworkPackage np) {


    }

    private void deleteCommands(NetworkPackage np) {

    }

    private void compareMethod() {

    }
}
