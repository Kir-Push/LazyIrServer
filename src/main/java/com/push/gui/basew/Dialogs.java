package com.push.gui.basew;

import com.push.gui.controllers.MainController;
import com.push.gui.entity.NotificationDevice;
import com.push.gui.utils.GuiUtils;
import com.push.lazyir.gui.GuiCommunicator;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Dialogs {

    private List<File> draggedFiles; // todo

    public void showAnswerMessenger(String id, NotificationDevice notification, MainController mainController){
        try {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(MainWin.class.getClassLoader().getResource("fxml/answerWindow.fxml"));
        AnchorPane rootLayout = (AnchorPane) loader.load();
        Scene scene = new Scene(rootLayout, 450, 450);
        Stage stage = new Stage();
        stage.setTitle("Answer Message");
        stage.setScene(scene);
        TextArea textArea = (TextArea) scene.lookup("#messageText");
        textArea.setText(notification.getText());
        if(notification.getPicture() != null) {
            ImageView imageView = (ImageView) scene.lookup("#messageImg");
            imageView.setImage(GuiUtils.pictureFromBase64(notification.getPicture()));
        }
        TextArea answerText = (TextArea) scene.lookup("#answerText");
        answerText.setOnDragOver(new EventHandler<DragEvent>() {

                @Override
                public void handle(DragEvent event) {
                    if (event.getDragboard().hasString()) {
                    /* allow for both copying and moving, whatever user chooses */
                        event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                    }
                    event.consume();
                }
            });
        answerText.setOnDragDropped(event->{
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
               db.getFiles().forEach(System.out::println);
               draggedFiles = db.getFiles();
                success = true;
            }
                /* let the source know whether the string was successfully
                 * transferred and used */
            event.setDropCompleted(success);

            event.consume();
        });

            String typName = notification.getPack() + ":" + notification.getTitle();
            String name = notification.getTitle();
            ButtonBar btnBar = (ButtonBar) scene.lookup("#buttonBar");
            Button answerBtn = (Button) btnBar.getButtons().get(0);
        answerBtn.setOnAction(event -> {
            if(notification.getType().equals("sms"))
                GuiCommunicator.sendSmsAnswer(id,name,answerText.getText(),draggedFiles);
            else if(notification.getType().equals("messenger"))
            GuiCommunicator.sendMessengerAnswer(id,typName,answerText.getText(), draggedFiles);
            stage.hide();
        });
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showAnswerSms(String id, NotificationDevice notification, MainController mainController) {

    }
}
