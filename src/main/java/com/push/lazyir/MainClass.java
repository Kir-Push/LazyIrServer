package com.push.lazyir;

import com.push.lazyir.gui.Communicator;
import com.push.lazyir.service.BackgroundService;

import java.io.*;

/**
 * Created by buhalo on 12.03.17.
 */
public class MainClass {

    public static String selected_id;
    public static String broadcast_adress = "";

    public static void main(String[] args) throws IOException { // main entry where started input output listening com.push.lazyir.gui & ipc thread and main thread;!

        new Thread(Communicator.getInstance()).start();
        BackgroundService.getInstance().startUdpListening();
        BackgroundService.getInstance().connectCached();
        BackgroundService.getInstance().startTcpListening();
        BackgroundService.getInstance().startClipBoardListener();
    }

}
