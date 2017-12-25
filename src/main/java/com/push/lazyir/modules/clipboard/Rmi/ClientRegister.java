package com.push.lazyir.modules.clipboard.Rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientRegister extends Remote {
  public void register (ClipboardChanger checker) throws RemoteException;

  public void receiveClipboard(String text) throws RemoteException;
}