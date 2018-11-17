package com.push.lazyir.modules.touch;

import com.push.lazyir.api.MessageFactory;
import com.push.lazyir.api.NetworkPackage;
import com.push.lazyir.modules.Module;
import com.push.lazyir.service.main.BackgroundService;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.awt.*;
import java.awt.event.InputEvent;

@Slf4j
public class TouchControl extends Touch {
    public enum api{
        MOVE,
        CLICK,
        DCLICK,
        RCLICK,
        MOUSEUP,
        MOUSEDOWN,
        MOUSECLICK,
        LONGCLICK,
        LONGRELEASE
    }

    @Inject
    public TouchControl(BackgroundService backgroundService, MessageFactory messageFactory)  {
        super(backgroundService, messageFactory);
    }


    @Override
    public void execute(NetworkPackage np) {
        TouchControlDto dto = (TouchControlDto) np.getData();
        determineWhatToDo(dto,this);
    }

    @Synchronized
    private static void determineWhatToDo(TouchControlDto dto,TouchControl control) {
        api command = api.valueOf(dto.getCommand());
        switch (command) {
            case MOVE:
                control.moveMouse(dto.getMoveX(), dto.getMoveY());
                break;
            case CLICK:
                control.mouseClick();
                break;
            case DCLICK:
                control.dmouseClick();
                break;
            case RCLICK:
                control.rmouseClick();
                break;
            case MOUSEUP:
            case MOUSEDOWN:
                control.wheelMove(dto.getMoveY());
                break;
            case MOUSECLICK:
                control.wheelClick();
                break;
            case LONGCLICK:
                control.longClick();
                break;
            case LONGRELEASE:
                control.longRelease();
                break;
            default:
                break;
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
        robot.mouseWheel(y);
    }
    private void rmouseClick() {
        mousePress(InputEvent.BUTTON3_MASK);
        mouseRelease(InputEvent.BUTTON3_MASK);
    }
    private void mousePress(int buttonMask) {
        robot.mousePress(buttonMask);
    }
    private void mouseRelease(int buttonMask) {
        robot.mouseRelease(buttonMask);
    }
    private void dmouseClick() {
        mouseClick();
        mouseClick();
    }

    private void moveMouse(int x,int y) {
        PointerInfo a = MouseInfo.getPointerInfo();
        Point pt = a.getLocation();
        moveSmooth(x,y,(int)pt.getX(),(int)pt.getY());
    }

    private void moveSmooth(int x, int y, int ptx, int pty) {
        move(ptx+x,pty+y);
    }

    private void move(int currX, int currY) {
        try {
            Thread.sleep(0,1);
        } catch (InterruptedException e) {
          log.error("error in move",e);
          Thread.currentThread().interrupt();
        }
        robot.mouseMove(currX, currY);
    }

    private void mouseClick() {
        mousePress(InputEvent.BUTTON1_MASK);
        mouseRelease(InputEvent.BUTTON1_MASK);
    }

    @Override
    public void endWork() {
        if(backgroundService.ifLastConnectedDeviceAreYou(device.getId())){
            clearResources();
        }
    }

    @Synchronized
    private static void clearResources() {
        robot = null;
    }
}
