package com.push.lazyir.modules.clipboard.Rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClipboardChanger extends Remote {
    public void setClipboard(String text) throws RemoteException;

}
