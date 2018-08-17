package com.push.gui.basew;

import com.push.gui.controllers.MainController;
import com.push.gui.entity.CommandGuiEntity;
import com.push.lazyir.modules.sync.SynchroModule;
import com.push.lazyir.pojo.Command;
import com.push.lazyir.service.main.BackgroundService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CommandsWindow {
    private boolean opened = false;
    private String usedId;
    private Lock lock = new ReentrantLock();
    private ObservableList<CommandGuiEntity> list = FXCollections.observableArrayList(FXCollections.synchronizedObservableSet(FXCollections.observableSet(new HashSet<>())));
    private Set<CommandGuiEntity> tempUpdateList = new TreeSet<>();
    private Set<CommandGuiEntity> tempDeleteList = new TreeSet<>();
    private Set<CommandGuiEntity> tempNewList = new TreeSet<>();
    private  Stage stage;

    public boolean isOpened() {
        return opened;
    }

    public void setOpened(boolean opened) {
        this.opened = opened;
    }

    public void receiveCommands(List<Command> commands, String id) {
        lock.lock();
        try {
            if (commands == null ||(usedId != null && !usedId.equals(id)))
                return;
            for (int i = 0;i<commands.size();i++) {
                Command command = commands.get(i);
                list.add(new CommandGuiEntity(id, command.getProducer(), command.getDevice(), command.getCommand_name(),
                        command.getCommand(), command.getOwner_id(), command.getType()));
                if(i == commands.size()-1){
                    list.add(new CommandGuiEntity(id,command.getProducer(),command.getDevice()," "," ",
                            command.getOwner_id(),command.getType()));
                }
            }
            usedId = id;
        }finally {
            lock.unlock();
        }

    }

    public void showWindow(String id,MainController mainController){
        try {
            if(opened)
                return;
            this.usedId = id;
            this.opened = true;
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainWin.class.getClassLoader().getResource("fxml/commandWindow.fxml"));
            AnchorPane rootLayout = (AnchorPane) loader.load();
            Scene scene = new Scene(rootLayout);
            stage = new Stage();

            TableView<CommandGuiEntity> table = (TableView)scene.lookup("#table");

            Button closeSaveBtn =(Button) scene.lookup("#clsBtn");
            closeSaveBtn.setOnAction(event -> {
                BackgroundService.submitNewTask(()->{
                    List<Command> cmdD = new ArrayList<>();
                    for (CommandGuiEntity commandGuiEntity : tempDeleteList) {
                        cmdD.add(new Command(commandGuiEntity.getProducer(),commandGuiEntity.getDevice(),commandGuiEntity.getCommand_name(),
                                commandGuiEntity.getCommand(),commandGuiEntity.getOwner_id(),commandGuiEntity.getType()));
                    }
                    tempDeleteList.clear();
                    List<Command> cmdN = new ArrayList<>();
                    for (CommandGuiEntity commandGuiEntity : tempNewList) {
                        cmdN.add(new Command(commandGuiEntity.getProducer(),commandGuiEntity.getDevice(),commandGuiEntity.getCommand_name(),
                                commandGuiEntity.getCommand(),commandGuiEntity.getOwner_id(),commandGuiEntity.getType()));
                    }
                    tempNewList.clear();
                    List<Command> cmdU = new ArrayList<>();
                    for (CommandGuiEntity commandGuiEntity : tempUpdateList) {
                        cmdU.add(new Command(commandGuiEntity.getProducer(),commandGuiEntity.getDevice(),commandGuiEntity.getCommand_name(),
                                commandGuiEntity.getCommand(),commandGuiEntity.getOwner_id(),commandGuiEntity.getType()));
                    }
                    tempUpdateList.clear();
                    SynchroModule.sendDeleteCommands(id,cmdD);
                    SynchroModule.sendAddCommands(id,cmdN);
                    SynchroModule.sendUpdateCommands(id,cmdU);
                });
                clearList();
                stage.close();
               setOpened(false);
            });

            Button closeBtn =(Button) scene.lookup("#cls");
            closeBtn.setOnAction(event->{
                stage.close();
                setOpened(false);
                clearResources();
                clearList();
            });

            TableColumn<CommandGuiEntity,String> name = new TableColumn<>("Name");
            TableColumn<CommandGuiEntity,String> command = new TableColumn<>("Command");
            name.setEditable(true);
            command.setEditable(true);
            table.setEditable(true);
            name.setCellValueFactory(new PropertyValueFactory<>("command_name"));
            command.setCellValueFactory(new PropertyValueFactory<>("command"));

            name.setOnEditCommit((value)->{
                String oldValue = value.getOldValue();
                String newValue = value.getNewValue();
                int row = value.getTablePosition().getRow();
                CommandGuiEntity commandGuiEntity1 = value.getTableView().getItems().get(row);
                for(int i = 0; i<value.getTableView().getItems().size(); i++){
                    if(i != row) {
                        CommandGuiEntity commandGuiEntity = value.getTableView().getItems().get(i);
                        if(commandGuiEntity.getCommand_name().equals(newValue)){
                            commandGuiEntity1.setCommand_name(oldValue);
                            value.getTableView().refresh();
                            newValue = oldValue;
                            return;
                        }
                    }
                }
                if(newValue.equals(" ") || newValue.equals("")){
                    value.getTableView().getItems().remove(row);
                    value.getTableView().refresh();
                    return;
                }
                CommandGuiEntity commandGuiEntity = commandGuiEntity1;
                if(oldValue.equals(" ") && (row == table.getItems().size()-1)) {
                    commandGuiEntity.setCommand_name(newValue);
                    tempNewList.add(commandGuiEntity);
                    list.add(new CommandGuiEntity(usedId,commandGuiEntity.getProducer(),commandGuiEntity.getDevice()," "," ",
                            commandGuiEntity.getOwner_id(),commandGuiEntity.getType()));
                }else if(!oldValue.equals(newValue)){
                    CommandGuiEntity commandGuiEntity2 = new CommandGuiEntity(usedId, commandGuiEntity.getProducer(), commandGuiEntity.getDevice(), oldValue, commandGuiEntity.getCommand(),
                            commandGuiEntity.getOwner_id(), commandGuiEntity.getType());
                    if(!tempNewList.contains(commandGuiEntity2))
                    tempDeleteList.add(commandGuiEntity2);

                    commandGuiEntity.setCommand_name(newValue);
                    tempNewList.add(commandGuiEntity);
                }
            });
            command.setOnEditCommit(value -> {
                String oldValue = value.getOldValue();
                String newValue = value.getNewValue();
                int row = value.getTablePosition().getRow();
                CommandGuiEntity commandGuiEntity = value.getTableView().getItems().get(row);
                commandGuiEntity.setCommand(newValue);
                if(!oldValue.equals(newValue)){
                    tempUpdateList.add(commandGuiEntity);
                }
            });
            name.setCellFactory(TextFieldTableCell.<CommandGuiEntity> forTableColumn());
            command.setCellFactory(TextFieldTableCell.<CommandGuiEntity> forTableColumn());
            table.setItems(list);

            table.getColumns().addAll(name,command);
            stage.setTitle("Edit Commands for " + id);
            stage.setOnCloseRequest((event -> {setOpened(false);
            usedId =null;
            tempUpdateList.clear();
            tempDeleteList.clear();
            tempNewList.clear();
            list.clear();
            }));
            stage.setScene(scene);
            stage.show();
        }catch (IOException e){

        }

    }

    private void clearResources() {
        tempUpdateList.clear();
        tempNewList.clear();
        tempDeleteList.clear();
    }

    private void clearList(){
        list.clear();
    }

    private ObservableList<CommandGuiEntity> getItemsList() {
        return list;
    }

    public String usedId() {
        return usedId;
    }
}
