package com.push.lazyir.modules.clipboard;

import com.push.lazyir.modules.clipboard.Rmi.ClientRegister;
import com.push.lazyir.modules.clipboard.Rmi.ClipboardRmiSeparateProcess;

import java.io.File;
import java.net.URISyntaxException;
import java.rmi.RemoteException;

/**
 * Created by buhalo on 11.07.17.
 */
public class ClipboardJni {

    private volatile boolean listening = false;
    private static ClientRegister server;

    public ClipboardJni() {
    }

    // todo проверь внимательно чтоб нельзя было два раза запустить и типо того
    public ClipboardJni(String libend, ClientRegister server) {
        try {
            System.load(new File(ClipboardRmiSeparateProcess.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile().getPath()+"/libServerClipboard" + libend);
            ClipboardJni.server = server;
        } catch (URISyntaxException e) {
        }
    }

    native public void startListener();
    native public void stopListener();
    native public String getClipboardText();
    native public void setClipboardText(String text);

    public static void clipboardChanged(String text)
    {
        if(server != null){
            try {
                server.receiveClipboard(text);
            } catch (RemoteException e) {
                //todo
            }
        }
    }

    public void startListening()
    {

            if (listening) {
                return;
            }
            listening = true;
        startListener();
    }

    public void stopListening()
    {

            if (!listening) {
                return;
            }
            listening = false;
            stopListener();
    }

    public void setClipboardData(String text)
    {
        if(!listening)
        {
            return;
        }
        try {
            setClipboardText(text);
        }catch (Throwable e)
        {
           e.printStackTrace();
        }
    }

}
