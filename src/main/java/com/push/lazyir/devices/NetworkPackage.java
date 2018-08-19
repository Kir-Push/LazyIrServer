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

import static com.push.lazyir.service.main.TcpConnectionManager.TCP_PING;
import static com.push.lazyir.service.main.UdpBroadcastManager.BROADCAST_INTRODUCE;
import static com.push.lazyir.service.main.UdpBroadcastManager.BROADCAST_INTRODUCE_MSG;
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

    public NetworkPackage(String type, String data) {
       // args = new ArrayList<>();
        factory = JsonNodeFactory.instance;
        idNode = factory.objectNode();
        idNode.put(ID,getMyId());
        idNode.put(NAME,getMyName());
        idNode.put(TYPE,type);
        idNode.put(DATA,data);
        idNode.put(DEVICE_TYPE,DEVICE);
    }

    public NetworkPackage(String message)
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

    @Override
    public String toString() {
        if(idNode != null)
            return getMessage();
        else
            return super.toString();
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

    public int getIntValue(String key){
        try {
            return idNode.get(key).intValue();
        }catch (NullPointerException e)
        {
          return 0;
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

    public static final String getMyId()
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

    public static String getMyName()
    {
        return System.getProperty("user.name");
    }


    public Device getDv() {
        return dv;
    }

    public void setDv(Device dv) {
        this.dv = dv;
    }




}
