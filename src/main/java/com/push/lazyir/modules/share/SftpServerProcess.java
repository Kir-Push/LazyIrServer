package com.push.lazyir.modules.share;

import com.push.lazyir.Loggout;

import java.io.*;
import java.util.concurrent.TimeUnit;

public interface SftpServerProcess extends Runnable {
    @Override
    void run();

    String connect() throws IOException,InterruptedException;

    void stopProcess();

    boolean isRunning();
}
