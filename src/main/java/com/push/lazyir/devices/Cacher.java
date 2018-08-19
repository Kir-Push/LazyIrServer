package com.push.lazyir.devices;

import com.push.lazyir.modules.dbus.Mpris;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.push.lazyir.modules.dbus.Mpris.ALL_PLAYERS;
import static com.push.lazyir.service.main.TcpConnectionManager.TCP_PING;
import static com.push.lazyir.service.main.UdpBroadcastManager.BROADCAST_INTRODUCE;
import static com.push.lazyir.service.main.UdpBroadcastManager.BROADCAST_INTRODUCE_MSG;

// caching for intensibly usable networkPackets
public class Cacher
{

    private final static  NetworkPackage pingPackage = new NetworkPackage(TCP_PING,TCP_PING);
    private final static NetworkPackage introducePackage = new NetworkPackage(BROADCAST_INTRODUCE,BROADCAST_INTRODUCE_MSG);
    private final static NetworkPackage mprisPackage = new NetworkPackage(Mpris.class.getSimpleName(), ALL_PLAYERS);
    //main Container for cache
    private ConcurrentHashMap<Integer,NetworkPackage> networkPackageCache = new ConcurrentHashMap<>();
    // counter of most usable NetworkPackages by their hash key=hash,value=numberOfUsage
    private ConcurrentHashMap<Integer,Integer> usableCounter = new ConcurrentHashMap<>();

    public Cacher() { }

    public void addToCache(Integer key,NetworkPackage networkPackage)
    {
        networkPackageCache.put(key,networkPackage);
        clearIfNeeded();
    }

    //if in cache more than 20 items clear less useful items
    private void clearIfNeeded() {
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

    private NetworkPackage getFromCache(Integer hash) {
        return networkPackageCache.get(hash);
    }

    //try get from cache, if null create and return new Object, increase counter by one, and add to cache if number ~10
    public NetworkPackage getOrCreatePackage(String type, String data) {
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

    private NetworkPackage checkForMostUsefulTypes(String type, String data) {
        if (type.equals(TCP_PING))
            return pingPackage;
        else if (type.equals(BROADCAST_INTRODUCE))
            return introducePackage;
        else if (type.equals(Mpris.class.getSimpleName()) && mprisPackage.getData().equals(data))
            return mprisPackage;
        return null;
    }

    //similar to other method, but for parsing answer message
    public NetworkPackage getOrCreatePackage(String message) {
        int hash = message.hashCode();
        NetworkPackage result = getFromCache(hash);
        if (result == null) {
            result = createNewNetworkPackage(message);
            countCacheAndAdd(hash, result);
        }
        return result;
    }

    private void countCacheAndAdd(int hash, NetworkPackage np) {
        // get value from map, increment it, and put back. Check if size of map
        // >= 10 and add to cache if true.
        Integer integer = usableCounter.get(hash);
        if(integer == null)
            integer = 0;
        usableCounter.put(hash, integer + 1);
        if (usableCounter.get(hash) >= 10)
            addToCache(hash, np);
    }

    private NetworkPackage createNewNetworkPackage(String message) {
        return new NetworkPackage(message);
    }

    private NetworkPackage createNewNetworkPackage(String type, String data) {
        return new NetworkPackage(type, data);
    }

    public int getHash(String type, String data) {
        return type.hashCode() | data.hashCode();
    }

}