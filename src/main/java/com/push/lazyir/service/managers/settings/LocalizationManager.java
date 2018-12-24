package com.push.lazyir.service.managers.settings;

import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

/*
must be always initialized after setting's manager, because he create non existing file's, directories and so on.
* */
@Slf4j
public class LocalizationManager {

    private String currLang;
    private Map<String,String> localStrings = new HashMap<>();

//    https://native2ascii.net/
    @Synchronized
    public String get(String key){
        return localStrings.getOrDefault(key, "text missing");
    }
    /*
    return true if file with selected lang exist, false otherwise
    * */
    @Synchronized
    public boolean changeLanguage(String newLang){
        currLang = newLang;
        return loadLangStrings();
    }

    /*
    load string in hashmap from file
    * */
    private boolean loadLangStrings() {
        try{
            localStrings.clear();
         String locPath =  System.getProperty("user.home") + File.separator + ".Jasech"
                 + File.separator + "localization" + File.separator + "loc_" + currLang + ".ini";
         File file = new File(locPath);
         boolean exists = file.exists();
         if(exists){
             try(InputStream fileInputStream = Files.newInputStream(file.toPath())){
                 Properties properties = new Properties();
                 properties.load(fileInputStream);
                 properties.stringPropertyNames().forEach(s -> localStrings.put(s,properties.getProperty(s)));
             }
         }
         return exists;
        } catch (IOException e) {
            log.error("error in loadLangStrings",e);
            return false;
        }
    }

    public Set<String> getLangs(){
        Set<String> set = new HashSet<>();
        String locPath =  System.getProperty("user.home") + File.separator + ".Jasech"
                + File.separator + "localization";
        File file = new File(locPath);
        File[] files = file.listFiles();
        if(files == null) {
            return set;
        }
        for (File fl : files) {
           if(fl.getName().matches("^loc_\\w{3}.ini")){
               set.add(fl.getName().split(".ini")[0].substring(4));
           }
        }
        return set;
    }


    public void init() {
        //nothing to do here
    }
}
