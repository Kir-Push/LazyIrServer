package com.push.gui.controllers;

import com.push.gui.basew.Dialogs;
import com.push.gui.basew.MainWin;
import com.push.gui.basew.CommandsWindow;
import com.push.gui.entity.NotificationDevice;
import com.push.gui.entity.PhoneDevice;
import com.push.gui.utils.GuiUtils;
import com.push.lazyir.gui.GuiCommunicator;
import com.push.lazyir.modules.command.SendCommand;
import com.push.lazyir.modules.command.Command;
import com.push.lazyir.service.main.BackgroundService;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import javax.inject.Inject;
import java.util.Set;


public class MainController {


    ListView<PhoneDevice> personList;
    ListView<NotificationDevice> notifTList;
    // Ссылка на главное приложение.
    private MainWin mainApp;
    private Dialogs dialogs;
    private CommandsWindow commandsWindow;
    private GuiCommunicator guiCommunicator;
    private GuiUtils guiUtils;
    private BackgroundService backgroundService;

    /**
     * Конструктор.
     * Конструктор вызывается раньше метода initialize().
     */
    @Inject
    public MainController(GuiCommunicator guiCommunicator, Dialogs dialogs, CommandsWindow commandsWindow,BackgroundService backgroundService,GuiUtils guiUtils) {
        this.guiCommunicator = guiCommunicator;
        this.dialogs = dialogs;
        this.commandsWindow = commandsWindow;
        this.backgroundService = backgroundService;
        this.guiUtils = guiUtils;
        personList = new ListView<>();
        notifTList = new ListView<>();
    }

    public void setMainApp(MainWin mainApp) {
        this.mainApp = mainApp;
        initLists();
    }


     MainWin getMainApp() {
        return mainApp;
    }

    /**
     * Инициализация списков и присвоение им Listner'ов
     */
    private void initLists(){
        personList = (ListView<PhoneDevice>) mainApp.getRootLayout().lookup("#personList");
        notifTList = (ListView<NotificationDevice>) mainApp.getRootLayout().lookup("#notifTList");
        personList.setStyle("-fx-control-inner-color: white;");
        setPersonListCellFactory(personList);

        personList.setOnMouseClicked(event -> {
            PhoneDevice selectedItem = personList.getSelectionModel().getSelectedItem();
            if(selectedItem != null) {
                guiCommunicator.sendToGetAllNotif(selectedItem.getId());
                guiCommunicator.setGetRequestTimer(selectedItem.getId(),5000);
            }
        });


        setNotifListCellFactory(notifTList);
        // Добавление в таблицу данных из наблюдаемого списка
        personList.setItems(mainApp.getConnectedDevices());
        notifTList.setItems(mainApp.getNotificationsList());
    }

    private void setPersonListCellFactory(ListView<PhoneDevice> persList){
        persList.setCellFactory(list -> new ListCell<>(){
            private final ImageView imageView = new ImageView();

            @Override
            protected void updateItem(PhoneDevice item, boolean empty) {
                super.updateItem(item, empty);
                if(empty){
                    setText(null);
                    setGraphic(null);
                }else{
                    Image image = guiUtils.getImageByPaired(item.isPaired());
                    imageView.setImage(image);
                    Text text = new Text();
                    text.setText(item.getName());
                    VBox vBox = new VBox();
                    vBox.setId(item.getId());
                    vBox.getChildren().addAll(imageView,text);
                    setGraphic(vBox);
                    PhoneDevice selectedItem = list.getSelectionModel().getSelectedItem();

                    if(selectedItem != null && item.getId().equals(selectedItem.getId())) {
                        onSelectDevice(selectedItem);
                    }
                }
            }
        });
    }

