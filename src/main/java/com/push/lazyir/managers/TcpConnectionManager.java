package com.push.lazyir.managers;


import com.push.lazyir.Loggout;
import com.push.lazyir.gui.Communicator;
import com.push.lazyir.modules.shareManager.ShareModule;
import com.push.lazyir.pojo.Command;
import com.push.lazyir.devices.Device;
import com.push.lazyir.devices.NetworkPackage;
import com.push.lazyir.modules.Module;
import com.push.lazyir.pojo.CommandsList;
import sun.nio.ch.Net;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import static com.push.lazyir.devices.NetworkPackage.N_OBJECT;

/**
 * Created by buhalo on 19.02.17.
 */

public class TcpConnectionManager {
    public final static String TCP_INTRODUCE = "tcpIntroduce";
    public final static  String TCP_INTRODUCE_MSG = "my name is";
    public final static String TCP_INTRODUCE_DATA = "mana data";
    public final static String TCP_PING = "ping pong";
    public final static String TCP_PAIR_RESULT = "pairedresult";
    public final static String RESULT = "result";
    public final static String OK = "ok";
    public final static String REFUSE = "refuse";
    public final static String TCP_PAIR = "pair";
    public final static String TCP_UNPAIR = "unpair";
    public final static String TCP_SYNC = "sync";

    private static TcpConnectionManager instance;
    public final static int port = 5667;

    ServerSocket myServerSocket;
    private static boolean ServerOn = false;

    private TcpConnectionManager() {
        try {
            myServerSocket = new ServerSocket(port);
        } catch (Exception e) {
            Loggout.e("Tcp",e.toString());
            Communicator.getInstance().iamCrushed();
            System.exit(-1);
        }
    }

    public static TcpConnectionManager getInstance()
    {
        if(instance == null)
        {
            instance = new TcpConnectionManager();
        }
        return instance;
    }


    public synchronized boolean sendCommandToServer(final String id, final String command)
    {
                Device device =  Device.connectedDevices.get(id);
                if(device != null)
                 {
                     device.setPaired(true);
                 }
                if(device == null || device.getSocket() == null || !device.getSocket().isConnected() || device.getSocket().isClosed())
                {
                    Loggout.d("Tcp","Error in output for socket");
                    StopListening(id);

                }
                if(!device.isPaired())
                {
                    Loggout.d("Tcp","Device is not paired and so on not allowed to continue");
                }
                try {

                    if(device.getOut() != null) {
                        device.getOut().println(command);
                        device.getOut().flush();
                        Loggout.d("Tcp", "Send command " + command);
                    }

                }catch (Exception e)
                {
                    Loggout.d("Tcp","Error in open output for socket " + e.toString());
                }
        return true;
    }



    public synchronized void sendSynchoCommand(String myid) {
        List<Command> allCommands = CommandManager.getInstance().getAllCommands();
        CommandsList commandsList = new CommandsList(allCommands);
        NetworkPackage np = new NetworkPackage(TCP_SYNC,TCP_SYNC);
        np.setObject(N_OBJECT,commandsList);
        String fromTypeAndData = np.getMessage();
        sendCommandToServer(myid,fromTypeAndData);

    }


    public void StopListening(String id) {
        if(!Device.connectedDevices.containsKey(id))
            return;
        Device closingDevice = Device.connectedDevices.get(id);
        if(closingDevice == null)
        {
            Device.connectedDevices.remove(id);
            return;
        }
        closingDevice.setListening(false);
        closingDevice.setPaired(false);
        try {
            Loggout.d("Tcp","Delete " + id + " connection");
            Device.connectedDevices.remove(id);
            closingDevice.getSocket().close();
        } catch (IOException e) {
            Loggout.e("Tcp","Error in closing listening");
        }
    }


    public void startListening(int port) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(ServerOn)
                {
                    Loggout.d("Tcp","Server already working");
                    return;
                }

                ServerOn = true;

