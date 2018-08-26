package com.push.lazyir.devices;

import com.push.lazyir.modules.dbus.Mpris;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.push.lazyir.modules.dbus.Mpris.ALL_PLAYERS;
import static com.push.lazyir.service.main.TcpConnectionManager.TCP_PING;
import static com.push.lazyir.service.main.UdpBroadcastManager.BROADCAST_INTRODUCE;
import static com.push.lazyir.service.main.UdpBroadcastManager.BROADCAST_INTRODUCE_MSG;

// caching for intensibly usable networkPackets
public class CacherOld
{

    private final static NetworkPackageOld pingPackage = new NetworkPackageOld(TCP_PING,TCP_PING);
    private final static NetworkPackageOld introducePackage = new NetworkPackageOld(BROADCAST_INTRODUCE,BROADCAST_INTRODUCE_MSG);
    private final static NetworkPackageOld mprisPackage = new NetworkPackageOld(Mpris.class.getSimpleName(), ALL_PLAYERS);
    //main Container for cache
    private ConcurrentHashMap<Integer, NetworkPackageOld> networkPackageCache = new ConcurrentHashMap<>();
    // counter of most usable NetworkPackages by their hash key=hash,value=numberOfUsage
    private ConcurrentHashMap<Integer,Integer> usableCounter = new ConcurrentHashMap<>();

    public CacherOld() { }

    public void addToCache(Integer key, NetworkPackageOld networkPackage)
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

    private NetworkPackageOld getFromCache(Integer hash) {
        return networkPackageCache.get(hash);
    }

    //try get from cache, if null create and return new Object, increase counter by one, and add to cache if number ~10
    public NetworkPackageOld getOrCreatePackage(String type, String data) {
        NetworkPackageOld networkPackage;
        if ((networkPackage = checkForMostUsefulTypes(type, data)) != null)
            return networkPackage;
        int hash = getHash(type, data);

        NetworkPackageOld result = getFromCache(hash);
        if (result == null) {
            result = createNewNetworkPackage(type, data);
            countCacheAndAdd(hash, result);
        }
        return result;
    }

    private NetworkPackageOld checkForMostUsefulTypes(String type, String data) {
        if (type.equals(TCP_PING))
            return pingPackage;
        else if (type.equals(BROADCAST_INTRODUCE))
            return introducePackage;
        else if (type.equals(Mpris.class.getSimpleName()) && mprisPackage.getData().equals(data))
            return mprisPackage;
        return null;
    }

    //similar to other method, but for parsing answer message
    public NetworkPackageOld getOrCreatePackage(String message) {
        int hash = message.hashCode();
        NetworkPackageOld result = getFromCache(hash);
        if (result == null) {
            result = createNewNetworkPackage(message);
            countCacheAndAdd(hash, result);
        }
        return result;
    }

    private void countCacheAndAdd(int hash, NetworkPackageOld np) {
        // get value from map, increment it, and put back. Check if size of map
        // >= 10 and add to cache if true.
        Integer integer = usableCounter.get(hash);
        if(integer == null)
            integer = 0;
        usableCounter.put(hash, integer + 1);
        if (usableCounter.get(hash) >= 10)
            addToCache(hash, np);
    }

    private NetworkPackageOld createNewNetworkPackage(String message) {
        return new NetworkPackageOld(message);
    }

    private NetworkPackageOld createNewNetworkPackage(String type, String data) {
        return new NetworkPackageOld(type, data);
    }

    public int getHash(String type, String data) {
        return type.hashCode() | data.hashCode();
    }

}