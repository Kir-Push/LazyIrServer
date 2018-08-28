package com.push.gui.basew;

import com.push.gui.controllers.ApiController;
import com.push.gui.controllers.MainController;
import com.push.gui.entity.NotificationDevice;
import com.push.gui.entity.PhoneDevice;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.io.IOException;


@Slf4j
public class MainWin  {

    private Stage primaryStage;
    private ApiController apiController;
    private VBox rootLayout;
    private Scene scene;
    private  MainController controller;
    private ObservableList<PhoneDevice> connectedDevices = FXCollections.observableArrayList();
    private  ObservableList<NotificationDevice> notificationsList = FXCollections.observableArrayList();

    @Inject
    public MainWin(ApiController apiController,MainController mainController) {
        this.apiController = apiController;
        this.controller = mainController;
    }

    public void start(Stage stage)  {
        this.primaryStage = stage;
        this.primaryStage.setTitle("LazyIr");
        initRootLayout();
    }

    private void initRootLayout() {
        try {
            // Загружаем корневой макет из fxml файла.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Thread.currentThread().getContextClassLoader().getResource("fxml/newGui.fxml"));
            rootLayout = loader.load();

            // Отображаем сцену, содержащую корневой макет.
            scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            // Инициализируем MainController и присваиваем его ApiController ответсвенному за взаимосдействие с backend
            loader.setController(controller);
            controller.setMainApp(this);
            apiController.setMainController(controller);
            // Тут не вызывает show stage потомучто мы будем вызывать из systray класса.
        } catch (IOException e) {
            log.error("initRootLayout",e);
        }
    }

    public MainController getController(){
        return controller;
    }

    public ObservableList<PhoneDevice> getConnectedDevices() {
        return connectedDevices;
    }

    public ObservableList<NotificationDevice> getNotificationsList() {
        return notificationsList;
    }

    public VBox getRootLayout() {
        return rootLayout;
    }

    public Scene getScene() {
        return scene;
    }
}
