package com.push.lazyir.modules.notifications.call;

import com.push.lazyir.api.MessageFactory;
import com.push.lazyir.api.NetworkPackage;
import com.push.lazyir.gui.GuiCommunicator;
import com.push.lazyir.modules.Module;
import com.push.lazyir.modules.dbus.Mpris;
import com.push.lazyir.modules.notifications.NotificationTypes;
import com.push.lazyir.service.main.BackgroundService;
import com.push.lazyir.service.managers.settings.SettingManager;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.sound.sampled.*;
import java.util.concurrent.ConcurrentSkipListSet;

@Slf4j
public class CallModule extends Module {
    public enum api{
        CALL,
        ENDCALL,
        ANSWER,
        ANSWER_CALL,
        DECLINE_CALL,
        MUTE,
        RECALL
    }
    @Getter @Setter(value = AccessLevel.PRIVATE)
    private static boolean calling;
    @Getter @Setter(value = AccessLevel.PRIVATE)
    private static boolean muteWhenCall;
    @Getter @Setter(value = AccessLevel.PRIVATE)
    private static boolean muteWhenOutcomingCall;
    private static ConcurrentSkipListSet<String> muted = new ConcurrentSkipListSet<>();
    private GuiCommunicator guiCommunicator;
    private SettingManager settingManager;


    @Inject
    public CallModule(BackgroundService backgroundService, MessageFactory messageFactory, GuiCommunicator guiCommunicator, SettingManager settingManager) {
        super(backgroundService, messageFactory);
        this.guiCommunicator = guiCommunicator;
        this.settingManager = settingManager;
    }

    @Override
    public void execute(NetworkPackage np) {
        CallModuleDto dto = (CallModuleDto) np.getData();
        api command = api.valueOf(dto.getCommand());
        boolean muteOutgoingCall = Boolean.parseBoolean(settingManager.get("muteWhenOutcomingCall"));
        boolean muteIncomingCall = Boolean.parseBoolean(settingManager.get("muteWhenCall"));
        switch (command) {
            case CALL:
                inCall(muteOutgoingCall, muteIncomingCall, dto);
                break;
            case ENDCALL:
                endCall(muteOutgoingCall, muteIncomingCall, dto);
                break;
            case ANSWER:
                answer(dto);
                break;
            default:
                break;
        }
    }

    private void answer(CallModuleDto dto) {
        guiCommunicator.callNotifEnd(dto);
    }

    private void endCall(boolean muteOutgoingCall, boolean muteIncomingCall, CallModuleDto dto) {
        if(isCalling()){
            setCalling(false);
            Mpris mpris = backgroundService.getModuleById(device.getId(), Mpris.class);
            if(mpris != null){
                mpris.playAll();
            }
            String callType = dto.getCallType();
            if((callType.equalsIgnoreCase(NotificationTypes.OUTGOING.name()) && muteOutgoingCall) ||
                    (callType.equalsIgnoreCase(NotificationTypes.INCOMING.name()) && muteIncomingCall) ||
                    callType.equalsIgnoreCase(NotificationTypes.MISSED_IN.name())){
                unMute();
            }
            guiCommunicator.callNotifEnd(dto);
        }
    }

    private void inCall(boolean muteWhenOutcomingCall, boolean muteWhenCall, CallModuleDto dto) {
        if(!isCalling()){
            Mpris mpris = backgroundService.getModuleById(device.getId(), Mpris.class);
            if(mpris != null) {
                mpris.pauseAll();
            }
            String callType = dto.getCallType();
            if((callType.equalsIgnoreCase(NotificationTypes.OUTGOING.name()) && muteWhenOutcomingCall) ||
                    (callType.equalsIgnoreCase(NotificationTypes.INCOMING.name()) && muteWhenCall)){
                mute();
            }
            setCalling(true);
        }
        guiCommunicator.callNotif(dto,device.getId());
    }

    public void mute(boolean mute){
        if(mute){
            mute();
        }else {
            unMute();
        }
    }

    private void unMute() {
        muteUnmute(false);
        muted.clear();
    }

    private void mute() {
        muteUnmute(true);
    }

    @Override
    public void endWork() {
        if(backgroundService.ifLastConnectedDeviceAreYou(device.getId())){
            setCalling(false);
            unMute();
        }
    }

    @Synchronized
    private static void muteUnmute(boolean mute){
        for (Mixer.Info mixerInfo : AudioSystem.getMixerInfo()) {
            Mixer mixer = AudioSystem.getMixer(mixerInfo);
            for (Line.Info lineInfo : mixer.getTargetLineInfo() ) { // target, not source
                Line line =  extractLine(lineInfo,mixer,mixerInfo.getName(),mute);
                if (line != null && (!line.isOpen() || line instanceof Clip)) {
                    line.close();
                }
            }
        }
    }

    private static Line extractLine(Line.Info lineInfo, Mixer mixer, String name, boolean mute) {
        try {
            Line line = mixer.getLine(lineInfo);
            if (!(line.isOpen() || line instanceof Clip)) {
                line.open();
            }
            for (Control control : line.getControls()) {
                findMuteControlAndMute(control, name, mute);
            }
            return line;
        }catch (LineUnavailableException | IllegalArgumentException e) {
            log.error("error in muteUnmute",e);
            return null;
        }
    }

    /*
    recursive descent to muteControl and set it true if mute arg true, false otherwise.
    if mute true add mixer arg to set, otherwise remove
    * */
    private static void findMuteControlAndMute(Control control,String mixer,boolean mute) {
        if (control instanceof CompoundControl) {
            Control[] controls = ((CompoundControl)control).getMemberControls();
            for (Control c: controls) {
                 findMuteControlAndMute(c,mixer,mute);
            }
        }else if(control instanceof BooleanControl && control.getType() == BooleanControl.Type.MUTE){
            boolean value = ((BooleanControl) control).getValue();
            if(mute && !value) {
                muted.add(mixer);
                ((BooleanControl) control).setValue(true);
            } else if (value && muted.contains(mixer)) {
                ((BooleanControl) control).setValue(false);
            }
        }
    }

    public void sendMute(){
        sendCommand(api.MUTE);
    }

    public void rejectCall() {
        sendCommand(api.DECLINE_CALL);
    }

    public void answerCall() {
        sendCommand(api.ANSWER_CALL);
    }

    public void rejectOutgoingCall() {
        sendCommand(api.DECLINE_CALL); // ??
    }

    public void recall(String number) {
        sendMsg(messageFactory.createMessage(this.getClass().getSimpleName(),true,new CallModuleDto(api.RECALL.name(),number)));
    }

    private void sendCommand(api mute) {
        sendMsg(messageFactory.createMessage(this.getClass().getSimpleName(),true,new CallModuleDto(mute.name())));
    }


}
