package com.push.lazyir.modules.notifications.call;

import com.push.gui.entity.NotificationDevice;
import com.push.lazyir.Loggout;
import com.push.lazyir.devices.Device;
import com.push.lazyir.devices.NetworkPackage;
import com.push.lazyir.gui.GuiCommunicator;
import com.push.lazyir.modules.Module;
import com.push.lazyir.modules.dbus.Mpris;
import com.push.lazyir.service.BackgroundService;

import javax.sound.sampled.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class CallModule extends Module {
    public static final String CALL = "com.android.call";
    public static final String ENDCALL = "com.android.endCall";
    public static final String ANSWER = "answer";
    public static final String ANSWER_CALL = "asnwerCall";
    public static final String DECLINE_CALL = "declineCall";
    public static final String MUTE = "mute";
    public static final String MUTE_NOVIBRO = "muteNoVibro";
    public static final String RECALL = "call";
    private static volatile boolean CALLING = false;
    private static volatile int muteWhenCall = -1;
    private static volatile int muteWhenOutcomingCall = -1;
    private static ConcurrentSkipListSet<String> muted = new ConcurrentSkipListSet<>();

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
                if(boolToCheck == 1 || callType.equalsIgnoreCase(callTypes.missedIn.name()))
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
        muteUnmute(false);
        muted.clear();
    }

    private void mute(NetworkPackage np) {
        muteUnmute(true);
    }

    @Override
    public void endWork() {
        if( Device.getConnectedDevices().size() == 0) {
            CALLING = false;
        }
    }

    private static void muteUnmute(boolean mute){
        Mixer.Info [] mixers = AudioSystem.getMixerInfo();
        for (Mixer.Info mixerInfo : mixers) {
            Mixer mixer = AudioSystem.getMixer(mixerInfo);
            Line.Info [] lineInfos = mixer.getTargetLineInfo(); // target, not source
            for (Line.Info lineInfo : lineInfos) {
                Line line = null;
                boolean opened = true;
                try {
                    line = mixer.getLine(lineInfo);
                    opened = line.isOpen() || line instanceof Clip;
                    if (!opened) {
                        line.open();
                    }
                    for (Control control : line.getControls()) {
                        findMuteControlAndMute(control,control.toString(),mixerInfo.getName(),mute);
                    }
                }
                catch (LineUnavailableException | IllegalArgumentException e) {
                    e.printStackTrace();
                }
                finally {
                    if (line != null && !opened) {
                        line.close();
                    }
                }
            }
        }
    }

    /*
    recursive descent to muteControl and set it true if mute arg true, false otherwise.
    if mute true add mixer arg to set, otherwise remove
    * */
    private static void findMuteControlAndMute(Control control,String parentControlName,String mixer,boolean mute)
    {
        if (control instanceof CompoundControl)
        {
            Control[] controls = ((CompoundControl)control).getMemberControls();
            for (Control c: controls)
            {
                 findMuteControlAndMute(c,parentControlName,mixer,mute);
            }
        }else if(control instanceof BooleanControl && control.getType() == BooleanControl.Type.MUTE){
            boolean value = ((BooleanControl) control).getValue();
            if(mute) {
                if (!value) {
                    String s =  mixer;
                    muted.add(s);
                    ((BooleanControl) control).setValue(true);
                }
            }else {
                if(value){
                    String s = mixer;
                    if(muted.contains(s)){
                        ((BooleanControl)control).setValue(false);
                    }
                }
            }
        }
    }

    public static void sendMute(String id){
        NetworkPackage orCreatePackage = NetworkPackage.Cacher.getOrCreatePackage(CallModule.class.getSimpleName(), MUTE);
        BackgroundService.sendToDevice(id,orCreatePackage.getMessage());
    }

    public static void rejectCall(String id) {
        NetworkPackage orCreatePackage = NetworkPackage.Cacher.getOrCreatePackage(CallModule.class.getSimpleName(), DECLINE_CALL);
        BackgroundService.sendToDevice(id,orCreatePackage.getMessage());
    }

    public static void answerCall(String id) {
        NetworkPackage orCreatePackage = NetworkPackage.Cacher.getOrCreatePackage(CallModule.class.getSimpleName(), ANSWER_CALL);
        BackgroundService.sendToDevice(id,orCreatePackage.getMessage());
    }

    public static void rejectOutgoingCall(String id) {
        NetworkPackage orCreatePackage = NetworkPackage.Cacher.getOrCreatePackage(CallModule.class.getSimpleName(), DECLINE_CALL); //?
        BackgroundService.sendToDevice(id,orCreatePackage.getMessage());
    }

    public static void recall(String id, String num) {
        NetworkPackage orCreatePackage = NetworkPackage.Cacher.getOrCreatePackage(CallModule.class.getSimpleName(), RECALL);
        orCreatePackage.setValue("number",num);
        BackgroundService.sendToDevice(id,orCreatePackage.getMessage());
    }
}
