package com.push.gui.basew;

import com.notification.NotificationFactory;
import com.notification.manager.QueueManager;
import com.notification.manager.SimpleManager;
import com.notification.manager.SlideManager;
import com.push.gui.controllers.MainController;
import com.push.gui.entity.CustomNotification;
import com.push.gui.entity.NotificationDevice;
import com.push.gui.utils.GuiUtils;
import com.push.lazyir.modules.notifications.Notification;
import com.push.lazyir.modules.notifications.callTypes;
import com.theme.ThemePackagePresets;
import com.utils.Time;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;
import org.controlsfx.control.action.Action;

import java.awt.*;
import java.util.HashMap;
import java.util.function.Consumer;

import static com.push.gui.utils.GuiUtils.createDummyStage;
import static java.awt.GraphicsDevice.TYPE_RASTER_SCREEN;


public class Popup {

    //todo handle multi monitor in Jcommunique , use screen from javafx
    private volatile static boolean initialized;
     private static NotificationFactory factory;
     private static QueueManager manager;
     private static SimpleManager callManager;
     private static HashMap<String,CustomNotification> callNotifs = new HashMap<>();
     private static int countScreens;


    public static void show(String id, NotificationDevice notification, MainController mainController) {


        int numberOfMonitors = Screen.getScreens().size();

        if(numberOfMonitors != countScreens){
            countScreens = numberOfMonitors;
            initialized = false;
        }

        if(!initialized){
            // register the custom builder with the factory
            factory = new NotificationFactory(ThemePackagePresets.cleanLight());
            factory.addBuilder(CustomNotification.class, new CustomNotification.CustomBuilder());
            if(manager != null)
                manager.stop();
            manager = new QueueManager(NotificationFactory.Location.SOUTHEAST);
            callManager = new SimpleManager(NotificationFactory.Location.SOUTHEAST);
            // add the Notification
            manager.setScrollDirection(QueueManager.ScrollDirection.NORTH);
            initialized = true;
        }
        CustomNotification build = factory.build(CustomNotification.class, notification, id, mainController);
        boolean incoming = notification.getType().equalsIgnoreCase(callTypes.incoming.name());
        if(incoming || notification.getType().equalsIgnoreCase(callTypes.missedIn.name())){
            if(!callNotifs.containsKey(notification.getTitle()))
            callNotifs.put(notification.getTitle(),build);
        }
        if(incoming){
            callManager.addNotification(build,Time.seconds(10));
        }
        else {
            manager.addNotification(build, Time.seconds(20));
        }

        GraphicsDevice defaultScreenDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
    }

    public static void callEnd(String id, String callerNumber) {
        if(callNotifs.containsKey(callerNumber)){
            callNotifs.get(callerNumber).hide();
            callNotifs.remove(callerNumber);
        }
    }
}
