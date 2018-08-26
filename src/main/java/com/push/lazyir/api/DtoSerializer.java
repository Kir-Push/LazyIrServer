package com.push.lazyir.api;

import com.google.gson.*;
import com.push.lazyir.modules.ModuleFactory;
import com.push.lazyir.service.dto.NetworkDtoRegister;

import javax.inject.Inject;
import java.lang.reflect.Type;

public class DtoSerializer implements JsonSerializer<Dto>, JsonDeserializer<Dto> {
    private ModuleFactory moduleFactory;
    private NetworkDtoRegister ndtoRegister;

    @Inject
    public DtoSerializer(ModuleFactory moduleFactory,NetworkDtoRegister ndtoRegister) {
        this.moduleFactory = moduleFactory;
        this.ndtoRegister = ndtoRegister;
    }

    @Override
    public Dto deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        JsonObject jsonObject = json.getAsJsonObject();
        String type = jsonObject.get("type").getAsString();
        boolean isModule = jsonObject.get("isModule").getAsBoolean();
        if(isModule) {
            return context.deserialize(json,moduleFactory.getModuleDto(type));
        }else{
            return context.deserialize(json,ndtoRegister.getBaseDto(type));
        }
    }

    @Override
    public JsonElement serialize(Dto src, Type typeOfSrc, JsonSerializationContext context) {
        return context.serialize(src,typeOfSrc);
    }
}
