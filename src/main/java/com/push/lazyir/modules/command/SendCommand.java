package com.push.lazyir.modules.command;



import com.push.lazyir.Loggout;
import com.push.lazyir.devices.Cacher;
import com.push.lazyir.devices.Device;
import com.push.lazyir.pojo.Command;
import com.push.lazyir.devices.NetworkPackage;
import com.push.lazyir.modules.Module;
import com.push.lazyir.pojo.CommandsList;
import com.push.lazyir.service.main.BackgroundService;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

/**
 * Created by buhalo on 05.03.17.
 */

public class SendCommand extends Module {
    public static final String SEND_COMMAND = "SendCommand";
    public static final String RECEIVED_COMMAND = "receivedCommand";
    public static final String EXECUTE = "execute";
    public static final String COMMAND = "command";

    @Inject
    public SendCommand(BackgroundService backgroundService, Cacher cacher) {
        super(backgroundService, cacher);
    }


    @Override
    public void execute(NetworkPackage np) {

        if(np.getData().equals(EXECUTE)) {
           executeCommand(np.getObject(NetworkPackage.N_OBJECT, CommandsList.class).getCommands());
        }

    }

    @Override
    public void endWork() {

    }

    private void executeCommand(List<Command> commands) {
        if(commands != null)
        for(Command command : commands)
        {
            try {
                String command1 = command.getCommand();
                if(command1 != null)
                Runtime.getRuntime().exec(command1);
            } catch (Exception e) {
                Loggout.e("SendCommand","error in executeCommand",e);
            }
        }
    }


    private void saveCommandToClient(List<Command> args)
    {

    }
}
