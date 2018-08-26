package com.push.lazyir.modules.ping;

import com.push.lazyir.devices.CacherOld;
import com.push.lazyir.devices.NetworkPackageOld;
import com.push.lazyir.modules.Module;
import com.push.lazyir.service.main.BackgroundService;

import javax.inject.Inject;

public class Ping extends Module{
    @Inject
    public Ping(BackgroundService backgroundService, CacherOld cacher) {
        super(backgroundService, cacher);
    }

    @Override
    public void execute(NetworkPackageOld np) {

    }

    public void sendPing(){
        NetworkPackageOld ping = cacher.getOrCreatePackage(Ping.class.getSimpleName(), "Ping");
        sendMsg(ping.getMessage());
    }
}
