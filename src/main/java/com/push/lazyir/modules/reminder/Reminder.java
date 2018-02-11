package com.push.lazyir.modules.reminder;

import com.push.gui.utils.GuiUtils;
import com.push.lazyir.devices.NetworkPackage;
import com.push.lazyir.gui.GuiCommunicator;
import com.push.lazyir.modules.Module;
import com.push.lazyir.modules.notifications.notifications.Notification;
import com.push.lazyir.modules.notifications.notifications.Notifications;
import com.push.lazyir.modules.notifications.sms.Sms;
import com.push.lazyir.modules.notifications.sms.SmsPack;
import com.push.lazyir.service.BackgroundService;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class Reminder extends Module {

    private final static String REMINDER_TYPE = "Reminder";
    private final static String MISSED_CALLS = "MissedCalls";
    private final static String UNREAD_MESSAGES = "UnreadMessages";
    private final static String DISSMIS_ALL_CALLS = "dismissAllCalls";
    private final static String DISSMIS_ALL_MESSAGES = "dismissAllMessages";

    @Override
    public void execute(NetworkPackage np) {
        String data = np.getData();
        switch (data){
            case MISSED_CALLS:
                missedCalls(np);
                break;
            case UNREAD_MESSAGES:
                unreadMessages(np);
                break;
            default:
                break;
        }
    }

    private void unreadMessages(NetworkPackage np) {
        MessagesPack object = np.getObject(NetworkPackage.N_OBJECT, MessagesPack.class);
        if(object != null){
            Notifications notifications = object.getNotifications();
            SmsPack smsPack = object.getSmsPack();
            Notification notification = createNotification(notifications,smsPack);
            GuiCommunicator.show_notification(device.getId(),notification,object);
        }
    }

    private Notification createNotification(Notifications notifications, SmsPack smsPack) {
        List<Notification> notificationsList = null;
        List<Sms> sms = null;
        if(notifications != null) {
            notificationsList = notifications.getNotifications();
        }
        if(smsPack != null) {
            sms = smsPack.getSms();
        }
        int size = (notificationsList != null ? notificationsList.size() : 0) + (sms != null ? sms.size() : 0);
        String text,title,pack,ticker,id,icon,type;
        pack = "reminder";
        type = "unreadMessages";
        ticker = device.getName();
        icon = null;
        title = "You have " + size + " unread message on " + ticker;
        id = "some Id";
        StringBuilder textBuilder = new StringBuilder();
        if(sms != null){
            textBuilder.append("SMS From ");
            HashMap<String, List<Sms>> stringListHashMap = collectSmsByNumber(sms);
            for (String s : stringListHashMap.keySet()) {
                List<Sms> messagesList = stringListHashMap.get(s);
                Sms msg = messagesList.get(0);
                String name = msg.getName();
                if(name != null && !name.equalsIgnoreCase("null"))
                textBuilder.append(name);
                long dat = 0;
                for (Sms sms1 : messagesList){
                    if(sms1.getDate() > dat)
                        dat = sms1.getDate();
                    if(icon == null)
                        icon = sms1.getIcon();
                }
                LocalDateTime date =
                        LocalDateTime.ofInstant(Instant.ofEpochMilli(dat), ZoneId.systemDefault());
                textBuilder.append(" (").append(msg.getNumber()).append(") ").append(messagesList.size())
                        .append(" messages, last: ").append(date.getYear()).append("/")
                        .append(date.getMonthValue()).append("/")
                        .append(date.getDayOfMonth()).append(" - (")
                        .append(date.getHour()).append(":")
                        .append(date.getMinute()).append(")\n");
            }
        }
        if(notificationsList != null){
            HashMap<String, List<Notification>> listHashMap = collectNotificationByMessenger(notificationsList);
            if(listHashMap.size() > 0) {
                textBuilder.append("Messengers messages: ");
                for (String s : listHashMap.keySet()) {
                    List<Notification> notificationsLst = listHashMap.get(s);
                    if (icon == null) {
                        for (Notification notification : notificationsLst) {
                            if (icon == null) {
                                icon = notification.getIcon();
                            } else {
                                break;
                            }
                        }
                    }
                    textBuilder.append(s).append(" ").append(notificationsLst.get(notificationsLst.size() - 1).getTitle()).append("\n");
                }
            }
        }
        text = textBuilder.toString();
        return new Notification(text,type,title,pack,ticker,id,icon,null);
    }

    private void missedCalls(NetworkPackage np) {
        MissedCalls object = np.getObject(NetworkPackage.N_OBJECT, MissedCalls.class);
        if(object != null){
            List<MissedCall> missedCalls = object.getMissedCalls();
            if(missedCalls != null){
            HashMap<String,List<MissedCall>> missedCallMap = collectByNumber(missedCalls);
            Notification missedNotification = createNotification(missedCallMap,missedCalls.size());
            GuiCommunicator.show_notification(device.getId(),missedNotification);
            }
        }

    }

    private Notification createNotification(HashMap<String, List<MissedCall>> missedCallMap, int size) {
        String text,title,pack,ticker,id,icon,type;
        pack = "reminder";
        type = "missedCalls";
        ticker = device.getName();
        icon = null;
        title = "You have " + size + " missed calls on " + ticker;
        StringBuilder db = new StringBuilder();
        StringBuilder textBuilder = new StringBuilder();
        for (List<MissedCall> missedCalls : missedCallMap.values()) {
            if(missedCalls.size() == 0)
                continue;
            long dat = 0;
            for (MissedCall missedCall : missedCalls) {
                db.append(missedCall.getId());
                db.append(":::");
                if(icon == null)
                    icon = missedCall.getPicture();
                if(missedCall.getDate() > dat)
                    dat = missedCall.getDate();
            }
            LocalDateTime date =
                    LocalDateTime.ofInstant(Instant.ofEpochMilli(dat), ZoneId.systemDefault());
            textBuilder.append("From ")
                    .append(missedCalls.get(0).getName()).append(" (")
                    .append(missedCalls.get(0).getNumber()).append(") ")
                    .append(missedCalls.size()).append(" calls, last: ")
                    .append(date.getYear()).append("/")
                    .append(date.getMonth()).append("/")
                    .append(date.getDayOfMonth()).append(" - (")
                    .append(date.getHour()).append(":")
                    .append(date.getMinute()).append(":").
                    append(date.getSecond()).append(")\n");

        }
        text = textBuilder.toString();
        db = db.delete(db.length()-3,db.length());
        id = db.toString();
        return new Notification(text,type,title,pack,ticker,id,icon,null);
    }

    private HashMap<String, List<MissedCall>> collectByNumber(List<MissedCall> missedCalls) {
        HashMap<String,List<MissedCall>> result = new HashMap<>();
        for (MissedCall missedCall : missedCalls) {
            List<MissedCall> missedCallsList = result.computeIfAbsent(missedCall.getNumber(), k -> new ArrayList<>());
            missedCallsList.add(missedCall);
        }
        return result;
    }

    private HashMap<String, List<Notification>> collectNotificationByMessenger(List<Notification> notificationsList) {
        HashMap<String, List<Notification>> result = new HashMap<>();
        for (Notification notification : notificationsList) {
            String[] split = notification.getPack().split(".");
            String pack;
            if(split.length == 0)
              pack = notification.getPack();
            else if(split.length == 1)
                pack = split[0]; // todo all this you need to test, it may not work in many messengers
            else
                pack = split[1];
            List<Notification> notifications = result.computeIfAbsent(pack, k -> new ArrayList<>());
            notifications.add(notification);
        }
        return result;
    }


    private HashMap<String, List<Sms>> collectSmsByNumber(List<Sms> missedMessage){
        HashMap<String,List<Sms>> result = new HashMap<>();
        for (Sms missedCall : missedMessage) {
            String number = missedCall.getNumber();
            List<Sms> missedMessages = result.computeIfAbsent(number == null ? missedCall.getName() : number, k -> new ArrayList<>());
            missedMessages.add(missedCall);
        }
        return result;
    }

    public static void sendDissmisAllCalls(String id,String msg){
        NetworkPackage orCreatePackage = NetworkPackage.Cacher.getOrCreatePackage(Reminder.class.getSimpleName(), DISSMIS_ALL_CALLS);
        orCreatePackage.setValue(MISSED_CALLS,msg);
        BackgroundService.sendToDevice(id,orCreatePackage.getMessage());
    }

    public static void sendDissmisAllMessages(String id, MessagesPack mspack) {
        Notifications notifications = mspack.getNotifications();
        SmsPack smsPack = mspack.getSmsPack();
        for (Notification notification : notifications.getNotifications()) { // this info don't need anymore
            notification.setIcon(null);
            notification.setPicture(null);
            notification.setText(null);
        }
        for (Sms sms : smsPack.getSms()) {
            sms.setIcon(null);
            sms.setPicture(null);
            sms.setText(null);
        }
        NetworkPackage orCreatePackage = NetworkPackage.Cacher.getOrCreatePackage(Reminder.class.getSimpleName(), DISSMIS_ALL_MESSAGES);
        orCreatePackage.setObject(NetworkPackage.N_OBJECT,mspack);
        BackgroundService.sendToDevice(id,orCreatePackage.getMessage());
    }
}
