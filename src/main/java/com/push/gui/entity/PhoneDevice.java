package com.push.gui.entity;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Data;

@Data
public class PhoneDevice {

    private final StringProperty id;
    private StringProperty name;
    private StringProperty type;
    private IntegerProperty battery;
    private BooleanProperty charging;
    private BooleanProperty paired;
    private BooleanProperty mounted;
    private LongProperty freeSpace;
    private LongProperty totalSpace;
    private LongProperty totalSpaceExt;
    private LongProperty freeSpaceExt;
    private IntegerProperty cpuLoad;
    private LongProperty totalRam;
    private LongProperty freeRam;
    private BooleanProperty lowMemory;
    private DoubleProperty temp;
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
        this.freeSpace = new SimpleLongProperty();
        this.totalSpace = new SimpleLongProperty();
        this.totalSpaceExt = new SimpleLongProperty();
        this.freeSpaceExt = new SimpleLongProperty();
        this.cpuLoad = new SimpleIntegerProperty();
        this.totalRam = new SimpleLongProperty();
        this.freeRam = new SimpleLongProperty();
        this.lowMemory = new SimpleBooleanProperty();
        this.temp = new SimpleDoubleProperty();
    }

    public String getId() {
        return id.get();
    }


    public void setId(String id) {
        this.id.set(id);
    }

    public String getName() {
        return name.get();
    }


    public void setName(String name) {
        this.name.set(name);
    }

    public String getType() {
        return type.get();
    }


    public void setType(String type) {
        this.type.set(type);
    }

    public int getBattery() {
        return battery.get();
    }


    public void setBattery(int battery) {
        this.battery.set(battery);
    }

    public boolean isCharging() {
        return charging.get();
    }


    public void setCharging(boolean charging) {
        this.charging.set(charging);
    }

    public boolean isPaired() {
        return paired.get();
    }


    public void setPaired(boolean paired) {
        this.paired.set(paired);
    }

    public boolean isMounted() {
        return mounted.get();
    }


    public void setMounted(boolean mounted) {
        this.mounted.set(mounted);
    }


    public ObservableList<NotificationDevice> getNotifications() {
        return notifications.get();
    }


    public void setNotifications(ObservableList<NotificationDevice> notifications) {
        this.notifications.set(notifications);
    }


    public long getFreeSpace() {
        return freeSpace.get();
    }


    public void setFreeSpace(long freeSpace) {
        this.freeSpace.set(freeSpace);
    }

    public long getTotalSpace() {
        return totalSpace.get();
    }


    public void setTotalSpace(long totalSpace) {
        this.totalSpace.set(totalSpace);
    }

    public int getCpuLoad() {
        return cpuLoad.get();
    }


    public void setCpuLoad(int cpuLoad) {
        this.cpuLoad.set(cpuLoad);
    }

    public long getTotalRam() {
        return totalRam.get();
    }


    public void setTotalRam(long totalRam) {
        this.totalRam.set(totalRam);
    }

    public long getFreeRam() {
        return freeRam.get();
    }


    public void setFreeRam(long freeRam) {
        this.freeRam.set(freeRam);
    }

    public double getTemp() {
        return temp.get();
    }


    public void setTemp(double temp) {
        this.temp.set(temp);
    }

    public boolean isLowMemory() {
        return lowMemory.get();
    }

    public void setLowMemory(boolean lowMemory) {
        this.lowMemory.set(lowMemory);
    }

    public long getFreeSpaceExt() {
        return freeSpaceExt.get();
    }

    public void setFreeSpaceExt(long freeSpaceExt) {
        this.freeSpaceExt.set(freeSpaceExt);
    }

    public long getTotalSpaceExt() {
        return totalSpaceExt.get();
    }


    public void setTotalSpaceExt(long totalSpaceExt) {
        this.totalSpaceExt.set(totalSpaceExt);
    }
}
