package com.push.gui.controllers;

import com.push.gui.basew.Dialogs;
import com.push.gui.basew.MainWin;
import com.push.gui.basew.CommandsWindow;
import com.push.gui.entity.NotificationDevice;
import com.push.gui.entity.PhoneDevice;
import com.push.gui.utils.GuiUtils;
import com.push.lazyir.gui.GuiCommunicator;
import com.push.lazyir.modules.sync.SynchroModule;
import com.push.lazyir.pojo.Command;
import com.push.lazyir.service.main.BackgroundService;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import javax.inject.Inject;
import java.util.List;


public class MainController {


    private ListView<PhoneDevice> personList;
    private ListView<NotificationDevice> notifTList;
    // Ссылка на главное приложение.
    private MainWin mainApp;
    private Dialogs dialogs;
    private CommandsWindow commandsWindow;
    private GuiCommunicator guiCommunicator;
    private BackgroundService backgroundService;

    /**
     * Конструктор.
     * Конструктор вызывается раньше метода initialize().
     */
    @Inject
    public MainController(GuiCommunicator guiCommunicator, Dialogs dialogs, CommandsWindow commandsWindow,BackgroundService backgroundService) {
        this.guiCommunicator = guiCommunicator;
        this.dialogs = dialogs;
        this.commandsWindow = commandsWindow;
        this.backgroundService = backgroundService;
        personList = new ListView<>();
        notifTList = new ListView<>();
    }

    public void setMainApp(MainWin mainApp) {
        this.mainApp = mainApp;
        initLists();
    }


    public MainWin getMainApp() {
        return mainApp;
    }

    /**
     * Инициализация списков и присвоение им Listner'ов
     */
    private void initLists(){
        personList = (ListView<PhoneDevice>) mainApp.getRootLayout().lookup("#personList");
        notifTList = (ListView<NotificationDevice>) mainApp.getRootLayout().lookup("#notifTList");
        personList.setStyle("-fx-control-inner-color: white;");
        personList.setCellFactory(personList -> new ListCell<>(){
            private final ImageView imageView = new ImageView();

            @Override
            protected void updateItem(PhoneDevice item, boolean empty) {
                super.updateItem(item, empty);
                if(empty){
                    setText(null);
                    setGraphic(null);
                }else{
                    Image image = GuiUtils.getImageByPaired(item.isPaired());
                    imageView.setImage(image);
                    Text text = new Text();
                    text.setText(item.getName());
                    VBox vBox = new VBox();
                    vBox.setId(item.getId());
                    vBox.getChildren().addAll(imageView,text);
                   setGraphic(vBox);
                    PhoneDevice selectedItem = personList.getSelectionModel().getSelectedItem();

                    if(selectedItem != null && item.getId().equals(selectedItem.getId())) {
                        onSelectDevice(selectedItem);
                    }
                }
            }
        });

        personList.setOnMouseClicked(event -> {
            PhoneDevice selectedItem = personList.getSelectionModel().getSelectedItem();
            if(selectedItem != null) {
                guiCommunicator.sendToGetAllNotif(selectedItem.getId());
                guiCommunicator.setGetRequestTimer(selectedItem.getId(),5000);
            }
        });


        notifTList.setCellFactory(notifTList -> new ListCell<>(){

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
                    ImageView delete48 = new ImageView( GuiUtils.getImage("delete48",15,15));
                    delete48.setPreserveRatio(true);
                    button.setGraphic(delete48);
                    button.setOnAction(event -> guiCommunicator.removeNotification(personList.getSelectionModel().getSelectedItem().getId(), item.getId()));

                    // https://stackoverflow.com/questions/32553658/about-javafx-need-to-align-a-node-that-is-inside-a-listview-to-the-far-right
                    GridPane listCellContents = new GridPane();
                    listCellContents.setHgap(10);

                    if(item.getIcon()!= null) {
                        ImageView icon = new ImageView(SwingFXUtils.toFXImage(GuiUtils.pictureFromBase64Swing(item.getIcon()),null));
                        icon.setFitHeight(50);
                        icon.setFitWidth(50);
                        listCellContents.add(icon, 0, 0);
                    }
                    if(item.getPicture() != null){
                        ImageView picture = new ImageView(GuiUtils.pictureFromBase64(item.getPicture(),200,200));
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
                    if(item.getType().equals("notification")) { }
                    else{
                        Button answer = new Button();
                        answer.setText("Answer");
                        PhoneDevice selectedDevice = personList.getSelectionModel().getSelectedItem();
                        if(item.getType().equals("sms")){
                           answer.setOnAction(event -> openMessengerDialog(item,selectedDevice.getId()));
                            }
                        else if(item.getType().equals("messenger"))
                            answer.setOnAction(event -> openMessengerDialog(item,selectedDevice.getId()));
                        else if(item.getType().equals("call"))
                            answer.setOnAction(event -> recall(item,selectedDevice.getId()));
                        listCellContents.add(answer,3,0);
                    }
                    listCellContents.add(button,4,0);
                    setGraphic(listCellContents);
                }
            }
        });


