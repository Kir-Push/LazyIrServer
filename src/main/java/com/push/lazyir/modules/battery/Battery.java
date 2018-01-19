package com.push.lazyir.modules.battery;

import com.push.lazyir.devices.Device;
import com.push.lazyir.devices.NetworkPackage;
import com.push.lazyir.gui.GuiCommunicator;
import com.push.lazyir.modules.Module;

import java.awt.*;
import java.awt.datatransfer.*;
import java.io.IOException;

/**
 * Created by buhalo on 09.04.17.
 */
public class Battery extends Module {
    private static final String STATUS = "status";
    private static final String PERCENTAGE = "percentage";

    @Override
    public void execute(NetworkPackage np) {
        System.out.println(np.getMessage());
        String id = np.getId();
        String percenstage = np.getValue(PERCENTAGE);
        String status = np.getValue(STATUS);
        GuiCommunicator.batteryStatus(percenstage,status, device);
    }

    @Override
    public void endWork() {

    }


}
