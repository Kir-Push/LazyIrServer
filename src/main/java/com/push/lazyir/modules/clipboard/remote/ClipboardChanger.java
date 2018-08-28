package com.push.lazyir.modules.clipboard.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClipboardChanger extends Remote {
    void setClipboard(String text) throws RemoteException;

}
