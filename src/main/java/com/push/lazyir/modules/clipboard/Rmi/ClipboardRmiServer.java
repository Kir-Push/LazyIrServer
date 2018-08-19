package com.push.lazyir.modules.clipboard.Rmi;

import com.push.lazyir.devices.Cacher;
import com.push.lazyir.devices.NetworkPackage;
import com.push.lazyir.modules.clipboard.ClipBoard;
import com.push.lazyir.service.main.BackgroundService;
import com.push.lazyir.service.main.MainClass;

import javax.inject.Inject;
import java.io.File;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import static com.push.lazyir.modules.clipboard.ClipBoard.RECEIVE;

public class ClipboardRmiServer implements ClientRegister {

    private ClipboardChanger changer;
    private boolean listening;
    private Cacher cacher;
    private BackgroundService backgroundService;

    @Inject
    public ClipboardRmiServer(Cacher cacher,BackgroundService backgroundService) {
        this.cacher = cacher;
        this.backgroundService = backgroundService;
    }

    @Override
    public void register(ClipboardChanger changer) throws RemoteException {
      this.changer = changer;
    }

    @Override
    public void receiveClipboard(String text) throws RemoteException {
        NetworkPackage np =  cacher.getOrCreatePackage(ClipBoard.class.getSimpleName(),RECEIVE);
        np.setValue("text",text);
        backgroundService.sendToAllDevices(np.getMessage());
    }


    public void startListening(){
        try{
            if(listening){
                return;
            }
            listening = true;
            ClientRegister stub = (ClientRegister) UnicastRemoteObject.exportObject(this,0);

            Registry registry = LocateRegistry.createRegistry(Integer.parseInt(backgroundService.getSettingManager().get("jniPort")));
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
                javaBin, "-cp", classpath, className); //todo here maybe add port from setting manager

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
