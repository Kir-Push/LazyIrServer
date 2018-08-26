package com.push.lazyir.modules.battery;

import com.push.lazyir.api.MessageFactory;
import com.push.lazyir.api.NetworkPackage;
import com.push.lazyir.gui.GuiCommunicator;
import com.push.lazyir.modules.Module;
import com.push.lazyir.service.main.BackgroundService;

import javax.inject.Inject;

public class Battery extends Module {
    private GuiCommunicator guiCommunicator;

    @Inject
    public Battery(GuiCommunicator guiCommunicator, BackgroundService backgroundService, MessageFactory messageFactory) {
        super(backgroundService,messageFactory);
        this.guiCommunicator = guiCommunicator;
    }

    @Override
    public void execute(NetworkPackage np) {
        BatteryDto dto = (BatteryDto) np.getData();
        guiCommunicator.batteryStatus(dto.getPercentage(),dto.getStatus(), device);
    }

    @Override
    public void endWork() {
        device = null;
    }


}
