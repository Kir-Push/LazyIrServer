package com.push.lazyir.service.tcp;

import com.push.lazyir.Loggout;
import com.push.lazyir.devices.Device;
import com.push.lazyir.devices.ModuleSetting;
import com.push.lazyir.devices.ModuleSettingList;
import com.push.lazyir.devices.NetworkPackage;
import com.push.lazyir.gui.GuiCommunicator;
import com.push.lazyir.modules.Module;
import com.push.lazyir.service.BackgroundService;

import javax.net.ssl.SSLSocket;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.push.lazyir.devices.NetworkPackage.N_OBJECT;
import static com.push.lazyir.service.TcpConnectionManager.*;

/* class represent thread & tcp connection for device
 listen for device commands and execute it's
 SENDING command to device, going in other thread's, but used this class instance*/
public class ConnectionThread implements Runnable {


        private volatile Socket connection;
        private String deviceId = null;
        private volatile boolean connectionRun;
        private volatile BufferedReader in = null;
        private volatile PrintWriter out = null;
        private ScheduledFuture<?> timerFuture;
        private Lock lock = new ReentrantLock();

        public ConnectionThread(Socket socket) throws SocketException {
            this.connection = socket;
            connection.setKeepAlive(true);
            connection.setSoTimeout(60000);
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
                sendIntroduce(); // send tcp introduce, which initiate on remote device newDevice method
                BackgroundService.getSettingManager().saveCache(connection.getInetAddress()); // save address to cache. Cache used when app start, to sending udp invitation's
                while (connectionRun)
                {
                    String clientCommand = in.readLine();
                    if(!BackgroundService.isServerOn() || clientCommand == null) { // if server off, exit from read loop
                        connectionRun = false;
                        continue;
                    }
                    NetworkPackage np =  NetworkPackage.Cacher.getOrCreatePackage(clientCommand); // create netwrokPackge from income message, actually it's parsing json
                    determineWhatTodo(np); // the name speaks for itself
                }
            }catch (IOException e)
            {
                connectionRun = false;
                Loggout.e("ConnectionThread " + deviceId,"Error in tcp out ",e);
            }
            finally {
                Device.getConnectedDevices().get(deviceId).closeConnection();  // clear resources, end modules and delete device from connected list
            }
        }

    private void configureSSLSocket() throws IOException
    {
        if(connection != null && connection instanceof SSLSocket) {
            ((SSLSocket)connection).setEnabledCipherSuites(((SSLSocket)connection).getSupportedCipherSuites());
            ((SSLSocket)connection).startHandshake();
        }
    }


    public void receivePairResult(NetworkPackage np)
    {
        receivePairResult( np.getId(),np.getValue("answer").equals("paired") ? OK : REFUSE,np.getData());
        BackgroundService.pairResultFromGui(np.getId(),OK,np.getData());
    }

    /* set device paired or not based on second arg
      inform gui about that
    * */
    public void receivePairResult(String id,String result,String data){
        lock.lock();
        try {
            Device device = Device.getConnectedDevices().get(id);
            if (result.equals(OK)) {
                BackgroundService.getSettingManager().saveValue(id, data);
                if (device != null)
                    device.setPaired(true);
                GuiCommunicator.devicePaired(id, true);
            } else {
                if (device != null)
                    device.setPaired(false);
                BackgroundService.getSettingManager().delete(id);
                GuiCommunicator.devicePaired(id, false);
            }
        }finally {
            lock.unlock();
        }
    }

     /*
     must important method based on package type determined what to do
     eat exception, we don't want to interrupt connection with device, so if something go wrong
     forgot, because if Device always send wrong command's TCP_PING won't be executed, and device
     will be disconnected on next pingCheck
     * */
        private void determineWhatTodo(NetworkPackage np)
        {
                String type = np.getType();
                if (deviceId == null && !type.equals(TCP_INTRODUCE)) {
                    return;
                }
                try {
                    switch (type) {
                        case TCP_INTRODUCE:
                            newConnectedDevice(np);
                            break;
                        case TCP_PING:
                            setDevicePing(deviceId, true);
                            break;
                        case TCP_PAIR:
                            pair(np);
                            break;
                        case TCP_UNPAIR:
                            unpair();
                            break;
                        case TCP_PAIR_RESULT:
                            receivePairResult(np);
                            break;
                        case ENABLED_MODULES:
                            receiveEnabledModules(np);
                            break;
                        default:
                            setDevicePing(deviceId, true); // most used case, order to specific modules
                            commandFromClient(np);                // modules may have specific values on json, on this stage we need to know only data & type values and id
                            break;
                    }

                } catch (Exception e) {
                    Loggout.e("ConnectionThread", "Error in DetermineWhatToDo ", e);
                }

        }

    /*
    receive list of enabledModules from Device
    * */
    private void receiveEnabledModules(NetworkPackage np) {
        ModuleSettingList object = np.getObject(N_OBJECT, ModuleSettingList.class);
        lock.lock();
        try {
        Device device = Device.getConnectedDevices().get(np.getId());
        if(device != null){
            device.refreshEnabledModules(object.getModuleSettingList());
        }
        }finally {
            lock.unlock();
        }
    }

    private void setDevicePing(String deviceId,boolean answer){
            lock.lock();
            try {
                Device device = Device.getConnectedDevices().get(deviceId);
                if (device != null)
                    device.setAnswer(answer);
            }finally {
                lock.unlock();
            }
    }

    private void sendIntroduce() {
        try {
            String temp =String.valueOf(InetAddress.getLocalHost().getHostName().hashCode());
            NetworkPackage networkPackage =  NetworkPackage.Cacher.getOrCreatePackage(TCP_INTRODUCE,temp);

            ModuleSettingList moduleSettingList = new ModuleSettingList();
            moduleSettingList.setModuleSettingList(new ArrayList<>(BackgroundService.getMyEnabledModules().values()));
            networkPackage.setObject(N_OBJECT,moduleSettingList);  // set myModuleConfig's to introduce package, android do the same

            printToOut(networkPackage.getMessage());
        } catch (UnknownHostException e) {
            Loggout.e("ConnectionThread","Send Introduce",e);
        }
    }

        private void newConnectedDevice(NetworkPackage np)
        {
            lock.lock();
            try {
                if (deviceId != null)
                    return;

                deviceId = np.getId();
                if (Device.getConnectedDevices().containsKey(deviceId) && Device.getConnectedDevices().get(deviceId).isConnected()) {
                  //  closeConnection();
                    return;
                }
                ModuleSettingList object = np.getObject(N_OBJECT, ModuleSettingList.class);
                Device device = new Device(deviceId, np.getName(), connection.getInetAddress(), this,object.getModuleSettingList());
                Device.getConnectedDevices().put(deviceId, device);
                String data = np.getData();
                String pair = BackgroundService.getSettingManager().get(deviceId);
                if (data != null && !data.equalsIgnoreCase("null") && data.equals(pair)) {
                    device.setPaired(true);
                    BackgroundService.pairResultFromGui(deviceId, OK, data);
                }else if(data == null || data.equals("nonPaired") || !data.equals(pair)){
                    unpair();
                    BackgroundService.pairResultFromGui(deviceId, REFUSE, data);
                }
                ping();
                pingCheck();
                GuiCommunicator.newDeviceConnected(device);
                if (device.isPaired()) {
                    GuiCommunicator.devicePaired(deviceId, true);
                }
            }finally {
                lock.unlock();
            }
        }


        public  void unpair() {
        lock.lock();
        try {
            Device device = Device.getConnectedDevices().get(deviceId);
            if(device != null)
            device.setPaired(false);
            BackgroundService.getSettingManager().delete(deviceId);
            GuiCommunicator.devicePaired(deviceId, false);
        }finally {
            lock.unlock();
        }
        }

        private void pair(NetworkPackage np) {
            reguestPair(np);
        }

      private void reguestPair(NetworkPackage np) {
        GuiCommunicator.requestPair(np);
    }

        private void pingCheck() {
            if(timerFuture == null || timerFuture.isDone() || timerFuture.isCancelled())
            timerFuture = BackgroundService.getTimerService().scheduleWithFixedDelay(()->{
                    Device device = Device.getConnectedDevices().get(deviceId);
                    if(device != null && device.isAnswer()) {
                        device.setAnswer(false);
                        ping();
                    }
            },0,20, TimeUnit.SECONDS);
        }

        private void ping() {
           printToOut(NetworkPackage.Cacher.getOrCreatePackage(TCP_PING,TCP_PING).getMessage());
        }

        public void commandFromClient(NetworkPackage np)
        {
            lock.lock();
            try {
                Device device = Device.getConnectedDevices().get(np.getId());
                if(device == null)
                    return;
                if (!device.isPaired()) {
                    return;
                }
                String moduleType = np.getType();
                ModuleSetting myModuleSetting = BackgroundService.getMyEnabledModules().get(moduleType);
                if(myModuleSetting == null || !myModuleSetting.isEnabled() || deviceInIgnore(device.getId(),myModuleSetting)){
                    return;
                }
                Module module = device.getEnabledModules().get(moduleType);
                if(module != null)
                module.execute(np);
            }catch (Exception e) {
                Loggout.e("ConnectionThread","commandFromClient",e);
            }finally {
                lock.unlock();
            }
        }

    private boolean deviceInIgnore(String id, ModuleSetting myModuleSetting) {
            for (String s : myModuleSetting.getIgnoredId()) {
                if(s.equals(id))
                    return !myModuleSetting.isWorkOnly(); // isWorkOnly change ignore list to white list, so if s equal device id - when isWorkOnly true - it has in white list and return false
            }                                             // if isWorkOnly false so it is ignore list so if s equals id we must return true
        return false;
    }

    public void printToOut(String message)
    {
        lock.lock();
        try{
            if(out == null) {
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
            return connection != null && out != null && in != null && connectionRun && connection.isConnected() && !connection.isClosed() && !connection.isInputShutdown() && !connection.isOutputShutdown();
        }finally {
            lock.unlock();
        }
    }

    public void closeConnection(Device device) {
        lock.lock();
        try {
            if (timerFuture != null && !timerFuture.isDone()) {
                timerFuture.cancel(true);
            }
            if(device != null) {
                ConcurrentHashMap<String, Module> enabledModules = device.getEnabledModules();
                if(enabledModules != null)
                enabledModules.values().forEach(Module::endWork);
            }
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

