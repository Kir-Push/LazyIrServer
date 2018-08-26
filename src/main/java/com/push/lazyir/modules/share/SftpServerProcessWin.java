package com.push.lazyir.modules.share;

import com.profesorfalken.jpowershell.PowerShell;
import com.profesorfalken.jpowershell.PowerShellNotAvailableException;
import com.profesorfalken.jpowershell.PowerShellResponse;
import com.push.lazyir.Loggout;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


//https://github.com/billziss-gh/sshfs-win
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
    private String driveLetter;
    private volatile boolean running;
    private  int i = 1;
    private char[] alphabet = "abcdefghijklmnopqrstuvwxyz".toUpperCase().toCharArray();
    // hash map, key device id, value network driver letter
    private HashMap<String,String> connectedDrivesMap = new HashMap<>();
    private Lock lock = new ReentrantLock();


    public SftpServerProcessWin(int port, InetAddress ip, String mountPoint, PathWrapper externalMountPoint, String userName, String pass, String id,String currentUsersHomeDir) {
        this.port = port;
        this.ip = ip;
        this.pass = pass;
        this.userName = userName;
        this.externalMountPoint = externalMountPoint;
        this.mountPoint = mountPoint;
        this.id = id;
        this.currentUsersHomeDir = currentUsersHomeDir;
        this.args = new ArrayList<>();

        driveLetter = getFreeDrive();
        programm = "net use";
        fillArgs();
    }

    private void fillArgs(){
        args.add("cmd");
        args.add("/c");
        args.add(programm);
        args.add(driveLetter);
        args.add("\\\\sshfs\\" + userName + "@" + ip.getHostAddress() + "!" + port + mountPoint);
        args.add(pass);
    }

    @Override
    public void run() {

        if (running) {
            return;
        }
        try {
            String result = connect();
            running = true;
            connectedDrivesMap.put(id,driveLetter);
            renameDrive(driveLetter,userName + " MainStorage");
            if(externalMountPoint != null){
                List<String> paths = externalMountPoint.getPaths();
                for (String path : paths) {
                    driveLetter = getFreeDrive();
                    mountPoint = path;
                    fillArgs();
                    String externalResult = connect();
                    connectedDrivesMap.put(id,driveLetter);
                    renameDrive(driveLetter,id + " ExternalStorage");
                }

            }
        }  catch (IOException | InterruptedException e) {
          Loggout.e("SftpServerPRocessWin","error in connection ",e);
          running = false;
        }

    }

    @Override
    public String connect() throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(args);
        Process start = pb.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(start.getErrorStream()));
        return reader.readLine();
    }

    @Override
    public void stopProcess() {
        List<String> closeArgs = new ArrayList<>();
        for (String s : connectedDrivesMap.values()) {
            try {
            closeArgs.add("cmd");
            closeArgs.add("/c");
            closeArgs.add(programm);
            closeArgs.add(s);
            closeArgs.add("/DELETE");
            ProcessBuilder pb = new ProcessBuilder(args);
            Process start = pb.start();
            running = false;
            } catch (IOException e) {
               //
            }
        }
    }


   // https://github.com/profesorfalken/jPowerShell
    private void renameDrive(String driveLetter,String newName){
        String cmd = " $rename.NameSpace(\"" + driveLetter + "\").Self.Name = \"" + newName +"\"";
        try {
            PowerShell powerShell1 = PowerShell.openSession();
            PowerShellResponse powerShellResponse = powerShell1.executeCommand("$rename = new-object -ComObject Shell.Application");
             powerShellResponse = powerShell1.executeCommand(cmd);
             powerShell1.close();
        } catch (PowerShellNotAvailableException e) {
          Loggout.e("SftpProcessWin","renameDrive Error ",e);
        }

    }
    @Override
    public boolean isRunning() {
        return running;
    }

    // execute win cmd FSUTIL FSINFO DRIVES, get list of used drives
    // iterate over alphabet list, and on each letter check if exist in list
    // if no return it, it free letter, may iterate over thousand times, much but
    // cmd executes only when device connect to sftp, not too often
    private String getFreeDrive(){
        Runtime rt = Runtime.getRuntime();
        try {
            List<String> closeArgs = new ArrayList<>();
            closeArgs.add("cmd");
            closeArgs.add("/c");
            closeArgs.add("FSUTIL");
            closeArgs.add("FSINFO");
            closeArgs.add("DRIVES");
            ProcessBuilder pb = new ProcessBuilder(closeArgs);
            BufferedReader reader = new BufferedReader(new InputStreamReader((   pb.start().getInputStream())));
            reader.readLine();
            String s = reader.readLine();
            if(s == null)
                return "";
            String[] split = s.split(" ");
            if( split.length <= 1)
                return "";

            for (char c : alphabet) {
                boolean has = false;
                for(int i=1;i<split.length;i++){
                    if(c == split[i].charAt(0)){
                        has = true;
                    }
                }
                if(!has){
                    return String.valueOf(c)+":";
                }
            }

        } catch (IOException e) {
            Loggout.e("SftpServerProcessWIn","error in getFreeDrive ",e);
        }
        return "";
    }

}
