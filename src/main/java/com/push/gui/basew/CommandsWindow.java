package com.push.gui.basew;

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
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class CommandsWindow {
    private boolean opened;
    private String usedId;
    private Lock lock = new ReentrantLock();
    private ObservableList<CommandGuiEntity> list = FXCollections.observableArrayList(FXCollections.synchronizedObservableSet(FXCollections.observableSet(new HashSet<>())));
    private Set<CommandGuiEntity> tempUpdateList = new TreeSet<>();
    private Set<CommandGuiEntity> tempDeleteList = new TreeSet<>();
    private Set<CommandGuiEntity> tempNewList = new TreeSet<>();
    private BackgroundService backgroundService;

    @Inject
    public CommandsWindow(BackgroundService backgroundService) {
        this.backgroundService = backgroundService;
    }

    public void receiveCommands(List<Command> commands, String id) {
        lock.lock();
        try {
            if (commands == null ||(getUsedId() != null && !getUsedId().equals(id)))
                return;
            commands.forEach(command -> list.add(new CommandGuiEntity(id, command.getProducer(), command.getDevice(), command.getCommand_name(),
                            command.getCommand(), command.getOwner_id(), command.getType())));
            CommandGuiEntity cge = list.get(commands.size() - 1);
            cge.setCommand_name(" ");
            cge.setCommand(" ");
            setUserId(id);
        }finally {
            lock.unlock();
        }

    }

    public void showWindow(String id){
        try {
            if(isOpened())
                return;
            setUserId(id);
            setOpened(true);

            FXMLLoader loader = new FXMLLoader(Thread.currentThread().getContextClassLoader().getResource("fxml/commandWindow.fxml"));
            AnchorPane rootLayout = loader.load();
            Scene scene = new Scene(rootLayout);
            Stage stage = new Stage();

            setClsButtonAction(((Button) scene.lookup("#clsBtn")),id,stage);
            setCloseButtonAction( ((Button) scene.lookup("#cls")),stage);

            TableView<CommandGuiEntity> table = (TableView)scene.lookup("#table");
            TableColumn<CommandGuiEntity,String> name = new TableColumn<>("Name");
            TableColumn<CommandGuiEntity,String> command = new TableColumn<>("Command");

            name.setEditable(true);
            name.setCellValueFactory(new PropertyValueFactory<>("command_name"));
            name.setCellFactory(TextFieldTableCell.<CommandGuiEntity> forTableColumn());
            setNameEditAction(name,table);

            command.setEditable(true);
            command.setCellFactory(TextFieldTableCell.<CommandGuiEntity> forTableColumn());
            command.setCellValueFactory(new PropertyValueFactory<>("command"));
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

            table.setEditable(true);
            table.setItems(list);
            table.getColumns().add(name);
            table.getColumns().add(command);
            stage.setTitle("Edit Commands for " + id);
            stage.setOnCloseRequest((event -> {setOpened(false);
            setUserId(null);
            clearResources();
            clearList();
            }));
            stage.setScene(scene);
            stage.show();

        }catch (IOException e){
            log.error("showWindow",e);
        }

    }

    private void setClsButtonAction(Button btn,String id,Stage stage) {
        btn.setOnAction(event -> {
            backgroundService.submitNewTask(()->{
                SynchroModule synchroModule = backgroundService.getModuleById(id, SynchroModule.class);
                synchroModule.sendDeleteCommands(id,populateCmd(tempDeleteList));
                synchroModule.sendAddCommands(id,populateCmd(tempNewList));
                synchroModule.sendUpdateCommands(id,populateCmd(tempUpdateList));
                clearResources();
            });
            clearList();
            stage.close();
            setOpened(false);
        });
    }
    private void setCloseButtonAction(Button button, Stage stage){
        button.setOnAction(event->{
            stage.close();
            setOpened(false);
            clearResources();
            clearList();
        });
    }

    private void setNameEditAction(TableColumn<CommandGuiEntity, String> name, TableView<CommandGuiEntity> table){
        name.setOnEditCommit(value->{
            TableView<CommandGuiEntity> tableView = value.getTableView();
            String oldValue = value.getOldValue();
            String newValue = value.getNewValue();
            int row = value.getTablePosition().getRow();
            ObservableList<CommandGuiEntity> items = tableView.getItems();
            CommandGuiEntity cge = items.get(row);

            for(int i =0;i<items.size();i++){
                if(i != row && items.get(i).getCommand_name().equals(newValue)){
                    cge.setCommand_name(oldValue);
                    tableView.refresh();
                    return;
                }
            }
            if(newValue.equals(" ") || newValue.equals("")){
                items.remove(row);
                tableView.refresh();
            }
            else if(oldValue.equals(" ") && (row == table.getItems().size()-1)) {
                cge.setCommand_name(newValue);
                tempNewList.add(cge);
                list.add(new CommandGuiEntity(getUsedId(), cge.getProducer(), cge.getDevice()," "," ",
                        cge.getOwner_id(), cge.getType()));
            }
            else if(!oldValue.equals(newValue)){
                CommandGuiEntity guiEntity = new CommandGuiEntity(getUsedId(), cge.getProducer(), cge.getDevice(), oldValue, cge.getCommand(),
                        cge.getOwner_id(), cge.getType());
                if(!tempNewList.contains(guiEntity)) {
                    tempDeleteList.add(guiEntity);
                }
                cge.setCommand_name(newValue);
                tempNewList.add(cge);
            }
        });
    }

    private List<Command> populateCmd(Set<CommandGuiEntity> tempList) {
        List<Command> cmd = new ArrayList<>();
        tempList.forEach(cge -> cmd.add(new Command(cge.getProducer(),cge.getDevice(),cge.getCommand_name(),
                cge.getCommand(),cge.getOwner_id(),cge.getType())));
       return cmd;
    }
    private void clearResources() {
        tempUpdateList.clear();
        tempNewList.clear();
        tempDeleteList.clear();
    }
    private void clearList(){
        list.clear();
    }
    private String getUsedId(){
        return usedId;
    }

    private void setUserId(String id){
        this.usedId = id;
    }
    private boolean isOpened() {
        return opened;
    }
    private void setOpened(boolean opened) {
        this.opened = opened;
    }
}
