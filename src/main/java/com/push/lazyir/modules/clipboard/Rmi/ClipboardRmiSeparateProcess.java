package com.push.lazyir.modules.clipboard.Rmi;

import com.push.lazyir.modules.clipboard.ClipboardJni;
import com.push.lazyir.service.main.BackgroundService;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class ClipboardRmiSeparateProcess  implements ClipboardChanger{
    private ClipboardJni clipboardJni;

    @Override
    public void setClipboard(String text) throws RemoteException {
        if(clipboardJni != null){
            clipboardJni.setClipboardData(text);
        }
    }

    public static void main(String[] args) {
        ClipboardRmiSeparateProcess client = new ClipboardRmiSeparateProcess();
        try {
            Registry registry = LocateRegistry.getRegistry(null, Integer.parseInt("7010")); //todo think about getting config from settings manager

            ClientRegister server = (ClientRegister) registry.lookup("ClientRegister");

            ClipboardChanger stub = (ClipboardChanger) UnicastRemoteObject.exportObject(client, 0);

            server.register(stub);
         String arg = null;
         if(isUnix())
             arg = ".so";
         else if(isWindows())
             arg = ".dll";
            client.startJni(arg,server);
        } catch (Exception e) {
            System.exit(1);
        }
    }

    private static boolean isWindows() {
        String OS = System.getProperty("os.name").toLowerCase();
        return (OS.indexOf("win") >= 0);

    }

    private static boolean isUnix() {
        String OS = System.getProperty("os.name").toLowerCase();
        return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 );

    }

    private void startJni(String libend, ClientRegister server){
      clipboardJni = new ClipboardJni(libend,server);
      clipboardJni.startListening();
    }

}
