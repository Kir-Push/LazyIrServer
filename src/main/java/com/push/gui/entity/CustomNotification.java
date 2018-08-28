package com.push.gui.entity;

import com.notification.types.BorderLayoutNotification;
import com.push.gui.utils.GuiUtils;
import com.theme.TextTheme;
import com.theme.WindowTheme;
import lombok.Data;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

@Data
public class CustomNotification extends BorderLayoutNotification {
    private GuiUtils guiUtils;
    private JLabel iconlbl;
    private JLabel textlbl;
    private JLabel imglbl;

    private JPanel btnpanel;
    private JButton mbutton;
    private JButton mbutton2;
    private TextTheme mtheme;
    private JPanel rightpanel;

    public CustomNotification(GuiUtils guiUtils) {
        super();
        iconlbl = new JLabel();
        textlbl = new JLabel();
        imglbl = new JLabel();
        mbutton = new JButton();
        mbutton2 = new JButton();
        GridLayout gl = new GridLayout(0,1);
        rightpanel = new JPanel();

        btnpanel = new JPanel(gl);
        rightpanel.add(imglbl);
        rightpanel.add(btnpanel);
        this.addComponent(iconlbl, BorderLayout.WEST);
        this.addComponent(textlbl,BorderLayout.CENTER);
        this.addComponent(rightpanel,BorderLayout.EAST);
        this.guiUtils = guiUtils;
    }

    public void setTextTemeFont(TextTheme theme) {
        textlbl.setFont(theme.subtitle);
        mbutton.setFont(theme.subtitle);
        mbutton2.setFont(theme.subtitle);
        mtheme = theme;
    }

    public void setTextThemeColor(TextTheme theme){
        textlbl.setForeground(theme.titleColor);
        textlbl.setBackground(theme.subtitleColor);
        mbutton.setForeground(theme.subtitleColor);
        mbutton2.setForeground(theme.subtitleColor);
    }

    public void setTextThemeCallFont(TextTheme theme){
        textlbl.setFont(theme.subtitle);
        textlbl.setForeground(theme.titleColor);
        textlbl.setBackground(theme.subtitleColor);
        mbutton.setFont(theme.title);
        mbutton2.setFont(theme.title);
    }


    public void setTextThemeCallColor(){
        /**
         * Implement later
         */
    }

    public void setTextThemecallMissedFont(TextTheme theme) {
        textlbl.setFont(theme.subtitle);
        textlbl.setForeground(theme.titleColor);
        textlbl.setBackground(theme.subtitleColor);
        mbutton.setFont(theme.title);
    }



    public void setImage(String base64,int width,int heigh){
        imglbl.setIcon(guiUtils.pictureFromBase64Swing(base64,width,heigh));
    }

    public void setIcon(String base64,int width,int heigh){
        iconlbl.setIcon(guiUtils.pictureFromBase64Swing(base64,width,heigh));
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
        title = "<b>" + title + "</b><br/>";
        dvName = "<br/><font size=\"2\">" + dvName +  "</font>";
        text = "<html>" + title +  text.replaceAll("<","&lt;").replaceAll(">", "&gt;").replaceAll("\n", "<br/>") + dvName + "</html>";
        textlbl.setText(text);
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