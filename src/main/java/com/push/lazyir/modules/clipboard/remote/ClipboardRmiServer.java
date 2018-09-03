package com.push.lazyir.modules.clipboard.remote;

import com.push.lazyir.api.MessageFactory;
import com.push.lazyir.modules.clipboard.ClipBoard;
import com.push.lazyir.modules.clipboard.ClipBoardDto;
import com.push.lazyir.service.main.BackgroundService;
import com.push.lazyir.service.managers.settings.SettingManager;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import static com.push.lazyir.modules.clipboard.ClipBoard.api.RECEIVE;

@Slf4j
public class ClipboardRmiServer implements ClientRegister {

    private ClipboardChanger changer;
    private boolean listening;
    private MessageFactory messageFactory;
    private BackgroundService backgroundService;
    private SettingManager settingManager;
    private Process process;

    @Inject
    public ClipboardRmiServer(MessageFactory messageFactory, BackgroundService backgroundService,SettingManager settingManager) {
        this.messageFactory = messageFactory;
        this.backgroundService = backgroundService;
        this.settingManager = settingManager;
    }

    @Override
    public void register(ClipboardChanger changer)  {
      this.changer = changer;
    }

    @Override
    public void receiveClipboard(String text) {
        System.out.println(text);
        String message = messageFactory.createMessage(ClipBoard.class.getSimpleName(), true, new ClipBoardDto(RECEIVE.name(), text));
        backgroundService.sendToAllDevices(message);
    }

    @Synchronized
    public void startListening(){
        String jniPort = settingManager.get("jniPort");
        try{
            listening = true;
            ClientRegister stub = (ClientRegister) UnicastRemoteObject.exportObject(this,0);
            Registry registry = LocateRegistry.createRegistry(Integer.parseInt(jniPort));
            registry.bind("ClientRegister",stub);
            startSeparateProcessWithClient(jniPort);
        }catch (AlreadyBoundException | RemoteException e){
            log.error("startListening using port - " + jniPort,e);
        }
    }

    private void startSeparateProcessWithClient(String jniPort) {
        String javaHome = System.getProperty("java.home");
        String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
        String classpath = System.getProperty("java.class.path");
        String className = ClipboardRmiSeparateProcess.class.getCanonicalName();
        ProcessBuilder builder = new ProcessBuilder(javaBin, "-cp", classpath, className,jniPort);
        try {
            clearOlderProcess();
            process = builder.start();
            backgroundService.getStartedProcesses().put(process.pid(),process);
        }catch (IOException e) {
            log.error("startSeparateProcessWithClient error - " + javaHome + " " + javaBin + " " + className + " " + className, e);
        }
    }

    private void clearOlderProcess() {
        if(process != null){
            backgroundService.getStartedProcesses().remove(process.pid());
            process.destroyForcibly();
        }
    }

    public void setClipboard(String text) {
        try {
            if (changer != null)
                changer.setClipboard(text);
        }catch (RemoteException e){
            log.error("setClipboard error",e);
        }
    }

    public boolean isListening(){return listening;}

    public boolean isWorking() {
        return (process != null && process.isAlive());
    }

    @Synchronized
    public void stopListening() {
        long pid = process.pid();
        process.destroy();
        backgroundService.getStartedProcesses().remove(pid);
        listening = false;
    }
}
