package com.push.lazyir.modules.ir;

import com.push.lazyir.devices.NetworkPackage;
import com.push.lazyir.modules.Module;

import java.io.File;


public class IrModule extends Module{
    @Override
    public void execute(NetworkPackage np) {
        File jarDir = new File(ClassLoader.getSystemClassLoader().getResource(".").getPath());

    }

    @Override
    public void endWork() {

    }
}
