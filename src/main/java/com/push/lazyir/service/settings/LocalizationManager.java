package com.push.lazyir.service.settings;

import com.push.lazyir.Loggout;
import com.push.lazyir.service.BackgroundService;

import java.io.*;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
/*
must be always initialized after setting's manager, because he create non existing file's, directories and so on.
* */
public class LocalizationManager {

    private volatile String currLang;
    private HashMap<String,String> localStrings = new HashMap<>();

    private Lock lock = new ReentrantLock();

    public LocalizationManager() {
    }

//    https://native2ascii.net/
    public String get(String key){
        lock.lock();
        try {
            String text_missing = localStrings.getOrDefault(key, "text missing");
            return text_missing;
        } finally {
            lock.unlock();
        }
    }


    /*
    return true if file with selected lang exist, false otherwise
    * */
    public boolean changeLanguage(String newLang){
        lock.lock();
        try {
            String oldLang = currLang;
            currLang = newLang;
            boolean loaded = loadLangStrings();
            return loaded;
        }finally {
            lock.unlock();
        }
    }

    /*
    load string in hashmap from file
    * */
    private boolean loadLangStrings() {
        lock.lock();
        boolean exists = false;
        try{
            localStrings.clear();
         String locPath =  System.getProperty("user.home") + File.separator + ".Jasech"
                 + File.separator + "localization" + File.separator + "loc_" + currLang + ".ini";
         File file = new File(locPath);
          exists = file.exists();
         if(exists){
             FileInputStream fileInputStream = new FileInputStream(file);
             Properties properties = new Properties();
             properties.load(fileInputStream);
             for (String s : properties.stringPropertyNames()) {
                 localStrings.put(s,properties.getProperty(s));
             }
         }
         return exists;
        } catch (IOException e) {
            Loggout.e("LocalizationManger","error in loadLangStrings ",e);
            return exists;
        } finally {
            lock.unlock();
        }
    }


}
