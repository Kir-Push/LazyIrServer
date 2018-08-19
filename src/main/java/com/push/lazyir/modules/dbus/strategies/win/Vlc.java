package com.push.lazyir.modules.dbus.strategies.win;


import com.push.lazyir.Loggout;
import com.push.lazyir.modules.dbus.Player;
import com.push.lazyir.modules.dbus.Players;
import com.push.lazyir.service.main.BackgroundService;
import com.push.lazyir.service.managers.settings.SettingManager;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by buhalo on 31.07.17.
 */
public class Vlc implements Strategy {
    private static int port = 9090;
    private static String passEncoded;
    private Socket socket;
    SAXParser saxParser;
    PlayerHandler handler;
    PrintWriter out;
    BufferedReader in;
    private volatile boolean instantiate = false;
    private AtomicInteger countInit = new AtomicInteger(0);
    private ReentrantLock lock = new ReentrantLock();
    private SettingManager settingManager;



    public Vlc(SettingManager settingManager) {
        try {
            this.settingManager = settingManager;
            initParameted();
            saxParser =   SAXParserFactory.newInstance().newSAXParser();
            handler = new PlayerHandler();
        }  catch (SAXException | ParserConfigurationException e) {
            Loggout.e("VlcStrategy","Error in vlc constructor",e);
        }
    }

    public boolean initiate() {
        lock.lock();
        try {
            if (instantiate && socket!= null)
                return true;
            tryToEraseConnections();
            socket = new Socket(InetAddress.getByName("127.0.0.1"),port);
            socket.setSoTimeout(50);
            out = new PrintWriter(socket.getOutputStream());
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            return true;
        }catch (IOException e)
        {
            tryToEraseConnections();
            Loggout.e("VlcStrategy","Error in initiate",e);
            return false;
        }finally {
            lock.unlock();
        }
    }

    private void tryToEraseConnections() {
        lock.lock();
        try{
        if(socket != null)
        {
            socket.close();
            socket = null;
        }
        if(out != null)
        {
            out.close();
            out = null;
        }
        if(in != null)
        {
            in.close();
            in = null;
        }
        }catch (IOException e)
        {
            Loggout.e("VlcStrategy","error in tryEraseconnection",e);
        }
        finally {
            lock.unlock();
        }
    }

    @Override
    public boolean Tryinitiate() {
        lock.lock();
        try {
            int i = countInit.get();
            if (i >= 20 || i == 0) {
                if(i == 0)
                    countInit.incrementAndGet();
                return initiate();
            }
            countInit.incrementAndGet();
        }finally {
            lock.unlock();
        }
        return false;
    }

    @Override
    public void stopConnection() {
        tryToEraseConnections();
    }

    @Override
    public boolean checkStatus() {
        return (socket != null && socket.isConnected());
    }

    @Override
    public Players getGetAll() { // yes it's stupid,
        List<Player> playerList = new ArrayList<>();
        Player onePlayer = getOnePlayer();
        System.out.println(onePlayer);
        playerList.add(onePlayer);
        return new Players(playerList);
    }

    @Override
    public Player getOnePlayer() {
        lock.lock();
        try {
        sendGet("requests/status.xml");
        String line = getLine();
        if(line.length() == 0)
            return null;
            System.out.println(line);
        InputSource inputSource = new InputSource(new StringReader(line.substring(line.indexOf("<?xml version="))));
            saxParser.parse(inputSource,handler);
            return handler.isEnded() ? handler.getPlayer() :null;
        }  catch (SAXException |  IOException e) {
            Loggout.e("VlcStrategy","getOneplayer error",e);
        }finally {
            lock.unlock();
        }
        return null;
    }

    @Override
    public void seek(String player, String arg) {
        String command = "requests/status.xml?command=seek&val=";
       if(arg.charAt(0) != '-')
           command += "+" + arg;
        else
            command += arg;
        sendGet(command);
    }
    @Override
    public void next(String player) {
        sendGet("requests/status.xml?command=pl_next");
    }

    @Override
    public void previous(String player) {
        sendGet("requests/status.xml?command=pl_previous");
    }

    //  http://127.0.0.1:9090/requests/status.xml?command=pl_stop
    @Override
    public void stop(String player) {
        sendGet("requests/status.xml?command=pl_stop");
    }

    @Override
    public void playPause(String player) {
        sendGet("requests/status.xml?command=pl_pause");
    }

    @Override
    public void openUri(String player, String uri) {
        sendGet("requests/status.xml?command=in_play&input="+uri);
    }

    @Override
    public void setPosition(String player, String path, String position) {
        sendGet("requests/status.xml?command=seek&val="+position);
    }

