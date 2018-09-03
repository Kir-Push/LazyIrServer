package com.push.lazyir.api;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Json message entity class
 * id - sender's id
 * name - sender's name
 * deviceType - type of sender - phone,pc,tablet
 * type - contain name of the data final recipient - module, or basic cmd
 * isModule - true when data is module object
 * data - depend of type field contain of specific module dto, or some other cmd
 * dto type calculated {type + 'dto'}
 */
@Data
public class NetworkPackage {
    private String id;
    private String name;
    private String deviceType;
    private String type;
    private boolean isModule;
    private Dto data;


    NetworkPackage(String id, String name, String deviceType, String type, boolean isModule, Dto data) {
        this.id = id;
        this.name = name;
        this.deviceType = deviceType;
        this.type = type;
        this.isModule = isModule;
        this.data = data;
        if(data != null){
            data.setClassName(type);
            data.setModule(isModule);
        }
    }
}
