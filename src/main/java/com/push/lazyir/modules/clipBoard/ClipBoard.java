package com.push.lazyir.modules.clipBoard;

import com.push.lazyir.devices.NetworkPackage;
import com.push.lazyir.modules.Module;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

/**
 * Created by buhalo on 19.04.17.
 */
public class ClipBoard extends Module {
    public static String RECEIVE = "receive";
    public static boolean inserted = false;

    @Override
    public void execute(NetworkPackage np) {
        if(np.getData().endsWith(RECEIVE))
        {
            onReceive(np);
        }
    }

    private void onReceive(NetworkPackage np) {
        inserted = true;
        StringSelection selection = new StringSelection(np.getValue("text"));
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, selection);
    }
}
