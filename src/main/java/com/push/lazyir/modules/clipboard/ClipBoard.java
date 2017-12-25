package com.push.lazyir.modules.clipboard;

import com.push.lazyir.devices.NetworkPackage;
import com.push.lazyir.modules.Module;
import com.push.lazyir.modules.clipboard.Rmi.ClipboardRmiServer;

/**
 * Created by buhalo on 19.04.17.
 */
public class ClipBoard extends Module {
    public final static String RECEIVE = "receive";
    private static ClipboardRmiServer server = new ClipboardRmiServer();

    public ClipBoard() {
        super();
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
    }

    private void onReceive(NetworkPackage np) {
        server.setClipboard(np.getValue("text"));
    }
}