                while(ServerOn)
                {
                    try {
                        Socket socket = myServerSocket.accept();
                        ConnectionThread connection = new ConnectionThread(socket);
                        connection.start();


                    } catch (IOException e) {
                        Loggout.e("Tpc","Exception on accept connection ignoring + " + e.toString());
                        if(myServerSocket.isClosed())
                        {
                            ServerOn = false;
                        }
                    }
                }
                try
                {
                    myServerSocket.close();
                    Loggout.d("Tcp","Closing server");
                }catch (IOException e)
                {
                    Loggout.e("Tcp","error in closing connecton");
                }
            }
        }).start();
    }

    public void sendCommandToAll(String message) {
        if(Device.getConnectedDevices().size() == 0)
        {
            return;
        }
        for (Device device : Device.getConnectedDevices().values()) {
            sendCommandToServer(device.getId(),message);
        }
    }

    /**
     * Created by buhalo on 12.03.17.
     */
    public class ConnectionThread extends Thread {

        private Socket connection;
        private String deviceId = null;
        private boolean connectionRun = true;
        BufferedReader in = null;
        PrintWriter out = null;
        java.util.Timer timer;

        public ConnectionThread(Socket socket) {
            this.connection = socket;
        }

        @Override
        public void run() {
            Loggout.d("Tcp","Start connecting to new connection");
            try{
                in = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));
                out = new PrintWriter(
                        new OutputStreamWriter(connection.getOutputStream()));
              sendIntroduce();
              SettingManager.getInstance().saveCache(connection.getInetAddress());
                while (connectionRun)
                {
                    String clientCommand = in.readLine();
                    Loggout.d("Tcp","Client says.. " + clientCommand);

                    if(!ServerOn) {
                        out.println("Server has already stopped");
                        out.flush();
                        connectionRun = false;
                    }

                    if(clientCommand == null)
                    {
                        connectionRun = false;
                        continue;
                    }
                    NetworkPackage np = new NetworkPackage(clientCommand);
                   // np.parsePackage(clientCommand);
                    determineWhatTodo(np);
                }

            }catch (IOException e)
            {
                Loggout.e("Tcp","Error in tcp out + " + e.toString());
            }
            finally {
                try {
                    timer.cancel();
                    timer.purge();
                    in.close();
                    out.close();
                    connection.close();
                    Loggout.d("Tcp","Stopped connection");
                    ShareModule module = (ShareModule) Device.getConnectedDevices().get(deviceId).getEnabledModules().get(ShareModule.SHARE_T);
                    module.stopSftpServer();
                    Device.getConnectedDevices().remove(deviceId);
                    Communicator.getInstance().deviceLost(deviceId);
                }catch (IOException e)
                {
                    Loggout.e("Tcp",e.toString());
                }
            }
        }


        private void sendIntroduce() {
            try {
                String temp =String.valueOf(InetAddress.getLocalHost().getHostName().hashCode());
                NetworkPackage networkPackage = new NetworkPackage(TCP_INTRODUCE,temp);
                out.println(networkPackage.getMessage());
                out.flush();
            } catch (UnknownHostException e) {
               Loggout.e("Tcp",e.toString());
            }
        }

        public void determineWhatTodo(NetworkPackage np)
        {

            if(!np.getType().equals(TCP_INTRODUCE) && deviceId == null)
            {
                return;
            }

            try {

                switch (np.getType()) {
                    case TCP_INTRODUCE:
                        newConnectedDevice(np);
                        break;
                    case TCP_PING:
                        Loggout.d("Tcp", "ccleint answer pong");
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
                Loggout.e("Tcp",e.toString());
            }
        }

        public void newConnectedDevice(NetworkPackage np)
        {

            if(deviceId == null) {
                Device  device = new Device(connection, np.getId(), np.getName(), connection.getInetAddress(), in, out);
                deviceId = np.getId();
                if(Device.getConnectedDevices().containsKey(deviceId))
                {
                    StopListening(deviceId);
                }
                Device.getConnectedDevices().put(device.getId(), device);
                if(np.getData() != null && np.getData().equals(SettingManager.getInstance().get(deviceId)))
                {
                    device.setPaired(true);
                    sendPairResult(deviceId,OK);
                }
                ping(np);
                pingCheck();
                Communicator.getInstance().newDeviceFound(device);
                if(device.isPaired())
                {
                    Communicator.getInstance().devicePaired(deviceId,true);
                }
            }
        }


        private void unpair()
        {
            Device.getConnectedDevices().get(deviceId).setPaired(false);
            SettingManager.getInstance().delete(deviceId);
            Communicator.getInstance().devicePaired(deviceId,false);
        }

        private void pair(NetworkPackage np)
        {
           reguestPair(np);
        }

        public void pingCheck()
        {
            TimerTask tt =  new TimerTask() {
                @Override
                public void run() {
                    Loggout.e("TcpPING","b");
                    if(Device.getConnectedDevices().get(deviceId) == null || !Device.getConnectedDevices().get(deviceId).isAnswer())
                    {
                        Loggout.e("Tcp","device not answered - closing connection");
                        try {
                            connectionRun = false;
                            connection.close();

                        } catch (IOException e) {
                            e.printStackTrace();
                        }finally {
                            timer.cancel();
                            timer.purge();
                        }

                    }else
                    {
                        Device.getConnectedDevices().get(deviceId).setAnswer(false);
                        ping(null);
                    }

                }
            };
            timer = new java.util.Timer();
            timer.schedule(tt,20000,20000); // maybe decrease time?
        }

        public void ping(NetworkPackage np)
        {
            NetworkPackage p = new NetworkPackage(TCP_PING,TCP_PING);
         //   Device.getConnectedDevices().get(deviceId).setAnswer(true);
              String msg = p.getMessage();
            Loggout.d("Tcp","Send " + msg);
              out.println(msg);
              out.flush();
        }

        public void commandFromClient(NetworkPackage np)
        {
            Module module = Device.getConnectedDevices().get(np.getId()).getEnabledModules().get(np.getType());
            module.execute(np);
        }
    }

    private void reguestPair(NetworkPackage np) {
            Communicator.getInstance().requestPair(np);
    }

    public void requestPairDevice(String id)
    {
        NetworkPackage np = new NetworkPackage(TCP_PAIR,"REQUEST");
        sendCommandToServer(id,np.getMessage());
    }

    public void sendPairResult(String id,String result) {
        try {
            String temp =String.valueOf(InetAddress.getLocalHost().getHostName().hashCode());
            NetworkPackage np = new NetworkPackage(TCP_PAIR_RESULT,temp);
            np.setValue(RESULT,result);
            String fromTypeAndData = np.getMessage();
            sendCommandToServer(id,fromTypeAndData);
            sendSynchoCommand(id);
            ShareModule.sendSetupServerCommand(id);
        } catch (UnknownHostException e) {
            Loggout.e("Tcp",e.toString());
        }

    }

    public void sendUnpair(String id)
    {
        NetworkPackage np = new NetworkPackage(TCP_UNPAIR,TCP_UNPAIR);
        sendCommandToServer(id,np.getMessage());
        Device.getConnectedDevices().get(id).setPaired(false);
        SettingManager.getInstance().delete(id);
        Communicator.getInstance().devicePaired(id,false);
    }


    public void pairResult(NetworkPackage np)
    {
        String id = np.getId();
        if(np.getValue("answer").equals("paired"))
        {
            String s = np.getData();
            SettingManager.getInstance().saveValue(id,s);
            Device.getConnectedDevices().get(id).setPaired(true);
            sendPairResult(id,OK);
            Communicator.getInstance().devicePaired(id,true);
        }
        else
        {
            Device.getConnectedDevices().get(id).setPaired(false);
            SettingManager.getInstance().delete(id);
            sendPairResult(id,REFUSE);
            Communicator.getInstance().devicePaired(id,false);
        }
    }


    public void startNewConenction(NetworkPackage np,InetAddress address)
    {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(address,port),10000);
            socket.setKeepAlive(true);
            ConnectionThread connection = new ConnectionThread(socket);
            connection.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
