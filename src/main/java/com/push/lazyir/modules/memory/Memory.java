package com.push.lazyir.modules.memory;


import com.push.lazyir.devices.NetworkPackage;
import com.push.lazyir.gui.GuiCommunicator;
import com.push.lazyir.modules.Module;
import com.push.lazyir.service.BackgroundService;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by buhalo on 30.01.18.
 */

public class Memory extends Module {
    private final static String GET_FREE_MEM = "getFreeMem";
    private final static String GET_CRT = "getCrt";
    private final static ReentrantLock staticLock = new ReentrantLock();
    private static volatile boolean timerSetted = false;
    private static volatile  ScheduledFuture<?> timerFuture;

    @Override
    public void execute(NetworkPackage np) {
        String data = np.getData();
        switch (data){
            case GET_FREE_MEM:
                receiveFreeMem(np);
                break;
            case GET_CRT:
               receiveCRT(np);
                break;
            default:
                break;
        }
    }

    private void receiveCRT(NetworkPackage np) {
        CRTEntity object = np.getObject(NetworkPackage.N_OBJECT, CRTEntity.class);
        GuiCommunicator.setDeviceCRT(object,device.getId());
    }

    private void receiveFreeMem(NetworkPackage np) {
        MemoryEntity object = np.getObject(NetworkPackage.N_OBJECT, MemoryEntity.class);
        GuiCommunicator.setDeviceMemory(object,device.getId());
    }

    public static void setGetRequestTimer(int time,String id){
        staticLock.lock();
        clearTimer();
        try{
            Runnable cmd = ()->{
                NetworkPackage orCreatePackage = NetworkPackage.Cacher.getOrCreatePackage(Memory.class.getSimpleName(), GET_CRT);
                BackgroundService.sendToDevice(id,orCreatePackage.getMessage());
            };
            if(timerFuture != null)
                clearTimer();
            timerFuture = BackgroundService.getTimerService().scheduleAtFixedRate(cmd, time, time, TimeUnit.MILLISECONDS);
            timerSetted = true;
        }finally {
            staticLock.unlock();
        }
    }

    public static void clearTimer() {
        staticLock.lock();
        try{
            if(timerFuture != null)
                timerFuture.cancel(true);
            timerFuture = null;
            timerSetted = false;
        }finally {
            staticLock.unlock();
        }
    }


}
