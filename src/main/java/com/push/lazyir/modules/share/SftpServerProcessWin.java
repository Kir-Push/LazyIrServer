package com.push.lazyir.modules.share;

import com.push.lazyir.Loggout;
import com.push.lazyir.service.MainClass;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SftpServerProcessWin implements SftpServerProcess {
    private int port;
    private InetAddress ip;
    private String pass;
    private String userName;
    private String programm;
    private String mountPoint;
    private PathWrapper externalMountPoint;
    private String id;
    private List<String> args;
    private Process process;
    private  String currentUsersHomeDir;
    private Process start;
    public String driveLetter;
    private volatile boolean running;
    private  int i = 1;
    private Lock lock = new ReentrantLock();

    // todo !! todo todo !! todo
    public SftpServerProcessWin(int port, InetAddress ip, String mountPoint, PathWrapper externalMountPoint, String userName, String pass, String id,String currentUsersHomeDir) {
        this.port = port;
        this.ip = ip;
        this.pass = pass;
        this.userName = userName;
        this.externalMountPoint = externalMountPoint;
        this.mountPoint = mountPoint;
        this.id = id;
        this.currentUsersHomeDir = currentUsersHomeDir;
         args = new ArrayList<>();

        if(MainClass.isWindows())
        {
            driveLetter = "T:";
            programm = "FTPUSE";
            args.add(programm);
            args.add(driveLetter);
            args.add(ip.getHostAddress()+mountPoint);
            args.add(pass);
            args.add("/USER:"+userName);
            args.add("/PORT:"+String.valueOf(port));
        }
    }

    @Override
    public void run() {

//            try {
//                ProcessBuilder pb = new ProcessBuilder(args);
//                start = pb.start();
//                running = true;
//            } catch (IOException e) {
//                Loggout.e("Sftp", e.toString());
//                running = false;
//            }

    }

    @Override
    public String connect() throws IOException, InterruptedException {
        return null;
    }

    @Override
    public void stopProcess() {
        List<String> closeArgs = new ArrayList<>();
        if (MainClass.isUnix()) {
            if (MainClass.isWindows()) {
                closeArgs.add(programm);
                closeArgs.add(driveLetter);
                closeArgs.add("/DELETE");
            }
        }
    }
    @Override
    public boolean isRunning() {
        return false;
    }
}
