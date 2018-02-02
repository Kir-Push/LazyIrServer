package com.push.lazyir.service;

import com.push.lazyir.devices.Device;
import com.push.lazyir.devices.ModuleSetting;
import com.push.lazyir.devices.NetworkPackage;
import com.push.lazyir.gui.GuiCommunicator;
import com.push.lazyir.modules.Module;
import com.push.lazyir.modules.ping.Ping;
import com.push.lazyir.modules.share.ShareModule;
import com.push.lazyir.service.settings.SettingManager;
import com.push.lazyir.utils.ExtScheduledThreadPoolExecutor;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

/**
 * Created by buhalo on 12.03.17.
 */
public class BackgroundService {


    private ReadWriteLock lock = new ReentrantReadWriteLock();

    final ExecutorService executorService = Executors.newCachedThreadPool();

    final ScheduledThreadPoolExecutor timerService = new ExtScheduledThreadPoolExecutor(5);

    private TcpConnectionManager tcp;
    private UdpBroadcastManager udp;
    private SettingManager settingManager;
    private static int port = 0;

    private volatile static BackgroundService instance;

    private HashMap<String,ModuleSetting> myEnabledModules;

    static BackgroundService getInstance() {
        if(instance == null) {
            instance = new BackgroundService();
        }
        return instance;
    }

    private BackgroundService() {
        tcp = new TcpConnectionManager();
        udp = new UdpBroadcastManager();
        settingManager = new SettingManager();
    }

    void configServices(){
        timerService.setRemoveOnCancelPolicy(true);
        timerService.setKeepAliveTime(10, TimeUnit.SECONDS);
        timerService.allowCoreThreadTimeOut(true);
        ((ThreadPoolExecutor)executorService).setKeepAliveTime(10,TimeUnit.SECONDS);
        ((ThreadPoolExecutor)executorService).allowCoreThreadTimeOut(true);
    }

    // start main tasks
    static void startTasks(){
        getInstance().startUdpListening();
        getInstance().startTcpListening();
        getInstance().connectCached();
    }

    public static void destroy(){
        getInstance().stopUdpListening();
        getInstance().eraseTcpConnections();
    }

    static void crushed(String message){
        GuiCommunicator.iamCrushed(message);
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

    public static Future<?> submitNewTask(Runnable runnable){
            return getInstance().executorService.submit(runnable);
    }

    private void eraseTcpConnections() {
        for (Device device : Device.getConnectedDevices().values()) {
            tcp.StopListening(device);
        }

    }

    public static SettingManager getSettingManager(){return getInstance().settingManager;}

    public static void sendToDevice(String id, String msg) {
        // separate thread maybe?
        Device device = Device.getConnectedDevices().get(id);
        if(device != null && device.isConnected())
            device.sendMessage(msg);
    }

    public static void sendToAllDevices(String message){
        getInstance().executorService.submit(()->{
            if(Device.getConnectedDevices().size() == 0) {
                return;
            }
            for (Device device : Device.getConnectedDevices().values()) {
                if(device != null && device.isConnected())
                    device.sendMessage(message);
            }});
    }

    public static HashMap<String,ModuleSetting> getMyEnabledModules(){
        getInstance().lock.writeLock().lock();
        try{
            if(getInstance().myEnabledModules == null){
                updateMyEnabledModules();
            }
            return  getInstance().myEnabledModules;
        }finally {
            getInstance().lock.writeLock().unlock();
        }
    }

    public static void updateMyEnabledModules(){
        getInstance().lock.writeLock().lock();
        try{
            getInstance().myEnabledModules = new HashMap<>();
            for (ModuleSetting moduleSetting : getInstance().settingManager.getMyEnabledModules()) {
                getInstance().myEnabledModules.put(moduleSetting.getName(),moduleSetting);
            }
        }finally {
            getInstance().lock.writeLock().unlock();
        }
    }


    public static void sendRequestPair(String id){
        getInstance().tcp.sendRequestPairDevice(id);
    }

    public static void sendRequestUnPair(String id){
        getInstance().tcp.sendUnpair(id);
    }

    public static void pairResultFromGui(String id, String result, String data){
        getInstance().tcp.sendPairResult(id,result,data);
    }

    public static int getPort() {
        if(port == 0)
            port = Integer.parseInt(getSettingManager().get("TCP-port"));
        return port;
    }

    public static boolean isServerOn(){
        return getInstance().tcp.isServerOn();
    }

    public static ScheduledThreadPoolExecutor getTimerService() {
        return getInstance().timerService;
    }

    /*
    first close connection, after that sendUdp as you do when receive broadcast
    * */
    public static void reconnect(String id) {
        BackgroundService.submitNewTask(()->{
            Device device = Device.getConnectedDevices().get(id);
            if(device == null)
                return;
            InetAddress ip = device.getIp();
            device.closeConnection();
            getInstance().udp.sendUdp(ip,getPort());
        });
    }

    public static void sendPing(String id) {
        Device device = Device.getConnectedDevices().get(id);
        if(device == null)
            return;
        Ping module = (Ping) device.getEnabledModules().get(Ping.class.getSimpleName());
        if(module != null)
            module.sendPing();
    }

    public static void unMount(String id) {
        BackgroundService.submitNewTask(()-> {
            Device device = Device.getConnectedDevices().get(id);
            if (device == null)
                return;
            Module module = device.getEnabledModules().get(ShareModule.class.getSimpleName());
            if (module != null) {
                module.endWork();
            }
        });
    }

    public static void mount(String id) {
        ShareModule.sendSetupServerCommand(id);
    }

    static void clearTempFolders(){
        getInstance().settingManager.clearFolders();
    }

    public static void sendUdpPing(InetAddress ip, int port, String message) {
        getInstance().udp.sendUdp(ip,port, message);
    }
}
