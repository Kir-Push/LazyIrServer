package com.push.lazyir.modules.notifications.messengers;

import com.push.lazyir.devices.Cacher;
import com.push.lazyir.devices.Device;
import com.push.lazyir.devices.NetworkPackage;
import com.push.lazyir.gui.GuiCommunicator;
import com.push.lazyir.modules.Module;
import com.push.lazyir.modules.dbus.Mpris;
import com.push.lazyir.modules.notifications.call.CallModule;
import com.push.lazyir.modules.notifications.notifications.Notification;
import com.push.lazyir.service.main.BackgroundService;

import javax.inject.Inject;

/**
 * Created by buhalo on 19.04.17.
 */
public class Messengers extends Module {
    private static final String ANSWER = "answer";
    private GuiCommunicator guiCommunicator;

    @Inject
    public Messengers(BackgroundService backgroundService, Cacher cacher, GuiCommunicator guiCommunicator) {
        super(backgroundService, cacher);
        this.guiCommunicator = guiCommunicator;
    }

    @Override
    public void execute(NetworkPackage np) {
        if(np.getData().equals(ANSWER)) {
            Notification message = np.getObject(NetworkPackage.N_OBJECT, Notification.class);
            guiCommunicator.show_notification(device.getId(),message);
        }
    }

    @Override
    public void endWork() {

    }

    public void sendAnswer(String typeName,String text,String id)
    {
        NetworkPackage np =  cacher.getOrCreatePackage(Messengers.class.getSimpleName(),ANSWER);
        np.setValue("typeName",typeName);
        np.setValue("text",text);
        backgroundService.sendToDevice(id,np.getMessage());
    }

    public boolean isCalling(Notification not, Device device, NetworkPackage np, boolean isReceive) {
        if(checkForIncomingCall(np,not)) {
            if(isReceive)
                pauseAll(np.getId(),device);
            else
                playAll(np.getId(),device);
            if(CallModule.muteWhenCall == 1){
                CallModule.muteUnmute(isReceive);
            }
            return true;
        }
        else if(checkForOutcomingCall(np,not)){
            if(isReceive)
                pauseAll(np.getId(),device);
            else
                playAll(np.getId(),device);
            if(CallModule.muteWhenOutcomingCall == 1){
                CallModule.muteUnmute(isReceive);
            }
            return true;
        }else if(checkForGoingCall(np,not)){

            return true;
        }else{
            return false;
        }
    }

    private void playAll(String id, Device device) {
        Mpris mpris = (Mpris) device.getEnabledModules().get(Mpris.class.getSimpleName());
        if (mpris != null)
            mpris.playAll(id);
    }

    private void pauseAll(String id,Device device){
        Mpris mpris = (Mpris) device.getEnabledModules().get(Mpris.class.getSimpleName());
        if (mpris != null)
            mpris.pauseAll(id);
    }

    private boolean checkForGoingCall(NetworkPackage np, Notification not) {
        String type = getCallType( not.getPack(),not.getId(),not.getText());
        if(type.equalsIgnoreCase("ongoing"))
            return true;
        return false;
    }

    private boolean checkForOutcomingCall(NetworkPackage np, Notification not) {
        String type = getCallType( not.getPack(),not.getId(),not.getText());
        if(type.equalsIgnoreCase("outcoming"))
            return true;
        return false;
    }

    private boolean checkForIncomingCall(NetworkPackage np, Notification not) {
        String type = getCallType( not.getPack(),not.getId(),not.getText());
        if(type.equalsIgnoreCase("incoming"))
            return true;
        return false;
    }

    private String getCallType(String pack,String id,String text){ //todo
        switch (pack){
            case "com.skype.raider":
                if(id.equalsIgnoreCase("16") || text.equalsIgnoreCase("Ongoing call"))
                    return "ongoing";
                else  if(id.equalsIgnoreCase("??") || text.equalsIgnoreCase("Incoming call"))
                    return "incoming";
                else  if(id.equalsIgnoreCase("??") || text.equalsIgnoreCase("Outcoming call"))
                    return "outcoming";
                break;
            case "com.whatsapp":
                if(id.equalsIgnoreCase("??") || text.equalsIgnoreCase("Ongoing call"))
                    return "ongoing";
                else  if(id.equalsIgnoreCase("??") || text.equalsIgnoreCase("Incoming call"))
                    return "incoming";
                else  if(id.equalsIgnoreCase("??") || text.equalsIgnoreCase("Outcoming call"))
                    return "outcoming";
                break;
            case "org.telegram.messenger":
                if(id.equalsIgnoreCase("??") || text.equalsIgnoreCase("Ongoing call"))
                    return "ongoing";
                else  if(id.equalsIgnoreCase("??") || text.equalsIgnoreCase("Incoming call"))
                    return "incoming";
                else  if(id.equalsIgnoreCase("??") || text.equalsIgnoreCase("Outcoming call"))
                    return "outcoming";
                break;
        }
        return "none";
    }

}
