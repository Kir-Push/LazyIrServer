package com.push.lazyir.modules.touch;

import com.push.lazyir.Loggout;
import com.push.lazyir.devices.NetworkPackage;
import com.push.lazyir.modules.Module;

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

    public TouchControl() throws Exception {
        try {
            if(r == null)
            r = new Robot();
        } catch (AWTException e) {
            Loggout.e("TouchControl","robotCreate",e);
            throw new Exception("TouchControl constructor error");
        }
    }

    @Override
    public void execute(NetworkPackage np) {
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
        r.mouseMove((int)pt.getX()+x,(int)pt.getY()+y);
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
               r.mouseMove(x,y);
           Thread.sleep(10000);
       }
    }

    @Override
    public void endWork() {

    }
}
