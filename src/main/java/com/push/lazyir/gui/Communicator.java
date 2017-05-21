package com.push.lazyir.gui;

import com.push.lazyir.Loggout;
import com.push.lazyir.MainClass;
import com.push.lazyir.devices.Device;
import com.push.lazyir.devices.NetworkPackage;
import com.push.lazyir.managers.*;
import com.push.lazyir.modules.Module;
import com.push.lazyir.modules.notifications.*;
import com.push.lazyir.modules.shareManager.ShareModule;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.push.lazyir.modules.notifications.ShowNotification.SHOW_NOTIFICATION;

/**
 * Created by buhalo on 12.03.17.
 */
//i think communication will be like rest (not exactly same)
public class Communicator implements Runnable{

    public final static String BS = "/";
    // first blocks
    public static String POST = "post";
    public static String GET = "get";
    public static String DELETE = "delete";
    // second blocks
    public static String LOG = "log";
    public static String COMMAND = "command";
    public static String SETTING = "setting";
    public static String STATE = "state";
    // third blocks include id or object name
    // and state blocks
    public static String ALL = "all";
    public static String TRUE = "true";
    public static String FALSE = "false";

    private static Communicator instance;
    private BufferedReader in;
   // private BufferedWriter out;
    private String commandFromGui;
    private boolean listenInput;
    private Timer timer;
    private boolean answer = true;

    private static final String BATTERY = "battery";

    private static final String DEVICE = "Device";
    private static final String FOUND = "found";
    private static final String PAIRED = "paired";
    private static final String UNPAIRED = "unpaired";
    private static final String LOST = "lost";

    private static final String ALL_NOTIF = "All Notififications: ";
    private static final String MESSENGERS = "messengers";


    public static Communicator getInstance()
    {
        if(instance == null)
        {
            instance = new Communicator();
        }
        return instance;
    }

    private Communicator() {
       in = new BufferedReader(new InputStreamReader(System.in));
      setListenInput(true);
    }

    @Override
    public void run() {
        pingCheck();
        while(isListenInput()) {
            try {
                commandFromGui = in.readLine();
                if(commandFromGui == null)
                {
                    setListenInput(false);
                    break;
                }
                Loggout.e("COMMAND PARSER START  ",commandFromGui);
                commandParser(commandFromGui);
            } catch (Exception e) {
               Loggout.e("Error",e.toString());
            }
        }
    }

    public void pingCheck()
    {
        TimerTask tt =  new TimerTask() {
            @Override
            public void run() {
                if(answer)
                {
                    sendToOut("ping");
                    answer = false;
                }
                else
                {
                    timer.cancel();
                    timer.purge();
                    Loggout.e("Communicator","Ending app");
                    tryToEraseAllResource();
                    System.exit(0);
                }
            }
        };
        timer = new java.util.Timer();
        timer.schedule(tt,10000,15000);
    }

    public synchronized void sendToOut(String msg)
    {
        System.out.println(msg);
        System.out.flush();
    }

    private void tryToEraseAllResource() {
        for(Device device : Device.getConnectedDevices().values())
        {
            ShareModule module = (ShareModule) device.getEnabledModules().get(ShareModule.SHARE_T);
            module.stopSftpServer();
            try {
                device.getSocket().close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                device.getIn().close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
            device.getOut().close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void commandParser(String commandFromGui)
    {
        if(commandFromGui.equals("pong\n") || commandFromGui.equals("pong"))
        {
            answer = true;
            return;
        }
        else if(commandFromGui.startsWith(ALL_NOTIF))
        {
            String substring = commandFromGui.substring(ALL_NOTIF.length());
            NetworkPackage np = new NetworkPackage(substring);
            String id = np.getValue("id");
            ShowNotification module = (ShowNotification) Device.getConnectedDevices().get(id).getEnabledModules().get(ShowNotification.class.getSimpleName());
            module.requestNotificationsFromDevice();
        }
        Loggout.e("FromGUi: ",commandFromGui);
        NetworkPackage cmdAnswr = new NetworkPackage(commandFromGui);

        if(cmdAnswr.getData().equals("SmsAnswer"))
        {
            SmsModule module = (SmsModule) Device.getConnectedDevices().get(cmdAnswr.getId()).getEnabledModules().get(SmsModule.class.getSimpleName());
            module.send_sms(cmdAnswr.getName(),cmdAnswr.getValue("text"),cmdAnswr.getId());
        }
        else if(cmdAnswr.getData().equals("MsgAnswer"))
        {
            Messengers.sendAnswer(cmdAnswr.getValue("typeName"),cmdAnswr.getValue("text"),cmdAnswr.getId());
        }
        //todo
        else if(cmdAnswr.getData().equals("Sync_Commands"))
        {
            CommandManager commandManager = CommandManager.getInstance();
            commandManager.syncCommands();
        }
        else if(cmdAnswr.getData().equals("Connect"))
        {
            String id = cmdAnswr.getId();
            TcpConnectionManager.getInstance().StopListening(id);
            UdpBroadcastManager.getInstance().connectRecconect(id);

        }
        else if(cmdAnswr.getData().equals("Disconnect"))
        {
            String id = cmdAnswr.getId();
            TcpConnectionManager.getInstance().StopListening(id);

        } else if(cmdAnswr.getData().equals("ReconnectToSftp"))
        {
        //todo
        }
        else if(cmdAnswr.getData().equals("Unpair"))
        {
            TcpConnectionManager.getInstance().sendUnpair(cmdAnswr.getId());
        }
        else if(cmdAnswr.getData().equals("Pair"))
        {
            TcpConnectionManager.getInstance().requestPairDevice(cmdAnswr.getId());
        }
        else if(cmdAnswr.getData().equals("PairAnswr"))
        {
          TcpConnectionManager.getInstance().pairResult(cmdAnswr);
        }


    }

    public synchronized void requestPair(NetworkPackage np)
    {
       sendToOut(np.getMessage());
    }

    public synchronized void iamCrushed()
    {
        sendToOut("ERROR CRUSH TCP"); //todo implement in gui
    }

    public synchronized void iamCrushedUdpListen()
    {
        sendToOut("ERROR CRUSH UDP");
    }

    public synchronized void newDeviceFound(Device device)
    {
        NetworkPackage np = new NetworkPackage(DEVICE,FOUND);
        np.setValue("name",device.getName());
        np.setValue("id",device.getId());
        String message = np.getMessage();
        sendToOut(message);
    }

    public synchronized void batteryStatus(String percentage,String status,Device device)
    {
        NetworkPackage np = new NetworkPackage(DEVICE,BATTERY);
        np.setValue("name",device.getName());
        np.setValue("id",device.getId());
        np.setValue("battery",percentage);
        np.setValue("status",status);
        String message = np.getMessage();
        sendToOut(message);
    }

    public synchronized void deviceLost(String id)
    {
        NetworkPackage np = new NetworkPackage(DEVICE,LOST);
        np.setValue("id",id);
        String message = np.getMessage();
        sendToOut(message);
    }

    public synchronized void devicePaired(String id,boolean paired)
    {
        String pair;
        if(paired)
        {
            pair = PAIRED;
        }
        else
        {
            pair = UNPAIRED;
        }
        NetworkPackage np = new NetworkPackage(DEVICE,pair);
        np.setValue("id",id);
        String message = np.getMessage();
        sendToOut(message);
    }




    public boolean isListenInput() {
        return listenInput;
    }

    public void setListenInput(boolean listenInput) {
        this.listenInput = listenInput;
    }

}
