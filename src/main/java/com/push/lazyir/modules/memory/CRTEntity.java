package com.push.lazyir.modules.memory;


import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * C mean Cpu, R ram, and T temperature ;)
 */
@Data
@AllArgsConstructor
public class CRTEntity {
    private int cpuLoad;
    private long freeRam;
    private long freeRamAll;
    private double tempC;
    private boolean lowMem;
}
