package com.push.lazyir.devices;



import com.push.lazyir.service.tcp.ConnectionThread;
import com.push.lazyir.modules.Module;
import com.push.lazyir.modules.ModuleFactory;
import lombok.Data;
import lombok.Synchronized;

import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by buhalo on 19.02.17.
 */

@Data
public class Device {
    private ConnectionThread thread;
    private String id;
    private String name;
    private InetAddress ip;
    private String deviceType;
    private boolean paired;
    private boolean listening;
    private boolean pinging;
    private boolean answer;
    private ConcurrentHashMap<String, Module> enabledModules = new ConcurrentHashMap<>();
    private List<ModuleSetting> enabledModulesConfig;
    private ModuleFactory moduleFactory;

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
    }

    @Synchronized
    public void enableModules(){
        enabledModulesConfig.stream().filter(ModuleSetting::isEnabled).forEach(module -> enableModule(module.getName()));
    }

    public boolean isConnected() {
        return thread != null && thread.isConnected();
    }

    @Synchronized
    public void closeConnection() {
        thread.closeConnection(this);
    }

    public void savePairedState( String result, String data){
        this.thread.receivePairResult(id,result,data);
    }

    private void enableModule(String name) {
        if(!enabledModules.containsKey(name)) {
            Module module = moduleFactory.instantiateModuleByName(this, name);
            if (module != null) {
                enabledModules.put(name, module);
            }
        }
    }

    public void sendMessage(String msg) {
        if(isConnected())
            thread.printToOut(msg);
    }

    public void unpair(){
        if(thread != null ){
            thread.unpair();
        }
    }

    @Synchronized
    private void disableModule(String name){
        Module module = enabledModules.get(name);
        if (module != null) {
            enabledModules.remove(name);
            module.endWork();
        }
    }
    /*
    when use changes it's enabled modules on phone, it send cmd to other device update enabled modules
    here iterate over hashMap of enabledModulesConfig, check if it correspond to income list
    if disable  - end module, and remove from list
    if enable(didn't contain in hashMap) - instantiate module
    * */
    @Synchronized
    public void refreshEnabledModules(List<ModuleSetting> moduleSettingList){
            if(!isConnected()){
                return;
            }
            setEnabledModulesConfig(moduleSettingList);
            moduleSettingList.forEach(module -> {
                String moduleName = module.getName();
                if(!module.isEnabled()){
                    disableModule(moduleName);
                }else{
                    enableModule(moduleName);
                } });
    }
}
