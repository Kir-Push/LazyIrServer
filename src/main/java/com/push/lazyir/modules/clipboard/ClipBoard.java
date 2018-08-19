package com.push.lazyir.modules.clipboard;

import com.push.lazyir.devices.Cacher;
import com.push.lazyir.devices.Device;
import com.push.lazyir.devices.NetworkPackage;
import com.push.lazyir.modules.Module;
import com.push.lazyir.modules.clipboard.Rmi.ClipboardRmiServer;
import com.push.lazyir.service.main.BackgroundService;

import javax.inject.Inject;

/**
 * Created by buhalo on 19.04.17.
 */
public class ClipBoard extends Module {
  public final static String RECEIVE = "receive";
    private ClipboardRmiServer server;

    @Inject
    public ClipBoard(BackgroundService backgroundService, Cacher cacher, ClipboardRmiServer server) {
        super(backgroundService, cacher);
        this.server = server;
        server.startListening();
    }


    @Override
    public void execute(NetworkPackage np) {
        if(np.getData().equals(RECEIVE)) {
            onReceive(np);
        }
    }

    @Override
    public void endWork() {
        //todo
    }

    private void onReceive(NetworkPackage np) {
        server.setClipboard(np.getValue("text")); 
    }
}