    private void setNotifListCellFactory(ListView<NotificationDevice> notList){
        notList.setCellFactory(list -> new ListCell<>(){

            @Override
            protected void updateItem(NotificationDevice item, boolean empty){
                super.updateItem(item, empty);
                if(empty){
                    setText(null);
                    setGraphic(null);
                }else {
                    VBox vBox = new VBox();
                    Text title = new Text();
                    title.setFont(Font.font("Verdana", FontWeight.BOLD, 15));
                    title.setText(item.getTitle());
                    Text text = new Text();
                    text.setText(item.getText());
                    vBox.getChildren().addAll(title, text);
                    vBox.setAlignment(Pos.CENTER_LEFT);

                    Button button = new Button();
                    ImageView delete48 = new ImageView( guiUtils.getImage("delete48",15,15));
                    delete48.setPreserveRatio(true);
                    button.setGraphic(delete48);
                    button.setOnAction(event -> guiCommunicator.removeNotification(personList.getSelectionModel().getSelectedItem().getId(), item.getId()));

                    // https://stackoverflow.com/questions/32553658/about-javafx-need-to-align-a-node-that-is-inside-a-listview-to-the-far-right
                    GridPane listCellContents = new GridPane();
                    listCellContents.setHgap(10);

                    if(item.getIcon()!= null) {
                        ImageView icon = new ImageView(SwingFXUtils.toFXImage(guiUtils.pictureFromBase64Swing(item.getIcon()),null));
                        icon.setFitHeight(50);
                        icon.setFitWidth(50);
                        listCellContents.add(icon, 0, 0);
                    }
                    if(item.getPicture() != null){
                        ImageView picture = new ImageView(guiUtils.pictureFromBase64(item.getPicture(),200,200));
                        listCellContents.add(picture, 2, 0);
                    }

                    listCellContents.add(vBox,1,0);
                    ColumnConstraints leftCol = new ColumnConstraints();
                    ColumnConstraints centerCol = new ColumnConstraints();
                    ColumnConstraints rightCol = new ColumnConstraints();

                    rightCol.setHalignment(HPos.RIGHT);
                    rightCol.setHgrow(Priority.ALWAYS);
                    centerCol.setHalignment(HPos.CENTER);
                    centerCol.setHgrow(Priority.ALWAYS);
                    listCellContents.getColumnConstraints().addAll(leftCol,rightCol,centerCol);
                    setAnswerAction(listCellContents,item);
                    listCellContents.add(button,4,0);
                    setGraphic(listCellContents);
                }
            }
        });
    }

    void setAnswerAction(GridPane listCellContents, NotificationDevice item) {
        if(!item.getType().equals("notification")) {
            Button answer = new Button();
            answer.setText("Answer");
            PhoneDevice selectedDevice = personList.getSelectionModel().getSelectedItem();
            switch (item.getType()) {
                case "SMS":
                    answer.setOnAction(event -> openMessengerDialog(item, selectedDevice.getId()));
                    break;
                case "MESSENGER":
                    answer.setOnAction(event -> openMessengerDialog(item, selectedDevice.getId()));
                    break;
                case "call":
                    answer.setOnAction(event -> recall(item, selectedDevice.getId()));
                    break;
                default:
                    break;
            }
            listCellContents.add(answer,3,0);
        }
    }

