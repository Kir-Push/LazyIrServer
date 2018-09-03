package com.push.lazyir.modules.clipboard;

import com.push.lazyir.modules.clipboard.remote.ClientRegister;
import com.push.lazyir.modules.clipboard.remote.ClipboardRmiSeparateProcess;

import java.io.File;
import java.net.URISyntaxException;
import java.rmi.RemoteException;

public class ClipboardJni {

    private boolean listening = false;
    private static ClientRegister clientServer;

    public ClipboardJni(String libend) {
        try {
            System.load(new File(ClipboardRmiSeparateProcess.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile().getPath()+ File.separator + "libServerClipboard" + libend);
        } catch (URISyntaxException e) {
            System.exit(1);
        }
    }

    public static void setClientRegister(ClientRegister server) {
        clientServer = server;
    }

    public native void startListener();
    public native void stopListener();
    public native String getClipboardText();
    public native void setClipboardText(String text);

    public static void clipboardChanged(String text) {
        try {
            if (clientServer != null) {
                clientServer.receiveClipboard(text);
            }
        }catch (RemoteException e){

        }
    }

    public void startListening() {
        if (listening) {
            return;
        }
        listening = true;
        startListener();
    }

    public void stopListening() {
        if (!listening) {
            return;
        }
        listening = false;
        stopListener();
    }

    public void setClipboardData(String text) {
        if(!listening) {
            return;
        }
        setClipboardText(text);
    }

}
