package com.push.lazyir.modules.ping;

import com.push.lazyir.devices.Cacher;
import com.push.lazyir.devices.NetworkPackage;
import com.push.lazyir.modules.Module;
import com.push.lazyir.service.main.BackgroundService;

import javax.inject.Inject;

public class Ping extends Module{
    @Inject
    public Ping(BackgroundService backgroundService, Cacher cacher) {
        super(backgroundService, cacher);
    }

    @Override
    public void execute(NetworkPackage np) {

    }

    public void sendPing(){
        NetworkPackage ping = cacher.getOrCreatePackage(Ping.class.getSimpleName(), "Ping");
        sendMsg(ping.getMessage());
    }
}
