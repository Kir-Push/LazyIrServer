package com.push.lazyir.gui;

import com.push.gui.controllers.ApiController;
import com.push.gui.entity.NotificationDevice;
import com.push.gui.entity.PhoneDevice;
import com.push.lazyir.devices.Device;
import com.push.lazyir.devices.NetworkPackageOld;
import com.push.lazyir.modules.command.Command;
import com.push.lazyir.modules.memory.CRTEntity;
import com.push.lazyir.modules.memory.Memory;
import com.push.lazyir.modules.memory.MemoryEntity;
import com.push.lazyir.modules.notifications.call.CallModule;
import com.push.lazyir.modules.notifications.messengers.Messengers;
import com.push.lazyir.modules.notifications.notifications.Notification;
import com.push.lazyir.modules.notifications.notifications.ShowNotification;
import com.push.lazyir.modules.notifications.sms.Sms;
import com.push.lazyir.modules.notifications.sms.SmsModule;
import com.push.lazyir.modules.reminder.MessagesPack;
import com.push.lazyir.modules.reminder.Reminder;
import com.push.lazyir.service.main.BackgroundService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;

import static com.push.lazyir.modules.notifications.sms.SmsModule.SMS_TYPE;
import static com.push.lazyir.service.main.TcpConnectionManager.OK;
import static com.push.lazyir.service.main.TcpConnectionManager.REFUSE;

// Class used to communicate between gui and backend
public class GuiCommunicator {

    private ApiController apiController;
    private BackgroundService backgroundService;

    @Inject
    public GuiCommunicator(ApiController apiController,BackgroundService backgroundService) {
        this.apiController = apiController;
        this.backgroundService = backgroundService;
    }

    public void unPair(String id) {
        backgroundService.sendRequestUnPair(id);
    }

    public void pair(String id) {
        backgroundService.sendRequestPair(id);
    }

    public void reconnect(String id) {
        backgroundService.reconnect(id);
    }

    public void unMount(String id) {
        backgroundService.unMount(id);
    }

    public void mount(String id) {
        backgroundService.mount(id);
    }

    public void ping(String id) {
        backgroundService.sendPing(id);
    }

    public void removeNotification(String ownerId, String notificationId) {
        backgroundService.submitNewTask(()->{
            backgroundService.getModuleById(ownerId,ShowNotification.class).sendRemoveNotification(ownerId,notificationId);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            backgroundService.getModuleById(ownerId,ShowNotification.class).requestNotificationsFromDevice(ownerId);
        });
    }

    public void sendToGetAllNotif(String id) {
        backgroundService.getModuleById(id,ShowNotification.class).requestNotificationsFromDevice(id);
    }

    public void newDeviceConnected(Device device){
        PhoneDevice phoneDevice = new PhoneDevice(device.getId(), device.getName(), device.getDeviceType(), 0, false, device.isPaired(), false);
        apiController.newDeviceConnected(phoneDevice);
    }

    public void devicePaired(String deviceId, boolean b) {
        apiController.setDevicePaired(deviceId,b);
    }



    public void receive_notifications(String id,List<Notification> notifications){
        ObservableList<NotificationDevice> notificationDevices = FXCollections.observableArrayList();
                for (Notification notification : notifications) {
                    if(notification == null)
                        continue;
                    NotificationDevice notificationDevice = new NotificationDevice(notification.getText(), notification.getType(),
                            notification.getTitle(), notification.getPack(), notification.getTicker(),
                            notification.getId(), notification.getIcon(), notification.getPicture());
                    notificationDevice.setOwnerName(id);
                    notificationDevices.add(notificationDevice);
        }
        apiController.setDeviceNotifications(id,notificationDevices);
    }

    public void show_notification(String id,Notification notification,Object... arg){
        NotificationDevice notificationDevice = new NotificationDevice(notification.getText(), notification.getType(),
                notification.getTitle(), notification.getPack(), notification.getTicker(),
                notification.getId(), notification.getIcon(), notification.getPicture());
        notificationDevice.setOwnerName(backgroundService.getConnectedDevices().get(id).getName());
        apiController.showNotification(id,notificationDevice,arg);
    }

