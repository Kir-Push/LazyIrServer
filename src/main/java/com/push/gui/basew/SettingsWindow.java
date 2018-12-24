package com.push.gui.basew;

import com.push.lazyir.service.managers.settings.LocalizationManager;
import com.push.lazyir.service.managers.settings.SettingManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;

@Slf4j
public class SettingsWindow {
    private boolean opened;
    private SettingManager settingManager;
    private LocalizationManager localizationManager;
    private UnaryOperator<TextFormatter.Change> filter = change -> {
        String text = change.getText();
        if (text.matches("[0-9]*")) {
            return change;
        }
        return null;
    };

    @Inject
    public SettingsWindow(SettingManager settingManager, LocalizationManager localizationManager) {
        this.settingManager = settingManager;
        this.localizationManager = localizationManager;
    }
    private boolean isOpened() {
        return opened;
    }

    private void setOpened(boolean opened) {
        this.opened = opened;
    }

    public void showWindow(){
        try {
            if(isOpened()){
                return;
            }
            setOpened(true);
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Thread.currentThread().getContextClassLoader().getResource("fxml/settingsWindow.fxml"));
            AnchorPane rootLayout = loader.load();
            Scene scene = new Scene(rootLayout);
            Stage stage = new Stage();

            Label mainPort =(Label) scene.lookup("#mainPort");
            mainPort.setText(localizationManager.get("settingMainPort"));
            mainPort.setTooltip(new Tooltip(localizationManager.get("settingMainPortTooltip")));
            TextField mainPortInput = (TextField) scene.lookup("#mainPortInput");
            mainPortInput.setTooltip(new Tooltip(localizationManager.get("settingMainPortTooltipInput")));
            mainPortInput.setEditable(true);
            mainPortInput.setText(settingManager.get("TCP-port"));
            mainPortInput.setTextFormatter( ( new TextFormatter<>(filter)));

            Label notifTime = (Label) scene.lookup("#notifTime");
            notifTime.setText(localizationManager.get("settingNotTime"));
            TextField notifTimeInput = (TextField) scene.lookup("#notifTimeInput");
            notifTimeInput.setEditable(true);
            notifTimeInput.setText(settingManager.get("Notif-time"));
            notifTimeInput.setTextFormatter( ( new TextFormatter<>(filter)));

            Label callNotifTime = (Label) scene.lookup("#callnotifTime");
            callNotifTime.setText(localizationManager.get("settingCallNotTime"));
            TextField callNotifTimeInput = (TextField) scene.lookup("#callnotifTimeInput");
            callNotifTimeInput.setEditable(true);
            callNotifTimeInput.setText(settingManager.get("Call-Notif-time"));
            callNotifTimeInput.setTextFormatter( ( new TextFormatter<>(filter)));

            Label maxnotifs = (Label) scene.lookup("#maxnotifs");
            maxnotifs.setText(localizationManager.get("settingsMaxNot"));
            TextField maxNotifsInput = (TextField) scene.lookup("#maxnotifsInput");
            maxNotifsInput.setEditable(true);
            maxNotifsInput.setText(settingManager.get("maxNotifOnScreen"));
            maxNotifsInput.setTextFormatter( new TextFormatter<>(filter));

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
            vlcPortInput.setTextFormatter( new TextFormatter<>(filter));
            vlcPortInput.setText(settingManager.get("Vlc-port"));

            Label vlcPass = (Label) scene.lookup("#vlcPass");
            vlcPass.setText(localizationManager.get("settingsVlcPass"));
            TextField vlcPassInput = (TextField) scene.lookup("#vlcpassInput");
            vlcPassInput.setEditable(true);
            vlcPassInput.setText(settingManager.get("Vlc-pass"));

            Label notificationFontSize = (Label) scene.lookup("#NotificationFontSize");
            notificationFontSize.setText(localizationManager.get("NotificationFontSize"));
            TextField fontSizeField = (TextField) scene.lookup("#fontSizeField");
            fontSizeField.setEditable(true);
            fontSizeField.setText(settingManager.get("NotificationFontSize"));
            fontSizeField.setTextFormatter( ( new TextFormatter<>(filter)));

            ((Label) scene.lookup("#notificationHeight")).setText(localizationManager.get("notificationHeight"));
            ((Label) scene.lookup("#notificationWidth")).setText(localizationManager.get("notificationWidth"));
            TextField heightField = (TextField) scene.lookup("#HeightField");
            heightField.setEditable(true);
            heightField.setText(settingManager.get("notification-height"));
            heightField.setTextFormatter( ( new TextFormatter<>(filter)));

            TextField widthField = (TextField) scene.lookup("#NotificationField");
            widthField.setEditable(true);
            widthField.setText(settingManager.get("notification-width"));
            widthField.setTextFormatter( ( new TextFormatter<>(filter)));

            CheckBox heightAuto = (CheckBox) scene.lookup("#heightAuto");
            heightAuto.setSelected(settingManager.getBool("notification-height-auto",true));
            heightAuto.setTooltip(new Tooltip(localizationManager.get("heigh-auto-tooltip")));
            CheckBox widthAuto = (CheckBox) scene.lookup("#widthAuto");
            widthAuto.setSelected(settingManager.getBool("notification-width-auto",true));
            widthAuto.setTooltip(new Tooltip(localizationManager.get("width-auto-tooltip")));


            Label notifScreen = (Label) scene.lookup("#notificationScreen");
            notifScreen.setText(localizationManager.get("notificationScreen"));
            ComboBox<String> screenBox = (ComboBox) scene.lookup("#screenBox");
            Set<String> screens = new HashSet<>();
            screens.add("primary");
            ObservableList<Screen> screenObservableList = Screen.getScreens();
            IntStream.range(0, screenObservableList.size()).forEach(i -> screens.add(String.valueOf(i+1)));
            screenBox.setItems(FXCollections.observableArrayList(screens));


            Label lang = (Label) scene.lookup("#lang");
            lang.setText(localizationManager.get("settingsLang"));
            ComboBox<String> langs = (ComboBox) scene.lookup("#langBox");
            Set<String> langs1 = localizationManager.getLangs();
            ObservableList<String> values = FXCollections.observableArrayList(langs1);
            langs.setItems(values);

            Button save = (Button) scene.lookup("#save");
            save.setOnAction(event -> {
                String value = langs.getValue();
                if(value != null) {
                    settingManager.saveValue("LANG", value);
                }
                settingManager.saveValue("Vlc-port",vlcPortInput.getText());
                settingManager.saveValue("Vlc-pass",vlcPassInput.getText());
                settingManager.saveValue("muteWhenCall",muteInCheck.isSelected() ? "true" : "false");
                settingManager.saveValue("muteWhenOutcomingCall",muteOutCheck.isSelected() ? "true" : "false");
                settingManager.saveValue("maxNotifOnScreen",maxNotifsInput.getText());
                settingManager.saveValue("Call-Notif-time",callNotifTimeInput.getText());
                settingManager.saveValue("Notif-time",notifTimeInput.getText());
                settingManager.saveValue("TCP-port",mainPortInput.getText());
                settingManager.saveValue("NotificationFontSize",fontSizeField.getText());
                settingManager.saveValue("notification-width",widthField.getText());
                settingManager.saveValue("notification-height",heightField.getText());
                settingManager.saveValue("notification-width-auto",widthAuto.isSelected() ? "true" : "false");
                settingManager.saveValue("notification-height-auto",heightAuto.isSelected() ? "true" : "false");
                String screen = screenBox.getValue();
                if(screen != null) {
                    settingManager.saveValue("notification-screen",screen);
                }
            });
            stage.setOnCloseRequest(event -> setOpened(false));
            stage.setScene(scene);
            stage.show();

        }catch (IOException e){
           setOpened(false);
           log.error("ShowMessage",e);
        }
    }
}
