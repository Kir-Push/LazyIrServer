package com.push.lazyir.managers;



import com.push.lazyir.Loggout;
import com.push.lazyir.MainClass;
import com.push.lazyir.devices.Device;
import com.push.lazyir.devices.NetworkPackage;
import com.push.lazyir.gui.Communicator;

import java.io.IOException;
import java.net.*;
import java.util.*;

/**
 * Created by buhalo on 19.02.17.
 */

public class UdpBroadcastManager  {

    private static final String BROADCAST_INTRODUCE = "broadcast introduce";
    private static final String BROADCAST_INTRODUCE_MSG = "I search Adventures";
    private DatagramSocket socket;
    private  DatagramSocket server;
    private int send_period = 30000;


    public static int port = 5667;
    private volatile static boolean listening = false;
    public volatile static boolean exitedFromSend = true;
    private volatile static boolean sending;
    private static UdpBroadcastManager instance;

    public final static HashMap<String,Device> neighboors = new HashMap<>();
    private InetAddress broadcastAddress;

    private UdpBroadcastManager() {
        try {
            configureManager();
        } catch (IOException e) {
            Loggout.e("Udp","Error in udp configure method");
        }
    }

    private void configureManager() throws IOException
    {
        socket = new DatagramSocket();
        socket.setReuseAddress(true);
    //    socket.setBroadcast(true);
    }


    public static UdpBroadcastManager getInstance()
    {
        if(instance == null)
        {
            instance = new UdpBroadcastManager();
        }
        return instance;
    }


    public void sendBroadcast(final String message, final int port)
    {
                try {
                    List<InetAddress> broadcastAddress = getBroadcastAddress();
                    for (InetAddress address : broadcastAddress) {
//                        this.broadcastAddress = address;
                        byte[] sendData = message.getBytes();
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, address, port);
                        Loggout.d("Udp", "Sending broadcast: " + message + " TO " + address);
                        socket.send(sendPacket);
                    }
                    } catch(IOException e){
                        Loggout.e("Udp", e.toString());
                    }

    }

    private List<InetAddress> getBroadcastAddress() throws IOException {
        List<InetAddress> list = new ArrayList<>();
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements())
        {
            NetworkInterface networkInterface = interfaces.nextElement();
            if (networkInterface.isLoopback())
                continue;
            for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses())
            {
                InetAddress broadcast = interfaceAddress.getBroadcast();
                if (broadcast == null)
                    continue;
                list.add(broadcast);
            }
        }
        return list;
    }

    public synchronized void startUdpListener(int port)
    {

        if(listening)
        {
            Loggout.d("Udp","listening already working");
            return;
        }

            try {
                server = new DatagramSocket(port);
                server.setReuseAddress(true);
            } catch (SocketException e) {
                Loggout.e("Udp",e.toString());
                return;
            }
            listening = true;
            new Thread(new Runnable() {
                @Override
                public void run() {

                    Loggout.d("Udp","start listening");
                    final int bufferSize = 1024 * 5;
                    byte[] data = new byte[bufferSize];
                    while (listening) {
                        DatagramPacket packet = new DatagramPacket(data, bufferSize);
                        try {
                            server.receive(packet);
                            broadcastReceived(packet);
                            data = new byte[bufferSize];
                        }
                        catch (Exception e) {
                            Loggout.e("Udp", "UdpReceive exception + " + e.toString());
                            listening = false;
                            Communicator.getInstance().iamCrushedUdpListen();
                            break;
                        }
                    }
                    Loggout.d("Udp", "Stopping UDP listener");
                    server.close();
                    server = null;
                    listening = false;
                }
            }).start();

    }



    public void broadcastReceived(DatagramPacket packet)
    {
        String pck = new String(packet.getData(),packet.getOffset(),packet.getLength());
        NetworkPackage np = new NetworkPackage(pck);
      //  np.parsePackage(pck);
        String myid = getMyId();
        if(np.getId().equals(myid))
        {
            return; // ignore my own packets
        }
        else if(np.getType().equals(BROADCAST_INTRODUCE))
        {
            Loggout.d("Udp","Broadcast data received: " + pck);

            addToNeighboors(np);

            if(Device.getConnectedDevices().containsKey(np.getId()))
            {
                Loggout.d("Udp","Received package from existing conenction");
                return;
            }
            else
            {
                sendUdp(packet.getAddress(),5667);
            }
        }
    }

    private void sendUdp(InetAddress address, int port) {
        NetworkPackage np = new NetworkPackage(BROADCAST_INTRODUCE, BROADCAST_INTRODUCE_MSG);
        String fromTypeAndData = np.getMessage();
        if(fromTypeAndData == null)
            return;
        byte[] bytes = fromTypeAndData.getBytes();
        DatagramPacket dp = new DatagramPacket(bytes,bytes.length,address,port);
        Loggout.d("Udp","send udp " + dp);
        try {
            socket.send(dp);
        } catch (IOException e) {
            Loggout.e("Udp",e.toString());
        }
    }

    public void addToNeighboors(NetworkPackage np)
    {
        if(!neighboors.containsKey(np.getId()))
        {

        }
    }

    public String getMyId()
    {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
           Loggout.e("Udp",e.toString());
          return null;
        }
    }


    public void stopUdpListener()
    {
        listening = false;
        if(server != null)
        server.close();
    }

    public void onNetworkChange(int port)
    {
        Loggout.d("Udp","OnNetworkChange method start");
        startUdpListener(port);
        sendBroadcast(BROADCAST_INTRODUCE_MSG,port);
    }


    public static boolean isSending() {return sending;}

    public static void startSending() {sending = true;}

    public static void stopSending()
    {
        sending = false;
    }

    public int getSend_period() {
        return send_period;
    }

    public void setSend_period(int send_period) {
        this.send_period = send_period;
    }

    public void connectRecconect(String id) {
        Set<String> allCachedThings = SettingManager.getInstance().getAllCachedThings();
        for (String allCachedThing : allCachedThings) {
            try {
                sendUdp(InetAddress.getByName(allCachedThing),5667);
            } catch (UnknownHostException e) {
                Loggout.d("Udp","connect recconect error: " + e.toString());
            }
        }


    }
}
