package com.push.lazyir.modules.ir;

import com.push.lazyir.devices.Cacher;
import com.push.lazyir.devices.Device;
import com.push.lazyir.devices.NetworkPackage;
import com.push.lazyir.modules.Module;
import com.push.lazyir.service.main.BackgroundService;

import javax.inject.Inject;
import java.io.File;


public class IrModule extends Module{
    @Inject
    public IrModule(BackgroundService backgroundService, Cacher cacher) {
        super(backgroundService, cacher);
    }

    @Override
    public void execute(NetworkPackage np) {
        File jarDir = new File(ClassLoader.getSystemClassLoader().getResource(".").getPath());

    }

    @Override
    public void endWork() {

    }
}
