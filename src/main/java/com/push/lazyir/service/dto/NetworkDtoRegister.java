package com.push.lazyir.service.dto;


import com.push.lazyir.service.main.TcpConnectionManager;

public class NetworkDtoRegister {

    public Class getBaseDto(String type){
        if(type.equalsIgnoreCase(TcpConnectionManager.api.TCP.name())){
            return TcpDto.class;
        }
       return null;
   }
}
