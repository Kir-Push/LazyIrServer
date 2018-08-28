package com.push.gui.basew;
import com.push.gui.entity.NotificationDevice;
import com.push.gui.utils.GuiUtils;
import com.push.lazyir.gui.GuiCommunicator;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.io.IOException;

@Slf4j
public class Dialogs {

    private GuiCommunicator guiCommunicator;
    private GuiUtils guiUtils;

    @Inject
    public Dialogs(GuiCommunicator guiCommunicator,GuiUtils guiUtils) {
        this.guiCommunicator = guiCommunicator;
        this.guiUtils = guiUtils;
    }

    public void showAnswerMessenger(String id, NotificationDevice notification){
        try {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Thread.currentThread().getContextClassLoader().getResource("fxml/answerWindow.fxml"));
        AnchorPane rootLayout = loader.load();

        Scene scene = new Scene(rootLayout, 450, 450);
        Stage stage = new Stage();
        stage.setTitle("Answer Message");
        stage.setScene(scene);

        TextArea textArea = (TextArea) scene.lookup("#messageText");
        textArea.setText(notification.getText());
        if(notification.getPicture() != null) {
            ImageView imageView = (ImageView) scene.lookup("#messageImg");
            imageView.setImage(guiUtils.pictureFromBase64(notification.getIcon()));
        }

        TextArea answerText = (TextArea) scene.lookup("#answerText");

        String typName = notification.getPack() + ":" + notification.getTitle();
        String name = notification.getTitle();

        ButtonBar btnBar = (ButtonBar) scene.lookup("#buttonBar");
        Button answerBtn = (Button) btnBar.getButtons().get(0);

        answerBtn.setOnAction(event -> {
            String type = notification.getType();
            if(type.equals("SMS"))
                guiCommunicator.sendSmsAnswer(id,name,answerText.getText());
             else if(type.equals("MESSENGER") )
                 guiCommunicator.sendMessengerAnswer(id,typName,answerText.getText());
            stage.hide();
        });
            stage.show();
        } catch (IOException e) {
            log.error("showAnswerMessenger id - " + id + " NotificationDevice - " + notification,e);
        }
    }
}
