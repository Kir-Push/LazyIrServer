package com.push.lazyir.modules.touch;

import com.push.lazyir.Loggout;
import com.push.lazyir.devices.CacherOld;
import com.push.lazyir.devices.NetworkPackageOld;
import com.push.lazyir.modules.Module;
import com.push.lazyir.service.main.BackgroundService;

import javax.inject.Inject;
import java.awt.*;
import java.awt.event.InputEvent;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by buhalo on 21.08.17.
 */
public class TouchControl extends Module {

    private static final String MOVE = "move";
    private static final String CLICK = "click";
    private static final String DCLICK = "dclick";
    private static final String RCLICK = "rclick";
    private static final String MOUSEUP = "mup";
    private static final String MOUSEDOWN = "mdown";
    private static final String MOUSECLICK = "mclick";
    private static final String LONGCLICK = "lclick";
    private static final String LONGRELEASE = "lrelease";
    private static Lock staticLock = new ReentrantLock();
    private static Robot r;
    private static volatile  int lastX;
    private static volatile int lastY;

    @Inject
    public TouchControl(BackgroundService backgroundService, CacherOld cacher)  {
        super(backgroundService, cacher);
        try {
            if(r == null)
                r = new Robot();
//            r.setAutoDelay(1);
        } catch (AWTException e) {
            Loggout.e("TouchControl","robotCreate",e);
         //   throw new Exception("TouchControl constructor error");
        }
    }

    @Override
    public void execute(NetworkPackageOld np) {
        String data = np.getData();
        staticLock.lock();
        try {
            switch (data) {
                case MOVE:
                    moveMouse(Integer.parseInt(np.getValue("x")), Integer.parseInt(np.getValue("y")));
                    break;
                case CLICK:
                    mouseClick();
                    break;
                case DCLICK:
                    dmouseClick();
                    break;
                case RCLICK:
                    rmouseClick();
                    break;
                case MOUSEUP:
                case MOUSEDOWN:
                    wheelMove(Integer.parseInt(np.getValue("wheelY")));
                    break;
                case MOUSECLICK:
                    wheelClick();
                    break;
                case LONGCLICK:
                    longClick();
                    break;
                case LONGRELEASE:
                    longRelease();
                    break;
                default:
                    break;

            }
        }finally {
            staticLock.unlock();
        }
    }

    private void longRelease() {
        mouseRelease(InputEvent.BUTTON1_MASK);
    }

    private void longClick() {
        mousePress(InputEvent.BUTTON1_MASK);
    }

    private void wheelClick() {
        mousePress(InputEvent.BUTTON2_MASK);
        mouseRelease(InputEvent.BUTTON2_MASK);
    }

    private void wheelMove(int y) {
        r.mouseWheel(y);
    }

    private void rmouseClick() {
        mousePress(InputEvent.BUTTON3_MASK);
        mouseRelease(InputEvent.BUTTON3_MASK);
    }

    private void mousePress(int buttonMask)
    {
        r.mousePress(buttonMask);
    }

    private void mouseRelease(int buttonMask)
    {
        r.mouseRelease(buttonMask);
    }

    private void dmouseClick() {
        mouseClick();
        mouseClick();
    }


    public void moveMouse(int x,int y) {
        PointerInfo a = MouseInfo.getPointerInfo();
        Point pt = a.getLocation();
        int currX = (int) (pt.getX() + x);
        int currY = (int) (pt.getY() + y);
        int xDiff = Math.abs(x);
        int yDiff = Math.abs(y);
        if(xDiff > 5 || yDiff > 5)
            moveSmooth((int)pt.getX(),(int)pt.getY(),x,y,xDiff,yDiff);
        else
        move(currX,currY);

    }

    private void move(int currX, int currY) {
        try {
            Thread.sleep(0,1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        r.mouseMove(currX, currY);
    }

    private void moveSmooth(int x, int y, int ptx, int pty, int xDiff, int yDiff) {
//        int count = yDiff > xDiff ? yDiff : xDiff;
//        count /= 5;
//        double yWeight= xDiff < 5 ? ptx :
//        double yAccum = y;
//        double xWeight= yDiff == 0 ? 0 : (double)ptx/(double)count;
//        double xAccum = x;
//            for (int i = 0; i < count; i++) {
//                xAccum += xWeight;
//                yAccum += yWeight;
//                move((int)xAccum,(int)yAccum);
//            }
        }

    public void mouseClick()
    {
        mousePress(InputEvent.BUTTON1_MASK);
        mouseRelease(InputEvent.BUTTON1_MASK);
    }

    public static void main(String[] args) throws AWTException, InterruptedException {
       Robot r = new Robot();
       int x= 100;
       int y = 200;
       while (true) {
           for(int i =0;i<100;i++) {
               Thread.sleep(0,1);
               x +=5;
               y +=5;
               r.mouseMove(x+5, y+5);
           }
           Thread.sleep(10000);
       }
    }

    @Override
    public void endWork() {

    }
}
