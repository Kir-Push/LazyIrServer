package com.push.lazyir.modules.memory;


import com.push.lazyir.api.MessageFactory;
import com.push.lazyir.api.NetworkPackage;
import com.push.lazyir.gui.GuiCommunicator;
import com.push.lazyir.modules.Module;
import com.push.lazyir.service.main.BackgroundService;
import lombok.Synchronized;

import javax.inject.Inject;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


public class Memory extends Module {
    public enum api{
        GET_FREE_MEM,
        GET_CRT
    }
    private static AtomicInteger memoryTimerCounter = new AtomicInteger();
    private static ScheduledFuture<?> timerFuture;
    private GuiCommunicator guiCommunicator;

    @Inject
    public Memory(BackgroundService backgroundService, MessageFactory messageFactory, GuiCommunicator guiCommunicator) {
        super(backgroundService, messageFactory);
        this.guiCommunicator = guiCommunicator;
    }

    @Override
    public void execute(NetworkPackage np) {
        MemoryDto dto = (MemoryDto) np.getData();
        api command = api.valueOf(dto.getCommand());
        switch (command){
            case GET_FREE_MEM:
                receiveFreeMem(dto.getMemoryEntity());
                break;
            case GET_CRT:
                receiveCRT(dto.getCrtEntity());
                break;
            default:
                break;
        }
    }

    @Override
    public void endWork() {
        clearTimer();
    }

    private void receiveCRT(CRTEntity crtEntity) {
        guiCommunicator.setDeviceCRT(crtEntity,device.getId());
    }

    private void receiveFreeMem(MemoryEntity memoryEntity) {
        guiCommunicator.setDeviceMemory(memoryEntity,device.getId());
    }

    public void setGetRequestTimer(int time){
        initiateTimer(backgroundService,messageFactory,device.getId(),time);
    }

    @Synchronized
    private static void initiateTimer(BackgroundService backgroundService,MessageFactory messageFactory,String id,int time){
        String simpleName = Memory.class.getSimpleName();
        String getCrtMessage = messageFactory.createMessage(simpleName, true, new MemoryDto(api.GET_CRT.name(), null, null));
        String getMemoryMessage = messageFactory.createMessage(simpleName,true,new MemoryDto(api.GET_FREE_MEM.name(),null,null));
        Runnable cmd = () -> {
            backgroundService.sendToDevice(id, getCrtMessage);
            int timerCounter = memoryTimerCounter.get();
            if (timerCounter % 5 == 0 || timerCounter == 0) {
                backgroundService.sendToDevice(id, getMemoryMessage);
            }
            memoryTimerCounter.incrementAndGet();
        };
        clearTimer();
        timerFuture = backgroundService.getTimerService().scheduleAtFixedRate(cmd, 0, time, TimeUnit.MILLISECONDS);
    }

    @Synchronized
    public static void clearTimer() {
        if (timerFuture != null)
            timerFuture.cancel(true);
        timerFuture = null;
        memoryTimerCounter.set(0);
    }


}
