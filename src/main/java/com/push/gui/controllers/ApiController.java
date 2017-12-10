package com.push.gui.controllers;

import com.push.gui.basew.MainWin;
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
           refreshSelection(id);
       });
    }

    public void setDevicePaired(String id,boolean paired){
        PhoneDevice deviceById = getDeviceById(id);
        Platform.runLater(()->{
            deviceById.setPaired(paired);
            refreshSelection(id);
        });
    }

    public void setDeviceMounted(String id,boolean mounted){
        PhoneDevice deviceById = getDeviceById(id);
        Platform.runLater(()->{
            deviceById.setPaired(mounted);
            refreshSelection(id);
        });
    }

    public void setDeviceNotifications(String id, List<NotificationDevice> notifications){
        PhoneDevice deviceById = getDeviceById(id);
        Platform.runLater(()->{
            deviceById.setNotifications(FXCollections.observableArrayList(notifications));
            refreshSelection(id);
        });
    }

    private void refreshSelection(String id){
        int selectedIndex = mainController.getPersonList().getSelectionModel().getSelectedIndex();
        if(mainController.getPersonList().getSelectionModel().getSelectedItem().getId().equals(id)){
            mainController.getPersonList().getSelectionModel().clearSelection(selectedIndex);
            mainController.getPersonList().getSelectionModel().select(selectedIndex);
        }
    }

    private PhoneDevice getDeviceById(String id){
     return   mainController.getMainApp().getConnectedDevices().stream().filter(device -> device.getId().equals(id)).findFirst().get();
    }

    public void showNotification(String id, Notification notification) {
        Platform.runLater(()->{
            GridPane listCellContents = new GridPane();
            listCellContents.setHgap(10);
            if(notification.getIcon() != null && notification.getIcon().length() > 1)
            listCellContents.add(new ImageView(GuiUtils.pictureFromBase64(notification.getIcon(),40,40)),0,0);

            VBox vBox = new VBox();
            Text title = new Text();
            title.setFont(Font.font("Verdana", FontWeight.BOLD, 15));
            title.setText(notification.getTitle()+"\n");
            Text text = new Text();
            text.setText(notification.getText());
            vBox.getChildren().addAll(title, text);
            vBox.setAlignment(Pos.TOP_LEFT);
            listCellContents.add(vBox,1,0);

            ColumnConstraints leftCol = new ColumnConstraints();
            ColumnConstraints centerCol = new ColumnConstraints();
            ColumnConstraints rightCol = new ColumnConstraints();

            if(notification.getPicture() != null && notification.getPicture().length() > 1)
           listCellContents.add(new ImageView(GuiUtils.pictureFromBase64(notification.getPicture(),120,120)),2,0);
            rightCol.setHalignment(HPos.RIGHT);
            rightCol.setHgrow(Priority.ALWAYS);
            centerCol.setHalignment(HPos.CENTER);
            centerCol.setHgrow(Priority.ALWAYS);
            listCellContents.getColumnConstraints().addAll(leftCol,centerCol,rightCol);
            listCellContents.setMinHeight(100);
            listCellContents.setAlignment(Pos.CENTER);
            Notifications.create().text("").title("")
                    .graphic(listCellContents)
                    .show();
        });
    }
}
