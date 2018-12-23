package com.push.lazyir.gui;

import com.push.gui.controllers.ApiController;
import com.push.gui.entity.NotificationDevice;
import com.push.gui.entity.PhoneDevice;
import com.push.lazyir.devices.Device;
import com.push.lazyir.modules.command.Command;
import com.push.lazyir.modules.memory.CRTEntity;
import com.push.lazyir.modules.memory.Memory;
import com.push.lazyir.modules.memory.MemoryEntity;
import com.push.lazyir.modules.notifications.call.CallModule;
import com.push.lazyir.modules.notifications.call.CallModuleDto;
import com.push.lazyir.modules.notifications.messengers.Messengers;
import com.push.lazyir.modules.notifications.notifications.Notification;
import com.push.lazyir.modules.notifications.notifications.ShowNotification;
import com.push.lazyir.modules.notifications.sms.Sms;
import com.push.lazyir.modules.notifications.sms.SmsModule;
import com.push.lazyir.modules.ping.Ping;
import com.push.lazyir.modules.reminder.Reminder;
import com.push.lazyir.modules.reminder.ReminderDto;
import com.push.lazyir.modules.share.ShareModule;
import com.push.lazyir.service.main.BackgroundService;
import com.push.lazyir.service.main.PairService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;

// Class used to communicate between gui and backend
@Slf4j
public class GuiCommunicator {

    private ApiController apiController;
    private BackgroundService backgroundService;
    @Setter
    private PairService pairService;

    @Inject
    public GuiCommunicator(ApiController apiController,BackgroundService backgroundService) {
        this.apiController = apiController;
        this.backgroundService = backgroundService;
    }

    public void unPair(String id) {
        pairService.sendUnpairRequest(id);
    }

    public void pair(String id) {
        pairService.sendPairRequest(id);
    }

    /*
    first close connection, after that sendUdp as you do when receive broadcast
    * */
    public void reconnect(String id) {
        backgroundService.submitNewTask(()-> {
            ShareModule module = backgroundService.getModuleById(id, ShareModule.class);
            if (module != null) {
                module.endWork();
            }
        });
    }

    public void unMount(String id) {
        backgroundService.submitNewTask(()-> {
            ShareModule module = backgroundService.getModuleById(id, ShareModule.class);
            if (module != null) {
                module.endWork();
            }
        });
    }

    public void mount(String id) {
        ShareModule module = backgroundService.getModuleById(id, ShareModule.class);
        if(module != null) {
            module.sendSetupServerCommand();
        }
    }

    public void ping(String id) {
        Ping ping = backgroundService.getModuleById(id, Ping.class);
        if(ping != null) {
            ping.sendPing();
        }
    }

