package com.push.lazyir.service;

import java.io.*;

public class MainClass {

    public static String selected_id;
    public static String broadcast_adress = "";


    public static void main(String[] args) throws IOException { // main entry where started input output listening com.push.lazyir.gui & ipc thread and main thread;!
        BackgroundService.getInstance().configServices();
        BackgroundService.getInstance().startTasks();
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
