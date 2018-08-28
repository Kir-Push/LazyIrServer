package com.push.lazyir.service.managers.settings;

import com.push.lazyir.devices.ModuleSetting;
import com.push.lazyir.modules.command.Command;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;


@Slf4j
public class SettingManager implements Manager {

    private File settings;
    protected File cache;
    private File modules;
    private Properties properties;
    public static final  String CURRENT_USERS_HOME_DIR = System.getProperty("user.home") +  File.separator + ".Jasech" + File.separator + "ConnectedDevices";


    private void copyFromBackupToActualXml(String s, File modules) {
        ClassLoader classLoader =getClass().getClassLoader();
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(classLoader.getResourceAsStream(s), "UTF-8"));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(modules.toPath()), "UTF-8"))) {
            if (modules.createNewFile()) {
                String read;
                while((read = reader.readLine()) != null){
                    writer.write(read);
                }
            }
        }catch (IOException e){
            log.error("error in copyFromBackupToActualXml from: "+ s + " to: " + modules.getAbsolutePath());
        }
    }

    /*
    get enabled modules from xml list, and instantiate ModuleSetting entity
    return the list
    * */
    @Synchronized
    public List<ModuleSetting> getMyEnabledModules(){
        List<ModuleSetting> modulesResultList = new ArrayList<>();
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(modules);
            NodeList elements = doc.getDocumentElement().getElementsByTagName("module");
            for(int i = 0;i<elements.getLength();i++){
                Element item = (Element)elements.item(i);
                Boolean enabled = Boolean.valueOf(item.getElementsByTagName("enabled").item(0).getTextContent());
                if(enabled) {
                    String name = item.getElementsByTagName("name").item(0).getTextContent();
                    Boolean workOnly = Boolean.valueOf(item.getElementsByTagName("workOnly").item(0).getTextContent());
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
            log.error("error in getMyEnabledModules",e);
        }
        return modulesResultList;
    }

    //clear device internal sftp temp folder's
    @Synchronized
    public void clearFolders(){
        File file = new File(CURRENT_USERS_HOME_DIR);
        File[] files = file.listFiles();
        if (files != null) {
            for (File fl : files) {
                    try {
                        if(fl.exists()) {
                            Files.delete(Paths.get(fl.getPath()));
                        }
                    } catch (IOException e) {
                        log.info("can't delete file: "+ fl.getAbsolutePath(),e);
                    }
                }
            }
    }

    @Synchronized
    private void copyFromBackupToActual(String internalFile,File toWhoCopy) {
        ClassLoader classLoader =getClass().getClassLoader();
        try(OutputStream fileOutputStream = Files.newOutputStream(toWhoCopy.toPath());
             InputStream is = classLoader.getResourceAsStream(internalFile)) {
            properties.load(is);
            boolean exist = toWhoCopy.exists();
            if(!exist) {
               exist = toWhoCopy.createNewFile();
            }
            if(exist){
                properties.store(fileOutputStream,"hey ja");
            }
        } catch (IOException e) {
            log.error("error while copyFromBackupToActual internalFile: " + internalFile + " toWhoCopy: " + toWhoCopy);
        }
    }



    @Override
    @Synchronized
    public void delete(String key) {
        try(InputStream fileInputStream = Files.newInputStream(settings.toPath());
            OutputStream fileOutputStream = Files.newOutputStream(settings.toPath())) {
            properties.load(fileInputStream);
            properties.remove(key);
            properties.store(fileOutputStream,"removed key");
        } catch (IOException e) {
            log.error("error in delete key: " + key);
        }
    }

    @Override
    @Synchronized
    public String get(String key) {
        return properties.getProperty(key);
    }

    @Synchronized
    public void saveCache(InetAddress inetAddress){
        String strAddr = inetAddress.getHostAddress();
        Set<String> addresses = getAllCachedThings();
        addresses.add(strAddr);
        try(BufferedWriter writer = Files.newBufferedWriter(cache.toPath())) {
           int count = 1;
            for (String address : addresses) {
                if(count >= 20) {
                    break;
                }
                writer.write(address);
                writer.newLine();
                count++;
            }
        } catch (IOException e) {
            log.error("error in saveCache",e);
        }
    }

    @Synchronized
    public Set<String> getAllCachedThings() {
        Set<String> addresses = new HashSet<>();
        try(BufferedReader reader = Files.newBufferedReader(cache.toPath())) {
            String line;
            while((line = reader.readLine()) != null) {
                addresses.add(line);
            }
        } catch (IOException e) {
            log.error("error in getAllCachedThings",e);
        }
        return addresses;
    }


    @Override
    public void save(List<Command> commandList) {
        //nothing to do
    }

    @Synchronized
    public void saveValue(String key,String value) {
        try(InputStream fileInputStream = Files.newInputStream(settings.toPath());
            OutputStream fileOutputStream = Files.newOutputStream(settings.toPath())) {
            properties.load(fileInputStream);
            properties.setProperty(key,value);
            properties.store(fileOutputStream,"add key");
        } catch (IOException e) {
            log.error("error while save value - key: " + key + " value: " + value,e);
        }
    }

    public void init() {
        properties = new Properties();
        String settingFilePath = System.getProperty("user.home") + File.separator + ".Jasech";
        createFolders(settingFilePath);
        populateProperties();
    }

    private void populateProperties() {
        try(InputStream fileInputStream = Files.newInputStream(settings.toPath())) {
            properties.load(fileInputStream);
        } catch (IOException e) {
            log.error("error in populateProperties",e);
        }
    }

    private void createFolders(String settingFilePath) {
        File dir = new File(settingFilePath);
        if(!dir.exists()) {
            dir.mkdir();
        }
        createLocalizationFiles(settingFilePath + File.separator + "localization");
        createSettingsFiles("baseProp",settingFilePath);
        createModulesFiles("modules.xml",settingFilePath);
        createCacheFiles("adresses.txt",settingFilePath);


    }

    private void createCacheFiles(String fileName, String settingFilePath) {
        cache = new File(settingFilePath + File.separator + fileName);
        if(!cache.exists()) {
            try {
                Files.createFile(cache.toPath());
            } catch (IOException e) {
                log.error("error while creating cache file",e);
            }
        }
    }

    private void createModulesFiles(String modulesBackup, String settingFilePath) {
        // file contains modules and it's setting's
        String confModules = "modules.xml";
        modules = new File(settingFilePath + File.separator + confModules);
        if(!modules.exists() || !modules.isDirectory()){
            copyFromBackupToActualXml(modulesBackup,modules);
        }

    }

    private void createSettingsFiles(String baseProp, String settingFilePath) {
        settings = new File(settingFilePath + File.separator+ "settingFile.ini");
        if(!settings.exists() || !settings.isDirectory()) {
            copyFromBackupToActual(baseProp,settings);
        }
    }

    private void createLocalizationFiles(String localizationPath) {
        File localization = new File(localizationPath);
        File engLocF =new File(localizationPath + File.separator + "loc_eng.ini");
        File rusLocF = new File(localizationPath + File.separator + "loc_rus.ini");
        if(!localization.exists()){
            localization.mkdir();
        }
        if(!engLocF.exists() || !engLocF.isDirectory()){
            String engLoc = "engLoc";
            copyFromBackupToActual(engLoc,engLocF);
        }
        if(!rusLocF.exists() || !rusLocF.isDirectory()){
            String rusLoc = "russianLoc";
            copyFromBackupToActual(rusLoc,rusLocF);
        }
    }
}
