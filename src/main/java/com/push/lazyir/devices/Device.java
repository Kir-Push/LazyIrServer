package com.push.lazyir.devices;



import com.push.lazyir.service.tcp.ConnectionThread;
import com.push.lazyir.modules.Module;
import com.push.lazyir.modules.ModuleFactory;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by buhalo on 19.02.17.
 */

public class Device {
    private ConnectionThread thread;
    private String id;
    private String name;
    private InetAddress ip;
    private String deviceType;
    private volatile boolean paired;
    private volatile boolean listening;
    private volatile boolean pinging;
    private volatile boolean answer;
    private ConcurrentHashMap<String, Module> enabledMdules = new ConcurrentHashMap<>();
    private ModuleFactory moduleFactory;
    private List<ModuleSetting> enabledModulesConfig;
    private Lock lock = new ReentrantLock();

    public Device(String id, String name, InetAddress ip, ConnectionThread runnableThread, List<ModuleSetting> enabledModules,ModuleFactory moduleFactory) {
        this.id = id;
        this.name = name;
        this.ip = ip;
        this.thread = runnableThread;
        this.paired = false;
        this.listening = true;
        this.pinging = false;
        this.answer = false;
        this.deviceType = "phone";
        this.enabledModulesConfig = enabledModules;
        this.moduleFactory = moduleFactory;
        for (ModuleSetting registeredModule : enabledModules) {
            if(registeredModule.isEnabled()){
                System.out.println(id + "     "  + registeredModule.getName());
                enableModule(registeredModule.getName());
            } }
    }


    public boolean isConnected()
    {
        return thread != null && thread.isConnected();
    }

    public void closeConnection()
    {
     lock.lock();
        try {
            thread.closeConnection(this);
        }finally {
            lock.unlock();
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public InetAddress getIp() {
        return ip;
    }

    public void setIp(InetAddress ip) {
        this.ip = ip;
    }

    public boolean isPaired() {
        return paired;
    }

    public void setPaired(boolean paired) {
        this.paired = paired;
    }

    public void savePairedState( String result, String data){
        this.thread.receivePairResult(id,result,data);
    }

    public boolean isListening() {
        return listening;
    }

    public void setListening(boolean listening) {
        this.listening = listening;
    }

    public boolean isPinging() {
        return pinging;
    }

    public void setPinging(boolean pinging) {
        this.pinging = pinging;
    }

    public boolean isAnswer() {
        return answer;
    }

    public void setAnswer(boolean answer) {
        this.answer = answer;
    }

    public void disableModules()
    {
        enabledMdules.clear();
    }

    private void enableModule(String name)
    {
        Module module = moduleFactory.instantiateModuleByName(this, name);
        if(module != null)
        enabledMdules.put(name, module);
    }

    public ConcurrentHashMap<String,Module> getEnabledModules()
    {
        return enabledMdules;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public void sendMessage(String msg) {
        if(thread != null && thread.isConnected())
            thread.printToOut(msg);
    }

    public void unpair(){
        if(thread != null ){
            thread.unpair();
        }
    }

    public List<ModuleSetting> getEnabledModulesConfig() {
        return enabledModulesConfig;
    }

    public void setEnabledModulesConfig(List<ModuleSetting> enabledModulesConfig) {
        this.enabledModulesConfig = enabledModulesConfig;
    }

    /*
    when use changes it's enabled modules on phone, it send command to other device update enabled modules
    here iterate over hashMap of enabledModules, check if it correspond to income list
    if disable  - end module, and remove from list
    if enable(didn't contain in hashMap) - instantiate module
    * */
    public void refreshEnabledModules(List<ModuleSetting> moduleSettingList) {
        lock.lock();
        try {
            if(!isConnected()){
                return;
            }
            setEnabledModulesConfig(moduleSettingList);
            for (ModuleSetting moduleSetting : moduleSettingList) {
                String name = moduleSetting.getName();
                if (enabledMdules.containsKey(name) && !moduleSetting.isEnabled()) { // if contain in enabledModules, but in income list is disabled
                    enabledMdules.get(name).endWork();                             // end work it and remove from list
                    enabledMdules.remove(name);
                } else if (!enabledMdules.containsKey(name) && moduleSetting.isEnabled()) { // opposite case, don't contain in enabledModules, but in list is enabled
                    enableModule(name);                                             // instantiate module, and put to enabledModules!
                }
            }
        }finally {
            lock.unlock();
        }
    }
}
