package com.push.gui.utils;

import com.push.gui.basew.MainWin;
import javafx.scene.image.Image;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

@Slf4j
public class GuiUtils {

    private Image getImage(String name){
      return getImage(name,50,50);
    }

    public Image getImage(String name,double requestedWidth,double requestedHeigh){
        return new Image(MainWin.class.getResourceAsStream("/icons/"+name+".png"),requestedWidth,requestedHeigh,true,false);
    }

    public Image getImageByPaired(boolean paired){
       if(paired)
           return getImage("phone");
       else
           return getImage("notPairedPhone");
    }

    public Image getImageByBattery(int batteryPercent,boolean charging){
        String pngName;
        if(charging) {
            pngName = "chargebattery";
        }else if(batteryPercent >= 99){
            pngName = "100battery";
        }else  if(batteryPercent >= 70){
            pngName = "75battery";
        }else if(batteryPercent >= 45){
            pngName = "50battery";
        }else if(batteryPercent >= 20){
            pngName = "25battery";
        }else if(batteryPercent >= 5){
           pngName = "5battery";
        } else{
            pngName = "0battery";
        }
        return getImage(pngName);
    }

    public Image pictureFromBase64(String base64){
       return pictureFromBase64(base64,50,50);

    }


    public Image pictureFromBase64(String base64,double width,double height){
        byte[] dencodedImg = Base64.getMimeDecoder().decode(base64);
        return new Image(new ByteArrayInputStream(dencodedImg),width,height,true,true);

    }

    public ImageIcon pictureFromBase64Swing(String base64,int width,int height){
        byte[] dencodedImg = Base64.getMimeDecoder().decode(base64);
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(dencodedImg));
            if(image != null) {
                return new ImageIcon(image.getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH));
            }
        } catch (IOException e) {
            log.error(" pictureFromBase64Swing -width: "+ width + " height: "+height,e);
        }
        return null;
    }

    public BufferedImage pictureFromBase64Swing(String base64){
        byte[] dencodedImg = Base64.getMimeDecoder().decode(base64);
        try {
            return ImageIO.read(new ByteArrayInputStream(dencodedImg));
        } catch (IOException e) {
            log.error("pictureFromBase64Swing(String base64)",e);
            return null;
        }
    }


    public ImageIcon getDefaultPersonIcon(int width,int height){
      return getIconByFileName("user",width,height);
    }

    public ImageIcon getDefaultSmsIcon(int width,int height){
        return getIconByFileName("chat",width,height);
    }

    public ImageIcon getIconByFileName(String file,int width,int height){
        try {
            return new ImageIcon(ImageIO.read(ClassLoader.getSystemResource( "icons/"+file+".png" )).getScaledInstance(width,height,java.awt.Image.SCALE_SMOOTH));
        } catch (IOException e) {
          log.error("getIconByFileName - "+ file + " width: "+ width + " height: "+ height,e);
            return null;
        }
    }

    public ImageIcon getDefaultMissedCallIcon(int width, int height) {
        return getIconByFileName("missed",width,height);
    }

    public ImageIcon getDefaultOutgoingCall(int width,int height){
        return getIconByFileName("OUTGOING",width,height);
    }

    public Icon getDefaultUnreadMessagesIcon(int width, int height) {
        return getIconByFileName("UNREAD_MESSAGES",width,height);
    }

    public Icon getDefaultInfo(int width, int height) {
        return getIconByFileName("information",width,height);
    }
}
