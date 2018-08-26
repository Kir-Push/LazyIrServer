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
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class NetworkPackage {
    private String id;
    private String name;
    private String deviceType;
    private String type;
    private boolean isModule;
    private Dto data;


}
