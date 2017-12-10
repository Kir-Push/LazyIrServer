package com.push.lazyir.gui;

import com.push.gui.controllers.ApiController;
import com.push.gui.entity.NotificationDevice;
import com.push.gui.entity.PhoneDevice;
import com.push.lazyir.devices.Device;
import com.push.lazyir.devices.NetworkPackage;
import com.push.lazyir.modules.notifications.Notification;
import com.push.lazyir.modules.notifications.ShowNotification;
import com.push.lazyir.service.BackgroundService;
import com.push.lazyir.service.BackgroundServiceCmds;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

import static com.push.lazyir.modules.notifications.ShowNotification.ALL_NOTIFS;
import static com.push.lazyir.modules.notifications.ShowNotification.SHOW_NOTIFICATION;

public class GuiCommunicator {
    public static void unPair(String id) {

    }

    public static void pair(String id) {

    }

    public static void reconnect(String id) {

    }

    public static void unMount(String id) {

    }

    public static void mount(String id) {

    }

    public static void ping(String id) {

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
        ApiController.getInstance().showNotification(id,notification);
    }
}
