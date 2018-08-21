package com.push.gui.entity;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.Data;

@Data
public class NotificationDevice {
    private StringProperty text;
    private StringProperty title;
    private StringProperty pack;
    private StringProperty ticker;
    private StringProperty id;
    private StringProperty type;
    private StringProperty icon;
    private StringProperty picture;
    private StringProperty ownerId;
    private StringProperty ownerName;


    public NotificationDevice(String text,String type, String title, String pack, String ticker, String id, String icon, String picture) {
        this.text = new SimpleStringProperty(text);
        this.type = new SimpleStringProperty(type);
        this.title = new SimpleStringProperty(title);
        this.pack = new SimpleStringProperty(pack);
        this.ticker = new SimpleStringProperty(ticker);
        this.id = new SimpleStringProperty(id);
        this.icon = new SimpleStringProperty(icon);
        this.picture = new SimpleStringProperty(picture);
        this.ownerName = new SimpleStringProperty();
        this.ownerId = new SimpleStringProperty();
    }

    public String getText() {
        return text.get();
    }

    public void setText(String text) {
        this.text.set(text);
    }

    public String getTitle() {
        return title.get();
    }


    public void setTitle(String title) {
        this.title.set(title);
    }

    public String getPack() {
        return pack.get();
    }

    public void setPack(String pack) {
        this.pack.set(pack);
    }

    public String getTicker() {
        return ticker.get();
    }

    public void setTicker(String ticker) {
        this.ticker.set(ticker);
    }

    public String getId() {
        return id.get();
    }


    public void setId(String id) {
        this.id.set(id);
    }

    public String getIcon() {
        return icon.get();
    }

    public void setIcon(String icon) {
        this.icon.set(icon);
    }

    public String getPicture() {
        return picture.get();
    }

    public void setPicture(String picture) {
        this.picture.set(picture);
    }

    public String getOwnerId() { return ownerId.get(); }

    public void setOwnerId(String ownerId) { this.ownerId.set(ownerId); }


    public String getType() { return type.get(); }

    public void setType(String type) { this.type.set(type); }

    @Override
    public String toString() {
        return "NotificationDevice{" +
                "text=" + text +
                ", title=" + title +
                ", pack=" + pack +
                ", ticker=" + ticker +
                ", id=" + id +
                ", type=" + type +
                ", icon=" + icon +
                ", picture=" + picture +
                ", ownerId=" + ownerId +
                '}';
    }

    public String getOwnerName() {
        return ownerName.get();
    }

    public void setOwnerName(String ownerName) {
        this.ownerName.set(ownerName);
    }
}
