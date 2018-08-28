package com.push.lazyir.modules.share;

import com.push.lazyir.api.MessageFactory;
import com.push.lazyir.api.NetworkPackage;
import com.push.lazyir.gui.GuiCommunicator;
import com.push.lazyir.modules.Module;
import com.push.lazyir.service.main.BackgroundService;
import com.push.lazyir.utils.Utility;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.util.concurrent.Future;

import static com.push.lazyir.service.managers.settings.SettingManager.CURRENT_USERS_HOME_DIR;


@Slf4j
public class ShareModule extends Module {
    public enum api{
        SETUP_SERVER_AND_SEND_ME_PORT,
        CONNECT_TO_ME_AND_RECEIVE_FILES,
        RECCONECT
    }
    private Future futuresftp;
    private SftpServerProcess sftpServerProcess;
    private GuiCommunicator guiCommunicator;

    @Inject
    public ShareModule(BackgroundService backgroundService, MessageFactory messageFactory, GuiCommunicator guiCommunicator) {
        super(backgroundService, messageFactory);
        this.guiCommunicator = guiCommunicator;
    }

    @Override
    public void execute(NetworkPackage np) {
        ShareModuleDto dto = (ShareModuleDto) np.getData();
        api command = ShareModule.api.valueOf(dto.getCommand());
        if(command.equals(api.CONNECT_TO_ME_AND_RECEIVE_FILES)){
            connectToSftpServer(dto);
        }else if (command.equals(api.RECCONECT)){
            recconectToSftp(dto);
        }
    }


    @Override
    public void endWork() {
        stopSftpServer();
        clearFolders(backgroundService,device.getId());
    }


    // when last device disconnected or close sftp connection, erase all folders in connectedDevicePath
    @Synchronized
    private static void clearFolders(BackgroundService backgroundService,String id) {
        try {
            if(backgroundService.ifLastConnectedDeviceAreYou(id)){
                Files.deleteIfExists(new File(CURRENT_USERS_HOME_DIR).toPath());
            }
        } catch (IOException e) {
           log.error("error in clearFolders - " + CURRENT_USERS_HOME_DIR,e);
        }
    }

    @Synchronized
    private void connectToSftpServer(ShareModuleDto dto) {
        if(sftpServerProcess.isRunning() && !futuresftp.isCancelled()){
            return;
        }
        String userName = dto.getUserName();
        String pass = dto.getPassword();
        if (userName == null || pass == null) {
            sendMsg(messageFactory.createMessage(this.getClass().getSimpleName(),true,new ShareModuleDto(api.RECCONECT.name())));
            return;
        }
        sftpServerProcess = instantiateSftpServerProcess(dto.getPort(), device.getIp(), dto.getMountPoint(), dto.getExternalMountPoint(), userName, pass, device.getId());
        if(sftpServerProcess != null) {
            futuresftp = backgroundService.submitNewTask(sftpServerProcess);
        }
    }

    private void recconectToSftp(ShareModuleDto dto) {
        stopSftpServer();
        connectToSftpServer(dto);
    }

    @Synchronized
    private void stopSftpServer() {
        if(futuresftp != null) {
            futuresftp.cancel(true);
        }
        if(sftpServerProcess != null) {
            sftpServerProcess.stopProcess();
        }
        guiCommunicator.sftpConnectResult(false,device.getId());
    }

    private SftpServerProcess instantiateSftpServerProcess(int port, InetAddress ip, String mountPoint, PathWrapper externalMountPoint, String userName, String pass, String id){
        if(Utility.isUnix()) {
            return new SftpServerProcessNix(port, ip, mountPoint, externalMountPoint, userName, pass,id, backgroundService);
        }
        else if(Utility.isWindows()) {
            return new SftpServerProcessWin(port, ip, mountPoint, externalMountPoint, userName, pass,id,guiCommunicator);
        }
        return null;
    }

    public void sendSetupServerCommand() {
        String os;
        if(Utility.isWindows())
            os = "win";
        else if(Utility.isUnix())
            os = "nix";
        else
            os = "hz";
        sendMsg(messageFactory.createMessage(this.getClass().getSimpleName(),true,new ShareModuleDto(api.SETUP_SERVER_AND_SEND_ME_PORT.name(),os)));
    }

}
