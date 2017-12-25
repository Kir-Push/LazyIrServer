package com.push.lazyir.modules.clipboard;

import com.push.gui.basew.MainWin;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.net.URISyntaxException;

public class ClipboardJava extends Thread implements ClipboardOwner {
    private String clipboardData;
    Clipboard sysClip = Toolkit.getDefaultToolkit().getSystemClipboard();


    public void stopListening() {

    }

    public void setClipboardData(String clipboardData) {
        this.clipboardData = clipboardData;
    }

    public void startListening() {
        try {
            System.load(new File(MainWin.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile().getPath()+"/libServerClipboard.so");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        Transferable trans = sysClip.getContents(this);
        regainOwnership(trans);
        System.out.println("Listening to board...");
        while(true) {}
    }


    void processContents(Transferable t) {
        System.out.println("Processing: " + t);
    }

    void regainOwnership(Transferable t) {
        sysClip.setContents(t, this);
    }

    public static void main(String[] args) {
        ClipboardJava b = new ClipboardJava();
        b.start();
    }

    public void lostOwnership(Clipboard c, Transferable t) {
        try {
            this.sleep(20);
        } catch(Exception e) {
            System.out.println("Exception: " + e);
        }
        Transferable contents = sysClip.getContents(this);
        processContents(contents);
        regainOwnership(contents);
    }
}
