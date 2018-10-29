package com.push.gui.utils;

import com.notification.NotificationBuilder;
import com.push.gui.controllers.MainController;
import com.push.gui.entity.CustomNotification;
import com.push.gui.entity.NotificationDevice;
import com.push.gui.systray.JavaFXTrayIconSample;
import com.push.lazyir.gui.GuiCommunicator;
import com.push.lazyir.modules.notifications.NotificationTypes;
import com.push.lazyir.modules.reminder.ReminderDto;
import com.push.lazyir.service.main.BackgroundService;
import com.push.lazyir.service.managers.settings.LocalizationManager;
import com.push.lazyir.service.managers.settings.SettingManager;
import com.theme.TextTheme;
import com.theme.ThemePackage;
import com.theme.WindowTheme;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;

import java.awt.*;

public class CustomBuilder implements NotificationBuilder<CustomNotification> {

    private GuiUtils guiUtils;
    private GuiCommunicator guiCommunicator;
    private LocalizationManager localizationManager;
    private SettingManager settingManager;
    private JavaFXTrayIconSample javaFXTrayIconSample;
    private TextTheme textTheme;

    public CustomBuilder(BackgroundService backgroundService, GuiUtils guiUtils) {
        this.guiUtils = guiUtils;
        this.guiCommunicator = backgroundService.getGuiCommunicator();
        this.localizationManager = backgroundService.getLocalizationManager();
        this.javaFXTrayIconSample = backgroundService.getJavaFXTrayIconSample();
        this.settingManager = backgroundService.getSettingManager();
    }

    @Override
    public CustomNotification buildNotification(ThemePackage pack, Object[] args) {
        CustomNotification notification = new CustomNotification(guiUtils,settingManager);
        Rectangle2D bounds = determineScreen();
        double width = bounds.getWidth();
        double height = bounds.getHeight();
        NotificationDevice notificationDevice = (NotificationDevice) args[0];
        if(notificationDevice == null) {
            return null;
        }

        String icon = notificationDevice.getIcon();
        String picture = notificationDevice.getPicture();

        String type = notificationDevice.getType();
        NotificationTypes notificationType;
        if(type != null) {
            notificationType = NotificationTypes.valueOf(type);
        }else {
            notificationType = NotificationTypes.NOTIFICATION;
        }
        // handled by WindowNotification
        WindowTheme windowTheme = pack.getTheme(WindowTheme.class);
        windowTheme.opacity = 1;
        textTheme = pack.getTheme(TextTheme.class);
        textTheme.titleColor = Color.gray;

        configNotification(notification,notificationType,icon,notificationDevice,args);
        setPicture(notification,picture);

        int calculatedWidth = calcNotifWidth(width);
        int calculatedHeight = calcNotifHeight(height);
        windowTheme.width = calculatedWidth;
        windowTheme.height = calculatedHeight;
        notification.setWindowTheme(windowTheme);
        if(notificationType.equals(NotificationTypes.INCOMING)){
            notification.setTextThemeCallColor();
        }else if(notificationType.equals(NotificationTypes.MISSED_IN)){
            notification.setTextThemeCallMiseedColor();
        }else{
            notification.setTextThemeColor(textTheme);
        }
        notification.setText(notificationDevice.getText(),notificationDevice.getTitle(),notificationDevice.getOwnerName());
        return notification;
    }

    private Rectangle2D determineScreen() {
        Rectangle2D bounds = Screen.getPrimary().getBounds();
        String value = settingManager.get("notification-screen");
        if(value != null && !value.equalsIgnoreCase("primary")) {
            int screenNum = Integer.parseInt(value); // todo add in config this key, and in setting window add selection to screen
            Screen screen = Screen.getScreens().get(screenNum);
            if (screen != null && !bounds.intersects(screen.getBounds())) {
                return screen.getBounds();
            }
        }
        return bounds;
    }

    private int calcNotifHeight(double height) {
        if(settingManager.getBool("notification-height-auto",true)){
            int calculatedHeight = (int) (height / 10);
            if (calculatedHeight < 140) {
                calculatedHeight = 140;
            }
            return calculatedHeight;
        }else {
           return settingManager.getInt("notification-height",140);
        }
    }

