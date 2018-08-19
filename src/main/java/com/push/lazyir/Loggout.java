package com.push.lazyir;

import com.push.lazyir.service.main.BackgroundService;
import lombok.NonNull;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Level;

/**
 * Created by buhalo on 12.03.17.
 */
@Slf4j
public class Loggout {

    private Loggout() {

    }

    private static Level logLevel = Level.INFO;

    public static void logInfo(Logger logger,String msg,Exception e){
        logger.info(msg,e);
    }

    public static void logInfo(Logger logger,String msg){
        logger.info(msg);
    }

    public static void logDebug(Logger logger,String msg){
        logger.debug(msg);
    }

    public static void logDebug(Logger logger,String msg,Exception e){
        logger.debug(msg,e);
    }

    public static void refresh(String level){
        if(level == null) {
            logLevel = Level.INFO;
            return;
        }
        switch (level){
            case "info":
                logLevel=Level.INFO;
                break;
            case "debug":
                logLevel=Level.SEVERE;
                break;
            default:
                logLevel=Level.INFO;
                break;
        }
    }

    public static void e(String s,String s2)
    {
        write(s,s2);
    }

    public static void d(String s, String s2)
    {
        write(s,s2);
    }

    public static void e(String s,String s2,Exception e)
    {
        StringBuilder sb = new StringBuilder(s2);
        sb.append(e.toString());
        sb.append(System.lineSeparator());
        for (StackTraceElement stackTraceElement : e.getStackTrace()) {
            sb.append(stackTraceElement);
            sb.append(System.lineSeparator());
        }
        write(s,sb.toString());
    }


    private static void write(String s,String s2)
    {
          Calendar cal = Calendar.getInstance();
           SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
       //     Communicator.getInstance().sendToOut(sdf.format(cal.getTime()) + ": " + s + " " + s2);
       log.info( s2);
//
    }
}
