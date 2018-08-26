package com.push.lazyir.modules.notifications.sms;


import com.push.lazyir.devices.CacherOld;
import com.push.lazyir.devices.NetworkPackageOld;
import com.push.lazyir.gui.GuiCommunicator;
import com.push.lazyir.modules.Module;
import com.push.lazyir.service.main.BackgroundService;

import javax.inject.Inject;

/**
 * Created by buhalo on 26.03.17.
 */

public class SmsModule extends Module {

    public static final String SMS_TYPE = "SmsModule";
    private static final String SEND = "send";
    public static final String RECEIVE = "receive";
    private static final String RESPONSE = "response";
    private GuiCommunicator guiCommunicator;

    @Inject
    public SmsModule(BackgroundService backgroundService, CacherOld cacher, GuiCommunicator guiCommunicator) {
        super(backgroundService, cacher);
        this.guiCommunicator = guiCommunicator;
    }

    @Override
    public void execute(NetworkPackageOld np) {
        if(np.getData().equals(RECEIVE))
        {
            Sms sms = np.getObject(NetworkPackageOld.N_OBJECT, Sms.class);
            guiCommunicator.show_sms(device.getId(),sms);
        }
    }

    @Override
    public void endWork() {

    }

    public void send_sms(String name,String text,String dvId) {
        NetworkPackageOld np =  cacher.getOrCreatePackage(SMS_TYPE,SEND);
        Sms message = new Sms(name,name,text,null,null);
        np.setObject(NetworkPackageOld.N_OBJECT,message);
        backgroundService.sendToDevice(dvId,np.getMessage());
    }



}
