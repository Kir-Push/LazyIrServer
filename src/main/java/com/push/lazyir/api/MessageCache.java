package com.push.lazyir.api;

import com.push.lazyir.service.main.TcpConnectionManager;

import java.util.HashMap;

class MessageCache {
     private HashMap<String,String> cache = new HashMap<>();

    public void warm(String name, String id, String deviceType) {
        cache.put(TcpConnectionManager.TCP_PING,"{ \"id\": \"" +id +"\", \"name\": \"" + name + "\", \"deviceType\": \"" + deviceType + "\", \"type\": \"ping pong\", \"isModule\" : false}, \"data\": null");
        cache.put("GETINFO","{ \"multipleVids\": \"false\", \"command\": \"getInfo\" }");
    }

     public String getCachedMessage(String type) {
       return cache.get(type);
     }
 }
