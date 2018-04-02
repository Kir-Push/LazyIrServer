package com.push.lazyir.devices;

import java.util.ArrayList;
import java.util.List;

public class ModuleSetting {
    private String name;
    private boolean enabled;
    private List<String> ignoredId;
    private boolean workOnly;

    public ModuleSetting() {
    }

    public ModuleSetting(String name, boolean enabled, List<String> ignoredId, boolean workOnly) {
        this.name = name;
        this.enabled = enabled;
        this.ignoredId = ignoredId;
        this.workOnly = workOnly;
    }

    public ModuleSetting(String name, boolean enabled,String ignoredId, boolean workOnly) {
        this.name = name;
        this.enabled = enabled;
        this.ignoredId = new ArrayList<String>();
        this.ignoredId.add(ignoredId);
        this.workOnly = workOnly;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<String> getIgnoredId() {
        return ignoredId;
    }

    public void setIgnoredId(List<String> ignoredId) {
        this.ignoredId = ignoredId;
    }

    public boolean isWorkOnly() {
        return workOnly;
    }

    public void setWorkOnly(boolean workOnly) {
        this.workOnly = workOnly;
    }
}
