package com.push.lazyir.service.main;

import com.push.gui.basew.About;
import com.push.gui.basew.MainWin;
import com.push.gui.basew.SettingsWindow;
import com.push.gui.controllers.MainController;
import com.push.lazyir.gui.GuiCommunicator;
import com.push.lazyir.service.managers.settings.LocalizationManager;
import dagger.Component;

import javax.inject.Provider;
import javax.inject.Singleton;

@Component(modules = ServiceModule.class)
@Singleton
public interface ServiceComponent {

    BackgroundService provideBackGroundService();

    GuiCommunicator provideGuiCommunicator();

    Provider<MainController> provideMainController();

    MainWin provideMainWin();

    About provideAbout();

    SettingsWindow provideSettingsWindow();

    LocalizationManager provideLocalizationManager();

    ModuleComponent getModuleComponent();


}
