package com.push.gui.basew;

import com.push.lazyir.modules.command.SendCommand;
import com.push.lazyir.modules.command.Command;
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
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.io.IOException;
import java.util.*;

@Slf4j
public class CommandsWindow {
    private boolean opened;
    private String usedId;
    private ObservableList<Command> list = FXCollections.observableArrayList(FXCollections.synchronizedObservableSet(FXCollections.observableSet(new HashSet<>())));
    private Set<Command> tempUpdateList = new TreeSet<>();
    private Set<Command> tempDeleteList = new TreeSet<>();
    private Set<Command> tempNewList = new TreeSet<>();
    private BackgroundService backgroundService;

    @Inject
    public CommandsWindow(BackgroundService backgroundService) {
        this.backgroundService = backgroundService;
    }

    @Synchronized
    public void receiveCommands(Set<Command> commands, String id) {
            if (commands == null ||(getUsedId() != null && !getUsedId().equals(id)))
                return;
            list.addAll(commands);
            list.add(new Command("","",generateTempId()));
            setUserId(id);
    }

    private int generateTempId() {
        return new Random().nextInt(10);
    }

    public void showWindow(String id){
        try {
            if(isOpened()) {
                return;
            }
            setUserId(id);
            setOpened(true);

            FXMLLoader loader = new FXMLLoader(Thread.currentThread().getContextClassLoader().getResource("fxml/commandWindow.fxml"));
            AnchorPane rootLayout = loader.load();
            Scene scene = new Scene(rootLayout);
            Stage stage = new Stage();

            setClsButtonAction(((Button) scene.lookup("#clsBtn")),id,stage);
            setCloseButtonAction( ((Button) scene.lookup("#cls")),stage);

            TableView<Command> table = (TableView)scene.lookup("#table");
            TableColumn<Command,String> name = new TableColumn<>("name");
            TableColumn<Command,String> command = new TableColumn<>("command");

            name.setEditable(true);
            name.setCellValueFactory(new PropertyValueFactory<>("commandName"));
            name.setCellFactory(TextFieldTableCell.<Command> forTableColumn());
            setNameEditAction(name);

            command.setEditable(true);
            command.setCellFactory(TextFieldTableCell.<Command> forTableColumn());
            command.setCellValueFactory(new PropertyValueFactory<>("cmd"));
           setCommandEditAction(command);

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
            log.error("showWindow - id: " + id,e);
        }

    }

    private void setClsButtonAction(Button btn,String id,Stage stage) {
        btn.setOnAction(event -> {
            backgroundService.submitNewTask(()->{
                SendCommand sendCommand = backgroundService.getModuleById(id, SendCommand.class);
                sendCommand.sendDeleteCommands(tempDeleteList);
               sendCommand.sendAddCommands(tempNewList);
               sendCommand.sendUpdateCommands(tempUpdateList);
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

    private void setCommandEditAction(  TableColumn<Command,String> command){
        command.setOnEditCommit(value -> {
            String oldValue = value.getOldValue();
            String newValue = value.getNewValue();
            Command commandGuiEntity =    value.getRowValue();
            commandGuiEntity.setCmd(newValue);
            if(!oldValue.equals(newValue) && !tempNewList.contains(commandGuiEntity)){
                tempUpdateList.add(commandGuiEntity);
            }
            value.getTableView().refresh();
        });
    }

    private void setNameEditAction(TableColumn<Command, String> name){
        name.setOnEditCommit(value->{
            TableView<Command> tableView = value.getTableView();
            String newValue = value.getNewValue();
            int row = value.getTablePosition().getRow();
            ObservableList<Command> items = tableView.getItems();
            Command cge  = value.getRowValue();
            if(row != items.size()-1){ // if it's not last row - which are empty row
                if(" ".equals(newValue) || "".equals(newValue)){
                    items.remove(row);
                    tempDeleteList.add(new Command("","",cge.getId()));
                }else{
                    cge.setCommandName(newValue);
                    tempUpdateList.add(cge);
                }
            }else if(!" ".equals(newValue) && !"".equals(newValue)){
                cge.setCommandName(newValue);
                tempNewList.add(cge);
                list.add(new Command("","",generateTempId()));
            }
            tableView.refresh();
        });
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
