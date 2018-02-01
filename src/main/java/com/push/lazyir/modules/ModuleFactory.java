package com.push.lazyir.modules;

import com.push.lazyir.Loggout;
import com.push.lazyir.devices.Device;
import com.push.lazyir.modules.battery.Battery;
import com.push.lazyir.modules.clipboard.ClipBoard;
import com.push.lazyir.modules.dbus.Mpris;
import com.push.lazyir.modules.memory.Memory;
import com.push.lazyir.modules.notifications.call.CallModule;
import com.push.lazyir.modules.notifications.messengers.Messengers;
import com.push.lazyir.modules.notifications.notifications.ShowNotification;
import com.push.lazyir.modules.notifications.sms.SmsModule;
import com.push.lazyir.modules.command.SendCommand;
import com.push.lazyir.modules.ping.Ping;
import com.push.lazyir.modules.reminder.Reminder;
import com.push.lazyir.modules.share.ShareModule;
import com.push.lazyir.modules.sync.SynchroModule;
import com.push.lazyir.modules.touch.TouchControl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by buhalo on 05.03.17.
 */

public class ModuleFactory {


    private static List<Class> registeredModules;
    private static Lock lock = new ReentrantLock();

    public static Module instantiateModule(Device dv, Class registeredModule)
    {
        lock.lock();
        try {
            if (registeredModules == null) {
                registerModulesInit();
            }
            Module module = null;
            try {
                module = (Module) registeredModule.newInstance();
                module.setDevice(dv);
            } catch (IllegalAccessException | InstantiationException e) {
                Loggout.e("ModuleFactory", e.toString());

            }
            return module;
        }finally {
            lock.unlock();
        }
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
        registeredModules.add(TouchControl.class);
        registeredModules.add(CallModule.class);
        registeredModules.add(Reminder.class);
        registeredModules.add(Memory.class);
        registeredModules.add(SynchroModule.class);
        registeredModules.add(Ping.class);
    }

    public static Module instantiateModuleByName(Device dv,String name)
    {
        if(registeredModules == null)
            registerModulesInit();
        for (Class registeredModule : registeredModules) {
            if(registeredModule.getSimpleName().equals(name))
            {
                return instantiateModule(dv,registeredModule);
            }
        }
        return null;

    }

    public static List<Class> getRegisteredModules() {
        lock.lock();
        try {
            if (registeredModules == null) {
                registerModulesInit();
            }
            return registeredModules;
        }finally {
            lock.unlock();
        }
    }

    public static void setRegisteredModules(List<Class> registeredModules) {
        lock.lock();
        ModuleFactory.registeredModules = registeredModules;
        lock.unlock();
    }

}
