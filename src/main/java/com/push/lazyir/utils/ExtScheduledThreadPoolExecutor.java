package com.push.lazyir.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ExtScheduledThreadPoolExecutor extends ScheduledThreadPoolExecutor {
    public ExtScheduledThreadPoolExecutor(int corePoolSize) {
        super(corePoolSize);
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
       if(t instanceof Exception){
           log.error("In some thread error: ", t);
       }
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        ScheduledFuture<?> scheduledFuture = super.scheduleAtFixedRate(command, initialDelay, period, unit);
        if(command instanceof ScheludeRunnable) {
            ((ScheludeRunnable) command).setFuture(scheduledFuture);
        }
        return scheduledFuture;
    }

    abstract static class ScheludeRunnable implements Runnable{
        ScheduledFuture<?> myFuture;
        void setFuture(ScheduledFuture<?> future) {
            this.myFuture = future;
        }
    }
}
