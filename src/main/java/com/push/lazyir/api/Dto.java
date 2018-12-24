package com.push.lazyir.api;

import lombok.Getter;
import lombok.Setter;

public class Dto {
    protected Dto(){

    }
    @Getter
    @Setter
    private String className;
    @Getter @Setter private boolean isModule;
}
