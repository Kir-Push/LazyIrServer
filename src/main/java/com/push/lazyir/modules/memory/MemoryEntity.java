package com.push.lazyir.modules.memory;


import java.util.List;

/**
 * Created by buhalo on 30.01.18.
 */

public class MemoryEntity {
    private long mainMem;
    private long mainMemFree;
    private List<MemPair> extMem;

    public MemoryEntity( long mainMem, long mainMemFree, List<MemPair> extMem) {
        this.mainMem = mainMem;
        this.mainMemFree = mainMemFree;
        this.extMem = extMem;
    }

    public MemoryEntity() {
    }


    public long getMainMem() {
        return mainMem;
    }

    public void setMainMem(long mainMem) {
        this.mainMem = mainMem;
    }

    public List<MemPair> getExtMem() {
        return extMem;
    }

    public void setExtMem(List<MemPair> extMem) {
        this.extMem = extMem;
    }

    public long getMainMemFree() {
        return mainMemFree;
    }

    public void setMainMemFree(long mainMemFree) {
        this.mainMemFree = mainMemFree;
    }
}
