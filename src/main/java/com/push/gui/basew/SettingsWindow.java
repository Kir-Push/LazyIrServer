package com.push.gui.basew;

import com.push.gui.controllers.MainController;
import com.push.lazyir.service.BackgroundService;
import com.push.lazyir.service.settings.LocalizationManager;
import com.push.lazyir.service.settings.SettingManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SettingsWindow {
    private static boolean opened = false;
    private Lock lock = new ReentrantLock();
//    private static Stage stage;

    public boolean isOpened() {
        return opened;
    }

    public void setOpened(boolean opened) {
        this.opened = opened;
    }

    public static void showWindow(String id,MainController mainController){
        try {
            if(opened){
                return;
            }
            opened = true;
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainWin.class.getClassLoader().getResource("fxml/settingsWindow.fxml"));
            AnchorPane rootLayout = (AnchorPane) loader.load();
            Scene scene = new Scene(rootLayout);
            Stage stage = new Stage();

            LocalizationManager localizationManager = BackgroundService.getLocalizationManager();
            SettingManager settingManager = BackgroundService.getSettingManager();

            Label mainPort =(Label) scene.lookup("#mainPort");
            mainPort.setText(localizationManager.get("settingMainPort"));
            mainPort.setTooltip(new Tooltip(localizationManager.get("settingMainPortTooltip")));
            TextField mainPortInput = (TextField) scene.lookup("#mainPortInput");
            mainPortInput.setTooltip(new Tooltip(localizationManager.get("settingMainPortTooltipInput")));
            mainPortInput.setEditable(true);
            mainPortInput.setText(settingManager.get("TCP-port"));

            Label notifTime = (Label) scene.lookup("#notifTime");
            notifTime.setText(localizationManager.get("settingNotTime"));
            TextField notifTimeInput = (TextField) scene.lookup("#notifTimeInput");
            notifTimeInput.setEditable(true);
            notifTimeInput.setText(settingManager.get("Notif-time"));

            Label callNotifTime = (Label) scene.lookup("#callnotifTime");
            callNotifTime.setText(localizationManager.get("settingCallNotTime"));
            TextField callNotifTimeInput = (TextField) scene.lookup("#callnotifTimeInput");
            callNotifTimeInput.setEditable(true);
            callNotifTimeInput.setText(settingManager.get("Call-Notif-time"));

            Label maxnotifs = (Label) scene.lookup("#maxnotifs");
            maxnotifs.setText(localizationManager.get("settingsMaxNot"));
            TextField maxNotifsInput = (TextField) scene.lookup("#maxnotifsInput");
            maxNotifsInput.setEditable(true);
            maxNotifsInput.setText(settingManager.get("maxNotifOnScreen"));

            Label muteIncome = (Label) scene.lookup("#incomCall");
            muteIncome.setText(localizationManager.get("settingsIncomeMute"));
            muteIncome.setTooltip(new Tooltip(localizationManager.get("settingsIncomeMuteTooltip")));
            CheckBox muteInCheck = (CheckBox) scene.lookup("#incomingCallcheck");
            muteInCheck.setSelected(Boolean.valueOf(settingManager.get("muteWhenCall")));

            Label muteOutcome = (Label) scene.lookup("#muteOutCall");
            muteOutcome.setText(localizationManager.get("settingsOutcomeMute"));
            CheckBox muteOutCheck = (CheckBox) scene.lookup("#outgoingCallcheck");
            muteOutCheck.setSelected(Boolean.valueOf(settingManager.get("muteWhenOutcomingCall")));

            Label vlcPort = (Label) scene.lookup("#vlcPort");
            vlcPort.setText(localizationManager.get("settingsVlcPorts"));
            TextField vlcPortInput = (TextField) scene.lookup("#vlcportInput");
            vlcPortInput.setEditable(true);
            vlcPortInput.setText(settingManager.get("Vlc-port"));

            Label vlcPass = (Label) scene.lookup("#vlcPass");
            vlcPass.setText(localizationManager.get("settingsVlcPass"));
            TextField vlcPassInput = (TextField) scene.lookup("#vlcpassInput");
            vlcPassInput.setEditable(true);
            vlcPassInput.setText(settingManager.get("Vlc-pass"));
            Label lang = (Label) scene.lookup("#lang");
            lang.setText(localizationManager.get("settingsLang"));
            ComboBox<String> langs = (ComboBox) scene.lookup("#langBox");
            Set<String> langs1 = localizationManager.getLangs();
            ObservableList<String> values = FXCollections.observableArrayList(langs1);
            langs.setItems(values);

            Button save = (Button) scene.lookup("#save");
            save.setOnAction(event -> {
                SettingManager settingManager1 = BackgroundService.getSettingManager();
                String value = langs.getValue();
                if(value != null)
                settingManager1.saveValue("LANG", value);
                settingManager1.saveValue("Vlc-port",vlcPortInput.getText());
                settingManager1.saveValue("Vlc-pass",vlcPassInput.getText());
                settingManager1.saveValue("muteWhenCall",muteInCheck.isSelected() ? "true" : "false");
                settingManager1.saveValue("muteWhenOutcomingCall",muteOutCheck.isSelected() ? "true" : "false");
                settingManager1.saveValue("maxNotifOnScreen",maxNotifsInput.getText());
                settingManager1.saveValue("Call-Notif-time",callNotifTimeInput.getText());
                settingManager1.saveValue("Notif-time",notifTimeInput.getText());
                settingManager1.saveValue("TCP-port",mainPortInput.getText());
            });
            stage.setOnCloseRequest(event -> {
                opened = false;
            });
            stage.setScene(scene);
            stage.show();

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
