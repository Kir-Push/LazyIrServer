package com.push.lazyir.modules.ping;

import com.push.lazyir.api.MessageFactory;
import com.push.lazyir.api.NetworkPackage;
import com.push.lazyir.modules.Module;
import com.push.lazyir.service.main.BackgroundService;

import javax.inject.Inject;

public class Ping extends Module{
    public enum api{
        PING,
        STOP
    }
    @Inject
    public Ping(BackgroundService backgroundService, MessageFactory messageFactory) {
        super(backgroundService, messageFactory);
    }

    @Override
    public void execute(NetworkPackage np) {
        //nothing here to do
    }

    @Override
    public void endWork() {
        //nothing here to do
    }

    public void sendPing(){
        sendMsg(messageFactory.createMessage(this.getClass().getSimpleName(),true,new PingDto(api.PING.name())));
    }
}
