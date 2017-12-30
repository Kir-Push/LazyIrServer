package com.push.lazyir.modules.share;



import com.push.lazyir.devices.Device;
import com.push.lazyir.devices.NetworkPackage;
import com.push.lazyir.modules.Module;
import com.push.lazyir.service.BackgroundService;
import com.push.lazyir.service.MainClass;

import java.io.File;
import java.net.InetAddress;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.push.lazyir.service.settings.SettingManager.currentUsersHomeDir;


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
    private String mountPoint;
    private PathWrapper externalMountPoint;
    private Future futuresftp;
    private SftpServerProcess sftpServerProcess;
    private static Lock staticLock = new ReentrantLock();

    @Override
    public void execute(NetworkPackage np) {
        staticLock.lock();
        try {
            if (np.getData().equals(CONNECT_TO_ME_AND_RECEIVE_FILES)) {
                connectToSftpServer(np);
            } else if (np.getData().equalsIgnoreCase(RECCONECT)) {
                recconectToSftp(np);
            }
        }finally {
            staticLock.unlock();
        }
    }


    @Override
    public void endWork() {
        stopSftpServer();
        clearFolders();
    }


    // when last device disconnected or close sftp connection, erase all folders in connectedDevicePath
    private void clearFolders() {
        staticLock.lock();
        try{
            int size = Device.getConnectedDevices().size();
            if(size == 0 || (Device.getConnectedDevices().containsKey(device.getId()) && size == 1)) {
                File file = new File(currentUsersHomeDir);
                if(file.exists()){
                   file.delete();
                }
            }
        }finally {
            staticLock.unlock();
        }
    }

    private void connectToSftpServer(NetworkPackage np) {
        lock.lock();
        try {
            String value = np.getValue(PORT);
            stopSftpServer();
            port = Integer.parseInt(value);
            userName = np.getValue("userName");
            pass = np.getValue("pass");
            mountPoint = np.getValue("mainDir");
            externalMountPoint = np.getObject("externalPath",PathWrapper.class);
            if(userName == null || pass == null) {
                NetworkPackage tryMore =  NetworkPackage.Cacher.getOrCreatePackage(SHARE_T,RECCONECT);
                BackgroundService.sendToDevice(device.getId(),tryMore.getMessage());
                return;
            }
            sftpServerProcess = instantiateSftpServerProcess(port, device.getIp(),mountPoint,externalMountPoint, userName, pass,device.getId());
            futuresftp = BackgroundService.submitNewTask(sftpServerProcess);
        }finally {
          lock.unlock();
        }
    }

    public void recconectToSftp(NetworkPackage np) //todo in android
    {
          connectToSftpServer(np);
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

    private SftpServerProcess instantiateSftpServerProcess(int port, InetAddress ip, String mountPoint, PathWrapper externalMountPoint, String userName, String pass, String id){
        if(MainClass.isUnix())
            return new SftpServerProcessNix(port, ip,mountPoint,externalMountPoint, userName, pass,id,currentUsersHomeDir);
        else if(MainClass.isWindows())
            return new SftpServerProcessWin(port, ip,mountPoint,externalMountPoint, userName, pass,id,currentUsersHomeDir);
        return null;
    }

    public static void sendSetupServerCommand(String dvID)
    {
        NetworkPackage np =  NetworkPackage.Cacher.getOrCreatePackage(SHARE_T,SETUP_SERVER_AND_SEND_ME_PORT);
        String os;
        if(MainClass.isWindows())
            os = "win";
        else if(MainClass.isUnix())
            os = "nix";
        else
            os = "hz";
        np.setValue("os",os);
        BackgroundService.sendToDevice(dvID,np.getMessage());
    }

}
