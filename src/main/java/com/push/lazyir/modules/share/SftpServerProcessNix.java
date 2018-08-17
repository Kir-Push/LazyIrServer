package com.push.lazyir.modules.share;

import com.push.lazyir.Loggout;
import com.push.lazyir.service.main.MainClass;
import com.push.lazyir.gui.GuiCommunicator;

import java.io.*;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by buhalo on 02.04.17.
 */
public class SftpServerProcessNix implements SftpServerProcess {
    private int port;
    private InetAddress ip;
    private String pass;
    private String userName;
    private String programm = "sshfs";
    private String mountPoint;
    private PathWrapper externalMountPoint;
    private String id;
    private List<String> args = new ArrayList<>();
    private Process process;
    private  String currentUsersHomeDir;
    private File currDeviceDir;
    private String driveLetter;
    private Process start;
    private volatile boolean running;
    private  int i = 1;
    private Lock lock = new ReentrantLock();

    public SftpServerProcessNix(int port, InetAddress ip, String mountPoint, PathWrapper externalMountPoint, String userName, String pass, String id,String currentUsersHomeDir) {
        this.port = port;
        this.ip = ip;
        this.pass = pass;
        this.userName = userName;
        this.mountPoint = mountPoint;
        this.externalMountPoint = externalMountPoint;
        this.id = id;
        this.currentUsersHomeDir = currentUsersHomeDir;
        File dir = new File(currentUsersHomeDir);
        if(!dir.exists())
         dir.mkdir();
        createDirForCurrentDevice();
        configShhfs();
        try {
            createLinks();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createDirForCurrentDevice() {
        StringBuilder stringBuilder = new StringBuilder(currentUsersHomeDir);
        stringBuilder.append(File.separator);
        stringBuilder.append(id);
        Random random = new Random();
        for(int i = 0;i<3;i++)
        stringBuilder.append(random.nextInt(10));

        currDeviceDir = new File(stringBuilder.toString());
        if(currDeviceDir.exists())
            createDirForCurrentDevice();
        currDeviceDir.mkdir();
        currDeviceDir.deleteOnExit(); // что будет если sftp еще работает а jvm уже прекращает работу, не удалит ли он файла на телефоне? проверь
    }

    private void configShhfs(){
        args.add(programm);
        args.add(userName + "@" + ip.getHostAddress() + ":" + "/");
        args.add(currDeviceDir.getPath());
        args.add("-p");
        args.add(Integer.toString(this.port));
        args.add("-f");
        args.add("-F");
        args.add("/dev/null");
        args.add("-o");
        args.add("StrictHostKeyChecking=no");
        args.add("-o");
        args.add("UserKnownHostsFile=/dev/null");
        args.add("-o");
        args.add("reconnect");
        args.add("-o");
        args.add("ServerAliveInterval=60");
        args.add("-o");
        args.add("password_stdin");
    }

    private void createLinks() throws IOException {
        File file = new File(System.getProperty("user.home") + File.separator + "ConnectedDevices");
        boolean mkdir = true;
        if (!file.exists())
            mkdir = file.mkdir();

        boolean devicePathmk = true;
        File deviceLink;
        if (mkdir) {
            deviceLink = new File(file.getPath() + File.separator + userName + id.substring(id.length() - 3, id.length()));
            if (!deviceLink.exists())
                devicePathmk = deviceLink.mkdir();
            if (devicePathmk) {
                File[] files = deviceLink.listFiles();
                if (files != null)
                    for (File file1 : files) {
                        file1.delete();
                    }

                Paths.get(deviceLink.getPath() + File.separator + "MainStorage").toFile().delete();
                Path mainStorageLink = Files.createSymbolicLink(Paths.get(deviceLink.getPath() + File.separator + "MainStorage"), Paths.get(currDeviceDir.getPath() + File.separator + mountPoint));
                if (externalMountPoint != null && externalMountPoint.getPaths() != null && externalMountPoint.getPaths().size() > 0) {
                    int count = 0;
                    for (String s : externalMountPoint.getPaths()) {
                        count++;
                        Files.createSymbolicLink(Paths.get(deviceLink.getPath() + File.separator + "ExternalStorage" + count), Paths.get(currDeviceDir.getPath() + File.separator + s));
                    }

                }
            }
        }


    }

    @Override
    public void run() {
        if (running) {
            return;
        }
        String answer = null;
        try {
            answer = connect();
            int tryCount = 5;
            while ((answer != null && (answer.contains("refused") || answer.contains("Error")) || i != 0) && tryCount > 0) {
                if (process != null)
                    process.destroy();
                try {
                    Thread.sleep(500);
                    answer = connect();
                    Loggout.e("Second try sftp ", answer);
                } catch (InterruptedException | IOException e) {
                    Loggout.e("Sftp", "run second try", e);
                    running = false;
                    break;
                }
                tryCount--;
            }
            running = (i == 0);
        } catch (IOException | InterruptedException e) {
            Loggout.e("sftp", "connect", e);
        }

        GuiCommunicator.sftpConnectResult(running, id);
    }

    @Override
    public String connect() throws IOException, InterruptedException {
        lock.lock();
        try{
            ProcessBuilder pb = new ProcessBuilder(args);
            BufferedWriter writer = null;
            BufferedReader reader = null;
            String answer = null;
            process = pb.start();
            MainClass.startedProcesses.put(process.pid(),process);
            process.getInputStream();
            writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
            writer.write(pass + "\n");
            writer.flush();
            writer.close();
            reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            answer = reader.readLine();
            i = (process.waitFor(50, TimeUnit.MILLISECONDS)) ? 1 : 0;
            try {
                writer.close();
                reader.close();
            } catch (IOException e) {
                Loggout.e("Sftp", "run second try", e);
            }
            return answer;
        }finally{
            lock.unlock();
        }
    }
    

    @Override
    public void stopProcess()
    {
        lock.lock();
        try {
            List<String> closeArgs = new ArrayList<>();
            closeArgs.add("fusermount");
            closeArgs.add("-u");
            closeArgs.add(currentUsersHomeDir);
            ProcessBuilder pb = new ProcessBuilder(closeArgs);
            try {
                Process unmount = pb.start();
                unmount.waitFor(200, TimeUnit.MILLISECONDS);
            } catch (IOException | InterruptedException e) {
                Loggout.e("Sftp", "stopProcess", e);
            }
            running = false;
        } finally {
            process.destroy();
            lock.unlock();
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }
}
