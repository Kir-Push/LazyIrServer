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
import com.theme.TextTheme;
import com.theme.ThemePackage;
import com.theme.ThemePackagePresets;
import com.theme.WindowTheme;
import com.utils.Time;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;

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
        m_button.setForeground(Color.GREEN);
        m_button2.setForeground(   Color.RED );
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

    public void setText(String text,String title) {
        title = "<b>" + title + "</b><br/>";
        text = "<html>" + title +  text.replaceAll("<","&lt;").replaceAll(">", "&gt;").replaceAll("\n", "<br/>") + "</html>";
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
                notification.setText(notificationDevice.getText(),notificationDevice.getTitle());
            } else {
                notification.setText("","");
            }

            String icon = notificationDevice.getIcon();
            String picture = notificationDevice.getPicture();

            boolean missed = notificationDevice.getType().equalsIgnoreCase(callTypes.missedIn.name());
            boolean incoming = notificationDevice.getType().equalsIgnoreCase(callTypes.incoming.name());

            if(icon != null && icon.length() > 0)
            notification.setIcon(icon, 100, 100);
            else if(icon == null && notificationDevice.getType().equals("sms"))
                notification.setIcon(GuiUtils.getDefaultIconMessage(),100,100);
            else if(icon == null && incoming){
                notification.setIcon(GuiUtils.getDefaultCallMessage(),100,100);
            }
            if(picture != null && picture.length() > 0)
            notification.setImage(picture,150,150);

            if(notificationDevice.getType().equals("missedCalls")){
                notification.setFirstButton("DismissAll",action -> {
                    notification.hide();
                    Platform.runLater(()->{
                        GuiCommunicator.dismissAllCalls(notificationDevice,id);
                    });
                });
            }
            else if(notificationDevice.getType().equalsIgnoreCase("unreadMessages")){
                notification.setFirstButton("DissmissAll",action -> {
                    notification.hide();
                    Platform.runLater(()->{
                        GuiCommunicator.dissMissAllMessages(notificationDevice,id, (MessagesPack) args[3]);
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
                notification.setFirstButton("Answer",action->{
                    notification.hide();
                    Platform.runLater(()->{
                        GuiCommunicator.answerCall(notificationDevice,id);
                    });
                });
                notification.setSecondButton("Reject",action->{
                    notification.hide();
                    Platform.runLater(()->{
                        GuiCommunicator.rejectCall(notificationDevice,id);
                    });
                });
            }else if(notificationDevice.getType().equalsIgnoreCase(callTypes.outgoing.name())){
                notification.setFirstButton("Reject",action->{
                    notification.hide();
                    Platform.runLater(()->{
                        GuiCommunicator.rejectOutgoingcall(notificationDevice,id);
                    });
                });
            }
            else if(missed){
                notification.setFirstButton("Recall",action->{
                    notification.hide();
                    Platform.runLater(()->{
                        GuiCommunicator.recall(notificationDevice,id);
                    });
                });
            }else if(notificationDevice.getType().equalsIgnoreCase("pair")){
                notification.setFirstButton("Yes",action->{
                    notification.hide();
                    Platform.runLater(()->{
                        GuiCommunicator.pairAnswer(id,true,notificationDevice.getTicker());
                    });
                });
                notification.setSecondButton("No",action->{
                    notification.hide();
                    Platform.runLater(()->{
                        GuiCommunicator.pairAnswer(id,false,notificationDevice.getTicker());
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


    public static void main(String args[]){

        NotificationFactory factory = new NotificationFactory(ThemePackagePresets.cleanLight());
        QueueManager manager = new QueueManager(NotificationFactory.Location.SOUTHEAST);
        factory.addBuilder(CustomNotification.class, new CustomNotification.CustomBuilder());
        // add the Notification
        manager.setScrollDirection(QueueManager.ScrollDirection.NORTH);

    CustomNotification build = factory.build(CustomNotification.class, new NotificationDevice("Dlinnoe   iz vjaskofo!!!","sms","dada","dada","dada","dada","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"), "555", null);
        manager.addNotification(build, Time.seconds(20));
    }
}