package com.push.gui.controllers;

import com.push.gui.basew.MainWin;
import com.push.gui.entity.NotificationDevice;
import com.push.gui.entity.PhoneDevice;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.io.File;

public class MainController {

    private ListView<PhoneDevice> personList;
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

        // Добавление в таблицу данных из наблюдаемого списка
        personList.setItems(mainApp.getConnectedDevices());
        notifTList.setItems(mainApp.getNotificationsList());
    }

    public void initLists(){
        personList.setCellFactory(personList -> new ListCell<>(){
            private final ImageView imageView = new ImageView();


            @Override
            protected void updateItem(PhoneDevice item, boolean empty) {
                super.updateItem(item, empty);
                if(empty){
                    setText(null);
                    setGraphic(null);
                }else{
                    File file = new File("icons/android-phone-128.png");
                    Image image = new Image(file.toURI().toString());
                    imageView.setImage(image);
                    Text text = new Text();
                    text.setText(item.getName());
                    VBox vBox = new VBox();
                    vBox.setSpacing(10);
                    vBox.setId(item.getId());
                    vBox.getChildren().addAll(text,imageView);
                    setText(item.getName());
//                    setGraphic(imageView);
                }
            }
        });
    }
}
