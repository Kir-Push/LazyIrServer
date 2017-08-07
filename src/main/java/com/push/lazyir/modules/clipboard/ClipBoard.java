package com.push.lazyir.modules.clipboard;

import com.push.lazyir.devices.NetworkPackage;
import com.push.lazyir.modules.Module;

import java.awt.datatransfer.Clipboard;

/**
 * Created by buhalo on 19.04.17.
 */
public class ClipBoard extends Module {
    public static String RECEIVE = "receive";
    private volatile static ClipboardJni clipboardJni;

    public ClipBoard() {
        super();
        ClipboardJni localInstance = clipboardJni;
        if(localInstance == null) {
            synchronized (ClipboardJni.class) {
                localInstance = clipboardJni;
                if (localInstance == null) {
                    localInstance = clipboardJni = new ClipboardJni();
                    clipboardJni.startListening();
                }
            }
        }
    }

    @Override
    public void execute(NetworkPackage np) {
        if(np.getData().endsWith(RECEIVE))
        {
            onReceive(np);
        }
    }

    private void onReceive(NetworkPackage np) {
        clipboardJni.setClipboardData(np.getValue("text"));
    }
}
