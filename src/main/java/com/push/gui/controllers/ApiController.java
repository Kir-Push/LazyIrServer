package com.push.gui.controllers;

import com.push.gui.basew.Popup;
import com.push.gui.entity.NotificationDevice;
import com.push.gui.entity.PhoneDevice;
import com.push.lazyir.modules.memory.CRTEntity;
import com.push.lazyir.modules.memory.MemPair;
import com.push.lazyir.modules.memory.MemoryEntity;
import javafx.application.Platform;
import javafx.collections.FXCollections;

import java.util.List;
import java.util.Optional;


// Class wich actually update gui
public class ApiController {

    private static MainController mainController;
    private static ApiController apiController;

    private ApiController(){}

    public static MainController getMainController() {
        return mainController;
    }

    public static void setMainController(MainController mainController) {
        ApiController.mainController = mainController;
    }

    public static ApiController getInstance() {
        if(apiController == null)
            apiController = new ApiController();
        return apiController;
    }

    public void newDeviceConnected(PhoneDevice phoneDevice) {
        Platform.runLater(() -> mainController.getMainApp().getConnectedDevices().add(phoneDevice));
    }

    public void deviceDisconnected(String id){
        Platform.runLater(
                () -> {  PhoneDevice deviceById = getDeviceById(id);
                        mainController.getMainApp().getConnectedDevices().remove(deviceById);});
    }

    public void setBatteryStatus(String id,int battery,boolean charging){
       Platform.runLater(()-> {
           PhoneDevice deviceById = getDeviceById(id);
           deviceById.setBattery(battery);
           deviceById.setCharging(charging);
           refreshSelection(id);
       });
    }

    public void setDevicePaired(String id,boolean paired){
        Platform.runLater(()->{
            PhoneDevice deviceById = getDeviceById(id);
            deviceById.setPaired(paired);
            refreshSelection(id);
        });
    }

    public void setDeviceMounted(String id,boolean mounted){
        Platform.runLater(()->{
            PhoneDevice deviceById = getDeviceById(id);
            deviceById.setPaired(mounted);
            refreshSelection(id);
        });
    }

    public void setDeviceNotifications(String id, List<NotificationDevice> notifications){
        Platform.runLater(()->{
            PhoneDevice deviceById = getDeviceById(id);
            deviceById.setNotifications(FXCollections.observableArrayList(notifications));
            refreshSelection(id);
        });
    }

    private void refreshSelection(String id){
        int selectedIndex = mainController.getPersonList().getSelectionModel().getSelectedIndex();
        mainController.getPersonList().getSelectionModel().select(-1);
        mainController.getPersonList().getSelectionModel().select(selectedIndex);
    }

    private PhoneDevice getDeviceById(String id){
        Optional<PhoneDevice> first = mainController.getMainApp().getConnectedDevices().stream().filter(device -> device.getId().equals(id)).findFirst();
        return first.orElse(null);
    }

    public void showNotification(String id, NotificationDevice notification,Object... arg) {
        Popup.show(id,notification,mainController,arg);
    }

    public void removeNotificationCallEnd(String id, String callerNumber){
        Popup.callEnd(id,callerNumber);
    }

    public void requestPair(String id,NotificationDevice notificationDevice) {
       showNotification(id,notificationDevice);
    }

    public void setDeviceCRT(CRTEntity crt, String id) {
        Platform.runLater(()->{
            PhoneDevice deviceById = getDeviceById(id);
            deviceById.setCpuLoad(crt.getCpuLoad());
            deviceById.setFreeRam(crt.getFreeRam()/1024/1024);
            deviceById.setTotalRam(crt.getFreeRamAll()/1024/1024);
            deviceById.setLowMemory(crt.isLowMem());
            deviceById.setTemp(crt.getTempC());
            refreshSelection(id);
        });
    }

    public void setDeviceMemory(MemoryEntity entity,String id){
        List<MemPair> extMem = entity.getExtMem();
        long mainMem = entity.getMainMem() / 1024 / 1024; // to mb
        long mainMemFree = entity.getMainMemFree() / 1024 / 1024;
        long extTotal = 0;
        long extFree = 0;
        for (MemPair memPair : extMem) {
            extTotal += memPair.getAllMem();
            extFree += memPair.getFreeMem();
        }
        long finalExtFree = extFree / 1024 / 1024; // effectively final hack
        long finalExtTotal = extTotal / 1024 / 1024;
        Platform.runLater(()->{
            PhoneDevice deviceById = getDeviceById(id);
            deviceById.setFreeSpace(mainMemFree);
            deviceById.setFreeSpaceExt(finalExtFree);
            deviceById.setTotalSpace(mainMem);
            deviceById.setTotalSpaceExt(finalExtTotal);
            refreshSelection(id);
        });
    }
}
