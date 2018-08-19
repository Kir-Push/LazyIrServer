package com.push.lazyir.modules.battery;

import com.push.lazyir.devices.Cacher;
import com.push.lazyir.devices.Device;
import com.push.lazyir.devices.NetworkPackage;
import com.push.lazyir.gui.GuiCommunicator;
import com.push.lazyir.modules.Module;
import com.push.lazyir.service.main.BackgroundService;

import javax.inject.Inject;
import java.awt.*;
import java.awt.datatransfer.*;
import java.io.IOException;
import java.lang.reflect.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by buhalo on 09.04.17.
 */
public class Battery extends Module {
    enum api{
        status,
        percentage
    }
    private GuiCommunicator guiCommunicator;

    @Inject
    public Battery(GuiCommunicator guiCommunicator, BackgroundService backgroundService, Cacher cacher) {
        super(backgroundService,cacher);
        this.guiCommunicator = guiCommunicator;
    }

    @Override
    public void execute(NetworkPackage np) {
        String percenstage = np.getValue(api.percentage.name());
        String status = np.getValue(api.status.name());
        guiCommunicator.batteryStatus(percenstage,status, device);
    }

    @Override
    public void endWork() {
        lock = null;
        device = null;
    }


}
