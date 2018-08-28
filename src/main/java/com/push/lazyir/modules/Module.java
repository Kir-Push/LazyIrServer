package com.push.lazyir.modules;


import com.push.lazyir.api.NetworkPackage;
import com.push.lazyir.devices.Device;
import com.push.lazyir.api.MessageFactory;
import com.push.lazyir.service.main.BackgroundService;

public abstract class Module {

    protected Device device;
    protected BackgroundService backgroundService;
    protected MessageFactory messageFactory;

    public Module(BackgroundService backgroundService, MessageFactory messageFactory) {
        this.backgroundService = backgroundService;
        this.messageFactory = messageFactory;
    }

    public void setDevice(Device dv)
    {
        this.device = dv;
    }
    public abstract void execute(NetworkPackage np);
    public abstract void endWork();
    protected void sendMsg(String msg) {
        backgroundService.sendToDevice(device,msg);
    }
    protected void sendToAll(String msg) {backgroundService.sendToAllDevices(msg);}
}
