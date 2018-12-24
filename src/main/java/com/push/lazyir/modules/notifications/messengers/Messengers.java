package com.push.lazyir.modules.notifications.messengers;

import com.push.lazyir.api.MessageFactory;
import com.push.lazyir.api.NetworkPackage;
import com.push.lazyir.gui.GuiCommunicator;
import com.push.lazyir.modules.Module;
import com.push.lazyir.modules.dbus.Mpris;
import com.push.lazyir.modules.notifications.NotificationTypes;
import com.push.lazyir.modules.notifications.call.CallModule;
import com.push.lazyir.modules.notifications.notifications.Notification;
import com.push.lazyir.service.main.BackgroundService;

import javax.inject.Inject;

public class Messengers extends Module {
    public enum api{
        ANSWER
    }
    private GuiCommunicator guiCommunicator;

    @Inject
    public Messengers(BackgroundService backgroundService, MessageFactory messageFactory, GuiCommunicator guiCommunicator) {
        super(backgroundService, messageFactory);
        this.guiCommunicator = guiCommunicator;
    }

    @Override
    public void execute(NetworkPackage np) {
        MessengersDto dto = (MessengersDto) np.getData();
        api command = api.valueOf(dto.getCommand());
        if(command.equals(api.ANSWER)) {
            Notification message = dto.getNotification();
            guiCommunicator.showNotification(device.getId(),message);
        }
    }

    @Override
    public void endWork() {
        //nothing here to do
    }

    public void sendAnswer(String typeName,String text) {
        sendMsg(messageFactory.createMessage(this.getClass().getSimpleName(),true,new MessengersDto(api.ANSWER.name(),text,typeName,null)));
    }

    public boolean isCalling(Notification not, boolean isReceive) {
        if(checkForIncomingCall(not)) {
            if(isReceive) {
                pauseAll();
            } else {
                playAll();
            }
            if(CallModule.isMuteWhenCall()){
                CallModule callModule = backgroundService.getModuleById(device.getId(), CallModule.class);
                callModule.mute(isReceive);
            }
            return true;
        } else if(checkForOutcomingCall(not)){
            if(isReceive) {
                pauseAll();
            } else {
                playAll();
            }
            if(CallModule.isMuteWhenOutcomingCall()){
                CallModule callModule = backgroundService.getModuleById(device.getId(), CallModule.class);
                callModule.mute(isReceive);
            }
            return true;
        }else {
            return false;
        }
    }

    private void playAll() {
        Mpris mpris = backgroundService.getModuleById(device.getId(), Mpris.class);
        if (mpris != null)
            mpris.playAll();
    }

    private void pauseAll(){
        Mpris mpris = backgroundService.getModuleById(device.getId(), Mpris.class);
        if (mpris != null)
            mpris.pauseAll();
    }

    private boolean checkForOutcomingCall(Notification not) {
        NotificationTypes type = getCallType( not.getPack(),not.getText());
        return type.equals(NotificationTypes.OUTGOING);
    }

    private boolean checkForIncomingCall(Notification not) {
        NotificationTypes type = getCallType( not.getPack(),not.getText());
        return type.equals(NotificationTypes.INCOMING);
    }

    private NotificationTypes getCallType(String pack,String text){ //todo, create and test detection whether notification call or not
        switch (pack){
            case "com.skype.raider":
              return skypeCallType(text);
            case "com.whatsapp":
              return whatsAppCallType(text);
            case "org.telegram.MESSENGER":
                return telegramCallType(text);
            default:
                break;
        }
        return NotificationTypes.MESSENGER;
    }

    private NotificationTypes telegramCallType(String text) {
        return whatsAppCallType(text); // now it's same
    }

    private NotificationTypes whatsAppCallType(String text) {
        if("Incoming call".equalsIgnoreCase(text))
            return NotificationTypes.INCOMING;
        else if("Outcoming call".equalsIgnoreCase(text))
            return NotificationTypes.OUTGOING;
        return NotificationTypes.MESSENGER;
    }

    private NotificationTypes skypeCallType(String text) {
        return whatsAppCallType(text);
    }

}
