package com.push.gui.systray;

import com.push.gui.basew.About;
import com.push.gui.basew.MainWin;
import com.push.gui.basew.SettingsWindow;
import com.push.gui.controllers.MainController;
import com.push.gui.entity.PhoneDevice;
import com.push.lazyir.service.main.BackgroundService;
import com.push.lazyir.service.main.DaggerServiceComponent;
import com.push.lazyir.gui.GuiCommunicator;
import com.push.lazyir.service.main.ServiceComponent;
import com.push.lazyir.service.managers.settings.LocalizationManager;
import javafx.application.*;
import javafx.scene.image.Image;
import javafx.stage.*;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.util.Objects;

import static java.awt.Image.SCALE_AREA_AVERAGING;

// Java 8 code
@Slf4j
public class JavaFXTrayIconSample extends Application {
    // application stage is stored so that it can be shown and hidden based on system tray icon operations.
    private Stage stage;
    private  MainWin mainWin;
    private BackgroundService backgroundService;
    private About about;
    private SettingsWindow settingsWindow;
    private LocalizationManager localizationManager;
    private GuiCommunicator guiCommunicator;


    // sets up the javafx application.
    // a tray icon is setup for the icon, but the main stage remains invisible until the user
    // interacts with the tray icon.
    @Override public void start(final Stage stage) {
        dependenciesInit();
        backgroundService.setJavaFXTrayIconSample(this);
        // stores a reference to the stage.
        this.stage = stage;
        this.stage.setOnHidden(event -> guiCommunicator.clearGetRequestTimer()); // todo check work or no
        // instructs the javafx system not to exit implicitly when the last application window is shut.
        Platform.setImplicitExit(false);

        // sets up the tray icon (using awt code run on the swing thread).
        addAppToTray();
        mainWin.start(stage);
        new Thread(() -> backgroundService.startWork()).start();
    }
    /**
     * Sets up a system tray icon for the application.
     */
    private void addAppToTray() {
        try {
            // ensure awt toolkit is initialized.
            java.awt.Toolkit.getDefaultToolkit();
            // app requires system tray support, just exit if there is no support.
            if (!java.awt.SystemTray.isSupported()) {
                log.debug("No system tray support, application exiting.");
                Platform.exit();
            }
            // add title image
            Image titleImage = new Image(JavaFXTrayIconSample.class.getClassLoader().getResource("icons/phone.png").toExternalForm());
            stage.getIcons().add(titleImage);
            // set up a system tray icon.
            java.awt.SystemTray tray = java.awt.SystemTray.getSystemTray();
            java.awt.Image image = ImageIO.read(Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource("icons/phone.png")));
            java.awt.TrayIcon trayIcon = new java.awt.TrayIcon(image.getScaledInstance(27,27,SCALE_AREA_AVERAGING));
            trayIcon.setImageAutoSize(true);
            // if the user double-clicks on the tray icon, show the main app stage.
            trayIcon.addActionListener(event -> Platform.runLater(this::showStage));
            // if the user selects the default menu item (which includes the app name),
            // show the main app stage.
            java.awt.MenuItem openItem = new java.awt.MenuItem(localizationManager.get("openTray"));
            openItem.addActionListener(event -> Platform.runLater(this::showStage));
            java.awt.MenuItem aboutItem = new java.awt.MenuItem(localizationManager.get("aboutTray"));
            aboutItem.addActionListener(event -> Platform.runLater(()-> about.showWindow()));
            // the convention for tray icons seems to be to set the default icon for opening
            // the application stage in a bold font.
            java.awt.MenuItem settingItem = new java.awt.MenuItem(localizationManager.get("settingTray"));
            settingItem.addActionListener(event ->  Platform.runLater(()->settingsWindow.showWindow()));
            // to really exit the application, the user must go to the system tray icon
            // and select the exit option, this will shutdown JavaFX and remove the
            // tray icon (removing the tray icon will also shut down AWT).
            java.awt.MenuItem exitItem = new java.awt.MenuItem(localizationManager.get("exitTray"));
            exitItem.addActionListener(event -> {
                Platform.exit();
                System.exit(0);
                tray.remove(trayIcon);

            });
            // setup the popup menu for the application.
            final java.awt.PopupMenu popup = new java.awt.PopupMenu();
            popup.add(openItem);
            popup.addSeparator();
            popup.add(settingItem);
            popup.addSeparator();
            popup.add(aboutItem);
            popup.addSeparator();
            popup.add(exitItem);
            trayIcon.setPopupMenu(popup);
            // add the application tray icon to the system tray.
            tray.add(trayIcon);
        } catch (java.awt.AWTException | IOException e) {
           log.error("Unable to init system tray",e);
        }
    }

    /**
     * Shows the application stage and ensures that it is brought ot the front of all stages.
     */
    public void showStage() {
        if (stage != null) {
            stage.show();
            stage.toFront();
            MainController controller = mainWin.getController();
            int selectedIndex = controller.getPersonList().getSelectionModel().getSelectedIndex();
            if(mainWin.getConnectedDevices().size() <= selectedIndex || selectedIndex == -1) {
                controller.getPersonList().getSelectionModel().select(0);
            }
            PhoneDevice selectedItem = controller.getPersonList().getSelectionModel().getSelectedItem();
            if(selectedItem != null) {
                guiCommunicator.sendToGetAllNotif(selectedItem.getId());
                guiCommunicator.setGetRequestTimer(selectedItem.getId(),5000);
            }
        }
    }



    private void dependenciesInit() {
        ServiceComponent serviceComponent = DaggerServiceComponent.builder().build();
        backgroundService = serviceComponent.provideBackGroundService();
        about = serviceComponent.provideAbout();
        localizationManager = serviceComponent.provideLocalizationManager();
        guiCommunicator = serviceComponent.provideGuiCommunicator();
        mainWin = serviceComponent.provideMainWin();
        settingsWindow = serviceComponent.provideSettingsWindow();
        // because recursive dependency set it apart
        backgroundService.setGuiCommunicator(guiCommunicator);
        backgroundService.setServiceComponent(serviceComponent);
        backgroundService.init();
        backgroundService.configServices();
    }

    /*
    Main entry in application
    * */
    public static void main(String[] args) {
        launch(args);
    }
}