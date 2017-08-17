package com.push.lazyir.managers.udp;



import com.push.lazyir.Loggout;
import com.push.lazyir.MainClass;
import com.push.lazyir.devices.Device;
import com.push.lazyir.devices.NetworkPackage;
import com.push.lazyir.gui.Communicator;
import com.push.lazyir.service.BackgroundService;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.push.lazyir.MainClass.executorService;

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

    public final static HashMap<String,Device> neighboors = new HashMap<>();
    private Lock lock = new ReentrantLock();

    public UdpBroadcastManager() {
    }

    public void configureManager()
    {
        try {
            socket = new DatagramSocket();
            socket.setReuseAddress(true);
        } catch (IOException e) {
            Loggout.e("Udp","Error in udp configure method");
        }
    }

    public void startUdpListener(int port)
    {
        lock.lock();
        try {
            if (listening) {
                Loggout.d("Udp", "listening already working");
                return;
            }
            try {
                server = new DatagramSocket(port);
                server.setReuseAddress(true);
            } catch (SocketException e) {
                Loggout.e("Udp", "Error in StartListener", e);
                return;
            }
            listening = true;
            executorService.submit(() -> {
                Loggout.d("Udp", "start listening");
                final int bufferSize = 1024 * 5;
                byte[] data = new byte[bufferSize];
                try {
                    while (listening) {
                        DatagramPacket packet = new DatagramPacket(data, bufferSize);
                        server.receive(packet);
                        broadcastReceived(packet);
                        data = new byte[bufferSize];
                    }
                } catch (Exception e) {
                    Loggout.e("Udp", "UdpReceive exception ", e);
                    Communicator.getInstance().iamCrushedUdpListen();
                } finally {
                    Loggout.d("Udp", "Stopping UDP listener");
                    stopUdpListener();
                }
            });
        }finally {
            lock.unlock();
        }
    }

    private void sendBroadcast(final String message, final int port)
    {
        lock.lock();
                try {
                    List<InetAddress> broadcastAddress = getBroadcastAddress();
                    for (InetAddress address : broadcastAddress) {
                        byte[] sendData = message.getBytes();
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, address, port);
                        Loggout.d("Udp", "Sending broadcast: " + message + " TO " + address);
                        socket.send(sendPacket);
                    }} catch(IOException e){
                        Loggout.e("Udp","sendBroadcast error",e);
                    }finally {
                        lock.unlock();
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




    private void broadcastReceived(DatagramPacket packet) throws UnknownHostException
    {
        String pck = new String(packet.getData(),packet.getOffset(),packet.getLength());
        NetworkPackage np = new NetworkPackage(pck);
        String myid = getMyId();
       if(np.getType().equals(BROADCAST_INTRODUCE) && !np.getId().equals(myid))
        {
            Loggout.d("Udp","Broadcast data received: " + pck);
            addToNeighboors(np);
            if(Device.getConnectedDevices().containsKey(np.getId()))
            {
                Loggout.d("Udp","Received package from existing conenction");
            }
            else
            {
                sendUdp(packet.getAddress(),5667);
            }
        }
    }

    private void sendUdp(InetAddress address, int port) {
        lock.lock();
        try {
            NetworkPackage np = new NetworkPackage(BROADCAST_INTRODUCE, BROADCAST_INTRODUCE_MSG);
            String fromTypeAndData = np.getMessage();
            if (fromTypeAndData == null)
                return;
            byte[] bytes = fromTypeAndData.getBytes();
            DatagramPacket dp = new DatagramPacket(bytes, bytes.length, address, port);
            Loggout.d("Udp", "send udp " + dp);
            try {
                socket.send(dp);
            } catch (IOException e) {
                Loggout.e("Udp", "Exception in sendUdp",e);
            }
        }finally {
            lock.unlock();
        }
    }

    public void addToNeighboors(NetworkPackage np)
    {
    }

    public String getMyId() throws UnknownHostException
    {
            return InetAddress.getLocalHost().getHostName();
    }


    public void stopUdpListener()
    {
        lock.lock();
        try {
            listening = false;
            if (server != null)
                server.close();
            server = null;
        }finally {
            lock.unlock();
        }
    }

    public void onNetworkChange(int port)
    {
        Loggout.d("Udp","OnNetworkChange method start");
        startUdpListener(port);
        sendBroadcast(BROADCAST_INTRODUCE_MSG,port);
    }


    public boolean isSending() {return sending;}

    public void startSending() {sending = true;}

    public void stopSending()
    {
        sending = false;
    }

    public int getSend_period() {
        return send_period;
    }

    public void setSend_period(int send_period) {
        this.send_period = send_period;
    }

    public void connectRecconect(String id)  {
        for (String allCachedThing : BackgroundService.getSettingManager().getAllCachedThings()) {
            try {
                sendUdp(InetAddress.getByName(allCachedThing),5667);
            } catch (UnknownHostException e) {
                Loggout.e("Udp","connect recconect error: " ,e);
            }
        }
    }
}
