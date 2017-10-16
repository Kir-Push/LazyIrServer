package com.push.lazyir.modules.notifications;

import com.push.lazyir.devices.NetworkPackage;
import com.push.lazyir.gui.Communicator;
import com.push.lazyir.modules.Module;
import com.push.lazyir.service.BackgroundService;

/**
 * Created by buhalo on 19.04.17.
 */
public class Messengers extends Module {
    public static final String ANSWER = "answer";
    @Override
    public void execute(NetworkPackage np) {
        if(np.getData().equals(ANSWER))
        {
            Communicator.getInstance().sendToOut(np.getMessage());
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
        BackgroundService.getTcp().sendCommandToServer(id,np.getMessage());
    }
}
