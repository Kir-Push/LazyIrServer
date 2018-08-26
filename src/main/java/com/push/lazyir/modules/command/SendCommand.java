package com.push.lazyir.modules.command;

import com.push.lazyir.api.MessageFactory;
import com.push.lazyir.api.NetworkPackage;
import com.push.lazyir.gui.GuiCommunicator;
import com.push.lazyir.modules.Module;
import com.push.lazyir.service.main.BackgroundService;
import lombok.extern.slf4j.Slf4j;
import javax.inject.Inject;
import java.io.IOException;
import java.util.Set;

@Slf4j
public class SendCommand extends Module {
    public enum api{
        EXECUTE,
        DELETE_COMMANDS,
        ADD_COMMAND,
        GET_ALL_COMMANDS,
        UPDATE_COMMANDS
    }
    private GuiCommunicator guiCommunicator;

    @Inject
    public SendCommand(BackgroundService backgroundService, MessageFactory messageFactory, GuiCommunicator guiCommunicator) {
        super(backgroundService, messageFactory);
        this.guiCommunicator = guiCommunicator;
    }


    @Override
    public void execute(NetworkPackage np) {
        SendCommandDto data = (SendCommandDto) np.getData();
        Set<Command> commands = data.getCommands();
        api cmd =api.valueOf(data.getCommand());
        switch (cmd){
            case EXECUTE:
                executeCommands(commands);
                break;
            case GET_ALL_COMMANDS:
                receiveAllCommands(commands);
                break;
            default:
                break;
        }

    }

    @Override
    public void endWork() {
        // here nothing to erase, so empty method :(
    }
    private void executeCommand(String command){
        try {
            Runtime.getRuntime().exec(command);
        }catch (IOException e){
            log.error("error in executeCommand - " + command,e);
        }
    }

    private void executeCommands(Set<Command> commands) {
        commands.forEach(cmd -> executeCommand(cmd.getCmd()));
    }

    private void receiveAllCommands(Set<Command> commands) {
        guiCommunicator.receiveCommands(commands,device.getId());
    }

    public void sendUpdateCommands( Set<Command> commands){
        sendCommand(commands,api.UPDATE_COMMANDS);
    }

    public void sendDeleteCommands(Set<Command> commands){
        sendCommand(commands,api.DELETE_COMMANDS);
    }

    public void sendAddCommands(Set<Command> commands){
        sendCommand(commands,api.ADD_COMMAND);
    }

    public void sendGetAllCommands(){
        sendCommand(null,api.GET_ALL_COMMANDS);
    }

    private void sendCommand(Set<Command> commands,api cmd){
        String message = messageFactory.createMessage(this.getClass().getSimpleName(), true,
                new SendCommandDto(cmd.name(), commands));
        sendMsg(message);
    }

}
