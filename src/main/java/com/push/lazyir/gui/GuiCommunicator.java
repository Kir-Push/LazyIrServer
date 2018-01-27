package com.push.lazyir.gui;

import com.push.gui.controllers.ApiController;
import com.push.gui.entity.NotificationDevice;
import com.push.gui.entity.PhoneDevice;
import com.push.lazyir.devices.Device;
import com.push.lazyir.devices.NetworkPackage;
import com.push.lazyir.modules.notifications.messengers.Messengers;
import com.push.lazyir.modules.notifications.notifications.Notification;
import com.push.lazyir.modules.notifications.notifications.ShowNotification;
import com.push.lazyir.modules.notifications.sms.Sms;
import com.push.lazyir.modules.notifications.sms.SmsModule;
import com.push.lazyir.modules.reminder.MessagesPack;
import com.push.lazyir.modules.reminder.Reminder;
import com.push.lazyir.service.BackgroundService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.util.List;

import static com.push.lazyir.modules.notifications.sms.SmsModule.SMS_TYPE;
import static com.push.lazyir.service.TcpConnectionManager.OK;
import static com.push.lazyir.service.TcpConnectionManager.REFUSE;

// Class used to communicate between gui and backend
public class GuiCommunicator {
    public static void unPair(String id) {
        BackgroundService.sendRequestUnPair(id);
    }

    public static void pair(String id) {
        BackgroundService.sendRequestPair(id);
    }

    public static void reconnect(String id) {
        BackgroundService.reconnect(id);
    }

    public static void unMount(String id) {
        BackgroundService.unMount(id);
    }

    public static void mount(String id) {
        BackgroundService.mount(id);
    }

    public static void ping(String id) {
        BackgroundService.sendPing(id);
    }

    public static void removeNotification(String ownerId, String notificationId) {
        ShowNotification.sendRemoveNotification(ownerId,notificationId);
    }

    public static void sendToGetAllNotif(String id) {
      ShowNotification.requestNotificationsFromDevice(id);
    }

    public static void newDeviceConnected(Device device){
        PhoneDevice phoneDevice = new PhoneDevice(device.getId(), device.getName(), device.getDeviceType(), 0, false, device.isPaired(), false);
        ApiController.getInstance().newDeviceConnected(phoneDevice);
    }

    public static void devicePaired(String deviceId, boolean b) {
        ApiController.getInstance().setDevicePaired(deviceId,b);
    }



    public static void receive_notifications(String id,List<Notification> notifications){
        ObservableList<NotificationDevice> notificationDevices = FXCollections.observableArrayList();
                for (Notification notification : notifications) {
                    NotificationDevice notificationDevice = new NotificationDevice(notification.getText(), notification.getType(),
                            notification.getTitle(), notification.getPack(), notification.getTicker(),
                            notification.getId(), notification.getIcon(), notification.getPicture());
                    notificationDevice.setOwnerName(id);
                    notificationDevices.add(notificationDevice);
        }
        ApiController.getInstance().setDeviceNotifications(id,notificationDevices);
    }

    public static void show_notification(String id,Notification notification,Object... arg){
        NotificationDevice notificationDevice = new NotificationDevice(notification.getText(), notification.getType(),
                notification.getTitle(), notification.getPack(), notification.getTicker(),
                notification.getId(), notification.getIcon(), notification.getPicture());
        notificationDevice.setOwnerName(Device.getConnectedDevices().get(id).getName());
        ApiController.getInstance().showNotification(id,notificationDevice,arg);
    }

    public static void show_sms(String id, Sms sms) {
        try {
            NotificationDevice notification = new NotificationDevice(sms.getText(), "sms", sms.getName(), SMS_TYPE, sms.getNumber(), sms.getNumber(), sms.getIcon(), sms.getPicture());
            notification.setOwnerName(Device.getConnectedDevices().get(id).getName());
            ApiController.getInstance().showNotification(id, notification);
        }catch (Throwable e){
            e.printStackTrace();
        }
    }



    //todo files add
    public static void sendMessengerAnswer(String id,String typeName, String text, List<File> dragImageurl) {
        Messengers.sendAnswer(typeName,text,id);
    }

    public static void sendSmsAnswer(String id, String name, String text, List<File> draggedFiles) {
        SmsModule.send_sms(name,text,id);
    }

    public static void deviceLost(String deviceId) {
        ApiController.getInstance().deviceDisconnected(deviceId);
    }

    public static void batteryStatus(String percenstage, String status, Device device) {
        boolean charging;
        if (status.equalsIgnoreCase("true")) charging = true;
        else charging = false;
        ApiController.getInstance().setBatteryStatus(device.getId(),Integer.parseInt(percenstage),charging);
    }

    public static void sftpConnectResult(boolean running, String id) {
        ApiController.getInstance().setDeviceMounted(id,running);
    }

    public static void iamCrushedUdpListen() {
    }

    public static void iamCrushed(String message) {
    }

    public static void pairAnswer(String id, boolean answer, String data){
        BackgroundService.pairResultFromGui(id,answer ? OK : REFUSE,data);
    }

    public static void requestPair(NetworkPackage np) {
        String name = np.getName();
        NotificationDevice notificationDevice = new NotificationDevice("Request Pair" ,"pair", name,np.getType(),np.getData(),np.getId(),np.getValue("icon"),null);
        notificationDevice.setOwnerName(name);
        ApiController.getInstance().requestPair(np.getId(),notificationDevice);
    }

    public static void call_Notif(NetworkPackage np) {
        String callType = np.getValue("callType");
           NotificationDevice notificationDevice = new NotificationDevice(np.getValue("text"),callType,np.getValue("number"),callType,callType,np.getId(),np.getValue("icon"),null);
           notificationDevice.setOwnerName(np.getName());
           ApiController.getInstance().showNotification(np.getId(),notificationDevice);

    }


    public static void call_notif_end(NetworkPackage np) {
        ApiController.getInstance().removeNotificationCallEnd(np.getId(),np.getValue("number"));
    }

    public static void tcpClosed() {

    }

    public static void answerCall(NotificationDevice notificationDevice, String id) {

    }

    public static void rejectCall(NotificationDevice notificationDevice, String id) {
    }

    public static void recall(NotificationDevice item, String id) {

    }

    public static void rejectOutgoingcall(NotificationDevice notificationDevice, String id) {

    }

    public static void dismissAllCalls(NotificationDevice notificationDevice, String id) {
        Reminder.sendDissmisAllCalls(id,notificationDevice.getId());
    }


    public static void dissMissAllMessages(NotificationDevice notificationDevice, String id, MessagesPack msg) {
        Reminder.sendDissmisAllMessages(id,msg);
    }
}
