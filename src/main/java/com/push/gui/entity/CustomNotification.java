package com.push.gui.entity;

import com.notification.NotificationBuilder;
import com.notification.NotificationFactory;
import com.notification.manager.QueueManager;
import com.notification.types.BorderLayoutNotification;
import com.push.gui.controllers.MainController;
import com.push.gui.systray.JavaFXTrayIconSample;
import com.push.gui.utils.GuiUtils;
import com.push.lazyir.gui.GuiCommunicator;
import com.push.lazyir.modules.notifications.call.callTypes;
import com.push.lazyir.modules.reminder.MessagesPack;
import com.push.lazyir.service.main.BackgroundService;
import com.theme.TextTheme;
import com.theme.ThemePackage;
import com.theme.ThemePackagePresets;
import com.theme.WindowTheme;
import com.utils.Time;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;

import javax.inject.Inject;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class CustomNotification extends BorderLayoutNotification {
    private JLabel icon_lbl;
    private JLabel text_lbl;
    private JLabel img_lbl;

    private JPanel btn_panel;
    private JPanel right_panel;
    private JButton m_button;
    private JButton m_button2;
    private TextTheme m_theme;

    public CustomNotification() {

        icon_lbl = new JLabel();
        text_lbl = new JLabel();
        img_lbl = new JLabel();
        m_button = new JButton();
        m_button2 = new JButton();
        GridLayout gl = new GridLayout(0,1);
        right_panel = new JPanel();

        btn_panel = new JPanel(gl);
        right_panel.add(img_lbl);
        right_panel.add(btn_panel);
        this.addComponent(icon_lbl, BorderLayout.WEST);
        this.addComponent(text_lbl,BorderLayout.CENTER);
        this.addComponent(right_panel,BorderLayout.EAST);
    }

    public void setTextTemeFont(TextTheme theme) {
        text_lbl.setFont(theme.subtitle);
        m_button.setFont(theme.subtitle);
        m_button2.setFont(theme.subtitle);
        m_theme = theme;
    }

    public void setTextThemeColor(TextTheme theme){
        text_lbl.setForeground(theme.titleColor);
        text_lbl.setBackground(theme.subtitleColor);
        m_button.setForeground(theme.subtitleColor);
        m_button2.setForeground(theme.subtitleColor);
    }

    public void setTextThemeCallFont(TextTheme theme){
        text_lbl.setFont(theme.subtitle);
        text_lbl.setForeground(theme.titleColor);
        text_lbl.setBackground(theme.subtitleColor);
        m_button.setFont(theme.title);
        m_button2.setFont(theme.title);
    }

    public void setTextThemeCallColor(){
     //   m_button.setForeground(Color.GREEN);
      //  m_button2.setForeground(   Color.RED );
    }

    public void setTextThemecallMissedFont(TextTheme theme) {
        text_lbl.setFont(theme.subtitle);
        text_lbl.setForeground(theme.titleColor);
        text_lbl.setBackground(theme.subtitleColor);
        m_button.setFont(theme.title);
    }

    public void setTextThemeCallMiseedColor(){
        m_button.setForeground(Color.GREEN);
    }


    public void setImage(String base64,int width,int heigh){
        img_lbl.setIcon(GuiUtils.pictureFromBase64Swing(base64,width,heigh));
    }

    public void setIcon(String base64,int width,int heigh){
        icon_lbl.setIcon(GuiUtils.pictureFromBase64Swing(base64,width,heigh));
    }

    public void setIcon(Icon icon){
        if(icon != null)
        icon_lbl.setIcon(icon);
    }

    public void setFirstButton(String text,ActionListener action){
        m_button.setText(text);
        m_button.addActionListener(action);
        btn_panel.add(m_button,0);
    }

    public void setSecondButton(String text,ActionListener action){
        m_button2.setText(text);
        m_button2.addActionListener(action);
        btn_panel.add(m_button2,1);
    }

    public String getText() {
        return text_lbl.getText();
    }

    public void setText(String text,String title,String dvName) {
        if(title == null)
            title = "";
        if(text == null)
            text = "";
        if(dvName == null)
            dvName = "";
        title = "<b>" + title + "</b><br/>";
        dvName = "<br/><font size=\"2\">" + dvName +  "</font>";
        text = "<html>" + title +  text.replaceAll("<","&lt;").replaceAll(">", "&gt;").replaceAll("\n", "<br/>") + dvName + "</html>";
        text_lbl.setText(text);
    }

    @Override
    public void setWindowTheme(WindowTheme theme) {
        super.setWindowTheme(theme);

        if (m_theme != null) {
            // the WindowNotification is going to automatically give all our labels with the set foreground color, but
            // we want to change this to the title color of the font
            icon_lbl.setForeground(m_theme.titleColor);
          //  m_button.setForeground(m_theme.subtitleColor);
        }
    }

    public static class CustomBuilder implements NotificationBuilder<CustomNotification> {

        private GuiUtils guiUtils;
        private GuiCommunicator guiCommunicator;
        private BackgroundService backgroundService;

        public CustomBuilder(BackgroundService backgroundService, GuiUtils guiUtils) {
            this.guiUtils = guiUtils;
            this.backgroundService = backgroundService;
            this.guiCommunicator = backgroundService.getGuiCommunicator();
        }

        @Override
        public CustomNotification buildNotification(ThemePackage pack, Object[] args) {
            CustomNotification notification = new CustomNotification();

            Rectangle2D bounds = Screen.getPrimary().getBounds();
            double width = bounds.getWidth();
            double height = bounds.getHeight();
            NotificationDevice notificationDevice = (NotificationDevice) args[0];
            String id = (String) args[1];
            MainController controller = (MainController) args[2];
            if(notificationDevice == null)
                return null;
            // handled by us
            if (args.length > 0) {
                notification.setText(notificationDevice.getText(),notificationDevice.getTitle(),notificationDevice.getOwnerName());
            } else {
                notification.setText("","","");
            }

            String icon = notificationDevice.getIcon();
            String picture = notificationDevice.getPicture();

            boolean missed = notificationDevice.getType().equalsIgnoreCase(callTypes.missedIn.name());
            boolean incoming = notificationDevice.getType().equalsIgnoreCase(callTypes.incoming.name());

            if(icon != null && icon.length() > 0)
            notification.setIcon(icon, 100, 100);
            else if(icon == null && notificationDevice.getType().equals("sms"))
                notification.setIcon(GuiUtils.getDefaultSmsIcon(100,100));
            else if(icon == null && incoming){
                notification.setIcon(GuiUtils.getDefaultPersonIcon(100,100));
            }else if(icon == null && notificationDevice.getType().equals("missedCalls")){
                notification.setIcon(GuiUtils.getDefaultMissedCallIcon(100,100));
            }else if(icon == null && notificationDevice.getType().equalsIgnoreCase(callTypes.outgoing.name())){
                notification.setIcon(GuiUtils.getDefaultOutgoingCall(100,100));
            }else if(icon == null && notificationDevice.getType().equals("unreadMessages")){
                notification.setIcon(GuiUtils.getDefaultUnreadMessagesIcon(100,100));
            }else if(icon == null){
                notification.setIcon(GuiUtils.getDefaultInfo(100,100));
            }
            if(picture != null && picture.length() > 0)
            notification.setImage(picture,150,150);

            if(notificationDevice.getType().equals("missedCalls")){
                notification.setFirstButton("DismissAll",action -> {
                    notification.hide();
                    Platform.runLater(()->{
                        guiCommunicator.dismissAllCalls(notificationDevice,id);
                    });
                });
            }
            else if(notificationDevice.getType().equalsIgnoreCase("unreadMessages")){
                notification.setFirstButton("DissmissAll",action -> {
                    notification.hide();
                    Platform.runLater(()->{
                        guiCommunicator.dissMissAllMessages(notificationDevice,id, (MessagesPack) args[3]);
                    });
                });
                notification.setSecondButton("Show All",action ->{
                    notification.hide();
                    Platform.runLater(()->{
                        JavaFXTrayIconSample.getInstance().showStage();
                    });
                });
            }
            else if(notificationDevice.getType().equals("sms")) {
                notification.setFirstButton("Reply", action -> {
                    notification.hide();
                    Platform.runLater(() -> {
                        controller.openSmsDialog(notificationDevice, id);
                    });
                });
            }
            else if(notificationDevice.getType().equals("messenger")){
                notification.setFirstButton("Reply",action->{
                    notification.hide();
                    Platform.runLater(()->{
                        controller.openMessengerDialog(notificationDevice,id);
                    });
                });
            }else if(incoming){
                notification.setFirstButton("Mute",action->{
                    notification.hide();
                    Platform.runLater(()->{
                        guiCommunicator.muteCall(notificationDevice,id);
                    });
                });
                notification.setSecondButton("Reject",action->{
                    notification.hide();
                    Platform.runLater(()->{
                        guiCommunicator.rejectCall(notificationDevice,id);
                    });
                });
            }else if(notificationDevice.getType().equalsIgnoreCase(callTypes.outgoing.name())){
                notification.setFirstButton("Reject",action->{
                    notification.hide();
                    Platform.runLater(()->{
                        guiCommunicator.rejectOutgoingcall(notificationDevice,id);
                    });
                });
            }
            else if(missed){
                notification.setFirstButton("Recall",action->{
                    notification.hide();
                    Platform.runLater(()->{
                        guiCommunicator.recall(notificationDevice,id);
                    });
                });
            }else if(notificationDevice.getType().equalsIgnoreCase("pair")){
                notification.setFirstButton("Yes",action->{
                    notification.hide();
                    Platform.runLater(()->{
                        guiCommunicator.pairAnswer(id,true,notificationDevice.getTicker());
                    });
                });
                notification.setSecondButton("No",action->{
                    notification.hide();
                    Platform.runLater(()->{
                        guiCommunicator.pairAnswer(id,false,notificationDevice.getTicker());
                    });
                });
            }

            TextTheme theme1 = pack.getTheme(TextTheme.class);
            theme1.titleColor = Color.gray;
            if(incoming){
               notification.setTextThemeCallFont(theme1);
            }else if(missed){
                notification.setTextThemecallMissedFont(theme1);
            }else {
                notification.setTextTemeFont(theme1);
            }
            notification.setCloseOnClick(true);
            // handled by WindowNotification
            WindowTheme theme = pack.getTheme(WindowTheme.class);
            theme.opacity = 1;
            int calculatedWidth = notification.text_lbl.getPreferredSize().width + notification.right_panel.getPreferredSize().width + notification.icon_lbl.getPreferredSize().width + 70;
            int calculatedHeight = 0;
            int maxHeighCalculated = notification.text_lbl.getPreferredSize().height;
            if(maxHeighCalculated < notification.right_panel.getPreferredSize().height)
                maxHeighCalculated = notification.right_panel.getPreferredSize().height;
            if(maxHeighCalculated < notification.icon_lbl.getPreferredSize().height)
                maxHeighCalculated = notification.icon_lbl.getPreferredSize().height;

            if(calculatedWidth < width/7)
                calculatedWidth = (int) (width/6);
            else if(calculatedWidth > width/3)
                calculatedWidth = (int) width/3; // test witch big text notifications

            maxHeighCalculated += 20;
            calculatedHeight = maxHeighCalculated;
            String text = notification.text_lbl.getText();
            int length = text.length();
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
                calculatedHeight += 5;
           }
            if(calculatedHeight > height/4)
                calculatedHeight = (int) ((double)height/4);
            theme.width = calculatedWidth;
            theme.height = calculatedHeight;
            notification.setWindowTheme(theme);
            if(incoming){
                notification.setTextThemeCallColor();
            }else if(missed){
                notification.setTextThemeCallMiseedColor();
            }else{
                notification.setTextThemeColor(theme1);
            }
            return notification;
        }
    }
}