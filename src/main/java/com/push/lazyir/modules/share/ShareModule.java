package com.push.lazyir.modules.share;



import com.push.lazyir.Loggout;
import com.push.lazyir.devices.Device;
import com.push.lazyir.devices.NetworkPackage;
import com.push.lazyir.modules.Module;
import com.push.lazyir.service.BackgroundService;

import javax.swing.filechooser.FileSystemView;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.push.lazyir.MainClass.executorService;


/**
 * Created by buhalo on 05.03.17.
 */
public class ShareModule extends Module {

    private static final String SHARE_T = "ShareModule";
    private static final String SETUP_SERVER_AND_SEND_ME_PORT = "setup server and send me port";
    private static final String CONNECT_TO_ME_AND_RECEIVE_FILES = "connect to me and receive files"; // first arg port,second number of files - others files
    private static final String PORT = "port";
    private static final String RECCONECT = "recconect";

    private int port = 9000;
    private String userName;
    private String pass;
    private Future futuresftp;
    private SftpServerProcess sftpServerProcess;
    private Lock lock = new ReentrantLock();


    @Override
    public void execute(NetworkPackage np) {
        if(np.getData().equals(CONNECT_TO_ME_AND_RECEIVE_FILES))
        {
            connectToSftpServer(np);
        }
        else if(np.getData().equalsIgnoreCase(RECCONECT))
        {
             userName = np.getValue("userName");
             pass = np.getValue("pass");
             if(userName != null && pass != null)
             recconectToSftp(np.getId());
        }
    }


    @Override
    public void endWork() {
        stopSftpServer();
    }

    private void connectToSftpServer(NetworkPackage np) {
        lock.lock();
        try {
            String value = np.getValue(PORT);
            //   if(connectedToserver)
            stopSftpServer();
            port = Integer.parseInt(value);
            userName = np.getValue("userName");
            pass = np.getValue("pass");
            sftpServerProcess = new SftpServerProcess(port, Device.getConnectedDevices().get(np.getId()).getIp(), userName, pass,device.getId());
            futuresftp = executorService.submit(sftpServerProcess);
        }finally {
          lock.unlock();
        }
    }

    public void recconectToSftp(String id) //todo in android
    {
        lock.lock();
        try {
       stopSftpServer();
       if(userName == null || pass == null)
       {
           NetworkPackage np =  NetworkPackage.Cacher.getOrCreatePackage(SHARE_T,RECCONECT);
           BackgroundService.getTcp().sendCommandToServer(id,np.getMessage());
       }
       else
       {
          sftpServerProcess =(sftpServerProcess == null ?  new SftpServerProcess(port, Device.getConnectedDevices().get(id).getIp(),userName,pass,device.getId()) : sftpServerProcess);
           futuresftp = executorService.submit(sftpServerProcess);
       }}finally {
            lock.unlock();
        }
    }

    private void stopSftpServer() {

        lock.lock();
        try{
        if(sftpServerProcess != null && futuresftp != null) {
            futuresftp.cancel(true);
        }}finally {
            if(sftpServerProcess != null)
                sftpServerProcess.stopProcess();
            lock.unlock();
        }

    }

    public static void sendSetupServerCommand(String dvID)
    {
        NetworkPackage np =  NetworkPackage.Cacher.getOrCreatePackage(SHARE_T,SETUP_SERVER_AND_SEND_ME_PORT);
        String os  = System.getProperty("os.name").toLowerCase();
        if(os.indexOf("win") >= 0)
        {
            os = "win";
        }
        else if(os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0 || os.indexOf("aix") > 0 )
        {
            os = "nix";
        }
        else
        {
            os = "hz";
        }
        np.setValue("os",os);
        BackgroundService.getTcp().sendCommandToServer(dvID,np.getMessage());
    }

}
