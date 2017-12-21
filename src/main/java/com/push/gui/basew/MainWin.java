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
import org.controlsfx.control.Notifications;

import java.io.IOException;


public class MainWin  {

    private Stage primaryStage;

    public Stage getHideStage() {
        return hideStage;
    }

    private Stage hideStage;
    private VBox rootLayout;
    private Scene scene;
    private  MainController controller;
    private ObservableList<PhoneDevice> connectedDevices = FXCollections.observableArrayList();
    private  ObservableList<NotificationDevice> notificationsList = FXCollections.observableArrayList();

    private static   Notifications notifications;
//    public static void main(String[] args) throws Exception {
//        launch(args);
//    }


    public void start(Stage stage) throws Exception {
        this.primaryStage = stage;
        this.primaryStage.setTitle("LazyIr");
        initRootLayout();
        //todo
        // start backend on separate thread
        // commented for gui develop separating
    }

    private Stage initRootLayout() {
        try {
            // Загружаем корневой макет из fxml файла.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainWin.class.getClassLoader().getResource("fxml/newGui.fxml"));
            rootLayout = (VBox) loader.load();


            // Отображаем сцену, содержащую корневой макет.
            scene = new Scene(rootLayout);
            primaryStage.setScene(scene);

            // Инициализируем MainController и присваиваем его ApiController ответсвенному за взаимосдействие с backend
             controller = loader.getController();
            controller.setMainApp(this);
            ApiController.setMainController(controller);
            // Тут не вызывает show stage потомучто мы будем вызывать из systray класса.
//            PhoneDevice phone = new PhoneDevice("lg-d855", "lg-d855", "phone", 5, false, true, false);
//            phone.setNotifications(FXCollections.observableArrayList(new NotificationDevice("dadadadadadadadadadadadadaAgasj","sms","aga","java","huj","mabva",new String(Files.readAllBytes(Paths.get("/home/buhalo/Загрузки/icons/jaja"))),new String(Files.readAllBytes(Paths.get("/home/buhalo/Загрузки/icons/kote")))),new NotificationDevice("samka","notification","da","java","huj","mabva",null,null)));
//            connectedDevices.add(phone);
//            connectedDevices.add(new PhoneDevice("lg-d8552","lg-d8552","phone",75,false,false,false));
//            connectedDevices.add(new PhoneDevice("lg-d85522","lg-d8552","phone",25,true,false,false));
//            System.out.println(connectedDevices.size());
        } catch (IOException e) {
            e.printStackTrace(); // todo
        }
        return primaryStage;
    }

    public MainController getController(){
        return controller;
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

    public VBox getRootLayout() {
        return rootLayout;
    }

    public Scene getScene() {
        return scene;
    }
}