    public void show_sms(String id, Sms sms) {
        try {
            NotificationDevice notification = new NotificationDevice(sms.getText(), "sms", sms.getName(), SMS_TYPE, sms.getNumber(), sms.getNumber(), sms.getIcon(), sms.getPicture());
            notification.setOwnerName(backgroundService.getConnectedDevices().get(id).getName());
            apiController.showNotification(id, notification);
        }catch (Throwable e){
            e.printStackTrace();
        }
    }




    public void sendMessengerAnswer(String id,String typeName, String text) {
        backgroundService.getModuleById(id,Messengers.class).sendAnswer(typeName,text,id);
    }

    public void sendSmsAnswer(String id, String name, String text) {
        backgroundService.getModuleById(id,SmsModule.class).send_sms(name,text,id);
    }

    public void deviceLost(String deviceId) {
        apiController.deviceDisconnected(deviceId);
    }

    public void batteryStatus(String percenstage, String status, Device device) {
        boolean charging;
        if (status.equalsIgnoreCase("true")) charging = true;
        else charging = false;
        apiController.setBatteryStatus(device.getId(),Integer.parseInt(percenstage),charging);
    }

    public void sftpConnectResult(boolean running, String id) {
        apiController.setDeviceMounted(id,running);
    }

    public void iamCrushedUdpListen() {
    }

    public void iamCrushed(String message) {
    }

    public void pairAnswer(String id, boolean answer, String data){
        backgroundService.pairResultFromGui(id,answer ? OK : REFUSE,data);
    }

    public void requestPair(NetworkPackageOld np) {
        String name = np.getName();
        NotificationDevice notificationDevice = new NotificationDevice("Request Pair" ,"pair", name,np.getType(),np.getData(),np.getId(),np.getValue("icon"),null);
        notificationDevice.setOwnerName(name);
        apiController.requestPair(np.getId(),notificationDevice);
    }

    public void call_Notif(NetworkPackageOld np) {
        String callType = np.getValue("callType");
           NotificationDevice notificationDevice = new NotificationDevice(np.getValue("text"),callType,np.getValue("number"),callType,callType,np.getId(),np.getValue("icon"),null);
           notificationDevice.setOwnerName(np.getName());
           apiController.showNotification(np.getId(),notificationDevice);

    }


    public void call_notif_end(NetworkPackageOld np) {
        apiController.removeNotificationCallEnd(np.getValue("number"));
    }

    public void tcpClosed() {
    }

    public void answerCall(NotificationDevice notificationDevice, String id) {
        backgroundService.getModuleById(id,CallModule.class).answerCall(id);
    }

    public void rejectCall(NotificationDevice notificationDevice, String id) {
        backgroundService.getModuleById(id,CallModule.class).rejectCall(id);
    }

    public void muteCall(NotificationDevice notificationDevice, String id){
        backgroundService.getModuleById(id,CallModule.class).sendMute(id);
    }

    public void recall(NotificationDevice item, String id) {
        backgroundService.getModuleById(id,CallModule.class).recall(id,item.getTitle());
    }

    public void rejectOutgoingcall(NotificationDevice notificationDevice, String id) {
        backgroundService.getModuleById(id,CallModule.class).rejectOutgoingCall(id);
    }

    public void dismissAllCalls(NotificationDevice notificationDevice, String id) {
        backgroundService.getModuleById(id,Reminder.class).sendDissmisAllCalls(id,notificationDevice.getId());
    }


    public void dissMissAllMessages(NotificationDevice notificationDevice, String id, MessagesPack msg) {
        backgroundService.getModuleById(id,Reminder.class).sendDissmisAllMessages(id,msg);
    }

    public void setDeviceCRT(CRTEntity crt,String id){
        apiController.setDeviceCRT(crt,id);
    }

    public void setDeviceMemory(MemoryEntity memory,String id){
        apiController.setDeviceMemory(memory,id);
    }

    public void setGetRequestTimer(String id, int time) {
        backgroundService.getModuleById(id,Memory.class).setGetRequestTimer(time);
    }

    public void clearGetRequestTimer(){
        Memory.clearTimer();
    }

    public void receiveCommands(Set<Command> cmd, String id) {
        apiController.receiveCommands(cmd,id);
    }
}
