package com.push.lazyir.modules.share;

import com.push.lazyir.Loggout;
import com.push.lazyir.service.MainClass;
import com.push.lazyir.gui.GuiCommunicator;

import java.io.*;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by buhalo on 02.04.17.
 */
public class SftpServerProcess implements Runnable{
    private int port;
    private InetAddress ip;
    private String pass;
    private String userName;
    private String programm;
    private String mountPoint;
    private String id;
    private List<String> args;
    private Process process;
    private  String currentUsersHomeDir;
    public String driveLetter;
    private volatile boolean running;
    private  int i = 1;
    private Lock lock = new ReentrantLock();

    public SftpServerProcess(int port, InetAddress ip,String userName,String pass,String id) {
        this.port = port;
        this.ip = ip;
        this.pass = pass;
        this.userName = userName;
        //todo in android send info about all path, uncluding emolated/1 /2 and sd card path
        // todo and here create folders with these path!
        this.mountPoint = "/storage/emulated/0";
        this.id = id;
        args = new ArrayList<>();
        if(MainClass.isUnix()) {
            programm = "sshfs";
            currentUsersHomeDir = System.getProperty("user.home") + File.separator + userName;
            Path path = Paths.get(currentUsersHomeDir);
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                Loggout.e("Sftp", "Constructor",e);
            }
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
            args.add("reconnect");
            args.add("-o");
            args.add("ServerAliveInterval=60");
            args.add("-o");
            args.add("password_stdin");
         //   args.add("-o");
           // args.add("IdentityFile="+ SettingManager.getInstance().getKeyPath());
        }
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
            if (running) {
                //stopProcess();
                return;
            }
            if (MainClass.isUnix()) {
                String answer = null;
                try {
                 answer = connect();
                    int tryCount = 5;
                    while ((answer != null && (answer.contains("refused") || answer.contains("Error")) || i != 0) && tryCount > 0)
                    {
                        if (process != null)
                            process.destroy();
                        try {
                            Thread.sleep(10000);
                            answer = connect();
                            Loggout.e("Second try sftp ", answer);
                        } catch (InterruptedException | IOException e) {
                            Loggout.e("Sftp", "run second try",e);
                            running = false;
                            break;
                        }
                        tryCount--;
                    }
                    running = i == 0;
                } catch (IOException | InterruptedException e) {
                    Loggout.e("sftp","connect",e);
                }
            } else if (MainClass.isWindows()) {
                try {
                    ProcessBuilder pb = new ProcessBuilder(args);
                    pb.start();
                    running = true;
                } catch (IOException e) {
                    Loggout.e("Sftp", e.toString());
                    running = false; //todo some stop
                }
            }
        GuiCommunicator.sftpConnectResult(running,id);
    }

    private String connect() throws IOException,InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(args);
        BufferedWriter writer = null;
        BufferedReader reader = null;
        String answer = null;
            process = pb.start();
            process.getInputStream();
            writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
            writer.write(pass + "\n");
            writer.flush();
            writer.close();
            reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            answer = reader.readLine();
            i = (process.waitFor(200,TimeUnit.MILLISECONDS)) ? 1 : 0 ;
            try {
                writer.close();
                reader.close();
            }catch (IOException e)
            {
                Loggout.e("Sftp", "run second try",e);
            }
            return answer;
    }
//todo handle error mountpoint not empty

    public void stopProcess()
    {
        lock.lock();
        try {
            List<String> closeArgs = new ArrayList<>();
            if (MainClass.isUnix()) {
                closeArgs.add("fusermount");
                closeArgs.add("-u");
                closeArgs.add(currentUsersHomeDir);
            }
            if (MainClass.isWindows()) {
                closeArgs.add(programm);
                closeArgs.add(driveLetter);
                closeArgs.add("/DELETE");
            }
            ProcessBuilder pb = new ProcessBuilder(closeArgs);
            try {
                Process unmount = pb.start();
                unmount.waitFor(1000, TimeUnit.MILLISECONDS);
            } catch (IOException | InterruptedException e) {
                Loggout.e("Sftp", "stopProcess",e);
            }
          //
            running = false;
            Loggout.d("Sftp", "process stopped");
        }finally {
            process.destroy();
            lock.unlock();
        }
    }

    public boolean isRunning() {
        return running;
    }
}
