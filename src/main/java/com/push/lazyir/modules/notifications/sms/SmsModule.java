package com.push.lazyir.modules.notifications.sms;


import com.push.lazyir.api.MessageFactory;
import com.push.lazyir.api.NetworkPackage;
import com.push.lazyir.gui.GuiCommunicator;
import com.push.lazyir.modules.Module;
import com.push.lazyir.service.main.BackgroundService;

import javax.inject.Inject;


public class SmsModule extends Module {
    public enum api{
        SEND,
        RECEIVE,
        RESPONSE
    }
    private GuiCommunicator guiCommunicator;

    @Inject
    public SmsModule(BackgroundService backgroundService, MessageFactory messageFactory, GuiCommunicator guiCommunicator) {
        super(backgroundService, messageFactory);
        this.guiCommunicator = guiCommunicator;
    }

    @Override
    public void execute(NetworkPackage np) {
        SmsModuleDto dto = (SmsModuleDto) np.getData();
        api command = api.valueOf(dto.getCommand());
        if(command.equals(api.RECEIVE)){
            Sms sms = dto.getSms();
            guiCommunicator.showSms(device.getId(),sms);
        }
    }

    @Override
    public void endWork() {
        //nothing to do
    }

    public void sendSms(String name, String text) {
        Sms sms = new Sms(name,name,text);
        String message = messageFactory.createMessage(this.getClass().getSimpleName(), true, new SmsModuleDto(api.SEND.name(), sms));
        sendMsg(message);
    }



}
