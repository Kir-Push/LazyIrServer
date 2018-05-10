package com.push.lazyir.modules.screenShare;

import com.push.lazyir.Loggout;
import com.push.lazyir.modules.screenShare.enity.AuthInfo;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/*
Get screenshot of desktop,
detect changes from previous screenshot (if was),
compress changes (or image) using gzip stream's.
have setting for min. pixel number detecting - from which number of pixel's
consider to change. Pixel checking only by squares. and y == x;
It's singleton class, and using listener pattern.
* */
public class ScreenRobot {

    private static ScreenRobot INSTANCE;

    private static ScreenShotJNi screenShotJNi;
    private static final ReentrantLock lock = new ReentrantLock();
    private List<ImgDiffEntity> diffList = new ArrayList<>();
    private byte[] lastImage;

    private volatile boolean serverWork = false;
    private HashMap<String,AuthInfo> listeners = new HashMap<>();
    private volatile int port = 6667;

    private ScreenRobot()  {
        screenShotJNi = new ScreenShotJNi(null);
    }

    public static ScreenRobot getInstance()  {
        lock.lock();
        try {
            if (INSTANCE == null)
                INSTANCE = new ScreenRobot();
            return INSTANCE;
        }finally {
            lock.unlock();
        }
    }

    /*
    Full screen capture
    * */
    private byte[] getScreenShot(){
        byte[] screenShot = new byte[0];
        try {
            screenShot = screenShotJNi.getScreenShot();
        } catch (Exception e){
            Loggout.e("ScreenRobot","Error in getScreenshot",e);
        }
        return screenShot;
    }

    /*
    Return number of differences
    * */
    private int getDiffs(int diffMin,byte[] currImage,int xSize,int ySize){

        int height = ySize;
        int width = xSize;
        byte[] currImgpixels = currImage;
        byte[] oldImgpixels =lastImage;

        // we have array of bytes, but pixel consist of 3 bytes, so we need
        // to iterate over each color of pixel.
        for(int i =0;i<3;i++) {
            // actually it's 1/3 size of image, we need add it to result coordinate, in second and third loop
            // to calculate 2/3 and 3/3 part of image.
            int addedvalue = ySize * xSize;

            int xsquaresCount = width / diffMin;
            int ysquaresCount = height / diffMin;

            // we separate image by squares, and fist iterate over y coord square
            for (int ysquare = 0; ysquare < ysquaresCount; ysquare++) {

                // calculate first absolute y coordinate in this square
                int currYstart = ysquare == 0 ? 0 : ysquare * diffMin;

                // in each column (y) we go over row (x), square by square
                for (int xSquare = 0; xSquare < xsquaresCount; xSquare++) {
                    // temp array of byte, where will be saved chaged pixels.
                    // size is - square with side of min difference
                    byte[] currChangeBytes = new byte[diffMin * diffMin];
                    // array currChangeBytes, we save byte's one by one, without coordinates.
                    int newByteArrayCount = 0;
                    // current number of difference in this square
                    int numberOfDiff = 0;
                    // calculate first absolute x coordinate in this square
                    int currXtart = xSquare == 0 ? 0 : xSquare * diffMin;
                    // iterate over current square start from y coordinate
                    for (int y = currYstart; y < currYstart + diffMin; y++) {
                        // first part of absolute coordinate in image array.
                        // it's start of row, in which we have our pixel.
                        int coorY = width * y;
                        // iterate over current square - x coordinate
                        for (int x = currXtart; x < currXtart + diffMin; x++) {
                            // complete coordinate in array - sum of start row and number in row
                            int coordInArray = coorY + x;
                            // if we in second loop in outer 0-3 loop
                            // it means we go over 2/3 part of image
                            if(i >= 1){
                                coordInArray += addedvalue;
                                // if we in third loop - add value again.
                                if(i == 2){
                                    coordInArray += addedvalue;
                                }
                            }
                            // we fill all temp array, but add only if we have needed number of differences
                            currChangeBytes[newByteArrayCount] = currImgpixels[coordInArray];
                            if (currImgpixels[coordInArray] != oldImgpixels[coordInArray]) {
                                numberOfDiff++;
                            }

                            newByteArrayCount++;
                        }
                    }
                    if (numberOfDiff >= diffMin) {
                        // create entity with x and y coords start(absolute), square size and array of bytes
                        ImgDiffEntity imgDiffEntity = new ImgDiffEntity(currXtart, currYstart, diffMin, currChangeBytes);
                        // add it to list, to process from another place
                        diffList.add(imgDiffEntity);
                    }
                }
            }
        }
        return diffList.size();
    }