    private int calcNotifWidth(double width) {
        if(settingManager.getBool("notification-width-auto", true)){
           int calcultedWidth = (int) width / 4;
            if (calcultedWidth < 640) {
                calcultedWidth = 640;
            }
            return calcultedWidth;
        }else {
            return settingManager.getInt("notification-width",640);
        }
    }

    private void setPicture(CustomNotification notification, String picture) {
        if(picture != null && picture.length() > 0)
            notification.setImage(picture,150,150);
    }

    private void configNotification(CustomNotification notification, NotificationTypes type, String icon, NotificationDevice notificationDevice, Object... args) {
        notification.setCloseOnClick(true);
        if(icon != null && icon.length() > 0){
            notification.setIcon(icon, 100, 100);
        }
        String id = (String) args[1];
        switch (type){
            case SMS:
                notification.setIfNull(guiUtils.getDefaultSmsIcon(100,100));
                notification.setFirstButton(localizationManager.get("Reply"), action -> {
                    notification.hide();
                    Platform.runLater(() -> ((MainController) args[2]).openSmsDialog(notificationDevice, id));
                });
                notification.setTextTemeFont(textTheme);
                break;
            case INCOMING:
                notification.setIfNull(guiUtils.getDefaultPersonIcon(100,100));
                notification.setFirstButton(localizationManager.get("Mute"),action->{
                    notification.hide();
                    Platform.runLater(()-> guiCommunicator.muteCall(id));
                });
                notification.setSecondButton(localizationManager.get("Reject"),action->{
                    notification.hide();
                    Platform.runLater(()-> guiCommunicator.rejectCall(id));
                });
                notification.setTextThemeCallFont(textTheme);
                break;
            case MISSED_CALLS:
                notification.setIfNull(guiUtils.getDefaultMissedCallIcon(100,100));
                notification.setFirstButton(localizationManager.get("DismissAll"),action -> {
                    notification.hide();
                    Platform.runLater(()-> guiCommunicator.dismissAllCalls(notificationDevice,id));
                });
                notification.setTextThemecallMissedFont(textTheme);
                break;
            case OUTGOING:
                notification.setIfNull(guiUtils.getDefaultOutgoingCall(100,100));
                notification.setFirstButton(localizationManager.get("Reject"),action->{
                    notification.hide();
                    Platform.runLater(()-> guiCommunicator.rejectOutgoingcall(id));
                });
                notification.setTextTemeFont(textTheme);
                break;
            case UNREAD_MESSAGES:
                notification.setIfNull(guiUtils.getDefaultUnreadMessagesIcon(100,100));
                notification.setFirstButton(localizationManager.get("DismissAll"),action -> {
                    notification.hide();
                    Platform.runLater(()-> guiCommunicator.dissMissAllMessages(id, (ReminderDto) args[3]));
                });
                notification.setSecondButton(localizationManager.get("ShowAll"),action ->{
                    notification.hide();
                    Platform.runLater(()-> javaFXTrayIconSample.showStage());
                });
                notification.setTextTemeFont(textTheme);
                break;
            case MESSENGER:
                notification.setFirstButton(localizationManager.get("Reply"),action->{
                    notification.hide();
                    Platform.runLater(()-> ((MainController) args[2]).openMessengerDialog(notificationDevice,id));
                });
                notification.setTextTemeFont(textTheme);
                break;
            case MISSED_IN:
                notification.setFirstButton(localizationManager.get("Recall"),action->{
                    notification.hide();
                    Platform.runLater(()-> guiCommunicator.recall(notificationDevice,id));
                });
                notification.setTextTemeFont(textTheme);
                break;
            case PAIR:
                notification.setFirstButton(localizationManager.get("Yes"),action->{
                    notification.hide();
                    Platform.runLater(()-> guiCommunicator.pairAnswer(id,true,notificationDevice.getTicker()));
                });
                notification.setSecondButton(localizationManager.get("No"),action->{
                    notification.hide();
                    Platform.runLater(()-> guiCommunicator.pairAnswer(id,false,notificationDevice.getTicker()));
                });
                notification.setTextTemeFont(textTheme);
                break;
            default:
                notification.setIfNull(guiUtils.getDefaultInfo(100,100));
                notification.setTextTemeFont(textTheme);
                break;
        }
    }

}
