package com.push.lazyir.modules.IrModule;

import com.push.lazyir.devices.NetworkPackage;
import com.push.lazyir.modules.Module;

import java.io.File;

/**
 * Created by buhalo on 02.04.17.
 */
public class IrModule extends Module{
    @Override
    public void execute(NetworkPackage np) {
        File jarDir = new File(ClassLoader.getSystemClassLoader().getResource(".").getPath());

    }
}
