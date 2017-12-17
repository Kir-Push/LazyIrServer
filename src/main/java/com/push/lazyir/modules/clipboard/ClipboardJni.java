package com.push.lazyir.modules.clipboard;

import com.push.gui.basew.MainWin;
import com.push.lazyir.Loggout;
import com.push.lazyir.MainClass;
import com.push.lazyir.devices.NetworkPackage;
import com.push.lazyir.service.BackgroundService;

import java.io.File;
import java.net.URISyntaxException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.push.lazyir.MainClass.executorService;
import static com.push.lazyir.modules.clipboard.ClipBoard.RECEIVE;

/**
 * Created by buhalo on 11.07.17.
 */
public class ClipboardJni {

    private volatile boolean listening = false;
    private Lock lock = new ReentrantLock();


    public ClipboardJni() {
        try {
            System.out.println(new File(MainWin.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile().getPath()+"/libServerClipboard.so");;
          if(  MainClass.isUnix())
            System.load(new File(MainWin.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile().getPath()+"/libServerClipboard.so");
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
        NetworkPackage np =  NetworkPackage.Cacher.getOrCreatePackage(ClipBoard.class.getSimpleName(),RECEIVE);
        np.setValue("text",text);
        BackgroundService.getTcp().sendCommandToAll(np.getMessage());
    }

    public void startListening()
    {
        lock.lock();
        try {

            if (listening) {
                return;
            }
            listening = true;
            executorService.submit(() -> {
                Loggout.d("ClipboardJni", "ListenerStarted");
                startListener();
                Loggout.d("ClipboardJni", "ListenerEnded");
            });
        }finally {
            lock.unlock();
        }

    }

    public void stopListening()
    {
        lock.lock();
        try {
            if (!listening) {
                return;
            }
            listening = false;
            stopListener();
        }finally {
            lock.unlock();
        }
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
