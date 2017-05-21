package com.push.lazyir.modules.shareManager;

import com.push.lazyir.Loggout;
import com.push.lazyir.devices.Device;
import com.push.lazyir.managers.SettingManager;
import sun.rmi.runtime.Log;

import java.io.*;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by buhalo on 02.04.17.
 */
public class SftpServerProcess extends Thread{
    private int port;
    private InetAddress ip;
    private String pass;
    private String userName;
    private String programm;
    private String mountPoint;
    private List<String> args;
    private Process process;
    private  String currentUsersHomeDir;
    private String OS;
    public String driveLetter;

    public SftpServerProcess(int port, InetAddress ip,String userName,String pass) {
        OS = System.getProperty("os.name").toLowerCase();
        this.port = port;
        this.ip = ip;
        this.pass = pass;
        this.userName = userName;
        this.mountPoint = "/storage/emulated/0";
        if(isUnix()) {
            programm = "sshfs";
            currentUsersHomeDir = System.getProperty("user.home") + File.separator + userName;
            Path path = Paths.get(currentUsersHomeDir);
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                Loggout.e("Sftp", e.toString());
            }
            args = new ArrayList<>();
            args.add(programm);
            args.add(userName + "@" + ip.getHostAddress() + ":" + mountPoint);
            args.add(currentUsersHomeDir);
            args.add("-p");
            args.add(Integer.toString(this.port));
            args.add("-f");
            args.add("-F");
            args.add("/dev/null");
            args.add("-o");
            args.add("StrictHostKeyChecking=no");
            args.add("-o");
            args.add("UserKnownHostsFile=/dev/null");
//        args.add("-o");
//        args.add("HostKeyAlgorithms=ssh-dss");
            args.add("-o");
            args.add("ServerAliveInterval=60");
            args.add("-o");
            args.add("password_stdin");
         //   args.add("-o");
           // args.add("IdentityFile="+ SettingManager.getInstance().getKeyPath());
        }
        if(isWindows())
        {
            driveLetter = "T:";
            programm = "FTPUSE";
            args.add(programm);
            args.add(driveLetter);
            args.add(ip.getHostAddress()+mountPoint);
            args.add(pass);
            args.add("/USER:"+userName);
            args.add("/PORT:"+port);

        }
    }

    @Override
    public void run() {
        if(isUnix()) {
            ProcessBuilder pb = new ProcessBuilder(args);
            BufferedWriter writer = null;
            BufferedReader reader = null;
            String answer = null;
            int i = 1;
            try {
                process = pb.start();
               // process.getInputStream();
                writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
                writer.write(pass+"\n");
                writer.flush();
                writer.close();
                reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                answer = reader.readLine();
                i =  process.waitFor();
            } catch (IOException | InterruptedException e) {
                Loggout.e("Sftp", e.toString());

            }finally {
                if(answer != null  && (answer.contains("refused") || answer.contains("Error")) || i != 0)
                {
                    if(process != null)
                    {
                        process.destroy();
                    }
                    try {
                        Thread.sleep(60000);
                    } catch (InterruptedException e) {
                        Loggout.e("Sftp", e.toString());
                    }
                    ProcessBuilder pb2 = new ProcessBuilder(args);
                    try {
                        process = pb2.start();
                        writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
                        writer.write(pass+"\n");
                        writer.flush();
                        writer.close();
                        reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                        answer = reader.readLine();
                        reader.close();
                        Loggout.e("Second try sftp ", answer);
                        process.waitFor();
                    } catch (IOException | InterruptedException e) {
                        Loggout.e("Sftp", e.toString());
                    }
                }


                try {
                    reader.close();
                } catch (Exception e1) {
                    Loggout.e("Sftp", e1.toString());
                }
                try {
                    writer.close();
                } catch (Exception e1) {
                    Loggout.e("Sftp", e1.toString());
                }

            }
        }
        else if(isWindows())
        {
            try {
                ProcessBuilder pb = new ProcessBuilder(args);
                pb.start();
            } catch (IOException e) {
                Loggout.e("Sftp", e.toString());
            }
        }
    }


    public void stopProcess()
    {
        List<String> closeArgs = new ArrayList<>();
        if(isUnix()) {
            closeArgs.add("fusermount");
            closeArgs.add("-u");
            closeArgs.add(currentUsersHomeDir);
        }
        if(isWindows())
        {
            closeArgs.add(programm);
            closeArgs.add(driveLetter);
            closeArgs.add("/DELETE");
        }
        ProcessBuilder pb = new ProcessBuilder(closeArgs);
        try {
            Process unmount = pb.start();
            unmount.waitFor();
        } catch (IOException | InterruptedException e) {
            Loggout.e("Sftp", e.toString());
        }
        process.destroy();
        Loggout.d("Sftp", "process stopped");
    }

    private boolean isWindows() {

        return (OS.indexOf("win") >= 0);

    }

    private boolean isUnix() {

        return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 );

    }
}
