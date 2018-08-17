package com.push.lazyir.modules.screenShare.rmi;

import com.push.lazyir.modules.screenShare.ScreenShotJNi;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import static com.push.lazyir.service.main.MainClass.isUnix;
import static com.push.lazyir.service.main.MainClass.isWindows;

public class ScreenShareRmiSeparateProcess implements  ServerRegister {
    private ScreenShotJNi screenShotJNi;

    private boolean startJNI(String libend, ClientRegister server) throws IOException {
        screenShotJNi = new ScreenShotJNi(libend);
        screenShotJNi.startListener();
        return true;
    }

    @Override
    public byte[] getScreenshot() throws RemoteException {
        return screenShotJNi.getScreenShot();
    }

    @Override
    public void stopListening() throws RemoteException {
        screenShotJNi.stopListener();
        System.exit(1);
    }


    public static void main(String[] args) throws IOException {
        ScreenShareRmiSeparateProcess client = new ScreenShareRmiSeparateProcess();
        try{
            Registry registry = LocateRegistry.getRegistry(null,7677);

            ClientRegister server = (ClientRegister) registry.lookup("ClientRegisterShareD");
            ServerRegister stub = (ServerRegister) UnicastRemoteObject.exportObject(client,0);
            server.register(stub);
            String arg = null;
            if(isUnix())
                arg = ".so";
            else if(isWindows())
                arg = ".dll";
            client.startJNI(arg,server);
        }catch (Exception e) {
            System.exit(1);
        }
    }
}
