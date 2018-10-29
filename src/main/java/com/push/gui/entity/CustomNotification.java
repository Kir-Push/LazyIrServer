package com.push.gui.entity;

import com.notification.types.BorderLayoutNotification;
import com.push.gui.utils.GuiUtils;
import com.push.lazyir.service.managers.settings.SettingManager;
import com.theme.TextTheme;
import com.theme.WindowTheme;
import lombok.Data;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;

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
        mbutton2.setBorder(null);
        mbutton.setBorder(null);
        rightpanel = new JPanel(new BorderLayout(5,0));
        btnpanel = new JPanel(new BorderLayout(0,10));
        JPanel jPanel = new JPanel(new BorderLayout());
        jPanel.add(btnpanel,BorderLayout.SOUTH);
        rightpanel.add(imglbl,BorderLayout.WEST);
        rightpanel.add(jPanel,BorderLayout.EAST);
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
        mbutton.setFont(new Font("Arial",Font.ITALIC,12));
        mbutton.setMargin(new Insets(0,0,0,0));
        mbutton2.setMargin(new Insets(0,0,0,0));
        mbutton2.setFont(new Font("Arial",Font.ITALIC,12));
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
        mbutton.setToolTipText("aga");
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

    public void setTextThemeCallMiseedColor(){
        mbutton.setForeground(Color.GREEN);
    }

}