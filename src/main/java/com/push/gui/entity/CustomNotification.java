package com.push.gui.entity;

import com.notification.NotificationBuilder;
import com.notification.NotificationFactory;
import com.notification.NotificationManager;
import com.notification.manager.QueueManager;
import com.notification.manager.SimpleManager;
import com.notification.manager.SlideManager;
import com.notification.types.BorderLayoutNotification;
import com.push.gui.utils.GuiUtils;
import com.theme.TextTheme;
import com.theme.ThemePackage;
import com.theme.ThemePackagePresets;
import com.theme.WindowTheme;
import com.utils.Time;
import javafx.scene.image.Image;

import javax.swing.*;
import javax.swing.plaf.BorderUIResource;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CustomNotification extends BorderLayoutNotification {
    private JLabel icon_lbl;
    private JLabel text_lbl;
    private JLabel img_lbl;

    private  JPanel jpnlMain;
    private JPanel right_panel;
    private JButton m_button;
    private JButton m_button2;
    private TextTheme m_theme;

    public CustomNotification() {
        icon_lbl = new JLabel();
        text_lbl = new JLabel();
        img_lbl = new JLabel();
        right_panel = new JPanel();
        m_button = new JButton();
        m_button2 = new JButton();
        GridLayout cl = new GridLayout(2,1);

        jpnlMain = new JPanel(cl);
        //jpnlMain.add(m_button);
      //  jpnlMain.add(m_button2);
        right_panel.add(img_lbl);
        right_panel.add(jpnlMain);
        this.addComponent(icon_lbl, BorderLayout.WEST);
        this.addComponent(text_lbl,BorderLayout.CENTER);
        this.addComponent(right_panel, BorderLayout.EAST);
    }

    public void setTextTeme(TextTheme theme) {
        text_lbl.setFont(theme.title);
        text_lbl.setForeground(theme.titleColor);
        m_button.setFont(theme.subtitle);
        m_button.setForeground(theme.subtitleColor);

        m_theme = theme;
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
        jpnlMain.add(m_button);
    }

    public void setSecondButton(String text,ActionListener action){
        m_button2.setText(text);
        m_button2.addActionListener(action);
        jpnlMain.add(m_button2);
    }

    public String getText() {
        return text_lbl.getText();
    }

    public void setText(String text) {
        text = "<html>" + text.replaceAll("<","&lt;").replaceAll(">", "&gt;").replaceAll("\n", "<br/>") + "</html>";
        text_lbl.setText(text);
    }

    @Override
    public void setWindowTheme(WindowTheme theme) {
        super.setWindowTheme(theme);

        if (m_theme != null) {
            // the WindowNotification is going to automatically give all our labels with the set foreground color, but
            // we want to change this to the title color of the font
            icon_lbl.setForeground(m_theme.titleColor);
            m_button.setForeground(m_theme.subtitleColor);
        }
    }

    public static class CustomBuilder implements NotificationBuilder<CustomNotification> {
        @Override
        public CustomNotification buildNotification(ThemePackage pack, Object[] args) {
            CustomNotification notification = new CustomNotification();

            // handled by us
            notification.setTextTeme(pack.getTheme(TextTheme.class));
            if (args.length > 0) {
                notification.setText((String) args[0]);
            } else {
                notification.setText("No text supplied");
            }
            notification.setIcon("iVBORw0KGgoAAAANSUhEUgAAAGAAAABgCAYAAADimHc4AAAABHNCSVQICAgIfAhkiAAAAAlwSFlzAAAbrwAAG68BXhqRHAAAABl0RVh0U29mdHdhcmUAd3d3Lmlua3NjYXBlLm9yZ5vuPBoAAAIFSURBVHic7d29ahRRGIfx98zOKimWhSCIGjV+pBCLiDZ2Vl6B2Aq5AZuAd+EHovdgIV6BVrYqgojNBgSFIIqFYCHZ7LGwTrXzzrNrnt8FzP8wDzuz3URIkiRJOmwKfYCDvPu0c6uUej8izs55qc+llu0rly686OJcXWvoAxykNPVpzH/zIyLWa6lPOrhOioUNEDWOd3i1Ex1eq1OLG+CQ6OUd8Objzpl2MHtQo9yIiGN9bM7hR9R4PZvFvWuXL06yx9IDfJhMTk+n9X1EWc3e6lb92Qxic3Nj42vmSvojaG8/Hi3fzY+IKKuz/eZh9kp6gFLjZvZGnpp+9j5ewqMeNrKMswf8FwQzAMwAMAPADAAzAMwAMAPADAAzAMwAMAPADAAzAMwAMAPADAAzAMwAMAPADAAzAMwAMAPADAAzAMwAMAPADABrswdefr+bPbHU/AXADAAzAMwAMAPADAAzAMwAMAPADAAzAMwAMAPADAAzAMwAMAPADAAzAMwAMAPADAAzAMwAMAPADAAzAMwAMAPADAAzAMwAMAPADAAzAMwAMAPA0gMMmqM1eyNLW/LPnh5gNDj1LXsjy6hd283eSA8wHp7fGpaVpfsVDMtKHbfntrJ3evmU4bO3j6//nk6e/9r7cnJa/yzsJ3Qj/j12Ru3a7vjI+p3bV7df0eeRJEmS9P/5C7rXRbGg2grIAAAAAElFTkSuQmCC",100,100);
            // handled by WindowNotification
            WindowTheme theme = pack.getTheme(WindowTheme.class);
            theme.opacity = 1;
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            double width = screenSize.getWidth();
            double height = screenSize.getHeight();
            int calculatedWidth = notification.text_lbl.getPreferredSize().width + notification.right_panel.getPreferredSize().width + notification.icon_lbl.getPreferredSize().width + 20;
            int calculatedHeight = notification.text_lbl.getPreferredSize().height + notification.right_panel.getPreferredSize().height + notification.icon_lbl.getPreferredSize().height + 20;
            if(calculatedWidth >= width/5)
                calculatedWidth = (int) (width/5);
            if(calculatedHeight >= height/7)
                calculatedHeight = (int) (height/7);

            System.out.println(width);
            System.out.println(calculatedWidth);
            System.out.println(height);
            System.out.println(calculatedHeight);
            theme.width = calculatedWidth;
            theme.height = calculatedHeight;
            notification.setWindowTheme(theme);
            return notification;
        }
    }

    public static void main(String args[]){
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        // register the custom builder with the factory
        NotificationFactory factory = new NotificationFactory(ThemePackagePresets.cleanLight());
        factory.addBuilder(CustomNotification.class, new CustomNotification.CustomBuilder());

// add the Notification
        QueueManager manager = new QueueManager(NotificationFactory.Location.SOUTHEAST);
        manager.setScrollDirection(QueueManager.ScrollDirection.NORTH);
        manager.addNotification(factory.build(CustomNotification.class, "this is test text fdfds fdsf dsf df "), Time.seconds(10));
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        manager.addNotification(factory.build(CustomNotification.class, "AAAAAAAAAAAAAA "), Time.seconds(5));
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        manager.addNotification(factory.build(CustomNotification.class, "BBBBBBBBB "), Time.seconds(5));

    }
}