package com.push.lazyir.modules.clipBoard;

/**
 * Created by buhalo on 18.04.17.
 */
import com.push.lazyir.devices.NetworkPackage;
import com.push.lazyir.managers.TcpConnectionManager;

import java.awt.*;
import java.awt.datatransfer.*;
import java.io.IOException;

import static com.push.lazyir.modules.clipBoard.ClipBoard.RECEIVE;
import static com.push.lazyir.modules.clipBoard.ClipBoard.inserted;

public class BoardListener implements ClipboardOwner {
    Clipboard sysClip = Toolkit.getDefaultToolkit().getSystemClipboard();

    public void run() {
        Transferable trans = sysClip.getContents(this);
        regainOwnership(trans);
    }

    public void lostOwnership(Clipboard c, Transferable t) {
            Transferable contents = sysClip.getContents(this); //EXCEPTION
            processContents(contents);
            regainOwnership(contents);

    }

    void processContents(Transferable t) {
        if(inserted)
        {
            inserted = false;
            return;
        }
        try {
            String text = (String)sysClip.getData(DataFlavor.stringFlavor);
            NetworkPackage np = new NetworkPackage(ClipBoard.class.getSimpleName(),RECEIVE);
            np.setValue("text",text);
            TcpConnectionManager.getInstance().sendCommandToAll(np.getMessage());
        } catch (UnsupportedFlavorException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void regainOwnership(Transferable t) {
        sysClip.setContents(t, this);
    }

    public static void main(String[] args) {
        BoardListener b = new BoardListener();
        b.run();
        while (true)
        {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}