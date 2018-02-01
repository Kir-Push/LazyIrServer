package com.push.lazyir.modules.ping;

import com.push.lazyir.devices.NetworkPackage;
import com.push.lazyir.modules.Module;

public class Ping extends Module{
    @Override
    public void execute(NetworkPackage np) {

    }

    public void sendPing(){
        NetworkPackage ping = NetworkPackage.Cacher.getOrCreatePackage(Ping.class.getSimpleName(), "Ping");
        sendMsg(ping.getMessage());
    }
}
