package com.push.lazyir.modules.notifications.notifications;

import com.push.lazyir.Loggout;
import com.push.lazyir.devices.CacherOld;
import com.push.lazyir.devices.NetworkPackageOld;
import com.push.lazyir.gui.GuiCommunicator;
import com.push.lazyir.modules.Module;
import com.push.lazyir.modules.notifications.messengers.Messengers;
import com.push.lazyir.service.main.BackgroundService;

import javax.inject.Inject;
import java.util.ArrayList;

/**
 * Created by buhalo on 21.03.17.
 */
public class ShowNotification extends Module {
    public static final String RECEIVE_NOTIFICATION = "receiveNotification";
    public static final String NOTIFICATION_CLASS = "notificationClass";
    public static final String ALL_NOTIFS = "ALL NOTIFS";
    public static final String NOTIFICATION_ID = "NOTIFICATION_ID";
    public static final String SHOW_NOTIFICATION = "ShowNotification";
    public static final String REMOVE_NOTIFICATION = "removeNotification";
    private GuiCommunicator guiCommunicator;

    @Inject
    public ShowNotification(BackgroundService backgroundService, CacherOld cacher, GuiCommunicator guiCommunicator) {
        super(backgroundService, cacher);
        this.guiCommunicator = guiCommunicator;
    }

    @Override
    public void execute(NetworkPackageOld np) {
        String data = np.getData();
        Messengers messengers = backgroundService.getModuleById(device.getId(), Messengers.class);
        try {
                            if (RECEIVE_NOTIFICATION.equals(data)) {
                                Notification not = np.getObject(NetworkPackageOld.N_OBJECT, Notification.class);
                                if(messengers.isCalling(not,device,np,true)){

                                }else
                                guiCommunicator.show_notification(device.getId(),not);
                            }
                            else if(ALL_NOTIFS.equals(data))
                            {
                                Notifications notifications = np.getObject(NetworkPackageOld.N_OBJECT, Notifications.class);
                                if(notifications.getNotifications().size() > 0) {
                                    notifications.getNotifications().forEach(notification ->{
//                                        if(notification != null) {
//                                            if(notification.)
//                                            notification.setType("notification");
//                                        }
                                    });
                                    guiCommunicator.receive_notifications(device.getId(), notifications.getNotifications());
                                }
                                else{
                                    guiCommunicator.receive_notifications(device.getId(), new ArrayList<>());
                                }
                            }else if(REMOVE_NOTIFICATION.equalsIgnoreCase(data)){
                                Notification not = np.getObject(NetworkPackageOld.N_OBJECT, Notification.class);
                                if(messengers.isCalling(not,device,np,false)){

                                }
                            }
        } catch(NullPointerException e){
            Loggout.e("ShowNotification", "execute ",e);
        }
}

    @Override
    public void endWork() {

    }


    public void requestNotificationsFromDevice(String id) {
      backgroundService.submitNewTask(()->{  backgroundService.sendToDevice(id,  cacher.getOrCreatePackage(SHOW_NOTIFICATION,ALL_NOTIFS).getMessage());});
    }

    public void sendRemoveNotification(String ownerId, String notificationId) {
        NetworkPackageOld np = cacher.getOrCreatePackage(SHOW_NOTIFICATION, REMOVE_NOTIFICATION);
        np.setValue(NOTIFICATION_ID,notificationId);
        System.out.println(ownerId + "  " + np.getMessage());
      backgroundService.submitNewTask(()->{backgroundService.sendToDevice(ownerId,np.getMessage());});
    }
}
