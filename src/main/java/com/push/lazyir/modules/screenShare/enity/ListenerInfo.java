package com.push.lazyir.modules.screenShare.enity;

import com.push.lazyir.utils.SettableFuture;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;

public class ListenerInfo {
    BufferedReader in;
    DataOutputStream out;
    Socket socket;
    boolean firstFrame;
    BlockingQueue<Integer> queue;

    public ListenerInfo(BufferedReader in, DataOutputStream out, Socket socket, boolean firstFrame, BlockingQueue<Integer> queue) {
        this.in = in;
        this.out = out;
        this.socket = socket;
        this.firstFrame = firstFrame;
        this.queue = queue;
    }


    public BufferedReader getIn() {
        return in;
    }

    public void setIn(BufferedReader in) {
        this.in = in;
    }

    public DataOutputStream getOut() {
        return out;
    }

    public void setOut(DataOutputStream out) {
        this.out = out;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public boolean isFirstFrame() {
        return firstFrame;
    }

    public void setFirstFrame(boolean firstFrame) {
        this.firstFrame = firstFrame;
    }


    public BlockingQueue<Integer> getBlockingQueue() {
        return queue;
    }

    public void setBlockingQueue(BlockingQueue<Integer> queue) {
        this.queue = queue;
    }
}
