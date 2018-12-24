package com.push.lazyir.service.main;

import com.push.gui.systray.JavaFXTrayIconSample;
import com.push.lazyir.api.MessageFactory;
import com.push.lazyir.devices.Device;
import com.push.lazyir.devices.ModuleSetting;
import com.push.lazyir.gui.GuiCommunicator;
import com.push.lazyir.modules.Module;
import com.push.lazyir.modules.ModuleFactory;
import com.push.lazyir.service.managers.settings.LocalizationManager;
import com.push.lazyir.service.managers.settings.SettingManager;
import com.push.lazyir.utils.ExtScheduledThreadPoolExecutor;
import lombok.Getter;
import lombok.Setter;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.awt.*;
import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.*;

@Slf4j
public class BackgroundService {

    @Getter
    private final ConcurrentHashMap<String, Device> connectedDevices = new ConcurrentHashMap<>();
    @Getter
    private final Set<InetAddress> neighbours = ConcurrentHashMap.newKeySet();
    @Getter
    private final ConcurrentHashMap<Long, Process> startedProcesses = new ConcurrentHashMap<>();
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    @Getter
    private final ScheduledThreadPoolExecutor timerService = new ExtScheduledThreadPoolExecutor(5);
    private Map<String, ModuleSetting> myEnabledModules;
    private TcpConnectionManager tcp;
    private UdpBroadcastManager udp;
    @Getter
    private SettingManager settingManager;
    @Getter
    private LocalizationManager localizationManager;
    @Setter
    @Getter  // need to set before init
    private GuiCommunicator guiCommunicator;
    @Setter // need to set before init
    private ServiceComponent serviceComponent;
    @Setter
    @Getter
    private JavaFXTrayIconSample javaFXTrayIconSample;
    private ModuleFactory moduleFactory;
    private MessageFactory messageFactory;
    private int port;


    @Inject
    public BackgroundService(SettingManager settingManager, LocalizationManager localizationManager, MessageFactory messageFactory, ModuleFactory moduleFactory) {
        this.settingManager = settingManager;
        this.localizationManager = localizationManager;
        this.messageFactory = messageFactory;
        this.moduleFactory = moduleFactory;
    }

    // call first
    public void init() {
        moduleFactory.setModuleComponent(serviceComponent.getModuleComponent());
        moduleFactory.registerModulesInit();
        PairService pairService = new PairService(this, settingManager, guiCommunicator, messageFactory);
        guiCommunicator.setPairService(pairService);
        tcp = new TcpConnectionManager(this, guiCommunicator, messageFactory, moduleFactory, pairService);
        udp = new UdpBroadcastManager(this, guiCommunicator, messageFactory);
        settingManager.init(); // must always be called before localization manager,because localization manager depends on setting manager's
        localizationManager.init();
        localizationManager.changeLanguage(settingManager.get("LANG"));
        loadFonts();
    }

    private void loadFonts() {
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont( Font.createFont(Font.PLAIN,   this.getClass().getClassLoader().getResourceAsStream("fonts/PoorStory-Regular.ttf")));
            ge.registerFont(Font.createFont(Font.PLAIN, this.getClass().getClassLoader().getResourceAsStream("fonts/Roboto-Bold.ttf")));
            ge.registerFont( Font.createFont(Font.PLAIN,   this.getClass().getClassLoader().getResourceAsStream("fonts/Roboto-Regular.ttf")));
            ge.registerFont( Font.createFont(Font.PLAIN, this.getClass().getClassLoader().getResourceAsStream("fonts/Roboto-Italic.ttf")));
        } catch (IOException |FontFormatException e) {
            log.error("error in loadFonts",e);
        }
    }

    public void configServices() {
        timerService.setRemoveOnCancelPolicy(true);
        timerService.setKeepAliveTime(10, TimeUnit.SECONDS);
        timerService.allowCoreThreadTimeOut(true);
        ((ThreadPoolExecutor) executorService).setKeepAliveTime(10, TimeUnit.SECONDS);
        ((ThreadPoolExecutor) executorService).allowCoreThreadTimeOut(true);
    }

    public void startWork() {
        addShutDownHook();
        startTasks();
    }

    // start main tasks
    private void startTasks() {
        startUdpListening();
        startTcpListening();
        connectCached();
    }

    private void addShutDownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            startedProcesses.values().stream().filter(Objects::nonNull).forEach(Process::destroyForcibly);
            destroy();
        }));
    }

    private void destroy() {
        stopUdpListening();
        eraseTcpConnections();
    }

    void crushed(String message) {
        guiCommunicator.iamCrushed(message);
        System.exit(-1);
    }

    private void startUdpListening() {
        udp.configureManager();
        udp.startUdpListener(getPort());
    }

    private void connectCached() {
        udp.connectRecconect();
    }

    private void stopUdpListening() {
        udp.stopUdpListener();
    }

    private void startTcpListening() {
        tcp.startServer();
        tcp.startListening();
    }

    public Future submitNewTask(Runnable runnable) {
        return executorService.submit(runnable);
    }

    private void eraseTcpConnections() {
        connectedDevices.values().stream().filter(Objects::nonNull).forEach(device -> tcp.stopListening(device));
    }

    public void sendToDevice(String id, String msg) {
        sendToDevice(connectedDevices.get(id), msg);
    }

    public void sendToDevice(Device device, String msg) {
        // separate thread maybe?
        if (device != null && device.isConnected()) {
            device.sendMessage(msg);
        }
    }

    public void sendToAllDevices(String message) {
        executorService.submit(() -> {
            if (connectedDevices.size() == 0) {
                return;
            }
            connectedDevices.values().stream()
                    .filter(Objects::nonNull)
                    .filter(Device::isConnected)
                    .forEach(device -> device.sendMessage(message));
        });
    }

    @Synchronized
    public Map<String, ModuleSetting> getMyEnabledModules() {
        if (myEnabledModules == null) {
            updateMyEnabledModules();
        }
        return myEnabledModules;
    }

    private void updateMyEnabledModules() {
        myEnabledModules = new HashMap<>();
        settingManager.getMyEnabledModules().forEach(module -> myEnabledModules.put(module.getName(), module));
    }

    public int getPort() {
        if (port == 0) {
            port = Integer.parseInt(getSettingManager().get("TCP-port"));
        }
        return port;
    }

    public boolean isServerOn() {
        return tcp.isServerOn();
    }

    void clearTempFolders() {
        settingManager.clearFolders();
    }

    public <T extends Module> T getModuleById(String id, Class<T> module) {
        Device device = connectedDevices.get(id);
        if (device != null) {
            return (T) device.getEnabledModules().get(module.getSimpleName());
        }
        return null;
    }

    @Synchronized
    public boolean ifLastConnectedDeviceAreYou(String id) {
        Device device = connectedDevices.get(id);
        int size = connectedDevices.size();
        return ((device != null && device.getId().equals(id) && size == 1) || size == 0);
    }
}
