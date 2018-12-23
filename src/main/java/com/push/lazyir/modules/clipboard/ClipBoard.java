package com.push.lazyir.modules.clipboard;

import com.push.lazyir.api.MessageFactory;
import com.push.lazyir.api.NetworkPackage;
import com.push.lazyir.modules.Module;
import com.push.lazyir.service.main.BackgroundService;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;

import java.awt.*;
import java.awt.datatransfer.*;
import java.io.IOException;

import static com.push.lazyir.modules.clipboard.ClipBoard.api.RECEIVE;

@Slf4j
public class ClipBoard extends Module implements ClipboardOwner {
  private static final Clipboard SYSTEM_CLIPBOARD = Toolkit.getDefaultToolkit().getSystemClipboard();
  private static String lastClipboard = "";
  private static boolean clipboardSet;
  private static FlavorListener flavorListener;

    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
        //?
    }

    public enum api{
         RECEIVE
     }
    @Inject
    public ClipBoard(BackgroundService backgroundService, MessageFactory messageFactory) {
        super(backgroundService, messageFactory);
        setClipboardListener(messageFactory,backgroundService);
    }

    @Synchronized
    private static void setClipboardListener(MessageFactory messageFactory,BackgroundService backgroundService) {
        if(!clipboardSet) {
            flavorListener = listener -> {
                try {
                    String clipboardText = (String) SYSTEM_CLIPBOARD.getData(DataFlavor.stringFlavor);
                    if(!clipboardText.equals(lastClipboard)) {
                        SYSTEM_CLIPBOARD.setContents(new StringSelection(clipboardText), null);
                        lastClipboard = clipboardText;
                        receiveClipboard(clipboardText,messageFactory,backgroundService);
                    }
                }catch (IOException | UnsupportedFlavorException e){
                    log.error("error in clipboardListener",e);
                }
            };
            SYSTEM_CLIPBOARD.addFlavorListener(flavorListener);
            clipboardSet = true;
        }
    }


    @Override
    public void execute(NetworkPackage np) {
        ClipBoardDto data = (ClipBoardDto) np.getData();
        if(data.getCommand().equalsIgnoreCase(RECEIVE.name())) {
            onReceive(data.getText());
        }
    }

    @Override
    public void endWork() {
        if(backgroundService.ifLastConnectedDeviceAreYou(device.getId())){
            SYSTEM_CLIPBOARD.removeFlavorListener(flavorListener);
            clipboardSet = false;
        }
    }

    private void onReceive(String text) {
        setToClipboard(text,this);
    }

    @Synchronized
    private static void setToClipboard(String text, ClipBoard clipBoard) {
        SYSTEM_CLIPBOARD.setContents(new StringSelection(text),clipBoard);
    }

    private static void receiveClipboard(String text,MessageFactory messageFactory,BackgroundService backgroundService) {
        String message = messageFactory.createMessage(ClipBoard.class.getSimpleName(), true, new ClipBoardDto(RECEIVE.name(), text));
        backgroundService.sendToAllDevices(message);
    }
}
