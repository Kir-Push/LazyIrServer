package com.push.lazyir.modules.notifications.call;

import com.push.lazyir.Loggout;
import com.push.lazyir.devices.Device;
import com.push.lazyir.devices.NetworkPackage;
import com.push.lazyir.gui.GuiCommunicator;
import com.push.lazyir.modules.Module;
import com.push.lazyir.modules.dbus.Mpris;
import com.push.lazyir.service.BackgroundService;

public class CallModule extends Module {
    public static final String CALL = "com.android.call";
    public static final String ENDCALL = "com.android.endCall";
    public static final String ANSWER = "answer";
    private static volatile boolean CALLING = false;
    private static volatile int muteWhenCall = -1;
    private static volatile int muteWhenOutcomingCall = -1;

    public CallModule() {
        if(muteWhenCall == -1)
            muteWhenCall = Boolean.parseBoolean(BackgroundService.getSettingManager().get("muteWhenCall")) ? 1 : 0;
        if(muteWhenOutcomingCall == -1)
            muteWhenOutcomingCall = Boolean.parseBoolean(BackgroundService.getSettingManager().get("muteWhenOutcomingCall")) ? 1 : 0;
    }

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

                    int boolToCheck = 0;
                    String callType = np.getValue("callType");
                    if(callType.equalsIgnoreCase(callTypes.outgoing.name()))
                        boolToCheck = muteWhenOutcomingCall;
                    else if(callType.equalsIgnoreCase(callTypes.incoming.name()))
                        boolToCheck = muteWhenCall;
                    if(boolToCheck == 1)
                        mute(np);
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
                int boolToCheck = 0;
                String callType = np.getValue("callType");
                if(callType.equalsIgnoreCase(callTypes.outgoing.name()))
                    boolToCheck = muteWhenOutcomingCall;
                else if(callType.equalsIgnoreCase(callTypes.incoming.name()))
                    boolToCheck = muteWhenCall;
                if(boolToCheck == 1)
                    unMute(np);
                GuiCommunicator.call_notif_end(np);
            }else if(ANSWER.equals(data)){
                GuiCommunicator.call_notif_end(np);
            }
        }catch (Exception e){
            Loggout.e("CallModule", "execute ",e);
        }
    }

    private void unMute(NetworkPackage np) {

    }

    private void mute(NetworkPackage np) {

    }

    @Override
    public void endWork() {
        if( Device.getConnectedDevices().size() == 0) {
            CALLING = false;
        }
    }
}
