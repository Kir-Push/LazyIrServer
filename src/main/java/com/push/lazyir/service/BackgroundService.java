package com.push.lazyir.service;

import com.push.lazyir.devices.Device;
import com.push.lazyir.service.settings.SettingManager;
import com.push.lazyir.utils.ExtScheduledThreadPoolExecutor;

import java.util.concurrent.*;
import java.util.concurrent.locks.*;

/**
 * Created by buhalo on 12.03.17.
 */
public class BackgroundService {


    private ReadWriteLock lock = new ReentrantReadWriteLock();

    final ExecutorService executorService = Executors.newCachedThreadPool();

    public static ScheduledThreadPoolExecutor getTimerService() {
        return getInstance().timerService;
    }

    final ScheduledThreadPoolExecutor timerService = new ExtScheduledThreadPoolExecutor(5);

    private TcpConnectionManager tcp;
    private UdpBroadcastManager udp;
    private SettingManager settingManager;
    private static int port = 5667;

    private volatile static BackgroundService instance;

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
    public void startTasks(){
        getInstance().startUdpListening();
        getInstance().startTcpListening();
        getInstance().connectCached();
    }

    public void destroy(){
        getInstance().stopUdpListening();
        getInstance().eraseTcpConnections();
    }

    private void startUdpListening() {
        udp.configureManager();
        udp.startUdpListener(port);
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

    public static void submitNewTask(Runnable runnable){
            getInstance().executorService.submit(runnable);
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


    public static void sendRequestPair(String id){
        getInstance().tcp.sendRequestPairDevice(id);
    }

    public static void sendRequestUnPair(String id){
        getInstance().tcp.sendUnpair(id);
    }

    public static void pairResultFromGui(String id,String result){
        getInstance().tcp.sendPairResult(id,result);
    }

    public static int getPort() {
        return port;
    }

    public static boolean isServerOn(){
        return getInstance().tcp.isServerOn();
    }
}
