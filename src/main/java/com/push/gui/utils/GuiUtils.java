package com.push.gui.utils;

import com.push.gui.controllers.MainController;
import javafx.scene.image.Image;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

public class GuiUtils {

    public static Image getImage(String name){
      return getImage(name,50,50);
    }

    public static Image getImage(String name,double requestedWidth,double requestedHeigh){
        try {
            File file = new File(GuiUtils.class.getClassLoader().getResource("icons/" + name + ".png").toURI());
            return new Image(file.toURI().toString(),requestedWidth,requestedHeigh,true,false);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static Image getImageByPaired(boolean paired){
       if(paired)
           return getImage("phone");
       else
           return getImage("notPairedPhone");
    }

    public static Image getImageByBattery(int batteryPercent,boolean charging){
        String pngName;
        if(charging)
            pngName = "chargebattery";
        else if(batteryPercent >= 99){
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

    public static Image pictureFromBase64(String base64){
       return pictureFromBase64(base64,50,50);

    }

    public static Image pictureFromBase64(String base64,double width,double height){
        byte[] dencodedImg = Base64.getMimeDecoder().decode(base64);
        Image image = new Image(new ByteArrayInputStream(dencodedImg),width,height,true,true);
        return image;

    }
}
