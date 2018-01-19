package com.push.lazyir.modules.notifications.call;

import com.push.lazyir.Loggout;
import com.push.lazyir.devices.Device;
import com.push.lazyir.devices.NetworkPackage;
import com.push.lazyir.gui.GuiCommunicator;
import com.push.lazyir.modules.Module;
import com.push.lazyir.modules.dbus.Mpris;

public class CallModule extends Module {
    public static final String CALL = "com.android.call";
    public static final String ENDCALL = "com.android.endCall";
    public static final String ANSWER = "answer";
    private static volatile boolean CALLING = false;

    @Override
    public void execute(NetworkPackage np) {
        String data = np.getData();
        try{
            if(CALL.equals(data))
            {
                if(!CALLING) {
                    Mpris mpris = (Mpris) device.getEnabledModules().get(Mpris.class.getSimpleName());
                    if(mpris != null)
                    {
                        mpris.pauseAll(np.getId());
                    }
                    CALLING = true;
                }
                GuiCommunicator.call_Notif(np);
            }
            else if(CALLING && ENDCALL.equals(data))
            {
                CALLING = false;
                Mpris mpris = (Mpris) device.getEnabledModules().get(Mpris.class.getSimpleName());
                if(mpris != null)
                {
                    mpris.playAll(np.getId());
                }
                GuiCommunicator.call_notif_end(np);
            }else if(ANSWER.equals(data)){
                GuiCommunicator.call_notif_end(np);
            }
        }catch (Exception e){
            Loggout.e("CallModule", "execute ",e);
        }
    }

    @Override
    public void endWork() {
        if( Device.getConnectedDevices().size() == 0) {
            CALLING = false;
        }
    }
}
