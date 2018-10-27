package com.push.gui.entity;

import com.notification.types.BorderLayoutNotification;
import com.push.gui.utils.GuiUtils;
import com.push.lazyir.service.managers.settings.SettingManager;
import com.theme.TextTheme;
import com.theme.WindowTheme;
import lombok.Data;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Set;

@Data
public class CustomNotification extends BorderLayoutNotification {
    private Font titleFont;
    private Font textFont;
    private GuiUtils guiUtils;
    private SettingManager settingManager;
    private JLabel iconlbl;
    private JTextArea textlbl;
    private JTextArea titlelbl;
    private JTextArea deviceLbl;
    private JLabel imglbl;

    private JPanel btnpanel;
    private JPanel textpanel;
    private JButton mbutton;
    private JButton mbutton2;
    private TextTheme mtheme;
    private JPanel rightpanel;

    public CustomNotification(GuiUtils guiUtils, SettingManager settingManager) {
        super();
        this.settingManager = settingManager;
        this.guiUtils = guiUtils;

        String titleFontSize = settingManager.get("NotificationFontSize");
        int size = 12;
        if(titleFontSize != null){
            size = Integer.parseInt(titleFontSize);
        }
        titleFont = new Font("Arial",Font.BOLD,size);
        textFont = new Font("Arial",Font.PLAIN,size);

        iconlbl = new JLabel();
        textlbl = new JTextArea();
        titlelbl = new JTextArea();
        deviceLbl = new JTextArea();
        BorderLayout borderLayout = new BorderLayout();
        borderLayout.setHgap(0);
        borderLayout.setVgap(10);
        textpanel = new JPanel(borderLayout);
        imglbl = new JLabel();
        mbutton = new JButton();
        mbutton2 = new JButton();
        GridLayout gl = new GridLayout(0,1);
        rightpanel = new JPanel();

        btnpanel = new JPanel(gl);
        rightpanel.add(imglbl);
        rightpanel.add(btnpanel);
        textlbl.setLineWrap(true);
        textlbl.setWrapStyleWord(true);
        titlelbl.setLineWrap(true);
        titlelbl.setWrapStyleWord(true);
        textpanel.add(titlelbl,BorderLayout.NORTH);
        textpanel.add(textlbl,BorderLayout.CENTER);
        textpanel.add(deviceLbl,BorderLayout.SOUTH);

        this.addComponent(iconlbl, BorderLayout.WEST);
        this.addComponent(textpanel,BorderLayout.CENTER);
        this.addComponent(rightpanel,BorderLayout.EAST);
    }

    public void setTextTemeFont(TextTheme theme) {
        titlelbl.setFont(titleFont);
        textlbl.setFont(textFont);
        mbutton.setFont(theme.subtitle);
        mbutton2.setFont(theme.subtitle);
        mtheme = theme;
    }

    public void setTextThemeColor(TextTheme theme){
        titlelbl.setForeground(theme.titleColor);
        textlbl.setForeground(theme.subtitleColor);
        mbutton.setForeground(theme.subtitleColor);
        mbutton2.setForeground(theme.subtitleColor);
    }

    public void setTextThemeCallFont(TextTheme theme){
        titlelbl.setFont(titleFont);
        textlbl.setFont(textFont);
        mbutton.setFont(theme.title);
        mbutton2.setFont(theme.title);
    }


    public void setTextThemeCallColor(){
        /**
         * Implement later
         */
    }

    public void setTextThemecallMissedFont(TextTheme theme) {
        titlelbl.setFont(titleFont);
        textlbl.setFont(textFont);
        mbutton.setFont(theme.title);
    }



    public void setImage(String base64,int width,int heigh){
        ImageIcon imageIcon = guiUtils.pictureFromBase64Swing(base64, width, heigh);
        if(imageIcon != null) {
            imglbl.setIcon(imageIcon);
        }
    }

    public void setIcon(String base64,int width,int heigh){
        ImageIcon imageIcon = guiUtils.pictureFromBase64Swing(base64, width, heigh);
        if(imageIcon != null) {
            iconlbl.setIcon(imageIcon);
        }
    }

    public void setIcon(Icon icon){
        if(icon != null) {
            iconlbl.setIcon(icon);
        }
    }

    public void setIfNull(Icon icon){
        if(iconlbl.getIcon() == null){
            iconlbl.setIcon(icon);
        }
    }

    public void setFirstButton(String text,ActionListener action){
        mbutton.setText(text);
        mbutton.addActionListener(action);
        btnpanel.add(mbutton,0);
    }

    public void setSecondButton(String text,ActionListener action){
        mbutton2.setText(text);
        mbutton2.addActionListener(action);
        btnpanel.add(mbutton2,1);
    }

    public String getText() {
        return textlbl.getText();
    }

    public void setText(String text,String title,String dvName) {
        if(title == null)
            title = "";
        if(text == null)
            text = "";
        if(dvName == null)
            dvName = "";
        titlelbl.setText(title);
        textlbl.setText(text);
        deviceLbl.setText(dvName);
    }

    @Override
    public void setWindowTheme(WindowTheme theme) {
        super.setWindowTheme(theme);

        if (mtheme != null) {
            // the WindowNotification is going to automatically give all our labels with the set foreground color, but
            // we want to change this to the title color of the font
            iconlbl.setForeground(mtheme.titleColor);
        }
    }

    public void setTextThemeCallMiseedColor(){
        mbutton.setForeground(Color.GREEN);
    }

}