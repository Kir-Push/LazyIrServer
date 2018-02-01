package com.push.lazyir.modules.notifications.messengers;

import com.push.lazyir.devices.NetworkPackage;
import com.push.lazyir.gui.GuiCommunicator;
import com.push.lazyir.modules.Module;
import com.push.lazyir.modules.notifications.notifications.Notification;
import com.push.lazyir.service.BackgroundService;

/**
 * Created by buhalo on 19.04.17.
 */
public class Messengers extends Module {
    private static final String ANSWER = "answer";
    @Override
    public void execute(NetworkPackage np) {
        if(np.getData().equals(ANSWER)) {
            Notification message = np.getObject(NetworkPackage.N_OBJECT, Notification.class);
            GuiCommunicator.show_notification(device.getId(),message);
        }
    }

    @Override
    public void endWork() {

    }

    public static void sendAnswer(String typeName,String text,String id)
    {
        NetworkPackage np =  NetworkPackage.Cacher.getOrCreatePackage(Messengers.class.getSimpleName(),ANSWER);
        np.setValue("typeName",typeName);
        np.setValue("text",text);
        BackgroundService.sendToDevice(id,np.getMessage());
    }
}