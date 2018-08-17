package com.push.lazyir.modules.screenShare.rmi;

import com.push.lazyir.Loggout;
import com.push.lazyir.service.main.MainClass;

import java.io.File;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class ScreenShareRmiServer implements ClientRegister {
    private ServerRegister register;
    private boolean listening;
    private long processPid;

    @Override
    public void register(ServerRegister register) throws RemoteException {
        this.register = register;
    }

    public boolean startListening(){
        try {
            if (listening) {
                return true;
            }
            listening = true;
            ClientRegister stub = (ClientRegister) UnicastRemoteObject.exportObject(this, 0);

//            BackgroundService.getSettingManager().get("jniPort"))
            Registry registry = LocateRegistry.createRegistry(7677);
            registry.bind("ClientRegisterShareD", stub);

            startSeparateProcessWithClient();
            return true;
        }catch (AlreadyBoundException | RemoteException e){
            Loggout.e("ScreenShareRmiServer","Error in startListening",e);
            return false;
        }
    }

    public boolean stopListener(){
        try{
            register.stopListening();
            UnicastRemoteObject.unexportObject(this,true);
            listening = false;
            MainClass.startedProcesses.get(processPid).destroy(); // todo create method in backgroundService to do this.
            MainClass.startedProcesses.remove(processPid);
            return true;
        }catch ( RemoteException e){
            Loggout.e("ScreenShareRmiServer","Error in stopListening",e);
            return false;
        }
    }

    private void startSeparateProcessWithClient() {
        String javaHome = System.getProperty("java.home");
        String javaBin = javaHome +
                File.separator + "bin" +
                File.separator + "java";
        String classpath = System.getProperty("java.class.path");
        String className = ScreenShareRmiSeparateProcess.class.getCanonicalName();

        ProcessBuilder builder = new ProcessBuilder(
                javaBin, "-cp", classpath, className);

        Process process = null;
        try {
            process = builder.start();
            processPid = process.pid();
            MainClass.startedProcesses.put(processPid,process);
        }catch (Exception e) {
          Loggout.e("ScreenShareRmiServer","Error in startSeparateProcess",e);
        }
    }

    public byte[] getScreenShot() throws RemoteException{
        System.out.println("In get screensHot server");
        if(register != null){
            System.out.println("DA");
            byte[] screenshot = register.getScreenshot();
            return register.getScreenshot();
        }
        return null;
    }
}
