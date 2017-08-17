package com.push.lazyir.managers.tcp;


import com.push.lazyir.Loggout;
import com.push.lazyir.gui.Communicator;
import com.push.lazyir.modules.share.ShareModule;
import com.push.lazyir.pojo.Command;
import com.push.lazyir.devices.Device;
import com.push.lazyir.devices.NetworkPackage;
import com.push.lazyir.modules.Module;
import com.push.lazyir.pojo.CommandsList;
import com.push.lazyir.service.BackgroundService;
import com.push.lazyir.utils.ExtScheduledThreadPoolExecutor;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static com.push.lazyir.MainClass.executorService;
import static com.push.lazyir.MainClass.timerService;
import static com.push.lazyir.devices.NetworkPackage.N_OBJECT;

/**
 * Created by buhalo on 19.02.17.
 */

public class TcpConnectionManager {
    final static String TCP_INTRODUCE = "tcpIntroduce";
    final static String TCP_PING = "ping pong";
    final static String TCP_PAIR_RESULT = "pairedresult";
    final static String RESULT = "result";
    final static String OK = "ok";
    final static String REFUSE = "refuse";
    final static String TCP_PAIR = "pair";
    final static String TCP_UNPAIR = "unpair";
    final static String TCP_SYNC = "sync";

    private int port = 5667;

    private ServerSocket myServerSocket;

    public boolean isServerOn() {
        return ServerOn;
    }

    private volatile boolean ServerOn = false;

    public TcpConnectionManager() {
    }

    public void startServer() //todo think about network(not wifi) connections, shout it work? and about security problems with it
    {
        boolean trying = true;
        int firstTryPort = port;
        while(trying && firstTryPort < 5777) { //todo внимание проверь остальные порты чтоб не пересекались
            try {
                myServerSocket = new ServerSocket(firstTryPort);
                this.port = firstTryPort;
                trying = false;
            } catch (Exception e) {
                Loggout.e("Tcp", "startServer with port " + firstTryPort + " failed",e);
                firstTryPort++;
                if(firstTryPort >= 5777)
                {
                    Communicator.getInstance().iamCrushed();
                    System.exit(-1);
                }
            }
        }
    }

    public synchronized boolean sendCommandToServer(final String id, final String command)
    {
                Device device =  Device.connectedDevices.get(id);
                if(device == null || !device.isConnected())
                {
                    Loggout.d("Tcp","Error in output for jasechsocket");
                    StopListening(id);
                    return false;
                }
                if(!device.isPaired())
                {
                    Loggout.d("Tcp","Device is not paired and so on not allowed to continue");
                    return false;
                }
                device.printToOut(command);
                Loggout.d("Tcp", "Send command " + command);
                return true;
    }


    public void StopListening(String id) {
        Device closingDevice = Device.connectedDevices.get(id);
        if(closingDevice == null)
        return;
        closingDevice.closeConnection();
    }

    public void startListening() {
        executorService.submit(() -> {
            if(isServerOn()) {
                Loggout.d("Tcp","Server already working");
                return;
            }
            ServerOn = true;
            while(isServerOn())
            {
                try {
                Socket socket = myServerSocket.accept();
                executorService.submit(new ConnectionThread(socket));
            } catch (IOException e) {
                    Loggout.e("Tpc","Exception on accept connection ignoring +",e);
                    if(myServerSocket.isClosed())
                        ServerOn = false;
            }}try {
                myServerSocket.close();
                Loggout.d("Tcp","Closing server");
            }catch (IOException e) {
                Loggout.e("Tcp","error in closing server",e);
            }
        });
    }

    public void sendCommandToAll(String message) {
        Device.getConnectedDevices().values().forEach(device -> sendCommandToServer(device.getId(),message));
    }

    void reguestPair(NetworkPackage np) {
            Communicator.getInstance().requestPair(np);
    }

    public void requestPairDevice(String id)
    {
        NetworkPackage np = new NetworkPackage(TCP_PAIR,"REQUEST");
        sendCommandToServer(id,np.getMessage());
    }

    void sendPairResult(String id,String result) {
        try {
            NetworkPackage np = new NetworkPackage(TCP_PAIR_RESULT,String.valueOf(InetAddress.getLocalHost().getHostName().hashCode()));
            np.setValue(RESULT,result);
            sendCommandToServer(id,np.getMessage());
            if(result.equals(OK))
            ShareModule.sendSetupServerCommand(id);
        } catch (UnknownHostException e) {
            Loggout.e("Tcp","sendPairResult",e);
        }

    }

    public void sendUnpair(String id)
    {
        sendCommandToServer(id,new NetworkPackage(TCP_UNPAIR,TCP_UNPAIR).getMessage());
        Device.getConnectedDevices().get(id).setPaired(false);
        BackgroundService.getSettingManager().delete(id);
        Communicator.getInstance().devicePaired(id,false);
    }


    public void pairResult(NetworkPackage np)
    {
        String id = np.getId();
        if(np.getValue("answer").equals("paired"))
        {
            BackgroundService.getSettingManager().saveValue(id,np.getData());
            Device.getConnectedDevices().get(id).setPaired(true);
            sendPairResult(id,OK);
            Communicator.getInstance().devicePaired(id,true);
        }
        else
        {
            Device.getConnectedDevices().get(id).setPaired(false);
            BackgroundService.getSettingManager().delete(id);
            sendPairResult(id,REFUSE);
            Communicator.getInstance().devicePaired(id,false);
        }
    }


//    public void startNewConenction(NetworkPackage np,InetAddress address)
//    {
//        try {
//            Socket socket = new Socket();
//            socket.setSoTimeout(20000);
//            socket.setKeepAlive(true);
//            socket.connect(new InetSocketAddress(address,port),10000);
//            ConnectionThread connection = new ConnectionThread(socket);
//        //    connection.start();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}
