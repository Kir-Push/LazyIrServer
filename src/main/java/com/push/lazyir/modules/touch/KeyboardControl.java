package com.push.lazyir.modules.touch;

import com.push.lazyir.api.MessageFactory;
import com.push.lazyir.api.NetworkPackage;
import com.push.lazyir.service.main.BackgroundService;
import com.push.lazyir.service.managers.settings.SettingManager;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import javax.inject.Inject;
import java.awt.event.KeyEvent;

import static com.push.lazyir.modules.touch.KeyboardControl.api.CHANGE_LANG;
import static java.awt.event.KeyEvent.getExtendedKeyCodeForChar;

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
                if(Character.isUpperCase(dto.getKeycode())){
                    keyboardControl.pressKeycodeUp(dto.getKeycode());
                }else {
                    keyboardControl.pressKeycode(dto.getKeycode());
                }
                break;
            case KEYS_UP:
                keyboardControl.pressKeycodeUp(dto.getKeycode());
                break;
            case CHANGE_LANG:
                keyboardControl.changeLand(CHANGE_LANG.name());
                break;
            case SPECIAL_KEYS:
                keyboardControl.pushSpecialKey(dto.getSymbol());
                break;
            default:
                break;
        }

    }

    private void pushSpecialKey(String symbol) {
        pressSpecialKey(symbol,true);
        pressSpecialKey(symbol,false);
    }

    private void pressSpecialKey(String symbol,boolean press) {
        switch (symbol.toLowerCase()) {
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
            case "backspace":
                pressOrRelease(KeyEvent.VK_BACK_SPACE,press);
                break;
            case "enter":
                pressOrRelease(KeyEvent.VK_ENTER,press);
                break;
            default:
                break;
        }
    }

    private void changeLand(String symbol) {
        String specialKeys = settingManager.getString(symbol, "alt+shift");
        String[] split = specialKeys.split("\\+");
        pressOrReleaseKeys(split,true);
        pressOrReleaseKeys(split,false);

    }

    private void pressOrReleaseKeys(String[] split,boolean press) {
        for (String s : split) {
           pressSpecialKey(s,press);
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
        getExtendedKeyCodeForChar(key);
        int keyCode =   getExtendedKeyCodeForChar(key);
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
