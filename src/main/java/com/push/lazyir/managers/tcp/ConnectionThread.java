package com.push.lazyir.managers.tcp;

import com.push.lazyir.Loggout;
import com.push.lazyir.devices.Device;
import com.push.lazyir.devices.NetworkPackage;
import com.push.lazyir.gui.Communicator;
import com.push.lazyir.gui.GuiCommunicator;
import com.push.lazyir.modules.Module;
import com.push.lazyir.service.BackgroundService;

import javax.net.ssl.SSLSocket;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.push.lazyir.MainClass.timerService;
import static com.push.lazyir.managers.tcp.TcpConnectionManager.*;


public class ConnectionThread implements Runnable {


        private volatile Socket connection;
        private String deviceId = null;
        private volatile boolean connectionRun;
        private volatile BufferedReader in = null;
        private volatile PrintWriter out = null;
        private ScheduledFuture<?> timerFuture;
        private Lock lock = new ReentrantLock();

        ConnectionThread(Socket socket) throws SocketException {
            this.connection = socket;
            connection.setKeepAlive(true);
            connection.setSoTimeout(60000);
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
                    NetworkPackage np =  NetworkPackage.Cacher.getOrCreatePackage(clientCommand);
                    determineWhatTodo(np);
                }
            }catch (IOException e)
            {
                connectionRun = false;
                Loggout.e("ConnectionThread","Error in tcp out",e);
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
            NetworkPackage networkPackage =  NetworkPackage.Cacher.getOrCreatePackage(TCP_INTRODUCE,temp);
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
                GuiCommunicator.newDeviceConnected(device);
                if(device.isPaired())
                {
                    GuiCommunicator.devicePaired(deviceId,true);
                }
        }


        private void unpair()
        {
            Device.getConnectedDevices().get(deviceId).setPaired(false);
            BackgroundService.getSettingManager().delete(deviceId);
            GuiCommunicator.devicePaired(deviceId,false);
        }

        private void pair(NetworkPackage np)
        {
            BackgroundService.getTcp().reguestPair(np);
        }

        private void pingCheck()
        {
//            if(timerFuture!=null && !timerFuture.isDone()) {
//                timerFuture.cancel(true);
//            }
            // start only if timer future null or not running
            if(timerFuture == null || timerFuture.isDone() || timerFuture.isCancelled())
            timerFuture = timerService.scheduleWithFixedDelay(()->{
                    Device device = Device.getConnectedDevices().get(deviceId);
                    if(device != null && device.isAnswer())
                    {
                        device.setAnswer(false);
                        ping();

                    }
            },0,20, TimeUnit.SECONDS);
        }

        private void ping()
        {
            System.out.println("PING to" + deviceId  + NetworkPackage.Cacher.getOrCreatePackage(TCP_PING,TCP_PING).getMessage());
           printToOut(NetworkPackage.Cacher.getOrCreatePackage(TCP_PING,TCP_PING).getMessage());
        }

        public void commandFromClient(NetworkPackage np)
        {
            try {
                Device device = Device.getConnectedDevices().get(np.getId());
                if (!device.isPaired()) {
                    return;
                }
                Module module = device.getEnabledModules().get(np.getType());
                if(module != null)
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
                timerFuture.cancel(true);
            }
            Device.getConnectedDevices().get(deviceId).getEnabledModules().values().forEach(module ->  module.endWork()); // todo something here nullpointer exception!
            Device.getConnectedDevices().remove(deviceId);
            GuiCommunicator.deviceLost(deviceId);
            // calling after because can throw exception and remove from hashmap won't be done
            in.close();
            out.close();
            connection.close();
        }catch (Exception e) {Loggout.e("ConnectionThread","Error in stopped connection",e);}
        finally {
            lock.unlock();
            Loggout.d("ConnectionThread", deviceId + " - Stopped connection");}
    }
}

