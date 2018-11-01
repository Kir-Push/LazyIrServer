package com.push.lazyir.service.main;

import com.push.lazyir.api.MessageFactory;
import com.push.lazyir.gui.GuiCommunicator;
import com.push.lazyir.modules.ModuleFactory;
import com.push.lazyir.devices.Device;
import com.push.lazyir.service.tcp.ConnectionThread;
import com.push.lazyir.utils.exceptions.CreateSSLException;
import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.*;
import java.io.*;
import java.net.*;
import java.security.*;
import java.security.cert.CertificateException;

@Slf4j
public class TcpConnectionManager {
    public enum api {
        TCP,
        INTRODUCE,
        PING,
        PAIR_RESULT,
        RESULT,
        OK,
        REFUSE,
        PAIR,
        UNPAIR,
        SYNC,
        ENABLED_MODULES
    }
    private ServerSocket myServerSocket;
    private BackgroundService backgroundService;
    private GuiCommunicator guiCommunicator;
    private MessageFactory messageFactory;
    private ModuleFactory moduleFactory;
    private PairService pairService;

    @Getter @Setter
    private boolean serverOn;

    TcpConnectionManager(BackgroundService backgroundService, GuiCommunicator guiCommunicator, MessageFactory messageFactory, ModuleFactory moduleFactory,PairService pairService) {
        this.backgroundService = backgroundService;
        this.guiCommunicator = guiCommunicator;
        this.messageFactory = messageFactory;
        this.moduleFactory = moduleFactory;
        this.pairService = pairService;
    }

    private SSLContext createSSLContext(){
        try{
            KeyStore keyStore = KeyStore.getInstance("jks");
            ClassLoader classLoader =getClass().getClassLoader();
            URL bimka = classLoader.getResource("bimka");
            if(bimka == null) {
                return null;
            }
            char[] chars = "bimkaSamokat".toCharArray();
            @Cleanup InputStream inputStream = bimka.openStream();
            keyStore.load(inputStream, chars);
            // Create key manager
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(keyStore, chars);
            KeyManager[] km = keyManagerFactory.getKeyManagers();
            // Create trust manager
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
            TrustManager[] tm = trustManagerFactory.getTrustManagers();
            // Initialize SSLContext
             SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
             sslContext.init(km  ,  tm, null);
            return sslContext;
        } catch (IOException | KeyManagementException | NoSuchAlgorithmException | KeyStoreException | UnrecoverableKeyException | CertificateException e){
            log.error("error while creating sslContext",e);
        }
        return null;
    }


    void startServer() {
        int port = Integer.parseInt(backgroundService.getSettingManager().get("TCP-port"));
        boolean tls = Boolean.parseBoolean(backgroundService.getSettingManager().get("TLS"));
        try {
            if (tls) {
                myServerSocket = startSSL(port);
            }
            if (!tls || myServerSocket == null) {
                myServerSocket = new ServerSocket(port);
            }
        } catch (IOException e) {
            log.error("startServer with port " + port + " failed ", e);
            backgroundService.crushed("startServer");
        }
        // you need clear folder when server created, because you may start multiple instances of app
        // and you clear folder of other instance
        // after server created(using one port) you know that only one instance running.
        backgroundService.clearTempFolders();
    }

    private ServerSocket startSSL(int port){
        try {
            SSLContext sslContext = createSSLContext();
            if (sslContext == null) {
                throw new CreateSSLException("sslContext is null");
            }
            SSLServerSocketFactory sslServerSocketFactory = sslContext.getServerSocketFactory();
            if (sslServerSocketFactory == null) {
                throw new CreateSSLException("sslServerSocketFactory is null");
            }
            return sslServerSocketFactory.createServerSocket(port);
        }catch (IOException | CreateSSLException e) {
            log.error("startServer with port " + port + " failed ", e);
            return null;
        }
    }



    void stopListening(Device dv) {
        if(dv != null){
            dv.closeConnection();
        }
    }

    void startListening() {
        backgroundService.submitNewTask(() -> {
            if (isServerOn()) {
                log.info("startListening - Server already working");
                return;
            }
            setServerOn(true);
            while (isServerOn()) {
                try {
                    Socket socket = myServerSocket.accept();
                    InetAddress inetAddress = socket.getInetAddress();
                    if(!backgroundService.getNeighbours().contains(inetAddress)) {
                        backgroundService.getNeighbours().add(inetAddress);
                        ConnectionThread connection = new ConnectionThread(socket, backgroundService, backgroundService.getSettingManager(), guiCommunicator, messageFactory, moduleFactory, pairService);
                        backgroundService.submitNewTask((connection));
                    }else{
                        socket.close();
                    }
                } catch (IOException e) {
                    log.error("exception in accept connection - ignoring", e);
                    if (myServerSocket.isClosed()) {
                        setServerOn(false);
                    }
                }
            }
            closeServer();
        });
    }

    private void closeServer(){
        try {
            setServerOn(false);
            guiCommunicator.tcpClosed();
            myServerSocket.close();
            log.info("server closed");
        }catch (IOException e) {
            log.error("error in closeServer",e);
        }
    }

}
