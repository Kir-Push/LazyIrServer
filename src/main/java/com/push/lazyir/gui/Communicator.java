package com.push.lazyir.gui;

import com.push.lazyir.Loggout;
import com.push.lazyir.devices.Device;
import com.push.lazyir.devices.NetworkPackage;
import com.push.lazyir.modules.notifications.*;
import com.push.lazyir.modules.share.ShareModule;
import com.push.lazyir.service.BackgroundService;
import com.push.lazyir.utils.ExtScheduledThreadPoolExecutor;

import java.io.*;
import java.util.concurrent.*;

import static com.push.lazyir.MainClass.timerService;

/**
 * Created by buhalo on 12.03.17.
 */
// todo rewrite to socket
//i think communication will be like rest (not exactly same)
public enum Communicator implements Runnable{

    INSTANCE;

    private BufferedReader in;
    private String commandFromGui;
    private boolean listenInput;
    private ScheduledFuture<?> timerFuture;
    private volatile boolean answer = true;
    private  String BATTERY = "battery";
    private String DEVICE = "Device";
    private  String FOUND = "found";
    private  String PAIRED = "paired";
    private  String UNPAIRED = "unpaired";
    private  String LOST = "lost";
    private  String ALL_NOTIF = "allNotifs";
    private  String MESSENGERS = "messengers";

    private final static String guiPing = "ping";


    public static Communicator getInstance()
    {
        return INSTANCE;
    }

    Communicator() {
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
            } catch (IOException e) {
               Loggout.e("Communicator","Error in run",e);
            }
        }
    }

    public void pingCheck()
    {
        if(timerFuture != null && !timerFuture.isDone())
        {
            return;
        }
         timerFuture = timerService.scheduleAtFixedRate(new ExtScheduledThreadPoolExecutor.ScheludeRunnable() {
                 @Override
                 public void run() {
                     if (answer) {
                         sendToOut(guiPing);
                         answer = false;
                     } else {
                         Loggout.e("Communicator", "Ending app");
                         myFuture.cancel(true);
                         //tryToEraseAllResource();
                         //System.exit(0);
                     }
                 }
        }, 10, 15, TimeUnit.SECONDS);

    }

    public synchronized void sendToOut(String msg)
    {
        System.out.println(msg);
        System.out.flush();
    }

    public static void tryToEraseAllResource() { //todo attention runtime.addhook in mainclass  mainmethod
        Device.getConnectedDevices().values().forEach(device -> device.closeConnection());
    }

    public synchronized void commandParser(String commandFromGui)
    {
        Loggout.e("FromGUi: ",commandFromGui);
        if(commandFromGui.equals("pong\n") || commandFromGui.equals("pong"))
        {
            answer = true;
            return;
        }
        NetworkPackage cmdAnswr = NetworkPackage.Cacher.getOrCreatePackage(commandFromGui);
        String data = cmdAnswr.getData();
        if(data.equals(ALL_NOTIF))
        {
            String id = cmdAnswr.getValue("id");
            ShowNotification module = (ShowNotification) Device.getConnectedDevices().get(id).getEnabledModules().get(ShowNotification.class.getSimpleName());
            module.requestNotificationsFromDevice();
        }
        else if(data.equals("SmsAnswer"))
        {
            SmsModule module = (SmsModule) Device.getConnectedDevices().get(cmdAnswr.getId()).getEnabledModules().get(SmsModule.class.getSimpleName());
            module.send_sms(cmdAnswr.getName(),cmdAnswr.getValue("text"),cmdAnswr.getId());
        }
        else if(data.equals("MsgAnswer"))
        {
            Messengers.sendAnswer(cmdAnswr.getValue("typeName"),cmdAnswr.getValue("text"),cmdAnswr.getId());
        }
        else if(data.equals("Connect"))
        {
            String id = cmdAnswr.getId();
            BackgroundService.getTcp().StopListening(id);
            BackgroundService.getUdp().connectRecconect(id);

        }
        else if(data.equals("Disconnect"))
        {
            String id = cmdAnswr.getId();
            BackgroundService.getTcp().StopListening(id);

        } else if(data.equals("ReconnectToSftp"))
        {
            String id = cmdAnswr.getId();
            Device device = Device.getConnectedDevices().get(id);
            if(device != null)
            {
                ((ShareModule)device.getEnabledModules().get(ShareModule.class.getSimpleName())).recconectToSftp(id);
            }
        }
        else if(data.equals("Unpair"))
        {
            BackgroundService.getTcp().sendUnpair(cmdAnswr.getId());
        }
        else if(data.equals("Pair"))
        {
            BackgroundService.getTcp().requestPairDevice(cmdAnswr.getId());
        }
        else if(cmdAnswr.getType().equals("PairAnswr"))
        {
          BackgroundService.getTcp().pairResult(cmdAnswr);
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

    public synchronized void sftpConnectResult(boolean result,String id) {
        NetworkPackage np =  NetworkPackage.Cacher.getOrCreatePackage("Sftp","Result");
        np.setValue("id",id);
        np.setValue("result",String.valueOf(result));
        sendToOut(np.getMessage());}

    public synchronized void newDeviceFound(Device device)
    {
        NetworkPackage np = NetworkPackage.Cacher.getOrCreatePackage(DEVICE,FOUND);
        np.setValue("name",device.getName());
        np.setValue("id",device.getId());
        String message = np.getMessage();
        sendToOut(message);
    }

    public synchronized void batteryStatus(String percentage,String status,Device device)
    {
        NetworkPackage np = NetworkPackage.Cacher.getOrCreatePackage(DEVICE,BATTERY);
        np.setValue("name",device.getName());
        np.setValue("id",device.getId());
        np.setValue("battery",percentage);
        np.setValue("status",status);
        String message = np.getMessage();
        sendToOut(message);
    }

    public synchronized void deviceLost(String id)
    {
        NetworkPackage np = NetworkPackage.Cacher.getOrCreatePackage(DEVICE,LOST);
        np.setValue("id",id);
        String message = np.getMessage();
        sendToOut(message);
    }

    public synchronized void devicePaired(String id,boolean paired)
    {
        String pair;
        if(paired)
            pair = PAIRED;
        else
            pair = UNPAIRED;
        NetworkPackage np = NetworkPackage.Cacher.getOrCreatePackage(DEVICE,pair);;
        np.setValue("id",id);
        sendToOut(np.getMessage());
    }




    public boolean isListenInput() {
        return listenInput;
    }

    public void setListenInput(boolean listenInput) {
        this.listenInput = listenInput;
    }

}
