package com.push.lazyir.modules.reminder;

import com.google.common.collect.ArrayListMultimap;
import com.push.lazyir.api.MessageFactory;
import com.push.lazyir.api.NetworkPackage;
import com.push.lazyir.gui.GuiCommunicator;
import com.push.lazyir.modules.Module;
import com.push.lazyir.modules.notifications.notifications.Notification;
import com.push.lazyir.modules.notifications.sms.Sms;
import com.push.lazyir.service.main.BackgroundService;

import javax.inject.Inject;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

public class Reminder extends Module {
    public enum api{
        MISSED_CALLS,
        UNREAD_MESSAGES,
        DISSMIS_ALL_CALLS,
        DISSMIS_ALL_MESSAGES
    }
    private GuiCommunicator guiCommunicator;

    @Inject
    public Reminder(BackgroundService backgroundService, MessageFactory messageFactory, GuiCommunicator guiCommunicator) {
        super(backgroundService, messageFactory);
        this.guiCommunicator = guiCommunicator;
    }

    @Override
    public void execute(NetworkPackage np) {
        ReminderDto dto = (ReminderDto) np.getData();
        api command = Reminder.api.valueOf(dto.getCommand());
        switch (command){
            case MISSED_CALLS:
                missedCalls(dto);
                break;
            case UNREAD_MESSAGES:
                unreadMessages(dto);
                break;
            default:
                break;
        }
    }

    @Override
    public void endWork() {
        // nothing to do
    }

    private void unreadMessages(ReminderDto dto) {
        Notification notification = createNotification( dto.getNotifications(),dto.getSmsList());
        guiCommunicator.showNotification(device.getId(),notification,dto);
    }

    private Notification createNotification(List<Notification> notifications, List<Sms> smsPack) {
        int size = (notifications != null ? notifications.size() : 0) + (smsPack != null ? smsPack.size() : 0);
        String pack = "reminder";
        String ticker = device.getName();
        String title = "You have " + size + " unread messages on " + ticker;
        String id = "some Id";
        String type = "UNREAD_MESSAGES";
        StringBuilder textBuilder = new StringBuilder();
        if(smsPack != null){
           appendSms(textBuilder,smsPack);
        }
        if(notifications != null){
            appendNotifications(textBuilder,notifications);
        }
        String text = textBuilder.toString();
        return new Notification(text,title,pack,ticker,id,null,null,type);
    }

    private void appendNotifications(StringBuilder textBuilder, List<Notification> notifications) {
        ArrayListMultimap<String, Notification> multimap = collectNotificationByMessenger(notifications);
        if(multimap.isEmpty()){
            return;
        }
        textBuilder.append("Messengers messages: ");
        multimap.keySet().forEach(key -> {
            Notification notification = multimap.get(key).get(0);
            textBuilder.append(key).append(" ").append(notification.getTitle()).append("\n");
        });
    }

    private void appendSms(StringBuilder textBuilder,List<Sms> smsPack) {
        textBuilder.append("SMS From ");
        ArrayListMultimap<String, Sms> smsMultimap = collectSmsByNumber(smsPack);
        for (String key : smsMultimap.keySet()) {
            List<Sms> messages = smsMultimap.get(key);
            Sms sms = messages.get(0);
            String name = sms.getName();
            if(name != null && !name.equalsIgnoreCase("null")) {
                textBuilder.append(name);
            }
            Optional<Sms> max = messages.stream().max(Comparator.comparing(Sms::getDate));
            if(max.isPresent()) {
                Sms message = max.get();
                LocalDateTime date = LocalDateTime.ofInstant(Instant.ofEpochMilli(message.getDate()), ZoneId.systemDefault());
                textBuilder.append(" (").append(message.getNumber()).append(") ").append(messages.size())
                        .append(" messages, last: ").append(date.getYear()).append("/")
                        .append(date.getMonthValue()).append("/")
                        .append(date.getDayOfMonth()).append(" - (")
                        .append(date.getHour()).append(":")
                        .append(date.getMinute()).append(")\n");
            }
        }
    }

    private void missedCalls(ReminderDto dto) {
        List<MissedCall> missedCalls = dto.getMissedCalls();
        if(missedCalls != null && !missedCalls.isEmpty()){
            ArrayListMultimap<String, MissedCall> multimap = collectByNumber(missedCalls);
            Notification missedNotification = createNotification(multimap);
            guiCommunicator.showNotification(device.getId(),missedNotification);
        }
    }

    private Notification createNotification( ArrayListMultimap<String, MissedCall> missedCallMap) {
        String pack = "reminder";
        String ticker = device.getName();
        String title = "You have " + missedCallMap.values().size() + " missed calls on " + ticker;
        String type = "MISSED_CALLS";
        StringBuilder db = new StringBuilder();
        StringBuilder textBuilder = new StringBuilder();
        for (String key : missedCallMap.keySet()) {
            List<MissedCall> missedCalls = missedCallMap.get(key);
            long dat = 0;
            for (MissedCall missedCall : missedCalls) {
                db.append(missedCall.getId());
                db.append(":::");
                if(missedCall.getDate() > dat) {
                    dat = missedCall.getDate();
                }
            }
            LocalDateTime date = LocalDateTime.ofInstant(Instant.ofEpochMilli(dat), ZoneId.systemDefault());
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
        String text = textBuilder.toString();
        int length = db.length();
        String id = db.delete(length -3, length).toString();
        return new Notification(text,title,pack,ticker,id,null,null,type);
    }

    private ArrayListMultimap<String, MissedCall> collectByNumber(List<MissedCall> missedCalls) {
        ArrayListMultimap<String,MissedCall> result = ArrayListMultimap.create();
        missedCalls.forEach(missedCall -> result.put(missedCall.getNumber(),missedCall));
        return result;
    }

    private ArrayListMultimap<String, Notification> collectNotificationByMessenger(List<Notification> notificationsList) {
        ArrayListMultimap<String,Notification> result = ArrayListMultimap.create();
        notificationsList.forEach(notification -> {
            String pack = notification.getPack();
            String[] split = pack.split(".");
           if(split.length == 1) {
                pack = split[0]; // todo all this you need to test, it may not work in many messengers
            }else if(split.length > 1) {
                pack = split[1];
            }
            result.put(pack,notification);
        });
        return result;
    }


    private ArrayListMultimap<String, Sms> collectSmsByNumber(List<Sms> messagePack){
        ArrayListMultimap<String,Sms> result = ArrayListMultimap.create();
        messagePack.forEach(sms -> {
            String number = sms.getNumber();
            result.put(number == null ? sms.getName() : number,sms);
        });
        return result;
    }

    public void sendDissmisAllCalls(String msg){
        sendMsg(messageFactory.createMessage(this.getClass().getSimpleName(), true, new ReminderDto(api.DISSMIS_ALL_CALLS.name(),msg)));
    }

    public void sendDissmisAllMessages(ReminderDto mspack) {
        List<Notification> notifications = mspack.getNotifications();
        List<Sms> smsList = mspack.getSmsList();
        if(notifications != null) {
            notifications.forEach(notification -> {
                notification.setIcon(null);
                notification.setPicture(null);
                notification.setText(null);
            });
        }
        if(smsList != null) {
            smsList.forEach(sms -> {
                sms.setIcon(null);
                sms.setPicture(null);
                sms.setText(null);
            });
        }
        mspack.setMissedCalls(null);
        mspack.setMissedCallStr(null);
        mspack.setCommand(api.DISSMIS_ALL_MESSAGES.name());
        sendMsg(messageFactory.createMessage(this.getClass().getSimpleName(),true,mspack));
    }
}
