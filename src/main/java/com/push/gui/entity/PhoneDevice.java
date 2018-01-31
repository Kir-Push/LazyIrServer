package com.push.gui.entity;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class PhoneDevice {

    private final StringProperty id;
    private StringProperty name;
    private StringProperty type;
    private IntegerProperty battery;
    private BooleanProperty charging;
    private BooleanProperty paired;
    private BooleanProperty mounted;
    private LongProperty freeSpace; // todo
    private LongProperty totalSpace;
    private LongProperty totalSpaceExt;
    private LongProperty freeSpaceExt;
    private IntegerProperty cpuLoad;
    private LongProperty totalRam;
    private LongProperty freeRam;
    private BooleanProperty lowMemory;
    private DoubleProperty temp;
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
        this.notifications = new SimpleListProperty<>(FXCollections.observableArrayList());
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


    public long getFreeSpace() {
        return freeSpace.get();
    }

    public LongProperty freeSpaceProperty() {
        return freeSpace;
    }

    public void setFreeSpace(long freeSpace) {
        this.freeSpace.set(freeSpace);
    }

    public long getTotalSpace() {
        return totalSpace.get();
    }

    public LongProperty totalSpaceProperty() {
        return totalSpace;
    }

    public void setTotalSpace(long totalSpace) {
        this.totalSpace.set(totalSpace);
    }

    public int getCpuLoad() {
        return cpuLoad.get();
    }

    public IntegerProperty cpuLoadProperty() {
        return cpuLoad;
    }

    public void setCpuLoad(int cpuLoad) {
        this.cpuLoad.set(cpuLoad);
    }

    public long getTotalRam() {
        return totalRam.get();
    }

    public LongProperty totalRamProperty() {
        return totalRam;
    }

    public void setTotalRam(long totalRam) {
        this.totalRam.set(totalRam);
    }

    public long getFreeRam() {
        return freeRam.get();
    }

    public LongProperty freeRamProperty() {
        return freeRam;
    }

    public void setFreeRam(long freeRam) {
        this.freeRam.set(freeRam);
    }

    public double getTemp() {
        return temp.get();
    }

    public DoubleProperty tempProperty() {
        return temp;
    }

    public void setTemp(double temp) {
        this.temp.set(temp);
    }

    public boolean isLowMemory() {
        return lowMemory.get();
    }

    public BooleanProperty lowMemoryProperty() {
        return lowMemory;
    }

    public void setLowMemory(boolean lowMemory) {
        this.lowMemory.set(lowMemory);
    }

    public long getFreeSpaceExt() {
        return freeSpaceExt.get();
    }

    public LongProperty freeSpaceExtProperty() {
        return freeSpaceExt;
    }

    public void setFreeSpaceExt(long freeSpaceExt) {
        this.freeSpaceExt.set(freeSpaceExt);
    }

    public long getTotalSpaceExt() {
        return totalSpaceExt.get();
    }

    public LongProperty totalSpaceExtProperty() {
        return totalSpaceExt;
    }

    public void setTotalSpaceExt(long totalSpaceExt) {
        this.totalSpaceExt.set(totalSpaceExt);
    }
}
