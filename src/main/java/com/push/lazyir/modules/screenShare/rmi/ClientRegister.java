package com.push.lazyir.modules.screenShare.rmi;

import com.push.lazyir.modules.clipboard.Rmi.ClipboardChanger;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientRegister extends Remote
{
    public void register (ServerRegister register) throws RemoteException;
}
