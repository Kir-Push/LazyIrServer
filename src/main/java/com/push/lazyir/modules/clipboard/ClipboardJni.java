package com.push.lazyir.modules.clipboard;

import com.push.lazyir.Loggout;
import com.push.lazyir.MainClass;
import com.push.lazyir.devices.NetworkPackage;
import com.push.lazyir.managers.TcpConnectionManager;
import com.push.lazyir.service.BackgroundService;

import java.io.File;
import java.net.URISyntaxException;

import static com.push.lazyir.MainClass.executorService;
import static com.push.lazyir.modules.clipboard.ClipBoard.RECEIVE;

/**
 * Created by buhalo on 11.07.17.
 */
public class ClipboardJni {

    private boolean listening = false;


    public ClipboardJni() {
        try {
          if(  MainClass.isUnix())
            System.load(new File(MainClass.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile().getPath()+"/libServerClipboard.so");
            else if(MainClass.isWindows())
          {
              //todo // System.load(new File(MainClass.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile().getPath()+"/libServerClipboard.dll");
          }
        } catch (URISyntaxException e) {
           Loggout.e("ClipboardJni",e.toString());
        }
    }

    native public void startListener();
    native public void stopListener();
    native public String getClipboardText();
    native public void setClipboardText(String text);

    public static void clipboardChanged(String text)
    {
        Loggout.d("ClipboardJni","  Clipboard java received - " + text);
        NetworkPackage np = new NetworkPackage(ClipBoard.class.getSimpleName(),RECEIVE);
        np.setValue("text",text);
        BackgroundService.getTcp().sendCommandToAll(np.getMessage());
    }

    public void startListening()
    {
        if(listening)
        {
            return;
        }
        listening = true;
        executorService.submit(() -> {
            Loggout.d("ClipboardJni","ListenerStarted");
            startListener();
            Loggout.d("ClipboardJni","ListenerEnded");
        });
    }

    public void stopListening()
    {
        if(!listening)
        {
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
        setClipboardText(text);
    }

}
