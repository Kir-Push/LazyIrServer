package com.push.lazyir.modules.clipboard.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientRegister extends Remote {
  void register(ClipboardChanger checker)  throws RemoteException;;

  void receiveClipboard(String text)  throws RemoteException;;
}