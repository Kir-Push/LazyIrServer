package com.push.gui.basew;

import com.push.gui.controllers.MainController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SettingsWindow {
    private boolean opened = false;
    private Lock lock = new ReentrantLock();
    private Stage stage;

    public boolean isOpened() {
        return opened;
    }

    public void setOpened(boolean opened) {
        this.opened = opened;
    }

    public void showWindow(String id,MainController mainController){
        try {
            if (opened)
                return;
            this.opened = true;
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainWin.class.getClassLoader().getResource("fxml/settingsWindow.fxml"));
            AnchorPane rootLayout = (AnchorPane) loader.load();
            Scene scene = new Scene(rootLayout);
            stage = new Stage();

            Label mainPort =(Label) scene.lookup("#mainPort");
            TextField mainPortInput = (TextField) scene.lookup("#mainPortInput");
        }catch (Exception e){

        }
    }
}
