package com.push.lazyir.modules.memory;

/**
 * Created by buhalo on 30.01.18.
 */

public class MemPair {
    String name;
    String state;
    long allMem;
    long freeMem;

    public MemPair() {
    }

    public MemPair(String name, long allMem, long freeMem,String state) {
        this.name = name;
        this.allMem = allMem;
        this.freeMem = freeMem;
        this.state = state;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getAllMem() {
        return allMem;
    }

    public void setAllMem(long allMem) {
        this.allMem = allMem;
    }

    public long getFreeMem() {
        return freeMem;
    }

    public void setFreeMem(long freeMem) {
        this.freeMem = freeMem;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

}
