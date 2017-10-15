package com.push.lazyir.modules.dbus.websocket;

//import org.glassfish.tyrus.server.Server;

import org.glassfish.tyrus.server.Server;


import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;


/**
 * Created by buhalo on 04.07.17.
 */
public class BrowserServer {
    private Server server;
    private volatile boolean running;
    private volatile int port = 11520;
    private String contextPath = "/lazyir";
    private ReentrantLock lock = new ReentrantLock();

    public void start() throws Exception
    {
        lock.lock();
        try {
        if(running)
            return;
        running = true;
            final Map<String, Object> serverProperties = new HashMap<String, Object>();
            serverProperties.put(Server.STATIC_CONTENT_ROOT, "./src/main/webapp");
            server = new Server("127.0.0.1", port, contextPath, serverProperties, PopupEndpoint.class);
            server.start();
        }catch (Exception e)
        {
            e.printStackTrace();
            stop();
        }finally {
            lock.unlock();
        }

    }

    public void stop()
    {
        lock.lock();
        try{
        running = false;
        server.stop();}
        finally {
            lock.unlock();
        }
    }

    public boolean isRunning() {
        return running;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

}
