package com.push.gui.controllers;

import com.push.gui.basew.MainWin;
import com.push.gui.entity.NotificationDevice;
import com.push.gui.entity.PhoneDevice;
import com.push.gui.utils.GuiUtils;
import com.push.lazyir.gui.GuiCommunicator;
import com.push.lazyir.modules.notifications.Notification;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.StageStyle;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;


public class MainController {


    @FXML
    private ListView<PhoneDevice> personList;
    @FXML
    private ListView<NotificationDevice> notifTList;

    // Ссылка на главное приложение.
    private MainWin mainApp;

    /**
     * Конструктор.
     * Конструктор вызывается раньше метода initialize().
     */
    public MainController() {
    }

    public void setMainApp(MainWin mainApp) {
        this.mainApp = mainApp;
        initLists();
    }


    public MainWin getMainApp() {
        return mainApp;
    }

    /**
     * Инициализация списков и присвоение им Listner'ов
     */
    private void initLists(){

        personList.setStyle("-fx-control-inner-color: white;");
        personList.setCellFactory(personList -> new ListCell<>(){
            private final ImageView imageView = new ImageView();

            @Override
            protected void updateItem(PhoneDevice item, boolean empty) {
                super.updateItem(item, empty);
                if(empty){
                    setText(null);
                    setGraphic(null);
                }else{
                    Image image = GuiUtils.getImageByPaired(item.isPaired());
                    imageView.setImage(image);
                    Text text = new Text();
                    text.setText(item.getName());
                    VBox vBox = new VBox();
                    vBox.setId(item.getId());
                    vBox.getChildren().addAll(imageView,text);
                   setGraphic(vBox);
                }
            }
        });
        // при выборе устройства из списка присваиваем иконки и дейтсвия кнопкам в зависимости от состояния устройсва.
        personList.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) ->{
            if(newSelection != null){
                VBox rootLayout = mainApp.getRootLayout();


                ImageView batteryImg = (ImageView) rootLayout.lookup("#batteryImg");
                batteryImg.setImage(GuiUtils.getImageByBattery(newSelection.getBattery(),newSelection.isCharging()));
                Label batteryText = (Label) rootLayout.lookup("#batteryText");
                batteryText.setText(newSelection.getBattery() + " %");


                Button pairedBtn = (Button) rootLayout.lookup("#pairBtn");
                if(newSelection.isPaired()){
                    pairedBtn.setText("Unpair");
                    pairedBtn.setOnAction(event -> GuiCommunicator.unPair(newSelection.getId()));
                }else{
                    pairedBtn.setText("Pair");
                    pairedBtn.setOnAction(event -> GuiCommunicator.pair(newSelection.getId()));
                }


                Button reconnect = (Button) rootLayout.lookup("#reconnectBtn");
                reconnect.setOnAction(event -> GuiCommunicator.reconnect(newSelection.getId()));


                Button mount = (Button) rootLayout.lookup("#mountBtn");
                if(newSelection.isMounted()){
                    mount.setText("Unmount");
                    mount.setOnAction(event -> GuiCommunicator.unMount(newSelection.getId()));
                }else{
                    mount.setText("Mount");
                    mount.setOnAction(event -> GuiCommunicator.mount(newSelection.getId()));
                }


                Button ping = (Button) rootLayout.lookup("#pingBtn");
                ping.setOnAction(event -> GuiCommunicator.ping(newSelection.getId()));

                // присваиваем списку уведомление, список из текущего устройсва
                mainApp.getNotificationsList().clear();
                mainApp.getNotificationsList().addAll(newSelection.getNotifications());

            }
        });

        notifTList.setCellFactory(notifTList -> new ListCell<>(){

            @Override
            protected void updateItem(NotificationDevice item, boolean empty){
                super.updateItem(item, empty);
                if(empty){
                    setText(null);
                    setGraphic(null);
                }else {
                    VBox vBox = new VBox();
                    Text title = new Text();
                    title.setFont(Font.font("Verdana", FontWeight.BOLD, 15));
                    title.setText(item.getTitle());
                    Text text = new Text();
                    text.setText(item.getText());
                    vBox.getChildren().addAll(title, text);
                    vBox.setAlignment(Pos.CENTER_LEFT);

                    Button button = new Button();
                    ImageView delete48 = new ImageView( GuiUtils.getImage("delete48",15,15));
                    delete48.setPreserveRatio(true);
                    button.setGraphic(delete48);
                    button.setOnAction(event -> GuiCommunicator.removeNotification(personList.getSelectionModel().getSelectedItem().getId(), item.getId()));

                    // https://stackoverflow.com/questions/32553658/about-javafx-need-to-align-a-node-that-is-inside-a-listview-to-the-far-right
                    GridPane listCellContents = new GridPane();
                    listCellContents.setHgap(10);

                    if(item.getIcon()!= null) {
                        ImageView icon = new ImageView(GuiUtils.pictureFromBase64(item.getIcon()));
                        listCellContents.add(icon, 0, 0);
                    }
                    if(item.getPicture() != null){
                        ImageView picture = new ImageView(GuiUtils.pictureFromBase64(item.getPicture(),200,200));
                        listCellContents.add(picture, 2, 0);
                    }

                    listCellContents.add(vBox,1,0);
                    ColumnConstraints leftCol = new ColumnConstraints();
                    ColumnConstraints centerCol = new ColumnConstraints();
                    ColumnConstraints rightCol = new ColumnConstraints();

                    rightCol.setHalignment(HPos.RIGHT);
                    rightCol.setHgrow(Priority.ALWAYS);
                    centerCol.setHalignment(HPos.CENTER);
                    centerCol.setHgrow(Priority.ALWAYS);
                    listCellContents.getColumnConstraints().addAll(leftCol,rightCol,centerCol);
                    if(item.getType().equals("notification")) { }
                    else{
                        Button answer = new Button();
                        answer.setText("Answer");
                        PhoneDevice selectedDevice = personList.getSelectionModel().getSelectedItem();
                        if(item.getType().equals("sms"))
                            answer.setOnAction(event -> openSmsDialog(item,selectedDevice.getId()));
                        else if(item.getType().equals("messenger"))
                            answer.setOnAction(event -> openMessengerDialog(item,selectedDevice.getId()));
                        listCellContents.add(answer,3,0);
                    }
                    listCellContents.add(button,4,0);
                    setGraphic(listCellContents);
                }
            }
        });


        // Добавление в таблицу данных из наблюдаемого списка
        personList.setItems(mainApp.getConnectedDevices());
        notifTList.setItems(mainApp.getNotificationsList());
    }

    public void openMessengerDialog(NotificationDevice item, String deviceId) {
    }

    public void openSmsDialog(NotificationDevice item, String deviceId) {
        try {
            GuiCommunicator.show_notification("id",new Notification("У вас пять неотвеченных сообщений: \n Бля ну где ты есть ёмаё","sms","Новое сообщение(5)","java","huj","mabva",new String(Files.readAllBytes(Paths.get("/home/buhalo/Загрузки/icons/jaja"))),new String(Files.readAllBytes(Paths.get("/home/buhalo/Загрузки/icons/kote")))));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void initialize() {
        // Инициализация таблицы адресатов с двумя столбцами.
    }

    public ListView<PhoneDevice> getPersonList() {
        return personList;
    }

    public void setPersonList(ListView<PhoneDevice> personList) {
        this.personList = personList;
    }

    public ListView<NotificationDevice> getNotifTList() {
        return notifTList;
    }

    public void setNotifTList(ListView<NotificationDevice> notifTList) {
        this.notifTList = notifTList;
    }
}
