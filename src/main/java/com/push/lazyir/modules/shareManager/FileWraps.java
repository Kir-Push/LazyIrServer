package com.push.lazyir.modules.shareManager;

import com.push.lazyir.pojo.Command;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by buhalo on 25.03.17.
 */
public class FileWraps {

    public FileWraps() {
        this.files = new ArrayList<>();
    }

    public FileWraps(List<FileWrap> files) {
        this.files = files;
    }

    public void addCommand(FileWrap cmd)
    {
        files.add(cmd);
    }

    private List<FileWrap> files;

    public List<FileWrap> getFiles() {
        return files;
    }

    public void setCommands(List<FileWrap> files) {
        this.files = files;
    }
}
