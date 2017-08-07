package com.push.lazyir.modules;


import com.push.lazyir.devices.Device;
import com.push.lazyir.devices.NetworkPackage;

/**
 * Created by buhalo on 05.03.17.
 */

public abstract class Module {

    protected Device device;


  public   void setDevice(Device dv)
    {
        this.device = dv;
    }


    public abstract void execute(NetworkPackage np);

  public abstract void endWork();
}
