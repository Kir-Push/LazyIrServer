package com.push.lazyir.modules.memory;

/**
 * Created by buhalo on 30.01.18.
 */
/*C mean Cpu, R ram, and T temperature ;)
* */
public class CRTEntity {
    private int cpuLoad;
    private long freeRam;
    private long freeRamAll;
    private double tempC;
    private boolean lowMem;

    public CRTEntity(int cpuLoad, long freeRam, long freeRamAll, double tempC, boolean lowMem) {
        this.cpuLoad = cpuLoad;
        this.freeRamAll = freeRamAll;
        this.freeRam = freeRam;
        this.tempC = tempC;
        this.lowMem = lowMem;
    }

    public CRTEntity() {
    }

    public int getCpuLoad() {
        return cpuLoad;
    }

    public void setCpuLoad(int cpuLoad) {
        this.cpuLoad = cpuLoad;
    }

    public long getFreeRam() {
        return freeRam;
    }

    public void setFreeRam(long freeRam) {
        this.freeRam = freeRam;
    }

    public double getTempC() {
        return tempC;
    }

    public void setTempC(double tempC) {
        this.tempC = tempC;
    }

    public long getFreeRamAll() {
        return freeRamAll;
    }

    public void setFreeRamAll(long freeRamAll) {
        this.freeRamAll = freeRamAll;
    }

    public boolean isLowMem() {
        return lowMem;
    }

    public void setLowMem(boolean lowMem) {
        this.lowMem = lowMem;
    }
}
