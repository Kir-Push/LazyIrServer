package com.push.lazyir.modules.share;


import java.io.*;

public interface SftpServerProcess extends Runnable {
    @Override
    void run();
    String connect() throws IOException,InterruptedException;
    void stopProcess();
    boolean isRunning();
}