        // Добавление в таблицу данных из наблюдаемого списка
        personList.setItems(mainApp.getConnectedDevices());
        notifTList.setItems(mainApp.getNotificationsList());
    }

    private void onSelectDevice(PhoneDevice newSelection){
        VBox rootLayout = mainApp.getRootLayout();

        ImageView batteryImg = (ImageView) rootLayout.lookup("#batteryImg");
        batteryImg.setImage(GuiUtils.getImageByBattery(newSelection.getBattery(),newSelection.isCharging()));
        Label batteryText = (Label) rootLayout.lookup("#batteryText");
        batteryText.setText(newSelection.getBattery() + " %");


        Button pairedBtn = (Button) rootLayout.lookup("#pairBtn");
        if(newSelection.isPaired()){
            pairedBtn.setText("Unpair");
            pairedBtn.setOnAction(event -> guiCommunicator.unPair(newSelection.getId()));
        }else{
            pairedBtn.setText("Pair");
            pairedBtn.setOnAction(event -> guiCommunicator.pair(newSelection.getId()));
        }


        Button reconnect = (Button) rootLayout.lookup("#reconnectBtn");
        reconnect.setOnAction(event -> guiCommunicator.reconnect(newSelection.getId()));


        Button mount = (Button) rootLayout.lookup("#mountBtn");
        if(newSelection.isMounted()){
            mount.setText("Unmount");
            mount.setOnAction(event -> guiCommunicator.unMount(newSelection.getId()));
        }else{
            mount.setText("Mount");
            mount.setOnAction(event -> guiCommunicator.mount(newSelection.getId()));
        }

        Button commandsButtn = (Button) rootLayout.lookup("#CommandsBtn");
        commandsButtn.setOnAction(event -> {
            backgroundService.getModuleById(newSelection.getId(),SynchroModule.class).sendGetAllCommands(newSelection.getId());
            commandsWindow.showWindow(newSelection.getId());
        });


        Button ping = (Button) rootLayout.lookup("#pingBtn");
        ping.setOnAction(event -> guiCommunicator.ping(newSelection.getId()));

        setMemoryText(newSelection.getFreeSpace(),newSelection.getTotalSpace(),newSelection.getFreeSpaceExt(),newSelection.getTotalSpaceExt());

        setCpu(newSelection.getCpuLoad());

        setRam(newSelection.getFreeRam(),newSelection.getTotalRam(),newSelection.isLowMemory());

        // присваиваем списку уведомление, список из текущего устройсва
        mainApp.getNotificationsList().clear();
        mainApp.getNotificationsList().addAll(newSelection.getNotifications());
    }

    public void setMemoryText(long freeMem,long allMem,long freeExt,long allExt){
        Label memory = (Label) mainApp.getRootLayout().lookup("#memoryLbl");
        memory.setText("Main Storage(MB): " + freeMem + "/" + allMem + ";  External Storage(s): " + freeExt + "/"+allExt);
        memory.setFont(Font.font(11));
    }

    public void setRam(long freeRam,long totalRam,boolean lowMemory){
        Label ram = (Label) mainApp.getRootLayout().lookup("#ramLbl");
        ram.setText("Ram usage(MB): " + freeRam+"/"+totalRam);
        ram.setFont(Font.font(11));
        if(lowMemory)
            ram.setTextFill(Color.RED);
    }

    public void setCpu(int cpuLoad){
        Label cpu = (Label) mainApp.getRootLayout().lookup("#cpuLoad");
        cpu.setText("Cpu load: " + cpuLoad + "%");
        cpu.setFont(Font.font(11));
    }

    private void recall(NotificationDevice item, String id) {
        guiCommunicator.recall(item,id);
    }

    public void openMessengerDialog(NotificationDevice item, String deviceId) {
        item.setType("messenger");
        dialogs.showAnswerMessenger(deviceId,item);
    }

    public void openSmsDialog(NotificationDevice item, String deviceId) {
        dialogs.showAnswerMessenger(deviceId,item);
    }

    @FXML
    private void initialize() {
        // Инициализация таблицы адресатов с двумя столбцами.
    }

    public ListView<PhoneDevice> getPersonList() {
        return personList;
    }

    public void setPersonList(ListView<PhoneDevice> personList) {
        this.personList = personList;
    }

    public ListView<NotificationDevice> getNotifTList() {
        return notifTList;
    }

    public void setNotifTList(ListView<NotificationDevice> notifTList) {
        this.notifTList = notifTList;
    }

    public void setCommands(List<Command> commands, String id) {
//        if(!commandsWindow.isOpened() || !commandsWindow.usedId().equals(id))
//            return;
        commandsWindow.receiveCommands(commands,id);
    }

    public void setAllToDefault(PhoneDevice id) {
        getMainApp().getConnectedDevices().remove(id);
       getMainApp().getNotificationsList().clear();
       setCpu(0);
       setRam(0,0,false);
       setMemoryText(0,0,0,0);
        VBox rootLayout = getMainApp().getRootLayout();
        ImageView batteryImg = (ImageView) rootLayout.lookup("#batteryImg");
        batteryImg.setImage(GuiUtils.getImageByBattery(0,false));
        Label batteryText = (Label) rootLayout.lookup("#batteryText");
        batteryText.setText("");
        Button pairedBtn = (Button) rootLayout.lookup("#pairBtn");
        pairedBtn.setText("Pair");
        Button mount = (Button) rootLayout.lookup("#mountBtn");
        mount.setText("Mount");
    }
}
