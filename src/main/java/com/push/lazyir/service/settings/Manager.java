package com.push.lazyir.service.settings;

import com.push.lazyir.pojo.Command;

import java.util.List;


public interface Manager {

    void delete(String string);

    String get(String string);

    void save(List<Command> commands);

}