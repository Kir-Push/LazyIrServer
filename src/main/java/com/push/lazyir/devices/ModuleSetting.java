package com.push.lazyir.devices;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class ModuleSetting {
    private String name;
    private boolean enabled;
    private List<String> ignoredId;
    private boolean workOnly;
}
