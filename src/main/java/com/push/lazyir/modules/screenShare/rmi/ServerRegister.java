package com.push.lazyir.modules.screenShare.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerRegister extends Remote {

    public byte[] getScreenshot() throws RemoteException;

    public void stopListening() throws RemoteException;
}
