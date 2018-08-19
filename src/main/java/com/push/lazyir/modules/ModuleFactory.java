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
import com.push.lazyir.modules.screenShare.ScreenShareModule;
import com.push.lazyir.modules.share.ShareModule;
import com.push.lazyir.modules.sync.SynchroModule;
import com.push.lazyir.modules.touch.TouchControl;
import com.push.lazyir.service.main.ModuleComponent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by buhalo on 05.03.17.
 */

public class ModuleFactory {


    private List<Class> registeredModules = new CopyOnWriteArrayList<>();
    private ModuleComponent moduleComponent;
    private Method[] methods;
    private Lock lock = new ReentrantLock();

    public Module instantiateModule(Device dv, Class registeredModule)
    {
        lock.lock();
        try {
            Module module = null;
            Method method = getMethod(registeredModule);
            if(method == null)
                throw new NullPointerException("Such method doesn't exist  " + registeredModule.getSimpleName());
           method.setAccessible(true);
            module =(Module) method.invoke(moduleComponent);
            module.setDevice(dv);
            return module;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        return null;
    }

    private Method getMethod(Class registeredModule) {
        if (methods == null)
            methods = moduleComponent.getClass().getDeclaredMethods();
        Method method = null;
        for (Method mt : methods) {
            if(mt.getName().equals("provide"+registeredModule.getSimpleName())){
               method = mt;
               break;
            }
        }
        return method;
    }

    public void registerModulesInit() {
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
        registeredModules.add(ScreenShareModule.class);
    }

    public Module instantiateModuleByName(Device dv,String name)
    {
        for (Class registeredModule : registeredModules) {
            if(registeredModule.getSimpleName().equals(name))
            {
                return instantiateModule(dv,registeredModule);
            }
        }
        return null;

    }


    public void setModuleComponent(ModuleComponent moduleComponent) {
        this.moduleComponent = moduleComponent;
    }
}
