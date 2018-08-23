package com.push.lazyir.service.main;

import com.push.gui.basew.CommandsWindow;
import com.push.gui.basew.Dialogs;
import com.push.gui.basew.Popup;
import com.push.gui.controllers.ApiController;
import com.push.gui.controllers.MainController;
import com.push.gui.utils.GuiUtils;
import com.push.lazyir.devices.Cacher;
import com.push.lazyir.gui.GuiCommunicator;
import com.push.lazyir.modules.ModuleFactory;
import com.push.lazyir.modules.clipboard.Rmi.ClipboardRmiServer;
import com.push.lazyir.modules.dbus.websocket.ServerController;
import com.push.lazyir.service.managers.settings.LocalizationManager;
import com.push.lazyir.service.managers.settings.SettingManager;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

@Module
public class ServiceModule {

    @Provides
    @Singleton
    public BackgroundService provideBackgroundService(SettingManager settingManager, LocalizationManager localizationManager,Cacher cacher,ModuleFactory moduleFactory){
        return new BackgroundService(settingManager,localizationManager,cacher,moduleFactory);
    }

    @Provides
    @Singleton
    public SettingManager provideSettingManager(){
        return new SettingManager();
    }

    @Provides
    @Singleton
    public LocalizationManager provideLocalizationManager(){
        return new LocalizationManager();
    }

    @Provides
    @Singleton
    public Cacher provideCacher(){
        return new Cacher();
    }

    @Provides
    @Singleton
    public GuiCommunicator provideGuiCommunicator(BackgroundService backgroundService, ApiController apiController){
        return new GuiCommunicator(apiController,backgroundService);
    }

    @Provides
    @Singleton
    public ApiController provideApiController(Popup popup){
        return new ApiController(popup);
    }

    @Provides
    @Singleton
    public Popup providePopup(SettingManager settingManager,BackgroundService backgroundService,GuiUtils guiUtils){
        return new Popup(settingManager,backgroundService,guiUtils);
    }

    @Provides
    @Singleton
    public MainController provideMainController(GuiCommunicator guiCommunicator, Dialogs dialogs, CommandsWindow commandsWindow,BackgroundService backgroundService,GuiUtils guiUtils){
        return new MainController(guiCommunicator,dialogs,commandsWindow,backgroundService,guiUtils);
    }

    @Provides
    @Singleton
    public GuiUtils provideGuiUtils(){
        return new GuiUtils();
    }

    @Provides
    @Singleton
    public Dialogs provideDialogs(GuiCommunicator guiCommunicator){
        return new Dialogs(guiCommunicator);
    }

    @Provides
    @Singleton
    public CommandsWindow provideCommandsWindow(BackgroundService backgroundService){
        return new CommandsWindow(backgroundService);
    }

    @Provides
    @Singleton
    public ModuleFactory provideModuleFactory(){
        return new ModuleFactory();
    }

    @Provides
    @Singleton
    public ClipboardRmiServer provideClipboardRmiServer(BackgroundService backgroundService,Cacher cacher){
        return new ClipboardRmiServer(cacher,backgroundService);
    }

    @Provides
    @Singleton
    public ServerController provideServeController(Cacher cacher){
        return new ServerController(cacher);
    }
}
