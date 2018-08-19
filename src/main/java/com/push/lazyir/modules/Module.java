package com.push.lazyir.modules;


import com.push.lazyir.devices.Cacher;
import com.push.lazyir.devices.Device;
import com.push.lazyir.devices.NetworkPackage;
import com.push.lazyir.service.main.BackgroundService;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by buhalo on 05.03.17.
 */

public abstract class Module {

    protected Device device;
    protected Lock lock = new ReentrantLock();
    protected BackgroundService backgroundService;
    protected Cacher cacher;

    public Module(BackgroundService backgroundService, Cacher cacher) {
        this.backgroundService = backgroundService;
        this.cacher = cacher;
    }

    public void setDevice(Device dv)
    {
        this.device = dv;
    }


    public abstract void execute(NetworkPackage np);

  public  void endWork(){ //todo made abstract

  }

    public void sendMsg(String msg) {
        backgroundService.sendToDevice(device.getId(),msg);
    }


    public void sendToAll(String msg) {backgroundService.sendToAllDevices(msg);}
}
