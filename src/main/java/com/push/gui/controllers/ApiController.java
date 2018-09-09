package com.push.gui.controllers;

import com.push.gui.basew.Popup;
import com.push.gui.entity.NotificationDevice;
import com.push.gui.entity.PhoneDevice;
import com.push.lazyir.modules.memory.CRTEntity;
import com.push.lazyir.modules.memory.MemPair;
import com.push.lazyir.modules.memory.MemoryEntity;
import com.push.lazyir.modules.command.Command;
import javafx.application.Platform;
import javafx.collections.FXCollections;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.Set;


/**
 * Wire non-gui with gui classes,
 * To control interface
 */
public class ApiController {

    private MainController mainController;
    private Popup popup;


    @Inject
    public ApiController(Popup popup) {
        this.popup = popup;
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void newDeviceConnected(PhoneDevice phoneDevice) {
        Platform.runLater(() -> mainController.getMainApp().getConnectedDevices().add(phoneDevice));
    }

    public void deviceDisconnected(String id){
        Platform.runLater(
                () -> { PhoneDevice deviceById = getDeviceById(id);
                        mainController.setAllToDefault(deviceById); });
    }

    public void setBatteryStatus(String id,int battery,boolean charging){
       Platform.runLater(()-> {
           PhoneDevice deviceById = getDeviceById(id);
           deviceById.setBattery(battery);
           deviceById.setCharging(charging);
           refreshSelection();
       });
    }

    public void setDevicePaired(String id,boolean paired){
        Platform.runLater(()->{
            PhoneDevice deviceById = getDeviceById(id);
            deviceById.setPaired(paired);
            refreshSelection();
        });
    }

    public void setDeviceMounted(String id,boolean mounted){
        Platform.runLater(()->{
            PhoneDevice deviceById = getDeviceById(id);
            deviceById.setMounted(mounted);
            refreshSelection();
        });
    }

    public void setDeviceNotifications(String id, List<NotificationDevice> notifications){
        Platform.runLater(()->{
            PhoneDevice deviceById = getDeviceById(id);
            deviceById.setNotifications(FXCollections.observableArrayList(notifications));
            refreshSelection();
        });
    }

    private void refreshSelection(){
        int selectedIndex = mainController.getPersonList().getSelectionModel().getSelectedIndex();
        mainController.getPersonList().getSelectionModel().select(-1);
        mainController.getPersonList().getSelectionModel().select(selectedIndex);
    }

    private PhoneDevice getDeviceById(String id){
        Optional<PhoneDevice> first = mainController.getMainApp().getConnectedDevices().stream().filter(device -> device.getId().equals(id)).findFirst();
        return first.orElse(null);
    }

    public void showNotification(String id, NotificationDevice notification,Object... arg) {
        popup.show(id,notification,mainController,arg);
    }

    public void removeNotificationCallEnd(String callerNumber){
        popup.callEnd(callerNumber);
    }

    public void requestPair(String id,NotificationDevice notificationDevice) {
       showNotification(id,notificationDevice);
    }

    public void setDeviceCRT(CRTEntity crt, String id) {
        Platform.runLater(()->{
            PhoneDevice deviceById = getDeviceById(id);
            if(deviceById != null && crt != null) {
                deviceById.setCpuLoad(crt.getCpuLoad());
                deviceById.setFreeRam(crt.getFreeRam() / 1024 / 1024);
                deviceById.setTotalRam(crt.getFreeRamAll() / 1024 / 1024);
                deviceById.setLowMemory(crt.isLowMem());
                deviceById.setTemp(crt.getTempC());
                mainController.setCpu(crt.getCpuLoad());
                mainController.setRam(crt.getFreeRam() / 1024 / 1024, crt.getFreeRamAll() / 1024 / 1024, crt.isLowMem());
            }
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
            mainController.setMemoryText(mainMemFree,mainMem,finalExtFree,finalExtTotal);
        });
    }

    public void receiveCommands(Set<Command> commands, String id) {
        mainController.setCommands(commands,id);
    }
}
