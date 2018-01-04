package com.push.lazyir.service.settings;

import com.push.lazyir.Loggout;
import com.push.lazyir.devices.ModuleSetting;
import com.push.lazyir.modules.share.ShareModule;
import com.push.lazyir.pojo.Command;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by buhalo on 12.03.17.
 */
public class SettingManager implements Manager {

    private File settings;
    protected File cache;
    private File modules;
    private Properties properties;
    private FileInputStream fileInputStream;
    private FileOutputStream fileOutputStream;
    private String baseProp = "baseProp";
    // file contains modules and it's setting's
    private String confModules = "modules.xml";
    public static  String currentUsersHomeDir = System.getProperty("user.home") +  File.separator + ".Jasech" + File.separator + "ConnectedDevices";
    private String mockId = null;
    private String keyPath;

    public SettingManager()
    {
        baseProp = "baseProp";
        properties = new Properties();
        String settingFilePath = System.getProperty("user.home") + File.separator + ".Jasech";
        modules = new File(settingFilePath + File.separator + confModules);
        settings = new File(settingFilePath + File.separator+ "settingFile.ini");
        cache = new File(settingFilePath + File.separator + "adresses.txt");
        keyPath = settingFilePath + File.separator + "keyFile.pem";
        File dir = new File(settingFilePath);
        if(!dir.exists())
        {
            dir.mkdir();
        }
        if(!settings.exists() && !settings.isDirectory())
        {
            copyFromBackupToActual(baseProp,settings);
        }
        if(!modules.exists() && !modules.isDirectory()){
            copyFromBackupToActual("modules.xml",modules);
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

    /*
    get enabled modules from xml list, and instantiate ModuleSetting entity
    return the list
    * */
    public List<ModuleSetting> getMyEnabledModules(){
        List<ModuleSetting> modulesResultList = new ArrayList<>();
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(modules);
            NodeList elements = doc.getElementById("modules").getElementsByTagName("module");
            for(int i = 0;i<elements.getLength();i++){
                Element item = (Element)elements.item(i);
                Boolean enabled = new Boolean(item.getElementsByTagName("enabled").item(0).getTextContent());
                if(enabled) {
                    String name = item.getElementsByTagName("name").item(0).getTextContent();
                    Boolean workOnly = new Boolean(item.getElementsByTagName("workOnly").item(0).getTextContent());
                    List<String> ignoredIds = new ArrayList<>();
                    NodeList ignoredElements = item.getElementsByTagName("ignored");
                    int ignoredL = ignoredElements.getLength();
                    for (int c = 0; c < ignoredL; c++) {
                        Node ignoredItem = ignoredElements.item(c);
                        ignoredIds.add(ignoredItem.getTextContent());
                    }
                    modulesResultList.add(new ModuleSetting(name,enabled,ignoredIds,workOnly));
                }
            }
        }catch (Exception e){
            Loggout.e("SettingManager","Error in getMyEnabledModules ",e);
        }
        return modulesResultList;
    }

    //clear device internal sftp temp folder's
    public void clearFolders(){
        File file = new File(currentUsersHomeDir);
        File[] files = file.listFiles();
        if (files != null)
            for (File file1 : files) {
                file1.delete();
            }

    }

    private synchronized void copyFromBackupToActual(String internalFile,File toWhoCopy) {
        try {
            ClassLoader classLoader =getClass().getClassLoader();
            String file = classLoader.getResource(internalFile).getFile();
            fileInputStream = new FileInputStream(file);
                    properties.load(fileInputStream);
            if(settings.createNewFile())
            {
                fileOutputStream = new FileOutputStream(toWhoCopy);
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

    public synchronized String getKeyPath() {
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
