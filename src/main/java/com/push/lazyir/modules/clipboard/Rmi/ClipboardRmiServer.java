package com.push.lazyir.modules.clipboard.Rmi;

import com.push.lazyir.devices.NetworkPackage;
import com.push.lazyir.modules.clipboard.ClipBoard;
import com.push.lazyir.service.BackgroundService;
import com.push.lazyir.service.MainClass;

import java.awt.datatransfer.Clipboard;
import java.io.File;
import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import static com.push.lazyir.modules.clipboard.ClipBoard.RECEIVE;

public class ClipboardRmiServer implements ClientRegister {

    private ClipboardChanger changer;
    private boolean listening;

    @Override
    public void register(ClipboardChanger changer) throws RemoteException {
      this.changer = changer;
    }

    @Override
    public void receiveClipboard(String text) throws RemoteException {
        NetworkPackage np =  NetworkPackage.Cacher.getOrCreatePackage(ClipBoard.class.getSimpleName(),RECEIVE);
        np.setValue("text",text);
        BackgroundService.sendToAllDevices(np.getMessage());
    }


    public void startListening(){
        try{
            if(listening){
                return;
            }
            listening = true;
            ClientRegister stub = (ClientRegister) UnicastRemoteObject.exportObject(this,0);

            Registry registry = LocateRegistry.createRegistry(Integer.parseInt(BackgroundService.getSettingManager().get("jniPort")));
            registry.bind("ClientRegister",stub);

            startSepateProcessWithClient();
        }catch (AlreadyBoundException e){

        }catch (RemoteException e){
        }
    }

    private void startSepateProcessWithClient() {
        String javaHome = System.getProperty("java.home");
        String javaBin = javaHome +
                File.separator + "bin" +
                File.separator + "java";
        String classpath = System.getProperty("java.class.path");
        String className = ClipboardRmiSeparateProcess.class.getCanonicalName();

        ProcessBuilder builder = new ProcessBuilder(
                javaBin, "-cp", classpath, className);

        Process process = null;
        try {
            process = builder.start();
            MainClass.startedProcesses.put(process.pid(),process);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setClipboard(String text){
        if(changer != null){
            try {
                changer.setClipboard(text);
            } catch (RemoteException e) {
            }
        }
    }

}
