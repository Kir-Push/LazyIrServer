package com.push.lazyir.modules.reminder;

import java.util.List;

/**
 * Created by buhalo on 16.01.18.
 */

public class MissedCalls {

    private List<MissedCall> missedCalls;

    public MissedCalls(List<MissedCall> missedCalls) {
        this.missedCalls = missedCalls;
    }

    public MissedCalls() {
    }

    public List<MissedCall> getMissedCalls() {
        return missedCalls;
    }

    public void setMissedCalls(List<MissedCall> missedCalls) {
        this.missedCalls = missedCalls;
    }
}
