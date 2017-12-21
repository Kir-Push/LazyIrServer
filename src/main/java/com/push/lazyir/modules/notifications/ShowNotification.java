package com.push.lazyir.modules.notifications;

import com.push.lazyir.Loggout;
import com.push.lazyir.devices.Device;
import com.push.lazyir.devices.NetworkPackage;
import com.push.lazyir.gui.Communicator;
import com.push.lazyir.gui.GuiCommunicator;
import com.push.lazyir.modules.Module;
import com.push.lazyir.modules.dbus.Mpris;
import com.push.lazyir.service.BackgroundService;

import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by buhalo on 21.03.17.
 */
public class ShowNotification extends Module {
    public static final String SHOW_NOTIFICATION = "ShowNotification";
    public static final String RECEIVE_NOTIFICATION = "receiveNotification";
    public static final String NOTIFICATION_CLASS = "notificationClass";
    public static final String REMOVE_NOTIFICATION = "removeNotification";
    public static final String CALL = "com.android.call";
    public static final String ENDCALL = "com.android.endCall";
    public static final String ALL_NOTIFS = "ALL NOTIFS";
    public static final String NOTIFICATION_ID = "NOTIFICATION_ID";
    private static volatile boolean CALLING = false;

    private Lock lock =new ReentrantLock();

    @Override
    public void execute(NetworkPackage np) {
        String data = np.getData();
        try {
                            if (RECEIVE_NOTIFICATION.equals(data)) {
                                Notification not = np.getObject(NetworkPackage.N_OBJECT, Notification.class);
                                GuiCommunicator.show_notification(device.getId(),not);
                            }
                            else if(ALL_NOTIFS.equals(data))
                            {
                                Notifications notifications = np.getObject(NetworkPackage.N_OBJECT, Notifications.class);
                                if(notifications.getNotifications().size() > 0) {
                                    notifications.getNotifications().forEach(notification -> notification.setType("notification"));
                                    GuiCommunicator.receive_notifications(device.getId(), notifications.getNotifications());
                                }
                                else{
                                    GuiCommunicator.receive_notifications(device.getId(), new ArrayList<>());
                                }
                            }
                            else if(CALL.equals(data))
                            {
                                if(!CALLING) {
                                    Mpris mpris = (Mpris) device.getEnabledModules().get(Mpris.class.getSimpleName());
                                    if(mpris != null)
                                    {
                                        mpris.pauseAll(np.getId());
                                    }
                                    CALLING = true;
                                }
                                GuiCommunicator.call_Notif(np);
                            }
                            else if(CALLING && ENDCALL.equals(data))
                            {
                                CALLING = false;
                                Mpris mpris = (Mpris) device.getEnabledModules().get(Mpris.class.getSimpleName());
                                if(mpris != null)
                                {
                                    mpris.playAll(np.getId());
                                }
                            }

                    } catch(NullPointerException e){
                            Loggout.e("ShowNotification", "execute",e);
                        }
}

    @Override
    public void endWork() {
        if( Device.getConnectedDevices().size() == 0)
        {
            CALLING = false;
        }
    }


//    public void sendNotifsToOut(Notifications allNotifications)
//    {
//        NetworkPackage toOut =  NetworkPackage.Cacher.getOrCreatePackage(SHOW_NOTIFICATION, "NOTIF TO ID");
//        toOut.setObject(NetworkPackage.N_OBJECT, allNotifications);
//        Communicator.getInstance().sendToOut(toOut.getMessage());
//    }


    public static void requestNotificationsFromDevice(String id) {
      BackgroundService.submitNewTask(()->{  BackgroundService.sendToDevice(id,  NetworkPackage.Cacher.getOrCreatePackage(SHOW_NOTIFICATION,ALL_NOTIFS).getMessage());});
    }

    public static void sendRemoveNotification(String ownerId, String notificationId) {
        NetworkPackage np = NetworkPackage.Cacher.getOrCreatePackage(SHOW_NOTIFICATION, REMOVE_NOTIFICATION);
        np.setValue(NOTIFICATION_ID,notificationId);
      BackgroundService.submitNewTask(()->{BackgroundService.sendToDevice(ownerId,np.getMessage())});
    }
}
