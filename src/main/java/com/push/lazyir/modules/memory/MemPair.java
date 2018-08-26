package com.push.lazyir.modules.memory;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MemPair {
    String name;
    String state;
    long allMem;
    long freeMem;
}
