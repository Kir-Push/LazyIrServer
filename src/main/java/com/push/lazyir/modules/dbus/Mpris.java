package com.push.lazyir.modules.dbus;

import com.push.lazyir.api.MessageFactory;
import com.push.lazyir.api.NetworkPackage;
import com.push.lazyir.modules.Module;
import com.push.lazyir.modules.dbus.websocket.ServerController;
import com.push.lazyir.service.main.BackgroundService;
import com.push.lazyir.service.managers.settings.SettingManager;
import lombok.NonNull;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import javax.inject.Inject;
import java.util.*;

import static com.push.lazyir.utils.Utility.*;

@Slf4j
public class Mpris extends Module {
    public enum api{
        SEEK,
        NEXT,
        PREVIOUS,
        STOP,
        PLAYPAUSE,
        OPENURI,
        SETPOSITION,
        VOLUME,
        ALLPLAYERS
    }
    private static int callersCount = 0;
    private static OsStrategy strategy;
    private static OsStrategy browserStrategy;
    private ServerController serverController;

    @Inject
    public Mpris(BackgroundService backgroundService, MessageFactory messageFactory, ServerController serverController, SettingManager settingManager) {
        super(backgroundService, messageFactory);
        this.serverController = serverController;
        initStrategies(settingManager,serverController);
        if(serverController.notWorking()) {
            serverController.startServer();
        }
    }

    private static void initStrategies(SettingManager settingManager, ServerController serverController) {
        if(strategy == null) {
            if (isUnix()) { strategy = new Nix(); }
            else if (isWindows()){ strategy = new Win(settingManager); }
        }
        if (browserStrategy == null) {
            browserStrategy = new HtmlVid(serverController);
        }
    }

    @Synchronized
    private static void clearStrategies(){
        strategy.endWork();
        strategy = null;
        browserStrategy.endWork();
        browserStrategy = null;
    }

    @Synchronized
    private static void ending(BackgroundService backgroundService,ServerController serverController,String id){
        if (backgroundService.ifLastConnectedDeviceAreYou(id)) {
            clearStrategies();
            serverController.stopServer();
        }
    }
    @Override
    public void endWork() {
        ending(backgroundService,serverController,device.getId());
    }

    @Override
    public void execute(NetworkPackage np) {
        determine(np);
    }

    private void determine(NetworkPackage np) {
        MprisDto dto = (MprisDto) np.getData();
        api command = api.valueOf(dto.getCommand());
        if (command.equals(api.ALLPLAYERS)) {
            getAllPlayers();
            return;
        }
        OsStrategy tempStrategy;
        if (browserCheck(dto.getPlayerType())) {
            tempStrategy = browserStrategy;
        } else {
            tempStrategy = strategy;
        }

        switch (command) {
            case SEEK:
                tempStrategy.seek(dto);
                break;
            case NEXT:
                tempStrategy.next(dto);
                break;
            case STOP:
                tempStrategy.stop(dto);
                break;
            case PREVIOUS:
                tempStrategy.previous(dto);
                break;
            case PLAYPAUSE:
                tempStrategy.playPause(dto);
                break;
            case OPENURI:
                tempStrategy.openUri(dto);
                break;
            case SETPOSITION:
                tempStrategy.setPosition(dto);
                break;
            case VOLUME:
                tempStrategy.setVolume(dto);
                break;
            default:
                break;
        }

    }

    private void getAllPlayers() {
        backgroundService.submitNewTask(()->{
            List<Player> playerList = new ArrayList<>();
            playerList.addAll(strategy.getAllPlayers());
            playerList.addAll(browserStrategy.getAllPlayers());
            if(!playerList.isEmpty()) {
                String message = messageFactory.createMessage(this.getClass().getSimpleName(), true,
                        new MprisDto(api.ALLPLAYERS.name(), playerList));
                sendMsg(message);
            }
        });
    }

    private boolean browserCheck(@NonNull String type) {
        return type.equals("browser");
    }


    public void pauseAll() {
        pauseAllSynchronized();
    }

    private static void incrementCallers() {
        callersCount++;
    }

    public void playAll() {
       playAllSynchronized();
    }

    @Synchronized
    private static void pauseAllSynchronized(){
        incrementCallers();
        strategy.pauseAll();
        browserStrategy.pauseAll();
    }

    @Synchronized
    private static void playAllSynchronized(){
        incrementCallers();
        if (callersCount > 0)
            return;
        strategy.playAll();
        browserStrategy.playAll();
    }

}
