package com.push.lazyir.modules.notifications.notifications;

import com.push.lazyir.api.MessageFactory;
import com.push.lazyir.api.NetworkPackage;
import com.push.lazyir.gui.GuiCommunicator;
import com.push.lazyir.modules.Module;
import com.push.lazyir.modules.notifications.messengers.Messengers;
import com.push.lazyir.service.main.BackgroundService;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

public class ShowNotification extends Module {
    public enum api{
        RECEIVE_NOTIFICATION,
        ALL_NOTIFS,
        NOTIFICATION_ID,
        SHOW_NOTIFICATION,
        REMOVE_NOTIFICATION
    }
    private GuiCommunicator guiCommunicator;

    @Inject
    public ShowNotification(BackgroundService backgroundService, MessageFactory messageFactory, GuiCommunicator guiCommunicator) {
        super(backgroundService, messageFactory);
        this.guiCommunicator = guiCommunicator;
    }

    @Override
    public void execute(NetworkPackage np) {
        ShowNotificationDto dto = (ShowNotificationDto) np.getData();
        api command = api.valueOf(dto.getCommand());
        switch (command){
            case RECEIVE_NOTIFICATION:
                receiveNotifitication(dto);
                break;
            case ALL_NOTIFS:
                allNotifications(dto);
                break;
            case REMOVE_NOTIFICATION:
                removeNotification(dto);
                break;
            default:
                break;
        }
    }

    private void removeNotification(ShowNotificationDto dto) {
        Messengers messengers = backgroundService.getModuleById(device.getId(), Messengers.class);
        messengers.isCalling(dto.getNotification(),false);
    }


    private void allNotifications(ShowNotificationDto dto) {
        List<Notification> notifications = dto.getNotifications();
        if(!notifications.isEmpty()) {
            guiCommunicator.receiveNotifications(device.getId(), notifications);
        } else {
            guiCommunicator.receiveNotifications(device.getId(), Collections.emptyList());
        }
    }

    private void receiveNotifitication(ShowNotificationDto dto) {
        Notification notification = dto.getNotification();
        Messengers messengers = backgroundService.getModuleById(device.getId(), Messengers.class);
        messengers.isCalling(notification,true);
        guiCommunicator.showNotification(device.getId(),notification);
    }

    @Override
    public void endWork() {
        //nothing to do
    }


    public void requestNotificationsFromDevice() {
        sendMsg(messageFactory.createMessage(this.getClass().getSimpleName(),true,new ShowNotificationDto(api.ALL_NOTIFS.name())));
    }

    public void sendRemoveNotification(String notificationId) {
        ShowNotificationDto dto = new ShowNotificationDto(api.REMOVE_NOTIFICATION.name(), new Notification(notificationId));
        sendMsg(messageFactory.createMessage(this.getClass().getSimpleName(),true,dto));
    }
}
