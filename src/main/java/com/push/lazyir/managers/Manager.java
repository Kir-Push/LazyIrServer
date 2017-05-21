package com.push.lazyir.managers;

import com.push.lazyir.pojo.Command;

import java.util.List;

/**
 * Created by buhalo on 12.03.17.
 */
public interface Manager {

    void delete(String string);

    String get(String string);

    void save(List<Command> commands);

}
