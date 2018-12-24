package com.push.lazyir.service.tcp;

import com.push.lazyir.api.MessageFactory;
import com.push.lazyir.api.NetworkPackage;
import com.push.lazyir.devices.*;
import com.push.lazyir.gui.GuiCommunicator;
import com.push.lazyir.modules.Module;
import com.push.lazyir.modules.ModuleFactory;
import com.push.lazyir.service.dto.TcpDto;
import com.push.lazyir.service.main.BackgroundService;
import com.push.lazyir.service.main.PairService;
import com.push.lazyir.service.main.TcpConnectionManager;
import com.push.lazyir.service.managers.settings.SettingManager;
import lombok.Setter;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLSocket;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static com.push.lazyir.service.main.TcpConnectionManager.*;
import static com.push.lazyir.service.main.TcpConnectionManager.api.INTRODUCE;

/* class represent thread & tcp connection for device
 listen for device commands and execute it's
 SENDING cmd to device, going in other thread's, but used this class instance*/
@Slf4j
public class ConnectionThread implements Runnable {

        private Socket connection;
        private String deviceId = "";
        @Setter
        private boolean connectionRun;
        private BufferedReader in;
        private PrintWriter out;
        private ScheduledFuture<?> timerFuture;
        private BackgroundService backgroundService;
        private SettingManager settingManager;
        private GuiCommunicator guiCommunicator;
        private ModuleFactory moduleFactory;
        private MessageFactory messageFactory;
        private PairService pairService;

        public ConnectionThread(Socket socket, BackgroundService backgroundService, SettingManager settingManager, GuiCommunicator guiCommunicator, MessageFactory messageFactory, ModuleFactory moduleFactory, PairService pairService) throws SocketException {
            this.connection = socket;
            this.backgroundService = backgroundService;
            this.settingManager = settingManager;
            this.guiCommunicator = guiCommunicator;
            this.messageFactory = messageFactory;
            this.moduleFactory = moduleFactory;
            this.pairService = pairService;
            configSocket();
        }

    private void configSocket() throws SocketException {
        connection.setKeepAlive(true);
        connection.setSoTimeout(60000);
    }

    private void configureSSLSocket() throws IOException {
        if(connection instanceof SSLSocket) {
            ((SSLSocket)connection).setEnabledCipherSuites(((SSLSocket)connection).getSupportedCipherSuites());
            ((SSLSocket)connection).startHandshake();
        }
    }

    @Override
    public void run() {
        try {
            configureSSLSocket();
            if(log.isDebugEnabled()) {
                log.debug(String.format("start thread %s device ip: %s", Thread.currentThread(), connection.getInetAddress()));
            }
        } catch (IOException e) {
            log.error("error in configureSSLSocket", e);
            clearResources();
            removeFromNeighbours();
            return;
        }
        try (BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
             PrintWriter output = new PrintWriter(new OutputStreamWriter(connection.getOutputStream()))) {
            in = input;
            out = output;
            setConnectionRun(true);
            sendIntroduce(); // send tcp introduce, which initiate on remote device newDevice method
            settingManager.saveCache(connection.getInetAddress()); // save address to cache. Cache used when app start, to sending udp invitation's
            while (isConnected()) {

                String clientCommand = in.readLine();
                if (!backgroundService.isServerOn() || clientCommand == null || !isConnected()) { // if server off, exit from read loop
                    return;
                }
                if(!"".equals(deviceId) && clientCommand.equalsIgnoreCase(api.PING.name())){
                    setDevicePing(true);
                    continue;
                }

                NetworkPackage networkPackage = messageFactory.parseMessage(clientCommand);  // create netwrokPackge from income message, actually it's parsing json
                determineWhatTodo(networkPackage); // the name speaks for itself
            }
        } catch (IOException e) {
            log.error("error in connectionThread: " + deviceId, e);
        } finally {
            setConnectionRun(false);
            closeConnection(backgroundService.getConnectedDevices().get(deviceId));   // clear resources, end modules and delete device from connected list
        }
    }

    private void sendIntroduce() {
        try {
            String data = String.valueOf(InetAddress.getLocalHost().getHostName().hashCode());
            TcpDto dto = new TcpDto(INTRODUCE.name(), data);
            dto.setModuleSettings(new ArrayList<>(backgroundService.getMyEnabledModules().values()));
            String message = messageFactory.createMessage(api.TCP.name(), false, dto);
            printToOut(message);
        } catch (UnknownHostException e) {
            log.error("error in sendIntroduce", e);
        }
    }
     /*
     most important method based on package type determined what to do
     eat exception, we don't want to interrupt connection with device, so if something go wrong
     forgot, because if Device always send wrong cmd's TCP_PING won't be executed, and device
     will be disconnected on next pingCheck
     * */
     private void determineWhatTodo(NetworkPackage np) {
         try {
             String type = np.getType();
             boolean module = np.isModule();
             if (module) {
                 setDevicePing(true); // most used case, order to specific modules
                 commandFromClient(np); // modules may have specific values on json, on this stage we need to know only data & type values and id
             } else if (type.equalsIgnoreCase(api.TCP.name())) {
                 receivedBaseCommand(np);
             }
         } catch (Exception e) {
             log.error("Error in DetermineWhatToDo  with package - " + np, e);
         }
     }

