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
      // todo
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
            /*todo IN FUTURE RELEASE IT WILL NOT WORK, REMEMBER!
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
}
