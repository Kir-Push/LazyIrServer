package com.push.gui.basew;

import com.push.gui.entity.NotificationDevice;
import com.push.gui.entity.PhoneDevice;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.controlsfx.control.Notifications;
import org.controlsfx.control.action.Action;

import java.io.IOException;


public class MainWin  extends Application {

    private Stage primaryStage;
    private SplitPane rootLayout;
    private ObservableList<PhoneDevice> connectedDevices = FXCollections.observableArrayList();
    private ObservableList<NotificationDevice> notificationsList = FXCollections.observableArrayList();

    private static   Notifications notifications;
    public static void main(String[] args) throws Exception {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        this.primaryStage = stage;
        this.primaryStage.setTitle("LazyIr");
        initRootLayout();
    }

    private void initRootLayout() {
        try {
            // Загружаем корневой макет из fxml файла.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainWin.class.getResource("fxml/newGui.fxml"));
            rootLayout = (SplitPane) loader.load();

            // Отображаем сцену, содержащую корневой макет.
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            // Тут не вызывает show stage потомучто мы будем вызывать из systray класса.
        } catch (IOException e) {
            e.printStackTrace(); // todo
        }
    }

    /**
     * Возвращает главную сцену.
     * @return
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public ObservableList<PhoneDevice> getConnectedDevices() {
        return connectedDevices;
    }

    public void setConnectedDevices(ObservableList<PhoneDevice> connectedDevices) {
        this.connectedDevices = connectedDevices;
    }

    public ObservableList<NotificationDevice> getNotificationsList() {
        return notificationsList;
    }

    public void setNotificationsList(ObservableList<NotificationDevice> notificationsList) {
        this.notificationsList = notificationsList;
    }
}
