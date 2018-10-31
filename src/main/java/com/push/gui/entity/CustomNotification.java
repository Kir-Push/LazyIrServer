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

@Data
public class CustomNotification extends BorderLayoutNotification {
    private Font titleFont;
    private Font textFont;
    private Font buttonFont;
    private GuiUtils guiUtils;
    private SettingManager settingManager;
    private JLabel iconlbl = new JLabel();;
    private JTextArea textlbl = new JTextArea();
    private JTextArea titlelbl = new JTextArea();
    private JTextArea deviceLbl = new JTextArea();
    private JLabel imglbl = new JLabel();
    private Insets zeroInset = new Insets(0,0,0,0);

    private JPanel btnpanel = new JPanel(new BorderLayout(0,10));;
    private JPanel textpanel = new JPanel( new BorderLayout());;
    private JButton mbutton = new JButton();
    private JButton mbutton2 = new JButton();
    private TextTheme mtheme;
    private JPanel rightpanel = new JPanel(new BorderLayout(5,0));

    public CustomNotification(GuiUtils guiUtils, SettingManager settingManager) {
        super();
        this.settingManager = settingManager;
        this.guiUtils = guiUtils;
        int titleFontSize = settingManager.getInt("NotificationFontSize",12);
        titleFont = new Font("Poor Story Regular",Font.PLAIN,titleFontSize + 4);
        textFont = new Font("Roboto",Font.PLAIN,titleFontSize);
        buttonFont = new Font("Poor Story Regular",Font.PLAIN,titleFontSize);
        mbutton2.setBorder(null);
        mbutton.setBorder(null);
        JPanel panelPad = new JPanel(new BorderLayout());
        panelPad.add(btnpanel,BorderLayout.SOUTH);
        rightpanel.add(imglbl,BorderLayout.WEST);
        rightpanel.add(panelPad,BorderLayout.EAST);
        textlbl.setLineWrap(true);
        textlbl.setWrapStyleWord(true);
        titlelbl.setLineWrap(true);
        titlelbl.setWrapStyleWord(true);
        textlbl.setEditable(false);
        titlelbl.setEditable(false);
        deviceLbl.setEditable(false);
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
        mbutton.setFont(buttonFont);
        mbutton.setMargin(zeroInset);
        mbutton2.setMargin(zeroInset);
        mbutton2.setFont(buttonFont);
        mtheme = theme;
    }

    public void setTextThemeColor(TextTheme theme){
        titlelbl.setForeground(theme.titleColor);
        textlbl.setForeground(theme.subtitleColor);
        mbutton.setForeground(theme.subtitleColor);
        mbutton2.setForeground(theme.subtitleColor);
    }

    public void setTextThemeCallFont(TextTheme theme){
        setTextTemeFont(theme);
    }


    public void setTextThemeCallColor(TextTheme theme){
        setTextThemeColor(theme);
    }

    public void setTextThemecallMissedFont(TextTheme theme) {
        setTextTemeFont(theme);
    }

    public void setTextThemeCallMiseedColor(TextTheme theme){
        setTextThemeColor(theme);
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
        btnpanel.add(mbutton,BorderLayout.CENTER);
    }

    public void setSecondButton(String text,ActionListener action){
        mbutton2.setText(text);
        mbutton2.addActionListener(action);
        btnpanel.add(mbutton2,BorderLayout.SOUTH);
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

}