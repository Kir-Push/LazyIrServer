package com.push.gui.controllers;

import com.push.gui.basew.MainWin;
import com.push.gui.entity.PhoneDevice;
import javafx.application.Platform;

import java.util.List;
import java.util.stream.Collectors;

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
        PhoneDevice deviceById = getDeviceById(id);
        Platform.runLater(() -> mainController.getMainApp().getConnectedDevices().remove(deviceById));
    }

    public void setBatteryStatus(String id,int battery,boolean charging){
        PhoneDevice deviceById = getDeviceById(id);
       Platform.runLater(()-> {
           deviceById.setBattery(battery);
           deviceById.setCharging(charging);
           int selectedIndex = mainController.getPersonList().getSelectionModel().getSelectedIndex();
           if(mainController.getPersonList().getSelectionModel().getSelectedItem().getId().equals(id)){
              mainController.getPersonList().getSelectionModel().clearSelection(selectedIndex);
              mainController.getPersonList().getSelectionModel().select(selectedIndex);
          }

       });
    }

    private PhoneDevice getDeviceById(String id){
     return   mainController.getMainApp().getConnectedDevices().stream().filter(device -> device.getId().equals(id)).findFirst().get();
    }
}
