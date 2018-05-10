package com.push.lazyir.modules.screenShare;

public class ImgDiffEntity {
    private int startX;
    private int startY;
    private int longXY;

    private byte[] bytes;

    public ImgDiffEntity(int startX, int startY, int longXY, byte[] bytes) {
        this.startX = startX;
        this.startY = startY;
        this.longXY = longXY;
        this.bytes = bytes;
    }

    public int getStartX() {
        return startX;
    }

    public void setStartX(int startX) {
        this.startX = startX;
    }

    public int getStartY() {
        return startY;
    }

    public void setStartY(int startY) {
        this.startY = startY;
    }

    public int getLongXY() {
        return longXY;
    }

    public void setLongXY(int longXY) {
        this.longXY = longXY;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }
}
