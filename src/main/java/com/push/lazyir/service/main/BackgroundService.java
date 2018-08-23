package com.push.lazyir.service.main;

import com.push.gui.systray.JavaFXTrayIconSample;
import com.push.lazyir.Loggout;
import com.push.lazyir.devices.Cacher;
import com.push.lazyir.devices.Device;
import com.push.lazyir.devices.ModuleSetting;
import com.push.lazyir.gui.GuiCommunicator;
import com.push.lazyir.modules.Module;
import com.push.lazyir.modules.ModuleFactory;
import com.push.lazyir.modules.ping.Ping;
import com.push.lazyir.modules.share.ShareModule;
import com.push.lazyir.service.managers.settings.LocalizationManager;
import com.push.lazyir.service.managers.settings.SettingManager;
import com.push.lazyir.utils.ExtScheduledThreadPoolExecutor;

import javax.inject.Inject;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

/**
 * Created by buhalo on 12.03.17.
 */
public class BackgroundService {


    private ReadWriteLock lock = new ReentrantReadWriteLock();
    private final ConcurrentHashMap<String,Device> connectedDevices = new ConcurrentHashMap<>();

    final ExecutorService executorService = Executors.newCachedThreadPool();

    final ScheduledThreadPoolExecutor timerService = new ExtScheduledThreadPoolExecutor(5);

    private TcpConnectionManager tcp;
    private UdpBroadcastManager udp;
    private SettingManager settingManager;
    private LocalizationManager localizationManager;
    private Cacher cacher;
    private GuiCommunicator guiCommunicator;
    private ModuleFactory moduleFactory;
    private JavaFXTrayIconSample javaFXTrayIconSample;
    private ServiceComponent serviceComponent;

    private static int port = 0;

    private HashMap<String,ModuleSetting> myEnabledModules;


    @Inject
    public BackgroundService(SettingManager settingManager, LocalizationManager localizationManager,Cacher cacher,ModuleFactory moduleFactory) {
        this.settingManager = settingManager;
        this.localizationManager = localizationManager;
        this.cacher = cacher;
        this.moduleFactory = moduleFactory;
    }


    public GuiCommunicator getGuiCommunicator() {
        return guiCommunicator;
    }
    // need to call before init
    public void setGuiCommunicator(GuiCommunicator guiCommunicator) {
        this.guiCommunicator = guiCommunicator;
    }

    // need to call before init
    public void setServiceComponent(ServiceComponent serviceComponent) {
        this.serviceComponent = serviceComponent;
    }

    // call first
    public void init(){
        moduleFactory.setModuleComponent(serviceComponent.getModuleComponent());
        moduleFactory.registerModulesInit();
        tcp = new TcpConnectionManager(this,guiCommunicator, cacher,moduleFactory);
        udp = new UdpBroadcastManager(this,getGuiCommunicator(),cacher);
        localizationManager.changeLanguage(settingManager.get("LANG"));
        Loggout.refresh( settingManager.get("log-level"));
    }

    public void configServices(){
        timerService.setRemoveOnCancelPolicy(true);
        timerService.setKeepAliveTime(10, TimeUnit.SECONDS);
        timerService.allowCoreThreadTimeOut(true);
        ((ThreadPoolExecutor)executorService).setKeepAliveTime(10,TimeUnit.SECONDS);
        ((ThreadPoolExecutor)executorService).allowCoreThreadTimeOut(true);
    }

    // start main tasks
     void startTasks(){
        startUdpListening();
        startTcpListening();
        connectCached();
    }

    public void destroy(){
        stopUdpListening();
        eraseTcpConnections();
    }

    void crushed(String message){
        guiCommunicator.iamCrushed(message);
        System.exit(-1);
    }

    private void startUdpListening() {
        udp.configureManager();
        udp.startUdpListener(getPort());
    }

    private void connectCached() {
      udp.connectRecconect("null");
    }

    private  void stopUdpListening() {
        udp.stopUdpListener();
    }

    private  void startTcpListening() {
            tcp.startServer();
            tcp.startListening();
    }

