package com.push.lazyir.devices;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ModuleSettingList {
    private List<ModuleSetting> settingList;
}
