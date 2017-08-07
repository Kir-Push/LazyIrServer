package com.push.lazyir.modules;

import com.push.lazyir.Loggout;
import com.push.lazyir.devices.Device;
import com.push.lazyir.modules.battery.Battery;
import com.push.lazyir.modules.clipboard.ClipBoard;
import com.push.lazyir.modules.dbus.Mpris;
import com.push.lazyir.modules.notifications.Messengers;
import com.push.lazyir.modules.notifications.ShowNotification;
import com.push.lazyir.modules.notifications.SmsModule;
import com.push.lazyir.modules.command.SendCommand;
import com.push.lazyir.modules.share.ShareModule;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by buhalo on 05.03.17.
 */

public class ModuleFactory {


    private static List<Class> registeredModules;

    public static Module instantiateModule(Device dv, Class registeredModule)
    {
        if(registeredModules == null)
        {
            registerModulesInit();
        }
        Module module = null;
        try {
            module = (Module)registeredModule.newInstance();
            module.setDevice(dv);
        } catch (IllegalAccessException | InstantiationException e) {
            Loggout.e("ModuleFactory",e.toString());

        }
        return module;
    }

    private static void registerModulesInit() {
        registeredModules = new ArrayList<>();
        registeredModules.add(SendCommand.class);
        registeredModules.add(ShareModule.class);
        registeredModules.add(ShowNotification.class);
        registeredModules.add(SmsModule.class);
        registeredModules.add(Battery.class);
        registeredModules.add(Mpris.class);
        registeredModules.add(ClipBoard.class);
        registeredModules.add(Messengers.class);
    }

    public static Module instantiateModuleByName(Device dv,String name)
    {
        for (Class registeredModule : registeredModules) {
            if(registeredModule.getSimpleName().equals(name))
            {
                return instantiateModule(dv,registeredModule);
            }
        }
        return null;

    }

    public static List<Class> getRegisteredModules() {
        if(registeredModules == null)
        {
            registerModulesInit();
        }
        return registeredModules;
    }

    public static void setRegisteredModules(List<Class> registeredModules) {
        ModuleFactory.registeredModules = registeredModules;
    }

}
