package com.push.lazyir.modules;


import com.push.lazyir.devices.Device;
import com.push.lazyir.devices.NetworkPackage;
import com.push.lazyir.service.BackgroundService;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by buhalo on 05.03.17.
 */

public abstract class Module {

    protected Device device;

    protected Lock lock = new ReentrantLock();
    protected volatile boolean working = true;


  public   void setDevice(Device dv)
    {
        this.device = dv;
    }


    public abstract void execute(NetworkPackage np);

  public  void endWork(){
      lock.lock();
      try {
          working = false;
      }finally {
          lock.unlock();
      }
  }

    public void sendMsg(String msg) {
        BackgroundService.sendToDevice(device.getId(),msg);
    }


    public void sendToAll(String msg) {BackgroundService.sendToAllDevices(msg);}
}
