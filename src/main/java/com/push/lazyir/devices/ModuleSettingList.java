package com.push.lazyir.devices;

import java.util.List;

public class ModuleSettingList {

    private List<ModuleSetting> moduleSettingList;

    public ModuleSettingList(List<ModuleSetting> moduleSettingList) {
        this.moduleSettingList = moduleSettingList;
    }

    public ModuleSettingList() {
    }

    public List<ModuleSetting> getModuleSettingList() {
        return moduleSettingList;
    }

    public void setModuleSettingList(List<ModuleSetting> moduleSettingList) {
        this.moduleSettingList = moduleSettingList;
    }
}
