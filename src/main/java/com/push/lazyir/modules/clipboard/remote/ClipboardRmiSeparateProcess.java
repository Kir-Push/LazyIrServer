package com.push.lazyir.modules.clipboard.remote;

import com.push.lazyir.modules.clipboard.ClipboardJni;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class ClipboardRmiSeparateProcess  implements ClipboardChanger {
    private ClipboardJni clipboardJni;

    @Override
    public void setClipboard(String text) {
        if(clipboardJni != null){
            clipboardJni.setClipboardData(text);
        }
    }

    public static void main(String[] args) {
        ClipboardRmiSeparateProcess client = new ClipboardRmiSeparateProcess();
        try {
            Registry registry = LocateRegistry.getRegistry(null, Integer.parseInt(args[0]));
            ClientRegister server = (ClientRegister) registry.lookup("ClientRegister");
            ClipboardChanger stub = (ClipboardChanger) UnicastRemoteObject.exportObject(client, 0);
            server.register(stub);
         String arg = null;
         if(isUnix())
             arg = ".so";
         else if(isWindows())
             arg = ".dll";
            client.startJni(arg,server);
        } catch (RemoteException | NotBoundException e) {
            System.exit(1);
        }
    }

    private static boolean isWindows() {
        String os = System.getProperty("os.name").toLowerCase();
        return (os.contains("win"));

    }

    private static boolean isUnix() {
        String os = System.getProperty("os.name").toLowerCase();
        return (os.contains("nix") || os.contains("nux") || os.contains("aix"));

    }

    private void startJni(String libend, ClientRegister server){
      clipboardJni = new ClipboardJni(libend);
      ClipboardJni.setClientRegister(server);
      clipboardJni.startListening();
    }

}
