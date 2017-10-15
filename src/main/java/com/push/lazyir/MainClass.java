package com.push.lazyir;

import com.push.lazyir.gui.Communicator;
import com.push.lazyir.managers.settings.SettingManager;
import com.push.lazyir.managers.tcp.TcpConnectionManager;
import com.push.lazyir.managers.udp.UdpBroadcastManager;
import com.push.lazyir.utils.ExtScheduledThreadPoolExecutor;

import java.io.*;
import java.util.concurrent.*;

import static com.push.lazyir.service.BackgroundService.getInstance;

public class MainClass {

    public static String selected_id;
    public static String broadcast_adress = "";
    public static final ExecutorService executorService = Executors.newCachedThreadPool();
    public static final ScheduledThreadPoolExecutor timerService = new ExtScheduledThreadPoolExecutor(5);

    public static void main(String[] args) throws IOException { // main entry where started input output listening com.push.lazyir.gui & ipc thread and main thread;!

        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
               Loggout.e("Closing Process quit signal","Pitajusj");
                Communicator.tryToEraseAllResource();
            }
        });
        timerService.setRemoveOnCancelPolicy(true);
        timerService.setKeepAliveTime(10, TimeUnit.SECONDS);
        timerService.allowCoreThreadTimeOut(true);
       // executorService.submit(Communicator::getInstance);
        ((ThreadPoolExecutor)executorService).setKeepAliveTime(10,TimeUnit.SECONDS);
        ((ThreadPoolExecutor)executorService).allowCoreThreadTimeOut(true);
        executorService.submit(Communicator.INSTANCE);
     //   new Thread(Communicator.INSTANCE).start();
        getInstance().setSettingManager(new SettingManager());
        getInstance().setTcp(new TcpConnectionManager());
        getInstance().setUdp(new UdpBroadcastManager());
        getInstance().startTcpListening();
        getInstance().startUdpListening();
        getInstance().connectCached();
    }

    public static boolean isWindows() {
        String OS = System.getProperty("os.name").toLowerCase();
        return (OS.indexOf("win") >= 0);

    }

    public static boolean isUnix() {
        String OS = System.getProperty("os.name").toLowerCase();
        return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 );

    }
}