    public Future<?> submitNewTask(Runnable runnable){
            return executorService.submit(runnable);
    }

    private void eraseTcpConnections() {
        for (Device device : connectedDevices.values()) {
            tcp.StopListening(device);
        }

    }

    public  SettingManager getSettingManager(){return settingManager;}

    public LocalizationManager getLocalizationManager(){return localizationManager;}

    public  void sendToDevice(String id, String msg) {
        // separate thread maybe?
        Device device = connectedDevices.get(id);
        if(device != null && device.isConnected())
            device.sendMessage(msg);
    }

    public void sendToAllDevices(String message){
        executorService.submit(()->{
            if(connectedDevices.size() == 0) {
                return;
            }
            for (Device device : connectedDevices.values()) {
                if(device != null && device.isConnected())
                    device.sendMessage(message);
            }});
    }

    public  HashMap<String,ModuleSetting> getMyEnabledModules(){
      lock.writeLock().lock();
        try{
            if(myEnabledModules == null){
                updateMyEnabledModules();
            }
            return  myEnabledModules;
        }finally {
            lock.writeLock().unlock();
        }
    }

    public void updateMyEnabledModules(){
       lock.writeLock().lock();
        try{
           myEnabledModules = new HashMap<>();
            for (ModuleSetting moduleSetting : settingManager.getMyEnabledModules()) {
                myEnabledModules.put(moduleSetting.getName(),moduleSetting);
            }
        }finally {
           lock.writeLock().unlock();
        }
    }


    public  void sendRequestPair(String id){
        tcp.sendRequestPairDevice(id);
    }

    public  void sendRequestUnPair(String id){
        tcp.sendUnpair(id);
    }

    public  void pairResultFromGui(String id, String result, String data){
       tcp.sendPairResult(id,result,data);
    }

    public  int getPort() {
        if(port == 0)
            port = Integer.parseInt(getSettingManager().get("TCP-port"));
        return port;
    }

    public  boolean isServerOn(){
        return tcp.isServerOn();
    }

    public  ScheduledThreadPoolExecutor getTimerService() {
        return timerService;
    }

    /*
    first close connection, after that sendUdp as you do when receive broadcast
    * */
    public  void reconnect(String id) {
        submitNewTask(()->{
            Device device = connectedDevices.get(id);
            if(device == null)
                return;
            InetAddress ip = device.getIp();
            device.closeConnection();
            udp.sendUdp(ip,getPort());
        });
    }

    public void sendPing(String id) {
        Device device = connectedDevices.get(id);
        if(device == null)
            return;
        Ping module = (Ping) device.getEnabledModules().get(Ping.class.getSimpleName());
        if(module != null)
            module.sendPing();
    }

    public void unMount(String id) {
        submitNewTask(()-> {
            Device device = connectedDevices.get(id);
            if (device == null)
                return;
            Module module = device.getEnabledModules().get(ShareModule.class.getSimpleName());
            if (module != null) {
                module.endWork();
            }
        });
    }

    public Map<String, Device> getConnectedDevices() {
        return connectedDevices;
    }

    public  void mount(String id) {
        getModuleById(id,ShareModule.class).sendSetupServerCommand(id);
    }

     void clearTempFolders(){
        settingManager.clearFolders();
    }

    public  void sendUdpPing(InetAddress ip, int port, String message) {
        udp.sendUdp(ip,port, message);
    }


    public <T extends Module> T getModuleById(String id, Class<T> module){
        System.out.println("getModuleById " + id + "  " + module.getSimpleName());
        Device device = connectedDevices.get(id);
        System.out.println("Device null? " + device);

        return (T)connectedDevices.get(id).getEnabledModules().get(module.getSimpleName());
    }

    public JavaFXTrayIconSample getJavaFXTrayIconSample() {
        return javaFXTrayIconSample;
    }

    public void setJavaFXTrayIconSample(JavaFXTrayIconSample javaFXTrayIconSample) {
        this.javaFXTrayIconSample = javaFXTrayIconSample;
    }
}
