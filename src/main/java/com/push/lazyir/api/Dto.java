package com.push.lazyir.api;

import lombok.Getter;
import lombok.Setter;

public abstract class Dto {
    @Getter
    @Setter
    private String className;
    @Getter @Setter private boolean isModule;
}
