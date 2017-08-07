package com.push.lazyir.managers;

import com.push.lazyir.Loggout;
import com.push.lazyir.devices.Device;
import com.push.lazyir.pojo.Command;
import com.push.lazyir.service.BackgroundService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by buhalo on 12.03.17.
 */
public class CommandManager extends SettingManager  {


    protected String settingsFile = "commands.ini";
    private String OS = System.getProperty("os.name").toLowerCase();
    FileInputStream fileInputStream;
  //  FileOutputStream fileOutputStream;
//    private String mockId = null;



    public CommandManager() {
//        super();
        if(isUnix())
        baseProp = "commands";
        else if(isWindows())
        {
            baseProp = "commandsWin";
        }
        properties = new Properties();
        settings = new File(settingFilePath+ File.separator+ settingsFile);
        File dir = new File(settingFilePath);
        if(!dir.exists())
        {
            dir.mkdir();
        }
        if(!settings.exists() && !settings.isDirectory())
        {
            copyFromBackupToActual();
        }
    }

    private boolean isWindows() {

        return (OS.indexOf("win") >= 0);

    }

    private boolean isUnix() {

        return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 );

    }




    public synchronized List<Command> getAllCommands()
    {
        List<Command> list = new ArrayList<>();
        try{
            fileInputStream = new FileInputStream(settings);
            properties.load(fileInputStream);
            for (String s : properties.stringPropertyNames()) {
                list.add(new Command(s,properties.getProperty(s),null,"pc"));
            }

        }catch (IOException e)
        {
            Loggout.e("CommandManager",e.toString());
        }finally {
            try {
                fileInputStream.close();
            } catch (IOException e) {
                Loggout.e("CommandManager",e.toString());
            }
        }
        return list;
    }

    public synchronized void save(List<Command> commands)
    {

    }

    public synchronized void syncCommands()
    {
        for(Device device : Device.getConnectedDevices().values())
        {
            if(device.isPaired())
            syncCommands(device.getId());
        }
    }

    public synchronized void syncCommands(String id)
    {
       BackgroundService.getTcp().sendSynchoCommand(id);
    }


    public List<String> getCommandsByNames(List<Command> strings) {
        List<String> commands = new ArrayList<>();
        try {

            fileInputStream = new FileInputStream(settings);
            properties.load(fileInputStream);
            for (Command command : strings) {
                commands.add(properties.getProperty(command.getCommand_name()));
            }
        }catch (IOException e)
        {
            Loggout.e("CommandManager",e.toString());
        }
        finally {
            try {
                fileInputStream.close();
            } catch (IOException e) {
                Loggout.e("CommandManager",e.toString());
            }
        }
        return commands;
    }
}
