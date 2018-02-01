package com.push.gui.basew;

import com.push.gui.controllers.MainController;
import com.push.lazyir.pojo.Command;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class CommandsWindow {
    private boolean opened = false;
    private String usedId;

    public boolean isOpened() {
        return opened;
    }

    public void setOpened(boolean opened) {
        this.opened = opened;
    }

    public void receiveCommands(List<Command> commands, String id) {

    }

    public void showWindow(String id,MainController mainController){
        try {
            if(opened)
                return;
            this.usedId = id;
            this.opened = true;
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainWin.class.getClassLoader().getResource("fxml/commandWindow.fxml"));
            AnchorPane rootLayout = (AnchorPane) loader.load();
            Scene scene = new Scene(rootLayout);
            Stage stage = new Stage();
            stage.setTitle("Edit Commands");
            stage.setScene(scene);
            TableView table = (TableView)scene.lookup("#table");
        }catch (IOException e){

        }

    }

    public String usedId() {
        return usedId;
    }
}
