package com.push.gui.utils;

import com.push.gui.basew.MainWin;
import com.push.gui.controllers.MainController;
import com.push.gui.systray.JavaFXTrayIconSample;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.*;
import javafx.stage.Popup;
import org.controlsfx.control.Notifications;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.Map;

public class GuiUtils {

    public static Image getImage(String name){
      return getImage(name,50,50);
    }

    public static Image getImage(String name,double requestedWidth,double requestedHeigh){
        try {
         // todo change to resourceasstream anywhere (from simple getresouce)
            return new Image(MainWin.class.getResourceAsStream("/icons/"+name+".png"),requestedWidth,requestedHeigh,true,false);
        } catch (Exception e) {
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

    public static ImageView getPictureFromBase64(String base64){
        return new ImageView(pictureFromBase64(base64));
    }

    public static ImageView getPictureFromBase64(String base64,double width,double height){
        return new ImageView(pictureFromBase64(base64,width,height));
    }

    public static Image pictureFromBase64(String base64,double width,double height){
        byte[] dencodedImg = Base64.getMimeDecoder().decode(base64);
        return new Image(new ByteArrayInputStream(dencodedImg),width,height,true,true);

    }

    public static ImageIcon pictureFromBase64Swing(String base64,int width,int height){
        byte[] dencodedImg = Base64.getMimeDecoder().decode(base64);
        BufferedImage image = null;
        try {
            image = ImageIO.read(new ByteArrayInputStream(dencodedImg));
        } catch (IOException e) {
           return null;
        }
        return new ImageIcon(image.getScaledInstance(width,height,java.awt.Image.SCALE_SMOOTH));
    }

    public static String formatText(String text){
        return null;
    }


    /*
     Controlfx don't allow hide notifications manually.
     Use reflection for this.
     */
    public static void hideNotif(Notifications notification,Pos position){
        Field f = null; //NoSuchFieldException
        try {
          /*   Notifications have inner static class {@link $NotificationPopupHandler}, which wee need
             get it*/
            Class<?> aClass = Class.forName("org.controlsfx.control.Notifications$NotificationPopupHandler");
            /*he is singleton, so we need get instance method*/
            Method m = aClass.getDeclaredMethod("getInstance");
            /* GetInstance method is packagePrivate, set it accessible*/
            m.setAccessible(true);
          /*   call method and receive class instance
            use null if the method is static*/
            Object o = m.invoke(null);
           /*  inside, NotificationPopupHandler contain popusMap, where store all popups (notification actually work thought it)
             get field*/
            f = o.getClass().getDeclaredField("popupsMap");
          /*   and set accesible*/
            f.setAccessible(true);
           /*  get field object, which is map
             key is Pos (Position) and value list of Popups
             when notification call show, it create popup and put in list if exist or create new,
             after put list in map
             it contain popups even if they don't anymore showing (don't know how much time)*/
            Map<Pos, List<Popup>> popupList = (Map<Pos, List<Popup>>) f.get(o);
          /*   we know our notification position, use it as key
             and receive list of popups*/
            List<Popup> popups = popupList.get(position);
            Popup popup = null;
           /*  get current mouse location
             when on one position showed many popups, they stacked at each other
             so we know position, but don't know which of many notification's wee need*/
            Point location = MouseInfo.getPointerInfo().getLocation();

           /*  iterate all over popups in the list
             and compare popups position on screen with mouse location
             if mouse somewhere inside notification frame
             wee found it*/
            for (Popup popup1 : popups) {
                if(location.getX() >= popup1.getX() && location.getX() <= popup1.getX() + popup1.getWidth()
                        && location.getY() >= popup1.getY() && location.getY() <= popup1.getY() + popup1.getHeight()) {
                    popup = popup1;
                    break;
                }
            }

            /* if null, we didn't found popup
             just return*/
            if(popup == null)
                return;
       /*      popup class inherit method hide from his parent PopuWindow
             we need get this method
             https://stackoverflow.com/questions/5411434/how-to-call-a-superclass-method-using-java-reflection*/
            /* IN FUTURE RELEASE IT WILL NOT WORK, REMEMBER!
             we can get pprivate method of superclass only from child
             we get lookup class which used for getting superclass method, when we call from child*/
            Field IMPL_LOOKUP = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
          /*   set it accessible, he is of course private-package*/
            IMPL_LOOKUP.setAccessible(true);
        /*     get actually object*/
            MethodHandles.Lookup lkp = (MethodHandles.Lookup) IMPL_LOOKUP.get(null);
           /*  use lookup method for finding method of interface or parent*/
            MethodHandle h1 = lkp.findSpecial(PopupWindow.class, "hide", MethodType.methodType(void.class), Popup.class);
          /*  and invoke it*/
            h1.invoke(popup);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static Stage createDummyStage() {
        Stage owner = new Stage(StageStyle.UTILITY);
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: TRANSPARENT");
        Scene scene = new Scene(root, 1, 1);
        scene.setFill(Color.TRANSPARENT);
        owner.setScene(scene);
        owner.setWidth(1);
        owner.setHeight(1);
      //  owner.setX(Double.MAX_VALUE);
        //owner.toBack();
        owner.show();
        return owner;

    }

    public static String getDefaultIconMessage(){
        return "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";
    }

    public static String getDefaultCallMessage(){
        return "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";
    }
}
