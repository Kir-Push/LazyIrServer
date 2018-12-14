package com.push.lazyir.modules.share;

import com.profesorfalken.jpowershell.PowerShell;
import com.profesorfalken.jpowershell.PowerShellNotAvailableException;
import com.push.lazyir.gui.GuiCommunicator;
import lombok.Cleanup;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


//https://github.com/billziss-gh/sshfs-win
@Slf4j
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
    private String driveLetter;
    private volatile boolean running;
    private static final char[] alphabet = "abcdefghijklmnopqrstuvwxyz".toUpperCase().toCharArray();
    // hash map, key device id, value network driver letter
    private HashMap<String,String> connectedDrivesMap = new HashMap<>();
    private GuiCommunicator guiCommunicator;


    SftpServerProcessWin(int port, InetAddress ip, String mountPoint, PathWrapper externalMountPoint, String userName, String pass, String id, GuiCommunicator guiCommunicator) {
        this.port = port;
        this.ip = ip;
        this.pass = pass;
        this.userName = userName;
        this.externalMountPoint = externalMountPoint;
        this.mountPoint = mountPoint;
        this.id = id;
        this.args = new ArrayList<>();
        this.guiCommunicator = guiCommunicator;
        driveLetter = getFreeDrive();
        programm = "net use";
        fillArgs();
    }

    private void fillArgs(){
        args.add("cmd");
        args.add("/c");
        args.add(programm + " " + driveLetter + " " + "\"\\\\sshfs\\" + userName + "@" + ip.getHostAddress() + "!" + port + mountPoint + "\"" + " " + pass);
    }

    @Override
    public void run() {
        if (running) {
            return;
        }
        try {
            connect();
            running = true;
            connectedDrivesMap.put(id, driveLetter);
            renameDrive(driveLetter, userName + " MainStorage");
            if (externalMountPoint != null) {
                List<String> paths = externalMountPoint.getPaths();
                for (String path : paths) {
                    driveLetter = getFreeDrive();
                    mountPoint = path;
                    fillArgs();
                    connect();
                    connectedDrivesMap.put(id, driveLetter);
                    renameDrive(driveLetter, id + " ExternalStorage");
                }
            }
            guiCommunicator.sftpConnectResult(running, id);
        } catch (IOException e) {
            log.error("error in run ", e);
            running = false;
        }
    }

    @Override
    @Synchronized
    public String connect() throws IOException {
        ProcessBuilder pb = new ProcessBuilder(args);
        Process start = pb.start();
        @Cleanup BufferedReader reader = new BufferedReader(new InputStreamReader(start.getErrorStream()));
        return reader.readLine();
    }

    @Override
    @Synchronized
    public void stopProcess() {
        for (String s : connectedDrivesMap.values()) {
            try {
                List<String> closeArgs = new ArrayList<>();
                closeArgs.add("cmd");
                closeArgs.add("/c");
                closeArgs.add(programm);
                closeArgs.add(s);
                closeArgs.add("/DELETE");
                ProcessBuilder pb = new ProcessBuilder(closeArgs);
                pb.start();
                running = false;
            } catch (IOException e) {
                log.error("error in stopProcess", e);
            }
        }
        connectedDrivesMap.clear();
    }


   // https://github.com/profesorfalken/jPowerShell
    @Synchronized
    private static void renameDrive(String driveLetter,String newName){
        String cmd = " $rename.NameSpace(\"" + driveLetter + "\").Self.Name = \"" + newName +"\"";
        try (PowerShell powerShell = PowerShell.openSession()){
            powerShell.executeCommand("$rename = new-object -ComObject Shell.Application");
            powerShell.executeCommand(cmd);
        } catch (PowerShellNotAvailableException e) {
          log.error("renameDrive Error - driveLetter: "+driveLetter + " newDriverName: " + newName,e);
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
    @Synchronized
    private static String getFreeDrive(){
        try {
            List<String> closeArgs = new ArrayList<>();
            closeArgs.add("cmd");
            closeArgs.add("/c");
            closeArgs.add("FSUTIL");
            closeArgs.add("FSINFO");
            closeArgs.add("DRIVES");
            ProcessBuilder pb = new ProcessBuilder(closeArgs);
            @Cleanup BufferedReader reader = new BufferedReader(new InputStreamReader((pb.start().getInputStream())));
            String firstLine = reader.readLine();
            if(firstLine == null){
                return null; //todo уверен? проверь хорошо!
            }
            String answer = reader.readLine();
            if (answer == null) {
                return "";
            }
            String[] split = answer.split(" ");
            if (split.length <= 1) {
                return "";
            }
            for (char c : alphabet) {
                boolean has = false;
                for (int i = 1; i < split.length; i++) {
                    if (c == split[i].charAt(0)) {
                        has = true;
                    }
                }
                if (!has) {
                    return String.valueOf(c) + ":";
                }
            }
        } catch (IOException e) {
            log.error("error in getFreeDrive ", e);
        }
        return "";
    }

}