    // recipient register, and start receive screenshots
    // until it will be unregistered
    public AuthInfo register(String dvId) throws ScreenCastException{
        lock.lock();
        try{
            boolean isAllOkey = true;
            if(!serverWork)
                isAllOkey = startServer();
            if(!isAllOkey)
                throw new ScreenCastException("Cant start Server");
            String token = generateToken();
            AuthInfo authInfo = new AuthInfo(port, token);
            listeners.put(dvId,authInfo);
            return authInfo;
        }finally {
            lock.unlock();
        }
    }

    public void unRegister(String dvId){
        lock.lock();
        try{
            listeners.remove(dvId);
            if(listeners.size() == 0)
                stopServer();
        }finally {
            lock.unlock();
        }
    }

    private void stopServer() {
        
    }

    private String generateToken() {
        // length is bounded by 7
        byte[] array = new byte[7];
        new Random().nextBytes(array);
        return new String(array, Charset.forName("UTF-8"));
    }

    private boolean startServer() {
        return false;
    }


    private   byte[] restoreImageTest(byte[] screenshot,int xSize,int ySize){
        byte[] bufferedImage = screenshot;
        for (ImgDiffEntity imgDiffEntity : diffList) {
            byte[] bytes = imgDiffEntity.getBytes();
            int startX = imgDiffEntity.getStartX();
            int startY = imgDiffEntity.getStartY();
            int longXY = imgDiffEntity.getLongXY();
            int newByteArrayCount = 0;
            for(int y = startY;y<startY+longXY;y++){
                for(int x = startX;x<startX+longXY;x++){
                    int coorY = xSize * y;
                    int coordInArray = coorY + x;
                    screenshot[coordInArray] = bytes[newByteArrayCount];
                    newByteArrayCount++;
                }
            }
        }
        return screenshot;


    }

  static   ScreenRobot robot;
    public static void main(String args[]) throws IOException {

         robot = ScreenRobot.getInstance();
      new Thread(new Runnable() {
          @Override
          public void run() {
              robot.screenShotJNi.startListener(); // start
          }
      }).start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        byte[] sc = robot.getScreenShot();
        File outputfile = new File("/home/kirill/image.jpg");
        ImageIO.write(robot.createImageFromBytes(sc), "jpg", outputfile);
        robot.lastImage = sc;
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
                    byte[] screenShot = robot.getScreenShot();
            int y =  robot.screenShotJNi.getSizeY();
            int x =  robot.screenShotJNi.getSizeX();
        System.out.println(    robot.getDiffs(10,screenShot,x,y));
        outputfile = new File("/home/kirill/imageNewRestored.jpg");
        ImageIO.write(robot.createImageFromBytes(robot.lastImage), "jpg", outputfile);
        outputfile = new File("/home/kirill/imageNew.jpg");
        ImageIO.write(robot.createImageFromBytes(screenShot), "jpg", outputfile);
        byte[] bytes = robot.restoreImageTest(robot.lastImage, x, y);
            for(int i = 0; i < screenShot.length;i++){
                if(screenShot[i] != sc[i]){

                }
            }
    }

    private BufferedImage createImageFromBytes(byte[] imageData) {
        ByteArrayInputStream bais = new ByteArrayInputStream(imageData);
        try {
            return ImageIO.read(bais);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
