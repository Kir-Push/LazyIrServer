package com.push.lazyir.modules.clipboard.remote;

import java.rmi.Remote;

public interface ClientRegister extends Remote {
  void register(ClipboardChanger checker);

  void receiveClipboard(String text);
}