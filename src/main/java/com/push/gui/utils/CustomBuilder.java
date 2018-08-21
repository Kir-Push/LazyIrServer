package com.push.gui.utils;

import com.notification.NotificationBuilder;
import com.push.gui.controllers.MainController;
import com.push.gui.entity.CustomNotification;
import com.push.gui.entity.NotificationDevice;
import com.push.gui.systray.JavaFXTrayIconSample;
import com.push.lazyir.gui.GuiCommunicator;
import com.push.lazyir.modules.notifications.call.NotificationTypes;
import com.push.lazyir.modules.reminder.MessagesPack;
import com.push.lazyir.service.main.BackgroundService;
import com.push.lazyir.service.managers.settings.LocalizationManager;
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
    private JavaFXTrayIconSample javaFXTrayIconSample;
    private TextTheme textTheme;
    private int widthMagicNumber = 70;
    private int heihhtMagicNumber = 20;

    public CustomBuilder(BackgroundService backgroundService, GuiUtils guiUtils) {
        this.guiUtils = guiUtils;
        this.guiCommunicator = backgroundService.getGuiCommunicator();
        this.localizationManager = backgroundService.getLocalizationManager();
        this.javaFXTrayIconSample = backgroundService.getJavaFXTrayIconSample();
    }

    @Override
    public CustomNotification buildNotification(ThemePackage pack, Object[] args) {
        CustomNotification notification = new CustomNotification(guiUtils);
        Rectangle2D bounds = Screen.getPrimary().getBounds();
        double width = bounds.getWidth();
        double height = bounds.getHeight();
        NotificationDevice notificationDevice = (NotificationDevice) args[0];
        if(notificationDevice == null) {
            return null;
        }

        notification.setText(notificationDevice.getText(),notificationDevice.getTitle(),notificationDevice.getOwnerName());

        String icon = notificationDevice.getIcon();
        String picture = notificationDevice.getPicture();

        NotificationTypes notificationType = NotificationTypes.valueOf(notificationDevice.getType());

        // handled by WindowNotification
        WindowTheme windowTheme = pack.getTheme(WindowTheme.class);
        windowTheme.opacity = 1;
        textTheme = pack.getTheme(TextTheme.class);
        textTheme.titleColor = Color.gray;

        configNotification(notification,notificationType,icon,notificationDevice,args);
        setPicture(notification,picture);

        int calculatedWidth = calcNotifWidth(notification,width);
        int calculatedHeight = calcNotifHeight(notification,height);
        windowTheme.width = calculatedWidth;
        windowTheme.height = calculatedHeight;
        notification.setWindowTheme(windowTheme);
        if(notificationType.equals(NotificationTypes.incoming)){
            notification.setTextThemeCallColor();
        }else if(notificationType.equals(NotificationTypes.missedIn)){
            notification.setTextThemeCallMiseedColor();
        }else{
            notification.setTextThemeColor(textTheme);
        }
        return notification;
    }

    private int calcNotifHeight(CustomNotification notification, double height) {
        int iconHeigh = notification.getIconlbl().getPreferredSize().height;
        int rightPanelHeigh = notification.getRightpanel().getPreferredSize().height;
        int textHeigh = notification.getTextlbl().getPreferredSize().height;
        int maxHeighCalculated = textHeigh;
        if(maxHeighCalculated < rightPanelHeigh)
            maxHeighCalculated = rightPanelHeigh;
        if(maxHeighCalculated < iconHeigh)
            maxHeighCalculated = iconHeigh;
        maxHeighCalculated += heihhtMagicNumber;
        String text = notification.getTextlbl().getText();
        int lastIndex = 0;
        int count = 0;
        while(lastIndex != -1){
            lastIndex = text.indexOf("<br/>",lastIndex);
            if(lastIndex != -1){
                count ++;
                lastIndex += "<br>".length();
            }
        }
        for(int i =0;i<count;i++){
            maxHeighCalculated += 5;
        }
        if(maxHeighCalculated > height/4)
            maxHeighCalculated = (int)(height/4);
        return maxHeighCalculated;
    }

    private int calcNotifWidth(CustomNotification notification, double width) {
        int calculatedWidth = notification.getTextlbl().getPreferredSize().width + notification.getRightpanel().getPreferredSize().width + notification.getIconlbl().getPreferredSize().width + widthMagicNumber;
        // if less than 1/7 part of screen
        // set to 1/6 part of screen
        //  if more than 1/3 part of screen
        // set to 1/5 part of screen
        if(calculatedWidth < width/7) {
            calculatedWidth = (int) (width/6);
        }
        else if(calculatedWidth > width/3) {
            calculatedWidth = (int) width/5; // test witch big text notifications
        }
        return calculatedWidth;
    }

    private void setPicture(CustomNotification notification, String picture) {
        if(picture != null && picture.length() > 0)
            notification.setImage(picture,150,150);
    }

    private void configNotification(CustomNotification notification, NotificationTypes type, String icon, NotificationDevice notificationDevice, Object... args) {
        if(icon != null && icon.length() > 0){
            notification.setIcon(icon, 100, 100);
            return;
        }
        String id = (String) args[1];
        switch (type){
            case sms:
                notification.setIcon(guiUtils.getDefaultSmsIcon(100,100));
                notification.setFirstButton(localizationManager.get("Reply"), action -> {
                    notification.hide();
                    Platform.runLater(() -> ((MainController) args[2]).openSmsDialog(notificationDevice, id));
                });
                notification.setTextTemeFont(textTheme);
                break;
            case incoming:
                notification.setIcon(guiUtils.getDefaultPersonIcon(100,100));
                notification.setFirstButton(localizationManager.get("Mute"),action->{
                    notification.hide();
                    Platform.runLater(()-> guiCommunicator.muteCall(notificationDevice,id));
                });
                notification.setSecondButton(localizationManager.get("Reject"),action->{
                    notification.hide();
                    Platform.runLater(()-> guiCommunicator.rejectCall(notificationDevice,id));
                });
                notification.setTextThemeCallFont(textTheme);
                break;
            case missedCalls:
                notification.setIcon(guiUtils.getDefaultMissedCallIcon(100,100));
                notification.setFirstButton(localizationManager.get("DismissAll"),action -> {
                    notification.hide();
                    Platform.runLater(()-> guiCommunicator.dismissAllCalls(notificationDevice,id));
                });
                notification.setTextThemecallMissedFont(textTheme);
                break;
            case outgoing:
                notification.setIcon(guiUtils.getDefaultOutgoingCall(100,100));
                notification.setFirstButton(localizationManager.get("Reject"),action->{
                    notification.hide();
                    Platform.runLater(()-> guiCommunicator.rejectOutgoingcall(notificationDevice,id));
                });
                notification.setTextTemeFont(textTheme);
                break;
            case unreadMessages:
                notification.setIcon(guiUtils.getDefaultUnreadMessagesIcon(100,100));
                notification.setFirstButton(localizationManager.get("DismissAll"),action -> {
                    notification.hide();
                    Platform.runLater(()-> guiCommunicator.dissMissAllMessages(notificationDevice,id, (MessagesPack) args[3]));
                });
                notification.setSecondButton(localizationManager.get("ShowAll"),action ->{
                    notification.hide();
                    Platform.runLater(()-> javaFXTrayIconSample.showStage());
                });
                notification.setTextTemeFont(textTheme);
                break;
            case messenger:
                notification.setFirstButton(localizationManager.get("Reply"),action->{
                    notification.hide();
                    Platform.runLater(()-> ((MainController) args[2]).openMessengerDialog(notificationDevice,id));
                });
                notification.setTextTemeFont(textTheme);
                break;
            case missedIn:
                notification.setFirstButton(localizationManager.get("Recall"),action->{
                    notification.hide();
                    Platform.runLater(()-> guiCommunicator.recall(notificationDevice,id));
                });
                notification.setTextTemeFont(textTheme);
                break;
            case pair:
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
                notification.setIcon(guiUtils.getDefaultInfo(100,100));
                notification.setTextTemeFont(textTheme);
                break;
        }
    }

}
