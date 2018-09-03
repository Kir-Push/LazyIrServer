package com.push.lazyir.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;

@Slf4j
public class MessageFactory {
    private DtoSerializer dtoSerializer;
    private Gson gson;
    private MessageCache cache;
    private String name;
    private String id;
    private String deviceType; //This variable depends on app version(pc or android)

    @Inject
    public MessageFactory(DtoSerializer dtoSerializer) {
        this.dtoSerializer = dtoSerializer;
        init();
    }

    private void init() {
        try {
        gson = new GsonBuilder().registerTypeAdapter(Dto.class,dtoSerializer).create();
        name =  System.getProperty("user.name");
        deviceType = "pc";
        id = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            id = "SomeErrorHostName"+ new Random().nextInt();
            log.error("getMyId error - generated failback id: " + id,e);
        }
        cache = new MessageCache();
        cache.warm();
    }

    public NetworkPackage parseMessage(String msg){
       return gson.fromJson(msg,NetworkPackage.class);
    }

    private String serialize(NetworkPackage np){
        return gson.toJson(np);
    }

    private NetworkPackage createNetworkPackage(String type,boolean isModule,Dto dto){
        return new NetworkPackage(id,name,deviceType,type,isModule,dto);
    }

    public String createMessage(String type,boolean isModule,Dto dto){
       String msg = getCachedMessage(type);
       return msg != null ? msg : serialize(createNetworkPackage(type,isModule,dto));
    }

    public String getCachedMessage(String type){
        return cache.getCachedMessage(type);
    }


}
