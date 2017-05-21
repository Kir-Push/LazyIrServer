package com.push.lazyir.modules.shareManager;


/**
 * Created by buhalo on 05.03.17.
 */

public class FileWrap {
    private boolean received; // 0 false 1 true;
    private boolean file; // 0 folder 1 file;
    private int res_id;
    private String path;
    private boolean checked;


    public FileWrap() {
    }

    public FileWrap(boolean received, boolean file, String path) {
        this.received = received;
        this.file = file;
        this.path = path;
        this.checked = false;
        if(file)
        {
         //   res_id = R.mipmap.file_icn;
        }
        else
        {
        //    res_id = R.mipmap.folder_icn;
        }
    }

    public FileWrap(String path) {
        this(false,false,path);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isFile() {
        return file;
    }

    public void setFile(boolean file) {
        this.file = file;
    }

    public boolean isReceived() {
        return received;
    }

    public void setReceived(boolean received) {
        this.received = received;
    }

    public int getRes_id() {
        return res_id;
    }

    public void setRes_id(int res_id) {
        this.res_id = res_id;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}
