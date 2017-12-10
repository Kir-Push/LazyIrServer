package com.push.lazyir.gui;

import com.push.gui.controllers.ApiController;
import com.push.gui.entity.PhoneDevice;
import com.push.lazyir.devices.Device;

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

    public static void removeNotification(int ownerId, String notificationId) {
        System.out.println("da");

    }

    public static void sendToGetAllNotif(String id) {

    }

    public static void newDeviceConnected(Device device){
        PhoneDevice phoneDevice = new PhoneDevice(device.getId(), device.getName(), device.getDeviceType(), 0, false, device.isPaired(), false);
        ApiController.getInstance().newDeviceConnected(phoneDevice);
    }
}
