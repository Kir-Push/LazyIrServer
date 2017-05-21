package com.push.lazyir.managers;

import com.push.lazyir.Loggout;
import com.push.lazyir.pojo.Command;
import sun.rmi.runtime.Log;

import java.io.*;
import java.net.InetAddress;
import java.util.*;

/**
 * Created by buhalo on 12.03.17.
 */
public class SettingManager implements Manager {

    private static SettingManager instance;

    protected String currentUsersHomeDir = System.getProperty("user.home");
    protected String settingFilePath =currentUsersHomeDir + File.separator + ".Jasech";
    protected String settingsFile = "settingFile.ini";
    protected String addressCacheFile = "adresses.txt";
    protected File settings;
    protected File cache;
    protected Properties properties;
    FileInputStream fileInputStream;
    FileOutputStream fileOutputStream;
    protected String baseProp = "baseProp";

    private String mockId = null;
    private String keyPath;


    protected SettingManager()
    {
        baseProp = "baseProp";
        properties = new Properties();
        settings = new File(settingFilePath+ File.separator+ settingsFile);
        cache = new File(settingFilePath + File.separator + addressCacheFile);
        keyPath = settingFilePath + File.separator + "keyFile.pem";
        File dir = new File(settingFilePath);
        if(!dir.exists())
        {
            dir.mkdir();
        }
        if(!settings.exists() && !settings.isDirectory())
        {
            copyFromBackupToActual();
        }
        if(!cache.exists() && !cache.isDirectory())
        {
            try {
                cache.createNewFile();
            } catch (IOException e) {
                Loggout.e("SettingManager",e.toString());
            }
        }

    }

    protected void copyFromBackupToActual() {
        try {
            ClassLoader classLoader =getClass().getClassLoader();
            String file = classLoader.getResource(baseProp).getFile();
            fileInputStream = new FileInputStream(file);
                    properties.load(fileInputStream);
            if(settings.createNewFile())
            {
                fileOutputStream = new FileOutputStream(settings);
                properties.store(fileOutputStream,"hey ja");
            }
        } catch (IOException e) {
            Loggout.e("SettingManager",e.toString());
        }finally {
            try {
              fileInputStream.close();
              fileOutputStream.close();
            } catch (Exception e) {
                Loggout.e("SettingManager",e.toString());
            }
        }
    }


    public static SettingManager getInstance() {
        if(instance == null)
        {
            instance = new SettingManager();
        }
        return instance;
    }

    @Override
    public synchronized void delete(String key) {
        try {
            fileInputStream = new FileInputStream(settings);
            fileOutputStream = new FileOutputStream(settings);
            properties.load(fileInputStream);
            properties.remove(key);
            properties.store(fileOutputStream,"removed key");
        } catch (IOException e) {
            Loggout.e("SettingManager",e.toString());
        }finally {
            try {
                fileInputStream.close();
                fileOutputStream.close();
            } catch (IOException e) {
                Loggout.e("SettingManager",e.toString());
            }
        }
    }

    @Override
    public synchronized String get(String key) {
        String get = null;
        try {
            fileInputStream = new FileInputStream(settings);
            properties.load(fileInputStream);
            get = properties.getProperty(key);
        } catch (IOException e) {
            Loggout.e("SettingManager",e.toString());
        }finally {
            try {
                fileInputStream.close();
            } catch (IOException e) {
                Loggout.e("SettingManager",e.toString());
            }
        }
        return get;
    }

    public synchronized void saveCache(InetAddress inetAddress)
    {
        String strAddr = inetAddress.getHostAddress();
        BufferedWriter writer = null;
        Set<String> addresses = getAllCachedThings();
        addresses.add(strAddr);
        try {
           writer = new BufferedWriter(new FileWriter(cache));
           String line;
           int count = 1;
         //  writer.write(strAddr);
        //   writer.newLine();
            for (String address : addresses) {
                if(count >= 10)
                {
                    break;
                }
                writer.write(address);
                writer.newLine();
                count++;
            }

        } catch (IOException e) {
            Loggout.e("SettingManager",e.toString());
        }finally {
            try {
                writer.close();
            } catch (Exception e) {
                Loggout.e("SettingManager",e.toString());
            }
        }
    }

    public synchronized Set<String> getAllCachedThings()
    {
        BufferedReader reader = null;
        Set<String> addresses = new HashSet<>();
        try {
            reader = new BufferedReader(new FileReader(cache));
            String line;
            while((line = reader.readLine()) != null)
            {
                addresses.add(line);
            }

        } catch (IOException e) {
            Loggout.e("SettingManager",e.toString());
        }finally {
            try {
                reader.close();
            } catch (Exception e) {
                Loggout.e("SettingManager",e.toString());
            }
        }
        return addresses;
    }


    @Override
    public synchronized void save(List<Command> commandList) {

    }

    public synchronized void saveValue(String key,String value)
    {
        try {
            fileInputStream = new FileInputStream(settings);
            fileOutputStream = new FileOutputStream(settings);
            properties.load(fileInputStream);
            properties.setProperty(key,value);
            properties.store(fileOutputStream,"add key");
        } catch (IOException e) {
            Loggout.e("SettingManager",e.toString());
        }finally {
            try {
                fileInputStream.close();
                fileOutputStream.close();
            } catch (IOException e) {
                Loggout.e("SettingManager",e.toString());
            }
        }
    }

    public String mockgetHash()
    {
        return mockId;
    }

    public void setMockId(String id)
    {
        mockId = id;
    }

    public String getKeyPath() {
        File file = new File(keyPath);
        if(!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                Loggout.e("SettingManager",e.toString());
            }
        }

        return keyPath;
    }
}
