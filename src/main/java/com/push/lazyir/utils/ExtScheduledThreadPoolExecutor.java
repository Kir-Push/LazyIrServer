package com.push.lazyir.utils;

import com.push.lazyir.Loggout;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Created by buhalo on 26.07.17.
 */
public class ExtScheduledThreadPoolExecutor extends ScheduledThreadPoolExecutor {
    public ExtScheduledThreadPoolExecutor(int corePoolSize) {
        super(corePoolSize);
    }

    public ExtScheduledThreadPoolExecutor(int corePoolSize, ThreadFactory threadFactory) {
        super(corePoolSize, threadFactory);
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
       if(t != null && t instanceof Exception){
           Loggout.e("ExtScheduledThreadPoolExecutor","In some thread error: ", (Exception) t);
       }
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        ScheduledFuture<?> scheduledFuture = super.scheduleAtFixedRate(command, initialDelay, period, unit);
        if(command instanceof ScheludeRunnable)
        {
            ((ScheludeRunnable) command).setFuture(scheduledFuture);
        }
        return scheduledFuture;
    }

    public static abstract class ScheludeRunnable implements Runnable{

        public void setFuture(ScheduledFuture<?> future) {
            this.myFuture = future;
        }

        public ScheduledFuture<?> myFuture;


    }
}
