package com.push.lazyir.modules.touch;

import com.push.lazyir.api.MessageFactory;
import com.push.lazyir.api.NetworkPackage;
import com.push.lazyir.service.main.BackgroundService;
import com.push.lazyir.service.managers.settings.SettingManager;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import javax.inject.Inject;
import javax.swing.*;
import java.awt.event.KeyEvent;

import static com.push.lazyir.modules.touch.KeyboardControl.api.CHANGE_LANG;

@Slf4j
public class KeyboardControl extends Touch {
    public enum api{
        PRESS,
        KEYS_UP,
        COMBO,
        SPECIAL_KEYS,
        CHANGE_LANG
    }

    private SettingManager settingManager;

    @Inject
    public KeyboardControl(BackgroundService backgroundService, MessageFactory messageFactory,SettingManager settingManager) {
        super(backgroundService, messageFactory);
        this.settingManager = settingManager;
    }

    @Override
    public void execute(NetworkPackage np) {
        KeyboardDto dto = (KeyboardDto) np.getData();
        determineWhatToDo(dto,this);
    }

    @Synchronized
    private static void determineWhatToDo(KeyboardDto dto, KeyboardControl keyboardControl) {
        api command = api.valueOf(dto.getCommand());
        switch (command){
            case PRESS:
                keyboardControl.pressKeycode(dto.getKeycode());
                break;
            case KEYS_UP:
                keyboardControl.pressKeycodeUp(dto.getKeycode());
                break;
            case CHANGE_LANG:
                keyboardControl.pressSpecialKey(CHANGE_LANG.name());
                break;
            default:
                break;
        }

    }

    private void pressSpecialKey(String symbol) {
        String specialKeys = settingManager.getString(symbol, "alt+shift");
        String[] split = specialKeys.split("\\+");
        pressOrReleaseKeys(split,true);
        pressOrReleaseKeys(split,false);

    }

    private void pressOrReleaseKeys(String[] split,boolean press) {
        for (String s : split) {
            switch (s.toLowerCase()) {
                case "alt":
                    pressOrRelease(KeyEvent.VK_ALT,press);
                    break;
                case "ctrl":
                    pressOrRelease(KeyEvent.VK_CONTROL,press);
                    break;
                case "shift":
                    pressOrRelease(KeyEvent.VK_SHIFT,press);
                    break;
                case "caps":
                    pressOrRelease(KeyEvent.VK_CAPS_LOCK,press);
                    break;
                case "tab":
                    pressOrRelease(KeyEvent.VK_TAB,press);
                    break;
                case "space":
                    pressOrRelease(KeyEvent.VK_SPACE,press);
                    break;
                default:
                    break;
            }
        }
    }

    private void pressOrRelease(int keycode, boolean press){
        if(press){
            robot.keyPress(keycode);
        } else {
          robot.keyRelease(keycode);
        }
    }


    private void pressKeycodeUp(char key) {
        robot.keyPress(KeyEvent.VK_SHIFT);
        pressKeycode(key);
        robot.keyRelease(KeyEvent.VK_SHIFT);
    }

    private void pressKeycode(char key) {
        KeyStroke ks = KeyStroke.getKeyStroke(key, 0);
        int keyCode = ks.getKeyCode();
        robot.keyPress(keyCode);
        robot.keyRelease(keyCode);
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