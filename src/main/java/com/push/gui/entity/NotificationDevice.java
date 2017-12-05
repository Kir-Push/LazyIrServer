package com.push.gui.entity;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class NotificationDevice {

    private StringProperty text;
    private StringProperty title;
    private StringProperty pack;
    private StringProperty ticker;
    private StringProperty id;
    private StringProperty icon;
    private StringProperty picture;
    private StringProperty ownerId;

    public NotificationDevice(String text, String title, String pack, String ticker, String id, String icon, String picture) {
        this.text = new SimpleStringProperty(text);
        this.title = new SimpleStringProperty(title);
        this.pack = new SimpleStringProperty(pack);
        this.ticker = new SimpleStringProperty(ticker);
        this.id = new SimpleStringProperty(id);
        this.icon = new SimpleStringProperty(icon);
        this.picture = new SimpleStringProperty(picture);
    }

    public String getText() {
        return text.get();
    }

    public StringProperty textProperty() {
        return text;
    }

    public void setText(String text) {
        this.text.set(text);
    }

    public String getTitle() {
        return title.get();
    }

    public StringProperty titleProperty() {
        return title;
    }

    public void setTitle(String title) {
        this.title.set(title);
    }

    public String getPack() {
        return pack.get();
    }

    public StringProperty packProperty() {
        return pack;
    }

    public void setPack(String pack) {
        this.pack.set(pack);
    }

    public String getTicker() {
        return ticker.get();
    }

    public StringProperty tickerProperty() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker.set(ticker);
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

    public String getIcon() {
        return icon.get();
    }

    public StringProperty iconProperty() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon.set(icon);
    }

    public String getPicture() {
        return picture.get();
    }

    public StringProperty pictureProperty() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture.set(picture);
    }

    public String getOwnerId() { return ownerId.get(); }

    public StringProperty ownerIdProperty() { return ownerId; }

    public void setOwnerId(String ownerId) { this.ownerId.set(ownerId); }
}
