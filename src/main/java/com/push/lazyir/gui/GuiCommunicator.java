package com.push.lazyir.gui;

import com.push.gui.basew.Dialogs;
import com.push.gui.controllers.ApiController;
import com.push.gui.entity.NotificationDevice;
import com.push.gui.entity.PhoneDevice;
import com.push.gui.utils.GuiUtils;
import com.push.lazyir.devices.Device;
import com.push.lazyir.devices.NetworkPackage;
import com.push.lazyir.modules.notifications.*;
import com.push.lazyir.service.BackgroundService;
import com.push.lazyir.service.BackgroundServiceCmds;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;
import org.controlsfx.control.action.Action;

import java.io.File;
import java.util.List;

import static com.push.lazyir.modules.notifications.ShowNotification.ALL_NOTIFS;
import static com.push.lazyir.modules.notifications.ShowNotification.SHOW_NOTIFICATION;
import static com.push.lazyir.modules.notifications.SmsModule.SMS_TYPE;

// Class used to communicate between gui and backend
public class GuiCommunicator {
    public static void unPair(String id) {
        //todo
    }

    public static void pair(String id) {
        //todo
    }

    public static void reconnect(String id) {
        //todo
    }

    public static void unMount(String id) {
        //todo
    }

    public static void mount(String id) {
        //todo
    }

    public static void ping(String id) {
        //todo
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
            notificationDevices.add(new NotificationDevice(notification.getText(),notification.getType(),
                    notification.getTitle(),notification.getPack(),notification.getTicker(),
                    notification.getId(),notification.getIcon(),notification.getPicture()));
        }
        ApiController.getInstance().setDeviceNotifications(id,notificationDevices);
    }

    public static void show_notification(String id,Notification notification){
        NotificationDevice notificationDevice = new NotificationDevice(notification.getText(), notification.getType(),
                notification.getTitle(), notification.getPack(), notification.getTicker(),
                notification.getId(), notification.getIcon(), notification.getPicture());
        ApiController.getInstance().showNotification(id,notificationDevice);
    }

    public static void show_sms(String id, Sms sms) {
        NotificationDevice notification = new NotificationDevice(sms.getText(),"sms",sms.getName(),SMS_TYPE,sms.getNumber(),sms.getNumber(),sms.getIcon(),sms.getPicture());
        ApiController.getInstance().showNotification(id,notification);
    }



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
        if (status.equalsIgnoreCase("charging") ? true : false) charging = true;
        else charging = false;
        ApiController.getInstance().setBatteryStatus(device.getId(),Integer.parseInt(percenstage),charging);
    }

    public static void sftpConnectResult(boolean running, String id) {

    }

    public static void iamCrushedUdpListen() {
    }

    public static void iamCrushed() {

    }

    public static void pairAnswer(String id,boolean answer){

    }

    public static void requestPair(NetworkPackage np) {

    }

    public static void call_Notif(NetworkPackage np) {

    }

    public static void tcpClosed() {

    }
}
