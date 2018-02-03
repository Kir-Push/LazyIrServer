package com.push.gui.entity;

public class CommandGuiEntity implements Comparable<CommandGuiEntity> {

    private String id;
    private String producer;
    private String device;
    private String command_name;
    private String command;
    private String owner_id;
    private String type;
    private boolean active;
    private boolean edited;
    private boolean updated;

    public CommandGuiEntity(String id, String producer, String device, String command_name, String command, String owner_id, String type) {
        this.id = id;
        this.producer = producer;
        this.device = device;
        this.command_name = command_name;
        this.command = command;
        this.owner_id = owner_id;
        this.type = type;
    }

    public CommandGuiEntity() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProducer() {
        return producer;
    }

    public void setProducer(String producer) {
        this.producer = producer;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getCommand_name() {
        return command_name;
    }

    public void setCommand_name(String command_name) {
        this.command_name = command_name;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getOwner_id() {
        return owner_id;
    }

    public void setOwner_id(String owner_id) {
        this.owner_id = owner_id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isEdited() {
        return edited;
    }

    public void setEdited(boolean edited) {
        this.edited = edited;
    }

    public boolean isUpdated() {
        return updated;
    }

    public void setUpdated(boolean updated) {
        this.updated = updated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CommandGuiEntity that = (CommandGuiEntity) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        return command_name != null ? command_name.equals(that.command_name) : that.command_name == null;
    }

    @Override
    public String toString() {
        return "CommandGuiEntity{" +
                "id='" + id + '\'' +
                ", producer='" + producer + '\'' +
                ", device='" + device + '\'' +
                ", command_name='" + command_name + '\'' +
                ", command='" + command + '\'' +
                ", owner_id='" + owner_id + '\'' +
                ", type='" + type + '\'' +
                ", active=" + active +
                ", edited=" + edited +
                ", updated=" + updated +
                '}';
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (command_name != null ? command_name.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(CommandGuiEntity o) {
        return this.getCommand_name().compareTo(o.getCommand_name());
    }
}
