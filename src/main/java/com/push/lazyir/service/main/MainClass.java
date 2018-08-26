package com.push.lazyir.service.main;

import java.io.*;
import java.util.concurrent.ConcurrentHashMap;

public class MainClass {

    public static String selected_id;
    public static String broadcast_adress = "";
    public final static ConcurrentHashMap<Long,Process> startedProcesses = new ConcurrentHashMap<>();


    public static void main(BackgroundService backgroundService) { // main entry where started input output listening com.push.lazyir.gui & ipc thread and main thread;!
        Runtime.getRuntime().addShutdownHook(new Thread() { //todo!!!
            public void run() {
                for (Process process : startedProcesses.values()) {
                    process.destroyForcibly();
                }
            }
        });
        backgroundService.startTasks();
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