    private void receivedBaseCommand(NetworkPackage np) {
        TcpDto dto = (TcpDto) np.getData();
        api command = TcpConnectionManager.api.valueOf(dto.getCommand());
        // when deviceId null, first command need to be introduce
        if ("".equals(deviceId) && (!command.equals(INTRODUCE))) {
            return;
        }

        switch (command) {
            case INTRODUCE:
                newConnectedDevice(np);
                break;
            case PAIR:
                pairService.receivePairRequest(np);
                break;
            case UNPAIR:
                pairService.receivePairSignal(np);
                break;
            case PAIR_RESULT:
                pairService.receivePairSignal(np);
                break;
            case ENABLED_MODULES:
                receiveEnabledModules(np);
                break;
            default:
                setDevicePing(true); // don't know command, but still have signal from device
                break;
        }
    }

    private void newConnectedDevice(NetworkPackage np) {
        // if device not null - you already know about device, so no introduction
        if (!"".equals(deviceId)) {
            return;
        }
        TcpDto dto = (TcpDto) np.getData();
        deviceId = np.getId();

        Device dv = backgroundService.getConnectedDevices().get(deviceId);
        if (dv != null) {
            dv.closeConnection();
        }

        Device device = new Device(deviceId, np.getName(), connection.getInetAddress(), this, dto.getModuleSettings(), moduleFactory);
        device.enableModules();
        backgroundService.getConnectedDevices().put(deviceId, device);
        removeFromNeighbours();

        String pairData = dto.getData();
        String savedPairData = backgroundService.getSettingManager().get(deviceId);
        String pairState = (pairData != null && !pairData.equalsIgnoreCase("null") && pairData.equals(savedPairData)) ? api.OK.name() : api.REFUSE.name();
        pairService.sendPairAnswer(deviceId, pairState);

        ping();
        pingCheck();
        guiCommunicator.newDeviceConnected(device);
        if (device.isPaired()) {
            guiCommunicator.devicePaired(deviceId, true);
        }
    }

    private void ping() {
         printToOut(api.PING.name());
    }

    private void pingCheck() {
        if(timerFuture != null && (!timerFuture.isDone() || !timerFuture.isCancelled())){
            timerFuture.cancel(true);
        }
        timerFuture = backgroundService.getTimerService().scheduleWithFixedDelay(()->{
            ping();
            Device device = backgroundService.getConnectedDevices().get(deviceId);
            if(device != null && !device.isPinging()){
                closeConnection(device);
            }
            setDevicePing(false);
        },20,20, TimeUnit.SECONDS);
    }

    /*
    receive list of enabledModulesConfig from Device
    * */
    private void receiveEnabledModules(NetworkPackage np) {
        TcpDto dto = (TcpDto) np.getData();
        List<ModuleSetting> moduleSettings = dto.getModuleSettings();
        Device device = backgroundService.getConnectedDevices().get(deviceId);
        if(!"".equals(deviceId)) {
            device.refreshEnabledModules(moduleSettings);
        }
    }

    private void setDevicePing(boolean answer){
        Device device = backgroundService.getConnectedDevices().get(deviceId);
        if (device != null) {
            device.setPinging(answer);
        }
    }

    private void commandFromClient(NetworkPackage np) {
        try {
            if ("".equals(deviceId)) {
                return;
            }
            Device device = backgroundService.getConnectedDevices().get(deviceId);
            if ("".equals(deviceId) || !device.isPaired()) {
                return;
            }
            String moduleType = np.getType();
            ModuleSetting myModuleSetting = backgroundService.getMyEnabledModules().get(moduleType);
            if (myModuleSetting == null || !myModuleSetting.isEnabled() || deviceInIgnore(myModuleSetting)) {
                return;
            }
            Module module = device.getEnabledModules().get(moduleType);
            if (module != null) {
                module.execute(np);
            }
        } catch (Exception e) {
            log.error("error in commandFromClient", e);
        }
    }

    private boolean deviceInIgnore(ModuleSetting myModuleSetting) {
        for (String s : myModuleSetting.getIgnoredId()) {    // isWorkOnly change ignore list to white list, so if s equal device id - when isWorkOnly true - it has in white list and return false
            if (s.equals(deviceId)) {                       // if isWorkOnly false so it is ignore list so if s equals id we must return true
                return !myModuleSetting.isWorkOnly();
            }
        }
        return false;
    }

    @Synchronized
    public void printToOut(String message) {
        if (out == null) {
            return;
        }
        out.println(message);
        out.flush();
    }

    @Synchronized
    public boolean isConnected() {
        return connection != null && out != null && in != null
                && connectionRun && connection.isConnected() && !connection.isClosed()
                && !connection.isInputShutdown() && !connection.isOutputShutdown();
    }

    @Synchronized
    private void closeConnection(Device device) {
        try {
            if (timerFuture != null && !timerFuture.isDone()) {
                timerFuture.cancel(true);
            }
            clearResources();
            backgroundService.getConnectedDevices().remove(deviceId);
            if(!deviceId.equals("") && device != null) {
                ConcurrentHashMap<String, Module> enabledModules = device.getEnabledModules();
                if(enabledModules != null) {
                    enabledModules.values().forEach(this::tryEndModule);
                }
            }
        }catch (Exception e) {
            log.error("error in closeConnection id: " + deviceId,e);
        }
        finally {
            removeFromNeighbours();
            guiCommunicator.deviceLost(deviceId);
            if(log.isDebugEnabled()) {
                log.debug(String.format("%s stopped connection", deviceId));
            }
        }
    }

    private void tryEndModule(Module module){
        try {
            module.endWork();
        }catch (Exception e){
            log.error("error in endMethod id; " + deviceId,e);
        }
    }

    @Synchronized
    public void clearResources(){
        try {
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
            if (connection != null) {
                connection.close();
            }
        }catch (IOException e){
            log.error("error in clearResources",e);
        }
    }

    private void removeFromNeighbours(){
        if(connection != null) {
            backgroundService.getNeighbours().remove(connection.getInetAddress());
        }
    }
}

