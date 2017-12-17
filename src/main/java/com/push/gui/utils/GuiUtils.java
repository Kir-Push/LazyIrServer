package com.push.gui.utils;

import com.push.gui.controllers.MainController;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Popup;
import javafx.stage.PopupWindow;
import org.controlsfx.control.Notifications;

import java.awt.*;
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

    public static ImageView getPictureFromBase64(String base64){
        return new ImageView(pictureFromBase64(base64));
    }

    public static ImageView getPictureFromBase64(String base64,double width,double height){
        return new ImageView(pictureFromBase64(base64,width,height));
    }

    public static Image pictureFromBase64(String base64,double width,double height){
        byte[] dencodedImg = Base64.getMimeDecoder().decode(base64);
        Image image = new Image(new ByteArrayInputStream(dencodedImg),width,height,true,true);
        return image;

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
}
