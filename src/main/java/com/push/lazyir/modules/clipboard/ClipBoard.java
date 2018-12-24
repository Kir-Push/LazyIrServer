package com.push.lazyir.modules.clipboard;

import com.push.lazyir.api.MessageFactory;
import com.push.lazyir.api.NetworkPackage;
import com.push.lazyir.modules.Module;
import com.push.lazyir.service.main.BackgroundService;
import lombok.Getter;
import lombok.Setter;
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
  @Getter @Setter
  private static boolean clipboardSet;
  private static FlavorListener flavorListener;
  private static boolean imSet;

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
        if(!isClipboardSet()) {
            flavorListener = listener -> {
                try {
                    Transferable content = SYSTEM_CLIPBOARD.getContents(backgroundService);
                    String clipboardText = content.getTransferData(DataFlavor.stringFlavor).toString();
                    if(!imSet){
                        SYSTEM_CLIPBOARD.setContents(content, null);
                        imSet = true;
                        if(!clipboardText.equals(lastClipboard)) {
                            receiveClipboard(clipboardText,messageFactory,backgroundService);
                        }
                    }else {
                        imSet = false;
                    }
                    lastClipboard = clipboardText;
                }catch (IOException | UnsupportedFlavorException e){
                    log.error("error in clipboardListener",e);
                }
            };
            SYSTEM_CLIPBOARD.addFlavorListener(flavorListener);
            setClipboardSet(true);
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
            setClipboardSet(false);
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
