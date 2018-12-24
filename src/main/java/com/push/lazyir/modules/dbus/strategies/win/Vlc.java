package com.push.lazyir.modules.dbus.strategies.win;

import com.push.lazyir.modules.dbus.Player;
import com.push.lazyir.service.managers.settings.SettingManager;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
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
import java.util.Base64;
import java.util.List;


@Slf4j
public class Vlc implements Strategy {
    private int port = 9090;
    private String passEncoded;
    private Socket socket;
    private SAXParser saxParser;
    private PlayerHandler handler;
    private PrintWriter out;
    private BufferedReader in;
    private SettingManager settingManager;


    public Vlc(SettingManager settingManager) {
        try {
            this.settingManager = settingManager;
            saxParser = SAXParserFactory.newInstance().newSAXParser();
            handler = new PlayerHandler();
            initParameted();
        }  catch (SAXException | ParserConfigurationException e) {
           log.error("Error in vlc constructor",e);
        }
    }

    private void initParameted() {
        String vlcPass = settingManager.get("Vlc-pass");
        port = Integer.parseInt(settingManager.get("Vlc-port"));
        passEncoded =  (Base64.getUrlEncoder().encodeToString((":" + vlcPass).getBytes()));
    }

    @Synchronized
    public boolean initiate() {
        try {
            if (checkStatus()) {
                return true;
            }
            socket = new Socket(InetAddress.getByName("127.0.0.1"),port);
            socket.setSoTimeout(50);
            out = new PrintWriter(socket.getOutputStream());
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            return true;
        }catch (IOException e) {
            tryToEraseConnections();
            log.error("Error in initiate",e);
            return false;
        }
    }

    @Synchronized
    private void tryToEraseConnections() {
        try{
        if(out != null) {
            out.close();
            socket = null;
        }
        if(in != null){
            in.close();
        }
        }catch (IOException e) {
            log.error("error in tryToEraseConnections",e);
        }
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
    public List<Player> getGetAll() { // yes it's stupid,
        Player onePlayer = getOnePlayer();
        return List.of(onePlayer);
    }

    @Override
    @Synchronized
    public Player getOnePlayer() {
        try {
        sendGet("requests/status.xml");
        String line = getLine();
        if(line == null || line.length() == 0) {
            return null;
        }
        InputSource inputSource = new InputSource(new StringReader(line.substring(line.indexOf("<?xml version="))));
        saxParser.parse(inputSource,handler);
        return handler.isEnded() ? handler.getPlayer() : null;
        }  catch (SAXException |  IOException e) {
            log.error("getOnePlayer error",e);
        }
        return null;
    }

    @Override
    public void seek(String player, String arg) {
        StringBuilder sb = new StringBuilder( "requests/status.xml?cmd=seek&val=");
        if(arg.charAt(0) != '-') {
            sb.append('+').append(arg);
        }
        else {
            sb.append(arg);
        }
        sendGet(sb.toString());
    }
    @Override
    public void next(String player) {
        sendGet("requests/status.xml?cmd=pl_next");
    }

    @Override
    public void previous(String player) {
        sendGet("requests/status.xml?cmd=pl_previous");
    }

    //  http://127.0.0.1:9090/requests/status.xml?command=pl_stop
    @Override
    public void stop(String player) {
        sendGet("requests/status.xml?cmd=pl_stop");
    }

    @Override
    public void playPause(String player) {
        sendGet("requests/status.xml?cmd=pl_pause");
    }

    @Override
    public void openUri(String player, String uri) {
        sendGet("requests/status.xml?cmd=in_play&input="+uri);
    }

    @Override
    public void setPosition(String player, String path, String position) {
        sendGet("requests/status.xml?cmd=seek&val="+position);
    }

    @Override
    public void setVolume(String player, double value) {
        sendGet("requests/status.xml?cmd=volume&val="+ (value * 256));
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

    @Synchronized
    private void sendGet(String command) {
        if(!checkStatus()) {
            return;
        }
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
        if (!"requests/status.xml".equalsIgnoreCase(command)){
            getLine();
        }
    }

    private String getLine()
    {
        StringBuilder sb = new StringBuilder();
        if (!checkStatus()) {
            tryToEraseConnections();
            return null;
        }

        try {
            String line;
            while ((line = in.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            log.error("getLine", e);
        }
        return sb.toString();
    }


  static class PlayerHandler extends DefaultHandler{

      String name;
      String status;
      double time;
      double currTime;
      double volume;
      String localStatus;
      boolean ended;
      Player player;

        public Player getPlayer() {
            return player;
        }

        boolean isEnded() {
            return ended;
        }

        @Override
        public void endDocument() throws SAXException {
            super.endDocument();
            player = new Player("vlc "+ name,status,name,time,volume,currTime);
            ended = true;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);
            localStatus = qName;
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            super.characters(ch, start, length);
            switch (localStatus){
                case "info":
                    name = new String(ch,start,length);
                    break;
                case "state":
                    status = new String(ch,start,length);
                    break;
                case "length":
                    time = Double.parseDouble(new String(ch,start,length));
                    break;
                case "time":
                    currTime = Double.parseDouble(new String(ch,start,length));
                    break;
                case "volume":
                    volume = Double.parseDouble(new String(ch,start,length))/2.56;
                    break;
                default:
                    break;
            }
        }
    }

}
