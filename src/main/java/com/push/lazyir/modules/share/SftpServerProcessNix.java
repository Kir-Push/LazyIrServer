package com.push.lazyir.modules.share;

import com.push.lazyir.service.main.BackgroundService;
import com.push.lazyir.gui.GuiCommunicator;
import lombok.Cleanup;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static com.push.lazyir.service.managers.settings.SettingManager.CURRENT_USERS_HOME_DIR;

@Slf4j
public class SftpServerProcessNix implements SftpServerProcess {
    private int port;
    private InetAddress ip;
    private String pass;
    private String userName;
    private String mountPoint;
    private PathWrapper externalMountPoint;
    private String id;
    private List<String> args = new ArrayList<>();
    private Process process;
    private File currDeviceDir;
    private boolean running;
    private GuiCommunicator guiCommunicator;
    private BackgroundService backgroundService;
    private boolean errorInitialization;

    SftpServerProcessNix(int port, InetAddress ip, String mountPoint, PathWrapper externalMountPoint, String userName, String pass, String id, BackgroundService backgroundService) {
        this.port = port;
        this.ip = ip;
        this.pass = pass;
        this.userName = userName;
        this.mountPoint = mountPoint;
        this.externalMountPoint = externalMountPoint;
        this.id = id;
        this.guiCommunicator = backgroundService.getGuiCommunicator();
        this.backgroundService = backgroundService;
        try {
            if(!Files.exists(Paths.get(CURRENT_USERS_HOME_DIR))) {
                Files.createDirectory(Paths.get(CURRENT_USERS_HOME_DIR));
            }
            createDirForCurrentDevice();
            configShhfs();
            createLinks();
        } catch (IOException e) {
            errorInitialization = true;
            log.error("Something goes wrong in SftpServerProcessNix",e);
        }
    }

    private void createDirForCurrentDevice() {
        Random random = new Random();
        String path = CURRENT_USERS_HOME_DIR + File.separator + id +
                random.nextInt(10) +
                random.nextInt(10) +
                random.nextInt(10);
        currDeviceDir = new File(path);
        if(currDeviceDir.exists()) {
            createDirForCurrentDevice();
        }else {
            currDeviceDir.mkdir();
        }
        currDeviceDir.deleteOnExit(); // что будет если sftp еще работает а jvm уже прекращает работу, не удалит ли он файла на телефоне? проверь
    }

    private void configShhfs(){
        args.add("sshfs");
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
        if (!file.exists()) {
            mkdir = file.mkdir();
        }
        if (mkdir) {
            File deviceLink = new File(file.getPath() + File.separator + userName + id.substring(id.length() - 3));
            if (!deviceLink.exists()) {
                deviceLink.mkdir();
            } else {
                deleteFiles(deviceLink.listFiles());
            }
            String deviceLinkPath = deviceLink.getPath();
            Path mainStoragePath = Paths.get(deviceLinkPath + File.separator + "MainStorage");
            if(Files.exists(mainStoragePath)) {
                Files.delete(mainStoragePath);
            }
                Files.createSymbolicLink(mainStoragePath, Paths.get(currDeviceDir.getPath() + File.separator + mountPoint));
            if (externalMountPoint != null) {
                List<String> paths = externalMountPoint.getPaths();
                if (paths != null && !paths.isEmpty()) {
                    for (int i = 0; i < paths.size(); i++) {
                        Files.createSymbolicLink(Paths.get(deviceLinkPath + File.separator + "ExternalStorage" + i),
                                Paths.get(currDeviceDir.getPath() + File.separator + paths.get(i)));
                    }
                }
            }
        }
    }

    private void deleteFiles(File[] files) throws IOException {
        if (files != null) {
            for (File fl : files) {
                Files.delete(fl.toPath());
            }
        }
    }



    @Override
    public void run() {
        if (running || errorInitialization) {
            return;
        }
        try {
            int tryCount = 5;
            while (tryCount > 0){
                if (process != null) {
                    process.destroy();
                }
                String answer = connect();
                running = isProcessRunning(answer,process);
                if(running){
                    break;
                }else{
                    tryCount--;
                    Thread.sleep(500);
                }
            }
        } catch (IOException | InterruptedException e) {
            log.error("error in run", e);
            Thread.currentThread().interrupt();
        }
        guiCommunicator.sftpConnectResult(running, id);
    }

    private boolean isProcessRunning(String answer, Process process) {
        return answer != null && !answer.contains("refused") && !answer.contains("Error") && process != null && process.isAlive();
    }

    @Override
    @Synchronized
    public String connect() throws IOException {
            ProcessBuilder pb = new ProcessBuilder(args);
            process = pb.start();
            backgroundService.getStartedProcesses().put(process.pid(),process); //todo
            process.getInputStream();
            @Cleanup BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
            @Cleanup BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            writer.write(pass + "\n");
            writer.flush();
            return reader.readLine();
    }
    

    @Override
    @Synchronized
    public void stopProcess() {
        Process unmount = null;
        try {
            backgroundService.getStartedProcesses().remove(process.pid());
            List<String> closeArgs = new ArrayList<>();
            closeArgs.add("fusermount");
            closeArgs.add("-u");
            closeArgs.add(CURRENT_USERS_HOME_DIR);

            ProcessBuilder pb = new ProcessBuilder(closeArgs);
            unmount = pb.start();
            unmount.waitFor(200, TimeUnit.MILLISECONDS);
        }catch (IOException | InterruptedException e) {
            log.error("error in stopProcess", e);
            Thread.currentThread().interrupt();

        } finally {
            running = false;
            process.destroy();
            if(unmount != null){
                unmount.destroy();
            }
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }
}