    @Override
    public void setVolume(String player, String value) {
        double v = Double.parseDouble(value) * 256;
        sendGet("requests/status.xml?command=volume&val="+String.valueOf(v));
    }

    @Override
    public double getVolume(String player) {
        return 0;
    }

    @Override
    public String getMetadata(String player) {
        return null;
    }

    @Override
    public String getPlaybackstatus(String player) {
        return null;
    }

    @Override
    public double getPosition(String player) {
        return 0;
    }

    @Override
    public String getPlayerName() {
        return null;
    }

    private void parseXml(String xml)
    {

    }

    private void initParameted()
    {
       String vlcPass = settingManager.get("Vlc-pass");
        port = Integer.parseInt(settingManager.get("Vlc-port"));
        passEncoded =  (Base64.getUrlEncoder().encodeToString((":" + vlcPass).getBytes()));
    }

    private void sendGet(String command)
    {
        if(!checkStatus())
        {
            return;
        }
        lock.lock();
        try {
            String getCmd = "GET /%s HTTP/1.1";
            String format = String.format(getCmd, command);
            out.println(format);
            out.println("\"Host: 127.0.0.1:" + port);
            out.println("Connection: keep-alive\nCache-Control: max-age=0");
            out.println("Authorization: Basic " + passEncoded);
            out.println("Upgrade-Insecure-Requests: 1\n" +
                    "User-Agent: Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.78 Safari/537.36\n" +
                    "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8\n" +
                    "Accept-Encoding: gzip, deflate, br\n" +
                    "Accept-Language: ru-RU,ru;q=0.8,en-US;q=0.6,en;q=0.4,lv;q=0.2");
            out.println("");
            out.flush();
            if(!command.equalsIgnoreCase("requests/status.xml"))
            System.out.println( getLine());
        }finally {
            lock.unlock();
        }
    }

    private String getLine()
    {
        StringBuilder sb = new StringBuilder();
        if(!checkStatus())
            return sb.toString();
            String line;
            try {
                do {
                    line = in.readLine();
                    sb.append(line);
                } while (line != null);
            }catch (SocketTimeoutException e)
            {

            } catch (IOException e) {
               Loggout.e("Vlc","getLine",e);
            }
            return sb.toString();
    }


  static class PlayerHandler extends DefaultHandler{

        String name;
        boolean nameb;
        String status;
        boolean statusb;
        String time;
        boolean timeb;
        String currTime;
        boolean currTimeb;
        String volume;
        boolean volumeb;
        Player player;
        boolean ended;

        public Player getPlayer() {
            return player;
        }


        public boolean isEnded() {
            return ended;
        }
        @Override
        public void startDocument() throws SAXException {
            super.startDocument();
        }

        @Override
        public void endDocument() throws SAXException {
            super.endDocument();
            player = new Player("vlc "+ name,status,name,Double.parseDouble(time),Double.parseDouble(volume)/2.56,Double.parseDouble(currTime),currTime +":" + time);
            ended = true;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);
            if(qName.equalsIgnoreCase("info") && attributes.getValue(0).equalsIgnoreCase("filename"))
            {
                nameb = true;
            }
            else if(qName.equalsIgnoreCase("state"))
            {
                statusb = true;
            }
            else if(qName.equalsIgnoreCase("length"))
            {
                timeb = true;
            }
            else if(qName.equalsIgnoreCase("time"))
            {
                currTimeb = true;
            }
            else if(qName.equalsIgnoreCase("volume"))
            {
                volumeb = true;
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            super.characters(ch, start, length);
            if(nameb)
            {
                name = new String(ch,start,length);
                nameb = false;
            }
            else if(statusb)
            {
                status = new String(ch,start,length);
                statusb = false;
            }
            else if(timeb)
            {
                time = new String(ch,start,length);
                timeb = false;
            }
            else if(currTimeb)
            {
                currTime = new String(ch,start,length);
                currTimeb = false;
            }
            else if(volumeb)
            {
                volume = new String(ch,start,length);
                volumeb = false;
            }
        }
    }

//    public static void main(String args[]){
//        try {
//            Robot robot = new Robot();
//
//            // Simulate a mouse click
//
//
//            // Simulate a key press
//            robot.keyPress(KeyEvent.VK_ALT);
//            robot.keyPress(KeyEvent.VK_CONTROL);
//            robot.keyPress(KeyEvent.VK_J);
//            robot.keyRelease(KeyEvent.VK_ALT);
//            robot.keyRelease(KeyEvent.VK_CONTROL);
//            robot.keyRelease(KeyEvent.VK_J);
//        } catch (AWTException e) {
//            e.printStackTrace();
//        }
//    }

}
