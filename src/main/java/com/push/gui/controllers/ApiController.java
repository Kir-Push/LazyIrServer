package com.push.gui.controllers;

import com.push.gui.basew.MainWin;
import com.push.gui.basew.Popup;
import com.push.gui.entity.NotificationDevice;
import com.push.gui.entity.PhoneDevice;
import com.push.gui.utils.GuiUtils;
import com.push.lazyir.modules.notifications.Notification;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import org.controlsfx.control.Notifications;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


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
        System.out.println("refresh");
        mainController.getPersonList().getSelectionModel().select(-1);
        mainController.getPersonList().getSelectionModel().select(selectedIndex);
    }

    private PhoneDevice getDeviceById(String id){
        Optional<PhoneDevice> first = mainController.getMainApp().getConnectedDevices().stream().filter(device -> device.getId().equals(id)).findFirst();
        return first.orElse(null);
    }

    public void showNotification(String id, NotificationDevice notification) {
        Popup.show(id,notification,mainController);
    }

}
