package com.push.lazyir.devices;



import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.push.lazyir.Loggout;
import com.push.lazyir.modules.dbus.Mpris;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.push.lazyir.service.TcpConnectionManager.TCP_PING;
import static com.push.lazyir.service.UdpBroadcastManager.BROADCAST_INTRODUCE;
import static com.push.lazyir.service.UdpBroadcastManager.BROADCAST_INTRODUCE_MSG;
import static com.push.lazyir.modules.dbus.Mpris.ALL_PLAYERS;

/**
 * Created by buhalo on 05.03.17.
 */


public class NetworkPackage {
    public final static String ID = "id";
    public final static String NAME = "name";
    public final static String TYPE = "type";
    public final static String DATA = "data";
    public final static String N_OBJECT = "object";
    public final static String DEVICE_TYPE = "deviceType";


    private final static  NetworkPackage pingPackage = new NetworkPackage(TCP_PING,TCP_PING);
    private final static NetworkPackage introducePackage = new NetworkPackage(BROADCAST_INTRODUCE,BROADCAST_INTRODUCE_MSG);
    private final static NetworkPackage mprisPackage = new NetworkPackage(Mpris.class.getSimpleName(), ALL_PLAYERS);


    //todo in Android version
    //This variable depends on app version(pc or android)
    private final static String DEVICE = "pc";

    private Device dv;

    private String msg;

    JsonNodeFactory factory;
    private ObjectNode idNode;

    public ObjectNode getIdNode() {
        return idNode;
    }

    public void setIdNode(ObjectNode idNode) {
        this.idNode = idNode;
    }

    private NetworkPackage(String type, String data) {
       // args = new ArrayList<>();
        factory = JsonNodeFactory.instance;
        idNode = factory.objectNode();
        idNode.put(ID,getMyId());
        idNode.put(NAME,getMyName());
        idNode.put(TYPE,type);
        idNode.put(DATA,data);
        idNode.put(DEVICE_TYPE,DEVICE);
    }

    private NetworkPackage(String message)
    {
        this.msg = message;
        parseMessage();
    }

    public final void parseMessage()
    {
        ObjectMapper mapper = new ObjectMapper();
        try {
            idNode = (ObjectNode) mapper.readTree(msg);
        } catch (IOException e) {
            Loggout.e("NetworkPackage","Error in Parse message",e);
        }
    }

    public void addStringArray(String key,List<String> list)
    {
        ArrayNode arrayNode = idNode.putArray(key);
        for(String st : list)
        {
            arrayNode.add(st);
        }
    }

    public String getMessage()
    {
      return idNode.toString();
    }


    @Override
    public int hashCode() {
        return msg != null ? msg.hashCode() : 0;
    }

    public String getType()
    {
        return getValue(TYPE);
    }

    public String getData()
    {
        return getValue(DATA);
    }

    public String getValue(String key)
    {
        try {
            return idNode.get(key).textValue();
        }catch (NullPointerException e)
        {
            return null;
        }

    }

    public double getDouble(String key)
    {
        try {
            return idNode.get(key).asDouble();
        }catch (NullPointerException e)
        {
            return 0;
        }
    }

    public void setValue(String key,String value)
    {
        idNode.put(key,value);
    }

    public <T> NetworkPackage setObject(String key,T object)
    {
        JsonNode node = new ObjectMapper().convertValue(object, JsonNode.class);
        idNode.set(key,node);
        return this;
    }

    public <T> T getObject(String key,Class<T> tClass)
    {
        String typeName = idNode.get(TYPE).textValue();
        JsonNode object = idNode.get(key);
        try {
            return new ObjectMapper().readValue(object.toString(),tClass);
        } catch (Exception e) {
            Loggout.e("NetworkPackage","Error in getObject ",e);
        }
        return null;
    }

    public final String getMyId()
    {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return   "SomeErrorHostName"+ new Random().nextInt();
        }
    }

    public String getId()
    {
        return idNode.get(ID).textValue();
    }

    public String getName()
    {
        return idNode.get(NAME).textValue();
    }

    public String getMyName()
    {
        return System.getProperty("user.name");
    }


    public Device getDv() {
        return dv;
    }

    public void setDv(Device dv) {
        this.dv = dv;
    }



    // caching for intensibly usable networkPackets
    public static class Cacher
    {
        //main Container for cache
        private static ConcurrentHashMap<Integer,NetworkPackage> networkPackageCache = new ConcurrentHashMap<>();
        // counter of most usable NetworkPackages by their hash key=hash,value=numberOfUsage
        private static ConcurrentHashMap<Integer,Integer> usableCounter = new ConcurrentHashMap<>();

        public Cacher() { }

        public static void addToCache(Integer key,NetworkPackage networkPackage)
        {
            networkPackageCache.put(key,networkPackage);
            clearIfNeeded();
        }

        //if in cache more than 20 items clear less useful items
        private static void clearIfNeeded() {
            if (networkPackageCache.size() > 20) {
                int lessValue = Integer.MAX_VALUE;
                int hashForRemove = -1;
                for (Map.Entry<Integer, Integer> integerIntegerEntry : usableCounter.entrySet()) {
                    Integer value = integerIntegerEntry.getValue();
                    if (lessValue > value) {
                        lessValue = value;
                        hashForRemove = integerIntegerEntry.getKey();
                    }
                }
                networkPackageCache.remove(hashForRemove);
                usableCounter.remove(hashForRemove);
            }
        }

        private static NetworkPackage getFromCache(Integer hash) {
            return networkPackageCache.get(hash);
        }

        //try get from cache, if null create and return new Object, increase counter by one, and add to cache if number ~10
        public static NetworkPackage getOrCreatePackage(String type, String data) {
            NetworkPackage networkPackage;
            if ((networkPackage = checkForMostUsefulTypes(type, data)) != null)
                return networkPackage;
            int hash = getHash(type, data);

            NetworkPackage result = getFromCache(hash);
            if (result == null) {
                result = createNewNetworkPackage(type, data);
                countCacheAndAdd(hash, result);
            }
            return result;
        }

        private static NetworkPackage checkForMostUsefulTypes(String type, String data) {
            if (type.equals(TCP_PING))
                return pingPackage;
            else if (type.equals(BROADCAST_INTRODUCE))
                return introducePackage;
            else if (type.equals(Mpris.class.getSimpleName()) && mprisPackage.getData().equals(data))
                return mprisPackage;
            return null;
        }

        //similar to other method, but for parsing answer message
        public static NetworkPackage getOrCreatePackage(String message) {
            int hash = message.hashCode();
            NetworkPackage result = getFromCache(hash);
            if (result == null) {
                result = createNewNetworkPackage(message);
                countCacheAndAdd(hash, result);
            }
            return result;
        }

        private static void countCacheAndAdd(int hash, NetworkPackage np) {
            // get value from map, increment it, and put back. Check if size of map
            // >= 10 and add to cache if true.
            Integer integer = usableCounter.get(hash);
            if(integer == null)
                integer = 0;
            usableCounter.put(hash, integer + 1);
            if (usableCounter.get(hash) >= 10)
                addToCache(hash, np);
        }

        private static NetworkPackage createNewNetworkPackage(String message) {
            return new NetworkPackage(message);
        }

        private static NetworkPackage createNewNetworkPackage(String type, String data) {
            return new NetworkPackage(type, data);
        }

    }

    public static int getHash(String type, String data) {
        return type.hashCode() | data.hashCode();
    }
}
