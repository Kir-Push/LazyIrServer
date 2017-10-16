package com.push.lazyir.modules.notifications;


import com.push.lazyir.devices.NetworkPackage;
import com.push.lazyir.gui.Communicator;
import com.push.lazyir.modules.Module;
import com.push.lazyir.service.BackgroundService;

/**
 * Created by buhalo on 26.03.17.
 */

public class SmsModule extends Module {

    private static final String SMS_TYPE = "SmsModule";
    private static final String SEND = "send";
    public static final String RECEIVE = "receive";
    private static final String RESPONSE = "response";

    @Override
    public void execute(NetworkPackage np) {
        if(np.getData().equals(RECEIVE))
        {
            Communicator.getInstance().sendToOut(np.getMessage());
        }
    }

    @Override
    public void endWork() {

    }

    public void send_sms(String name,String text,String dvId) {
        NetworkPackage np =  NetworkPackage.Cacher.getOrCreatePackage(SMS_TYPE,SEND);
        Sms message = new Sms(name,text);
        np.setObject(NetworkPackage.N_OBJECT,message);
        BackgroundService.getTcp().sendCommandToServer(dvId,np.getMessage());
    }



}
