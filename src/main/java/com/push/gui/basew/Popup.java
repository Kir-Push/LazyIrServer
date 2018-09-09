package com.push.gui.basew;

import com.notification.Notification;
import com.notification.NotificationFactory;
import com.notification.manager.QueueManager;
import com.push.gui.controllers.MainController;
import com.push.gui.entity.CustomNotification;
import com.push.gui.entity.NotificationDevice;
import com.push.gui.utils.CustomBuilder;
import com.push.gui.utils.GuiUtils;
import com.push.lazyir.modules.notifications.NotificationTypes;
import com.push.lazyir.service.main.BackgroundService;
import com.push.lazyir.service.managers.settings.SettingManager;
import com.theme.ThemePackagePresets;
import com.utils.Time;
import javafx.stage.Screen;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;


public class Popup {

     private boolean initialized;
     private NotificationFactory factory;
     private QueueManager manager;
     private SettingManager settingManager;
     private GuiUtils guiUtils;
     private BackgroundService backgroundService;
     private double notTime;
     private double callNotTime;
     private int maxNotifOnScreen;

     private HashMap<String,CustomNotification> callNotifs = new HashMap<>();
     private int countScreens;

     @Inject
    public Popup(SettingManager settingManager,BackgroundService backgroundService,GuiUtils guiUtils) {
        this.settingManager = settingManager;
        this.backgroundService = backgroundService;
        this.guiUtils = guiUtils;
    }

    public void show(String id, NotificationDevice notification, MainController mainController, Object... arg) {
        int numberOfMonitors = Screen.getScreens().size();
        if(numberOfMonitors != countScreens){
            countScreens = numberOfMonitors;
            initialized = false;
        }
        if(!initialized){
            // register the custom builder with the factory
            factory = new NotificationFactory(ThemePackagePresets.cleanLight());
            factory.addBuilder(CustomNotification.class, new CustomBuilder(backgroundService,guiUtils));
            if(manager != null)
                manager.stop();
            manager = new QueueManager(NotificationFactory.Location.SOUTHEAST);
            manager.setScrollDirection(QueueManager.ScrollDirection.NORTH);
            initialized = true;
            notTime = Double.parseDouble(settingManager.get("Notif-time"));
            callNotTime = Double.parseDouble(settingManager.get("Call-Notif-time"));
            maxNotifOnScreen = Integer.parseInt(settingManager.get("maxNotifOnScreen"));
        }
        // if you have many than maxNotifOnScreen notif on screen remove all oldest
        List<Notification> notifications = manager.getNotifications();
        if(notifications.size() >= maxNotifOnScreen){
            for(int i = (maxNotifOnScreen-1);i<notifications.size();i++) {
                notifications.get(i).removeFromManager();
            }
        }
        CustomNotification build = factory.build(CustomNotification.class, notification, id, mainController,(arg == null || arg.length == 0) ? null : arg[0]);
        String type = notification.getType();
        boolean incoming = false;
        if(type != null) {
            incoming = type.equalsIgnoreCase(NotificationTypes.INCOMING.name());
            boolean missed = type.equalsIgnoreCase(NotificationTypes.MISSED_IN.name());
            boolean outgoing = type.equalsIgnoreCase(NotificationTypes.OUTGOING.name());
            if (incoming || missed || outgoing) {
                callNotifs.put(notification.getTitle(), build);
            }
        }
        double timeTemp = notTime;
        for(int i = 20;i<build.getText().length();i+=20){
            timeTemp += 2;
        }
        Time time = Time.seconds(timeTemp);

        if(incoming){
           time = Time.seconds(callNotTime);
        }
        manager.addNotification(build, time);

    }

    public void callEnd(String callerNumber) {
        if(callNotifs.containsKey(callerNumber)){
            CustomNotification customNotification = callNotifs.get(callerNumber);
            if(customNotification != null) {
                customNotification.removeFromManager();
                callNotifs.remove(callerNumber);
            }
        }
    }
}
