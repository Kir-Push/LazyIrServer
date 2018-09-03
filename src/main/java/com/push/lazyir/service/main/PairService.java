package com.push.lazyir.service.main;

import com.push.lazyir.api.MessageFactory;
import com.push.lazyir.api.NetworkPackage;
import com.push.lazyir.devices.Device;
import com.push.lazyir.gui.GuiCommunicator;
import com.push.lazyir.modules.share.ShareModule;
import com.push.lazyir.service.dto.TcpDto;
import com.push.lazyir.service.managers.settings.SettingManager;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Slf4j
public class PairService {
    private BackgroundService backgroundService;
    private SettingManager settingManager;
    private GuiCommunicator guiCommunicator;
    private MessageFactory messageFactory;

    PairService(BackgroundService backgroundService, SettingManager settingManager, GuiCommunicator guiCommunicator, MessageFactory messageFactory) {
        this.backgroundService = backgroundService;
        this.settingManager = settingManager;
        this.guiCommunicator = guiCommunicator;
        this.messageFactory = messageFactory;
    }

    public void sendPairRequest(String id){
        try {
            String message = messageFactory.createMessage(TcpConnectionManager.api.TCP.name(), false, new TcpDto(TcpConnectionManager.api.PAIR.name(), String.valueOf(InetAddress.getLocalHost().getHostName().hashCode())));
            backgroundService.sendToDevice(id,message);
        } catch (UnknownHostException e) {
            log.error("error in sendRequestPairDevice with id - " + id,e);
        }
    }

    @Synchronized
    public void sendUnpairRequest(String id){
        String message = messageFactory.createMessage(TcpConnectionManager.api.TCP.name(), false, new TcpDto(TcpConnectionManager.api.UNPAIR.name()));
        backgroundService.sendToDevice(id,message);
        unPairDevice(id);
    }

    @Synchronized
    private void unPairDevice(String id) {
        Device device = backgroundService.getConnectedDevices().get(id);
        if(device != null) {
            device.setPaired(false);
            ShareModule module = backgroundService.getModuleById(id, ShareModule.class);
            if(module != null) {
                module.endWork();
            }
        }
        settingManager.delete(id);
        guiCommunicator.devicePaired(id, false);
    }

    private void setDevicePair(String id,String data){
        Device device = backgroundService.getConnectedDevices().get(id);
        if(device != null){
            device.setPaired(true);
        }
        settingManager.saveValue(id, data);
        guiCommunicator.devicePaired(id, true);
        ShareModule shareModule = backgroundService.getModuleById(id, ShareModule.class);
        if (shareModule != null) {
            shareModule.sendSetupServerCommand();
        }
    }

    @Synchronized
    public void setPairStatus(String id, String data, String result){
        if (result.equalsIgnoreCase(TcpConnectionManager.api.OK.name())) {
            setDevicePair(id,data);
        }else{
            unPairDevice(id);
        }
    }

    @Synchronized
    public void receivePairRequest(NetworkPackage networkPackage){
        String name = networkPackage.getName();
        String id = networkPackage.getId();
        TcpDto dto = (TcpDto) networkPackage.getData();
        String type = dto.getCommand();
        String icon = dto.getIcon();
        String data = dto.getData();
        guiCommunicator.requestPair(name,id,type,data,icon);
    }

    @Synchronized
    public void receivePairSignal(NetworkPackage np){
        String id = np.getId();
        TcpDto dto = (TcpDto) np.getData();
        setPairStatus(id,dto.getData(),dto.getResult());
    }

    @Synchronized
    public void pairRequestAnswerFromGui(String id, boolean answer, String data) {
            String result = answer ? TcpConnectionManager.api.OK.name() : TcpConnectionManager.api.REFUSE.name();
            sendPairAnswer(id,result);
            setPairStatus(id,data,result);
    }

    @Synchronized
    public void sendPairAnswer(String id, String answer)  {
        try {
            String data = String.valueOf(InetAddress.getLocalHost().getHostName().hashCode());
            TcpDto dto = new TcpDto(TcpConnectionManager.api.PAIR_RESULT.name(), data, answer);
            String message = messageFactory.createMessage(TcpConnectionManager.api.TCP.name(), false, dto);
            backgroundService.sendToDevice(id, message);
        }catch (UnknownHostException e) {
            log.error("error in sendPairAnswer - id: " + id + " result: " + answer, e);
        }
    }
}