    void onSelectDevice(PhoneDevice newSelection){
        String id = newSelection.getId();
        VBox rootLayout = mainApp.getRootLayout();

        ImageView batteryImg = (ImageView) rootLayout.lookup("#batteryImg");
        batteryImg.setImage(guiUtils.getImageByBattery(newSelection.getBattery(),newSelection.isCharging()));
        Label batteryText = (Label) rootLayout.lookup("#batteryText");
        batteryText.setText(newSelection.getBattery() + " %");

        Button pairedBtn = (Button) rootLayout.lookup("#pairBtn");
        if(newSelection.isPaired()){
            pairedBtn.setText("Unpair");
            pairedBtn.setOnAction(event -> guiCommunicator.unPair(id));
        }else{
            pairedBtn.setText("Pair");
            pairedBtn.setOnAction(event -> guiCommunicator.pair(id));
        }


        Button reconnect = (Button) rootLayout.lookup("#reconnectBtn");
        reconnect.setOnAction(event -> guiCommunicator.reconnect(id));


        Button mount = (Button) rootLayout.lookup("#mountBtn");
        if(newSelection.isMounted()){
            mount.setText("Unmount");
            mount.setOnAction(event -> guiCommunicator.unMount(id));
        }else{
            mount.setText("Mount");
            mount.setOnAction(event -> guiCommunicator.mount(id));
        }

        Button commandsButtn = (Button) rootLayout.lookup("#CommandsBtn");
        commandsButtn.setOnAction(event -> {
            backgroundService.getModuleById(id, SendCommand.class).sendGetAllCommands();
            commandsWindow.showWindow(id);
        });


        Button ping = (Button) rootLayout.lookup("#pingBtn");
        ping.setOnAction(event -> guiCommunicator.ping(id));

        setMemoryText(newSelection.getFreeSpace(),newSelection.getTotalSpace(),newSelection.getFreeSpaceExt(),newSelection.getTotalSpaceExt());

        setCpu(newSelection.getCpuLoad());

        setRam(newSelection.getFreeRam(),newSelection.getTotalRam(),newSelection.isLowMemory());

        // присваиваем списку уведомление, список из текущего устройсва
        mainApp.getNotificationsList().clear();
        mainApp.getNotificationsList().addAll(newSelection.getNotifications());
    }

    void setMemoryText(long freeMem,long allMem,long freeExt,long allExt){
        Label memory = (Label) mainApp.getRootLayout().lookup("#memoryLbl");
        memory.setText("Main Storage(MB): " + freeMem + "/" + allMem + ";  External Storage(s): " + freeExt + "/"+allExt);
        memory.setFont(Font.font(11));
    }

    void setRam(long freeRam,long totalRam,boolean lowMemory){
        Label ram = (Label) mainApp.getRootLayout().lookup("#ramLbl");
        ram.setText("Ram usage(MB): " + freeRam+"/"+totalRam);
        ram.setFont(Font.font(11));
        if(lowMemory) {
            ram.setTextFill(Color.RED);
        }
    }

    void setCpu(int cpuLoad){
        Label cpu = (Label) mainApp.getRootLayout().lookup("#cpuLoad");
        cpu.setText("Cpu load: " + cpuLoad + "%");
        cpu.setFont(Font.font(11));
    }

    private void recall(NotificationDevice item, String id) {
        guiCommunicator.recall(item,id);
    }

    public void openMessengerDialog(NotificationDevice item, String deviceId) {
        item.setType("MESSENGER");
        dialogs.showAnswerMessenger(deviceId,item);
    }

    public void openSmsDialog(NotificationDevice item, String deviceId) {
        dialogs.showAnswerMessenger(deviceId,item);
    }


    public ListView<PhoneDevice> getPersonList() {
        return personList;
    }

    public ListView<NotificationDevice> getNotifTList() {
        return notifTList;
    }

    void setCommands(Set<Command> commands, String id) {
        commandsWindow.receiveCommands(commands,id);
    }

    void setAllToDefault(PhoneDevice id) {
        getMainApp().getConnectedDevices().remove(id);
        personList.refresh();
       getMainApp().getNotificationsList().clear();
       setCpu(0);
       setRam(0,0,false);
       setMemoryText(0,0,0,0);
        VBox rootLayout = getMainApp().getRootLayout();
        ImageView batteryImg = (ImageView) rootLayout.lookup("#batteryImg");
        batteryImg.setImage(guiUtils.getImageByBattery(0,false));
        Label batteryText = (Label) rootLayout.lookup("#batteryText");
        batteryText.setText("");
        Button pairedBtn = (Button) rootLayout.lookup("#pairBtn");
        pairedBtn.setText("Pair");
        Button mount = (Button) rootLayout.lookup("#mountBtn");
        mount.setText("Mount");
    }
}
