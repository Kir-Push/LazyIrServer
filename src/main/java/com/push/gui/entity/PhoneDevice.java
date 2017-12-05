package com.push.gui.entity;

import javafx.beans.property.*;
import javafx.collections.ObservableList;

public class PhoneDevice {

    private final StringProperty id;
    private StringProperty name;
    private StringProperty type;
    private IntegerProperty battery;
    private BooleanProperty charging;
    private BooleanProperty paired;
    private BooleanProperty mounted;
    private ListProperty<String> enabledModules;
    private ListProperty<NotificationDevice> notifications;

    public PhoneDevice(String id, String name, String type, int battery, boolean charging, boolean paired, boolean mounted) {
        this.id = new SimpleStringProperty(id);
        this.name =  new SimpleStringProperty(name);
        this.type =  new SimpleStringProperty(type);
        this.battery = new SimpleIntegerProperty(battery);
        this.charging = new SimpleBooleanProperty(charging);
        this.paired =  new SimpleBooleanProperty(paired);
        this.mounted =  new SimpleBooleanProperty(mounted);
    }

    public String getId() {
        return id.get();
    }

    public StringProperty idProperty() {
        return id;
    }

    public void setId(String id) {
        this.id.set(id);
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getType() {
        return type.get();
    }

    public StringProperty typeProperty() {
        return type;
    }

    public void setType(String type) {
        this.type.set(type);
    }

    public int getBattery() {
        return battery.get();
    }

    public IntegerProperty batteryProperty() {
        return battery;
    }

    public void setBattery(int battery) {
        this.battery.set(battery);
    }

    public boolean isCharging() {
        return charging.get();
    }

    public BooleanProperty chargingProperty() {
        return charging;
    }

    public void setCharging(boolean charging) {
        this.charging.set(charging);
    }

    public boolean isPaired() {
        return paired.get();
    }

    public BooleanProperty pairedProperty() {
        return paired;
    }

    public void setPaired(boolean paired) {
        this.paired.set(paired);
    }

    public boolean isMounted() {
        return mounted.get();
    }

    public BooleanProperty mountedProperty() {
        return mounted;
    }

    public void setMounted(boolean mounted) {
        this.mounted.set(mounted);
    }

    public ObservableList<String> getEnabledModules() {
        return enabledModules.get();
    }

    public ListProperty<String> enabledModulesProperty() {
        return enabledModules;
    }

    public void setEnabledModules(ObservableList<String> enabledModules) {
        this.enabledModules.set(enabledModules);
    }

    public ObservableList<NotificationDevice> getNotifications() {
        return notifications.get();
    }

    public ListProperty<NotificationDevice> notificationsProperty() {
        return notifications;
    }

    public void setNotifications(ObservableList<NotificationDevice> notifications) {
        this.notifications.set(notifications);
    }


}
