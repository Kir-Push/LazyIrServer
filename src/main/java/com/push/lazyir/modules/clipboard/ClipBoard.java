package com.push.lazyir.modules.clipboard;

import com.push.lazyir.api.MessageFactory;
import com.push.lazyir.api.NetworkPackage;
import com.push.lazyir.modules.Module;
import com.push.lazyir.modules.clipboard.rmi.ClipboardRmiServer;
import com.push.lazyir.service.main.BackgroundService;
import javax.inject.Inject;

import static com.push.lazyir.modules.clipboard.ClipBoard.api.RECEIVE;

public class ClipBoard extends Module {
  private ClipboardRmiServer server;
     public enum api{
         RECEIVE
     }
    @Inject
    public ClipBoard(BackgroundService backgroundService, MessageFactory messageFactory, ClipboardRmiServer server) {
        super(backgroundService, messageFactory);
        this.server = server;
        if(!server.isWorking()) {
            server.startListening();
        }
    }


    @Override
    public void execute(NetworkPackage np) {
        ClipBoardDto data = (ClipBoardDto) np.getData();
        if(data.getCommand().equalsIgnoreCase(RECEIVE.name())) {
            onReceive(data.getText());
        }
    }

    @Override
    public void endWork() {
        if(backgroundService.ifLastConnectedDeviceAreYou(device.getId())){
            server.stopListening();
        }
    }

    private void onReceive(String text) {
        server.setClipboard(text);
    }
}
