package com.push.lazyir.managers.tcp;

import com.push.lazyir.Loggout;
import com.push.lazyir.devices.Device;
import com.push.lazyir.devices.NetworkPackage;
import com.push.lazyir.gui.Communicator;
import com.push.lazyir.modules.Module;
import com.push.lazyir.service.BackgroundService;

import javax.net.ssl.SSLSocket;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.push.lazyir.MainClass.timerService;
import static com.push.lazyir.managers.tcp.TcpConnectionManager.*;


public class ConnectionThread implements Runnable {

       private static  String pingPackage = new NetworkPackage(TCP_PING,TCP_PING).getMessage();

        private volatile Socket connection;
        private String deviceId = null;
        private volatile boolean connectionRun;
        private volatile BufferedReader in = null;
        private volatile PrintWriter out = null;
        private ScheduledFuture<?> timerFuture;
        private Lock lock = new ReentrantLock();

        ConnectionThread(Socket socket) throws SocketException {
            this.connection = socket;
            connection.setSoTimeout(25000);
        }

     ConnectionThread(){

    }

        @Override
        public void run() {
            try{
                configureSSLSocket();
                in = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));
                out = new PrintWriter(
                        new OutputStreamWriter(connection.getOutputStream()));
                connectionRun = true;
                sendIntroduce();
                BackgroundService.getSettingManager().saveCache(connection.getInetAddress());
                while (connectionRun)
                {
                    String clientCommand = in.readLine();
                    if(!BackgroundService.getTcp().isServerOn() || clientCommand == null) {
                        connectionRun = false;
                        continue;
                    }
                    NetworkPackage np = new NetworkPackage(clientCommand);
                    determineWhatTodo(np);
                }
            }catch (IOException e)
            {
                connectionRun = false;
                Loggout.e("ConnectionThread","Error in tcp out",e);
            }catch (Throwable e)
            {
                e.printStackTrace();
            }
            finally {
                closeConnection();
            }
        }

    private void configureSSLSocket() throws IOException
    {
        if(connection != null && connection instanceof SSLSocket)
        {
            ((SSLSocket)connection).setEnabledCipherSuites(((SSLSocket)connection).getSupportedCipherSuites());
            ((SSLSocket)connection).startHandshake();
        }
    }

        private void determineWhatTodo(NetworkPackage np)
        {

            String type = np.getType();
            if( deviceId == null && !type.equals(TCP_INTRODUCE))
            {
                return;
            }
            try {
                switch (type) {
                    case TCP_INTRODUCE:
                        newConnectedDevice(np);
                        break;
                    case TCP_PING:
                        Device.getConnectedDevices().get(deviceId).setAnswer(true);
                        break;
                    case TCP_PAIR:
                        pair(np);
                        break;
                    case TCP_UNPAIR:
                        unpair();
                        break;
                    default:
                        Device.getConnectedDevices().get(deviceId).setAnswer(true);
                        commandFromClient(np);
                        break;
                }

            }catch (Exception e)
            {
                e.printStackTrace();
                Loggout.e("ConnectionThread","Error in DetermineWhatToDo",e);
            }
        }

    private void sendIntroduce() {
        try {
            String temp =String.valueOf(InetAddress.getLocalHost().getHostName().hashCode());
            NetworkPackage networkPackage = new NetworkPackage(TCP_INTRODUCE,temp);
            printToOut(networkPackage.getMessage());
        } catch (UnknownHostException e) {
            Loggout.e("ConnectionThread","Send Introduce",e);
        }
    }

        private void newConnectedDevice(NetworkPackage np)
        {
            if(deviceId != null)
                return;

                deviceId = np.getId();
                if(Device.getConnectedDevices().containsKey(deviceId))
                    closeConnection();
                Device  device = new Device(deviceId, np.getName(), connection.getInetAddress(), this);
                Device.getConnectedDevices().put(deviceId, device);
            String data = np.getData();
            if(data != null && !data.equalsIgnoreCase("null") && data.equals( BackgroundService.getSettingManager().get(deviceId)))
                {
                    device.setPaired(true);
                    BackgroundService.getTcp().sendPairResult(deviceId,OK);
                }
                ping();
                pingCheck();
                Communicator.getInstance().newDeviceFound(device);
                if(device.isPaired())
                {
                    Communicator.getInstance().devicePaired(deviceId,true);
                }
        }


        private void unpair()
        {
            Device.getConnectedDevices().get(deviceId).setPaired(false);
            BackgroundService.getSettingManager().delete(deviceId);
            Communicator.getInstance().devicePaired(deviceId,false);
        }

        private void pair(NetworkPackage np)
        {
            BackgroundService.getTcp().reguestPair(np);
        }

        private void pingCheck()
        {
            if(timerFuture!=null && !timerFuture.isDone()) {
                timerFuture.cancel(true);
            }
            timerFuture = timerService.scheduleAtFixedRate(()->{
                    Device device = Device.getConnectedDevices().get(deviceId);
                    if(device != null && device.isAnswer())
                    {
                        device.setAnswer(false);
                        ping();

                    }
            },20,20, TimeUnit.SECONDS);
        }

        private void ping()
        {
           printToOut(pingPackage);
        }

        public void commandFromClient(NetworkPackage np)
        {
            try {
                Device device = Device.getConnectedDevices().get(np.getId());
                if (!device.isPaired()) {
                    return;
                }
                Module module = device.getEnabledModules().get(np.getType());
                module.execute(np);
            }catch (Exception e)
            {
                Loggout.e("ConnectionThread","commandFromClient",e);
            }
        }

    public void printToOut(String message)
    {
        lock.lock();
        try{
            if(out == null)
            {
                return;
            }
            out.println(message);
            out.flush();
        }finally {
            lock.unlock();
        }
    }

    public boolean isConnected() {
        lock.lock();
        try {
            return connection != null && out != null && in != null && connectionRun;
        }finally {
            lock.unlock();
        }
    }

    public void closeConnection() {
        lock.lock();
        try {
            if (timerFuture != null && !timerFuture.isDone()) {
                timerFuture.cancel(false);
            }
            Device.getConnectedDevices().get(deviceId).getEnabledModules().values().forEach(module -> module.endWork());
            Device.getConnectedDevices().remove(deviceId);
            Communicator.getInstance().deviceLost(deviceId);
            in.close();
            out.close();
            connection.close();
        }catch (Exception e) {Loggout.e("ConnectionThread","Error in stopped connection",e);}
        finally {
            lock.unlock();
            Loggout.d("ConnectionThread", deviceId + " - Stopped connection");}
    }
}