    public void removeNotification(String ownerId, String notificationId) {
        backgroundService.submitNewTask(()->{
            ShowNotification module = backgroundService.getModuleById(ownerId, ShowNotification.class);
            if(module != null) {
                module.sendRemoveNotification(notificationId);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    log.error("error in removeNotification", e);
                    Thread.currentThread().interrupt();
                }
                module.requestNotificationsFromDevice();
            }
        });
    }

    public void sendToGetAllNotif(String id) {
        ShowNotification module = backgroundService.getModuleById(id, ShowNotification.class);
        if(module != null) {
            module.requestNotificationsFromDevice();
        }
    }

    public void newDeviceConnected(Device device){
        PhoneDevice phoneDevice = new PhoneDevice(device.getId(), device.getName(), device.getDeviceType(), 0, false, device.isPaired(), false);
        apiController.newDeviceConnected(phoneDevice);
    }

    public void devicePaired(String deviceId, boolean b) {
        apiController.setDevicePaired(deviceId,b);
    }

    public void receiveNotifications(String id, List<Notification> notifications) {
        ObservableList<NotificationDevice> notificationDevices = FXCollections.observableArrayList();
        for (Notification notification : notifications) {
            if (notification == null) {
                continue;
            }
            NotificationDevice notificationDevice = new NotificationDevice(notification.getText(), notification.getType(),
                    notification.getTitle(), notification.getPack(), notification.getTicker(),
                    notification.getId(), notification.getIcon(), notification.getPicture());
            notificationDevice.setOwnerName(id);
            notificationDevices.add(notificationDevice);
        }
        apiController.setDeviceNotifications(id, notificationDevices);
    }

    public void showNotification(String id, Notification notification, Object... arg){
        NotificationDevice notificationDevice = new NotificationDevice(notification.getText(), notification.getType(),
                notification.getTitle(), notification.getPack(), notification.getTicker(),
                notification.getId(), notification.getIcon(), notification.getPicture());
        notificationDevice.setOwnerName(backgroundService.getConnectedDevices().get(id).getName());
        apiController.showNotification(id,notificationDevice,arg);
    }

    public void showSms(String id, Sms sms) {
        String name = sms.getName();
        if(name == null || name.isEmpty()) {
            name = sms.getNumber();
        }
        NotificationDevice notification = new NotificationDevice(sms.getText(), "SMS", name, SmsModule.class.getSimpleName(), sms.getNumber(), sms.getNumber(), sms.getIcon(), sms.getPicture());
        notification.setOwnerName(backgroundService.getConnectedDevices().get(id).getName());
        apiController.showNotification(id, notification);
    }


    public void sendMessengerAnswer(String id,String typeName, String text) {
        Messengers module = backgroundService.getModuleById(id, Messengers.class);
        if(module != null) {
            module.sendAnswer(typeName, text);
        }
    }

    public void sendSmsAnswer(String id, String name, String text) {
        SmsModule module = backgroundService.getModuleById(id, SmsModule.class);
        if(module != null) {
            module.sendSms(name, text);
        }
    }

    public void deviceLost(String deviceId) {
        apiController.deviceDisconnected(deviceId);
    }

    public void batteryStatus(String percenstage, String status, Device device) {
        boolean charging = status.equalsIgnoreCase("true");
        apiController.setBatteryStatus(device.getId(),Integer.parseInt(percenstage),charging);
    }

    public void sftpConnectResult(boolean running, String id) {
        apiController.setDeviceMounted(id,running);
    }

    public void iamCrushedUdpListen() {
        //neznaju nado li
    }

    public void iamCrushed(String message) {
        //hz nado li
    }

    public void pairAnswer(String id, boolean answer, String data){
        pairService.pairRequestAnswerFromGui(id,answer,data);
    }

    public void requestPair(String name,String id,String type,String data,String icon) {
        NotificationDevice notificationDevice = new NotificationDevice("Request Pair" ,"PAIR", name,type,data,id,icon,null);
        notificationDevice.setOwnerName(name);
        apiController.requestPair(id,notificationDevice);
    }

    public void callNotif(CallModuleDto dto, String id) {
        String callType = dto.getCallType();
        NotificationDevice notificationDevice = new NotificationDevice(dto.getText(),callType,dto.getNumber(),callType,callType,id,dto.getIcon(),null);
        notificationDevice.setOwnerName(dto.getName());
        apiController.showNotification(id,notificationDevice);
    }


    public void callNotifEnd(CallModuleDto dto) {
        apiController.removeNotificationCallEnd(dto.getNumber());
    }

    public void tcpClosed() {
        // nu kaknitj v budushem
    }

    public void answerCall(String id) {
        CallModule module = backgroundService.getModuleById(id, CallModule.class);
        if(module != null) {
            module.answerCall();
        }
    }

    public void rejectCall(String id) {
        CallModule module = backgroundService.getModuleById(id, CallModule.class);
        if(module != null){
            module.rejectCall();
        }
    }

    public void muteCall(String id){
        CallModule module = backgroundService.getModuleById(id, CallModule.class);
        if(module != null){
            module.sendMute();
        }
    }

    public void recall(NotificationDevice item, String id) {
        CallModule module = backgroundService.getModuleById(id, CallModule.class);
        if(module != null){
            module.recall(item.getTitle());
        }
    }

    public void rejectOutgoingcall(String id) {
        CallModule module = backgroundService.getModuleById(id, CallModule.class);
        if(module != null){
            module.rejectOutgoingCall();
        }
    }

    public void dismissAllCalls(NotificationDevice notificationDevice, String id) {
        Reminder module = backgroundService.getModuleById(id, Reminder.class);
        if(module != null){
            module.sendDissmisAllCalls(notificationDevice.getId());
        }
    }


    public void dissMissAllMessages(String id, ReminderDto msg) {
        Reminder module = backgroundService.getModuleById(id, Reminder.class);
        if(module != null){
            module.sendDissmisAllMessages(msg);
        }
    }

    public void setDeviceCRT(CRTEntity crt,String id){
        apiController.setDeviceCRT(crt,id);
    }

    public void setDeviceMemory(MemoryEntity memory,String id){
        apiController.setDeviceMemory(memory,id);
    }

    public void setGetRequestTimer(String id, int time) {
        Memory memoryModule = backgroundService.getModuleById(id, Memory.class);
        if(memoryModule != null) {
            memoryModule.setGetRequestTimer(time);
        }
    }

    public void clearGetRequestTimer(){
        Memory.clearTimer();
    }

    public void receiveCommands(Set<Command> cmd, String id) {
        apiController.receiveCommands(cmd,id);
    }

}
