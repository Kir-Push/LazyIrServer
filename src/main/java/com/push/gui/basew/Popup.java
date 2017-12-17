package com.push.gui.basew;

import com.push.gui.controllers.MainController;
import com.push.gui.entity.NotificationDevice;
import com.push.gui.utils.GuiUtils;
import com.push.lazyir.modules.notifications.Notification;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
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
import javafx.util.Duration;
import org.controlsfx.control.Notifications;
import org.controlsfx.control.action.Action;

import java.util.function.Consumer;


public interface Popup {
    static void show(String id, NotificationDevice notification, MainController mainController) {
        GridPane listCellContents = new GridPane();
        listCellContents.setHgap(10);
        if (notification.getIcon() != null && notification.getIcon().length() > 1)
            listCellContents.add(GuiUtils.getPictureFromBase64(notification.getIcon(), 40, 40), 0, 0);

        VBox vBox = new VBox();
        Text title = new Text();
        title.setFont(Font.font("Verdana", FontWeight.BOLD, 15));
        title.setText(notification.getTitle() + "\n");
        Text text = new Text();
        text.setText(notification.getText());
        vBox.getChildren().addAll(title, text);
        vBox.setAlignment(Pos.TOP_LEFT);
        listCellContents.add(vBox, 1, 0);

        ColumnConstraints leftCol = new ColumnConstraints();
        ColumnConstraints centerCol = new ColumnConstraints();
        ColumnConstraints rightCol = new ColumnConstraints();

        if (notification.getPicture() != null && notification.getPicture().length() > 1)
            listCellContents.add(GuiUtils.getPictureFromBase64(notification.getPicture(), 120, 120), 2, 0);
        rightCol.setHalignment(HPos.RIGHT);
        rightCol.setHgrow(Priority.ALWAYS);
        centerCol.setHalignment(HPos.CENTER);
        centerCol.setHgrow(Priority.ALWAYS);
        listCellContents.getColumnConstraints().addAll(leftCol, centerCol, rightCol);
        listCellContents.setMinHeight(100);
        listCellContents.setAlignment(Pos.CENTER);

        Notifications notif = Notifications.create();
        Pos bottomRight = Pos.BOTTOM_RIGHT;
        notif.text("").title("")
                .graphic(listCellContents)
                .position(bottomRight)
                .hideAfter(Duration.seconds(20));
        String type = notification.getType();
        if (type.equals("messenger") || type.equals("sms")) {
            Action reply = new Action(consumer->{
                GuiUtils.hideNotif(notif,bottomRight);
                new Dialogs().showAnswerMessenger(id,notification,mainController);
            });
            reply.setText("Reply");
            notif.action(reply);
        }

            Platform.runLater(() -> notif.show());

    }
}
