package com.push.lazyir;

import com.push.lazyir.gui.Communicator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by buhalo on 12.03.17.
 */
public class Loggout {


    private final static Logger logger = LoggerFactory.getLogger(Loggout.class);

    public static void e(String s,String s2)
    {
        write(s,s2);
    }

    public static void d(String s, String s2)
    {
        write(s,s2);
    }

    private static void write(String s,String s2)
    {
      //    Calendar cal = Calendar.getInstance();
       //    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
       //     Communicator.getInstance().sendToOut(sdf.format(cal.getTime()) + ": " + s + " " + s2);
       logger.debug(s+ " " +s2);
       //    System.out.println(sdf.format(cal.getTime()) + ": " + s + " " + s2);
    }
}
