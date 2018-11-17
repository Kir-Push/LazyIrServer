package com.push.lazyir.modules.touch;

import com.push.lazyir.api.MessageFactory;
import com.push.lazyir.modules.Module;
import com.push.lazyir.service.main.BackgroundService;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;

@Slf4j
abstract class Touch extends Module {
     static Robot robot;

     Touch(BackgroundService backgroundService, MessageFactory messageFactory) {
        super(backgroundService, messageFactory);
        initRobot();
    }

    @Synchronized
    static void initRobot() {
        try {
            if(robot == null) {
                robot = new Robot();
            }
        } catch (AWTException e) {
            log.error("robotCreate",e);
        }
    }
}
