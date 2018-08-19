package com.push.lazyir.modules.screenShare;

import com.push.lazyir.Loggout;
import com.push.lazyir.modules.screenShare.enity.AuthInfo;
import com.push.lazyir.modules.screenShare.enity.ListenerInfo;
import com.push.lazyir.service.main.BackgroundService;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

/*
Get screenshot of desktop,
detect changes from previous screenshot (if was),
compress changes (or image) using gzip stream's.
have setting for min. pixel number detecting - from which number of pixel's
consider to change. Pixel checking only by squares. and y == x;
It's singleton class, and using listener pattern.
* */
public class ScreenRobot implements Runnable {
    @Override
    public void run() {

    }

//    private static ScreenRobot INSTANCE;
//
//    private static ScreenShotJNi screenShotJNi;
//    private static final ReentrantLock lock = new ReentrantLock();
//    // list of differences in current screenShot
//    private List<ImgDiffEntity> diffList = new CopyOnWriteArrayList<>();
//    private byte[] lastImage;
//    private byte[] currImage;
//
//    private static volatile boolean serverWork = false;
//    // stores client's token, used in authorization
//    private HashMap<String,AuthInfo> listenersAuthInfo = new HashMap<>();
//    private HashMap<String,ListenerInfo> listeners = new HashMap<>();
//    // local executor pool, used mainly for listener's thread's
//    private ExecutorService pool = Executors.newCachedThreadPool();
//    private volatile int port = 9667;
//    private ServerSocket serverSocket;
//    // future to control server task working
//    private  Future<?> serverFuture;
//    private  Future<?> loopFuture;
//    private ScreenLoop screenLoop;
//    private boolean lastOperationcomplete;
//    // does screenGetting loop work?
//    private static boolean loopWork;
//
//
//    private ScreenRobot()  {
//        screenShotJNi = new ScreenShotJNi(null);
//      //  port = Integer.parseInt(BackgroundService.getSettingManager().get("ScreenPort")); //todo uncomment after testing
//    }
//
//    /*
//    Singleton because used in many module instances
//    * */
//    public static ScreenRobot getInstance()  {
//        lock.lock();
//        try {
//            if (INSTANCE == null)
//                INSTANCE = new ScreenRobot();
//            return INSTANCE;
//        }finally {
//            lock.unlock();
//        }
//    }
//
//    /*
//    Full screen capture use jni class
//    * */
//    private byte[] getScreenShot(){
//        byte[] screenShot = new byte[0];
//        try {
//            screenShot = screenShotJNi.getScreenShot();
//        } catch (Exception e){
//            Loggout.e("ScreenRobot","Error in getScreenshot",e);
//        }
//        return screenShot;
//    }
//
//    /*
//    Return number of differences
//    add dufferences in diffList
//    * */
//    private int getDiffs(int diffMin,byte[] currImage,byte[] lastImage,int xSize,int ySize){
//        int height = ySize;
//        int width = xSize;
//        byte[] currImgpixels = currImage;
//        byte[] oldImgpixels =lastImage;
//        List<ImgDiffEntity> tempList = new ArrayList<>();
//        // we have array of bytes, but pixel consist of 3 bytes, so we need
//        // to iterate over each color of pixel.
//        for(int i =0;i<3;i++) {
//            // actually it's 1/3 size of image, we need add it to result coordinate, in second and third loop
//            // to calculate 2/3 and 3/3 part of image.
//            int addedvalue = ySize * xSize;
//            int xsquaresCount = width / diffMin;
//            int ysquaresCount = height / diffMin;
//            // we separate image by squares, and fist iterate over y coord square
//            for (int ysquare = 0; ysquare < ysquaresCount; ysquare++) {
//                // calculate first absolute y coordinate in this square
//                int currYstart = ysquare == 0 ? 0 : ysquare * diffMin;
//                // in each column (y) we go over row (x), square by square
//                for (int xSquare = 0; xSquare < xsquaresCount; xSquare++) {
//                    // temp array of byte, where will be saved chaged pixels.
//                    // size is - square with side of min difference
//                    byte[] currChangeBytes = new byte[diffMin * diffMin];
//                    // array currChangeBytes, we save byte's one by one, without coordinates.
//                    int newByteArrayCount = 0;
//                    // current number of difference in this square
//                    int numberOfDiff = 0;
//                    // calculate first absolute x coordinate in this square
//                    int currXtart = xSquare == 0 ? 0 : xSquare * diffMin;
//                    // iterate over current square start from y coordinate
//                    for (int y = currYstart; y < currYstart + diffMin; y++) {
//                        // first part of absolute coordinate in image array.
//                        // it's start of row, in which we have our pixel.
//                        int coorY = width * y;
//                        // iterate over current square - x coordinate
//                        for (int x = currXtart; x < currXtart + diffMin; x++) {
//                            // complete coordinate in array - sum of start row and number in row
//                            int coordInArray = coorY + x;
//                            // if we in second loop in outer 0-3 loop
//                            // it means we go over 2/3 part of image
//                            if(i >= 1){
//                                coordInArray += addedvalue;
//                                // if we in third loop - add value again.
//                                if(i == 2){
//                                    coordInArray += addedvalue;
//                                }
//                            }
//                            // we fill all temp array, but add only if we have needed number of differences
//                            currChangeBytes[newByteArrayCount] = currImgpixels[coordInArray];
//                            if (currImgpixels[coordInArray] != oldImgpixels[coordInArray]) {
//                                numberOfDiff++;
//                            }
//                            newByteArrayCount++;
//                        }
//                    }
//                    if (numberOfDiff >= diffMin) {
//                        // create entity with x and y coords start(absolute), square size and array of bytes
//                        ImgDiffEntity imgDiffEntity = new ImgDiffEntity(currXtart, currYstart, diffMin, currChangeBytes);
//                        // add it to list, to process from another place
//                        tempList.add(imgDiffEntity);
//                    }
//                }
//            }
//        }
//        // clear list of differences
//        diffList.clear();
//        diffList.addAll(tempList);
//        return diffList.size();
//    }
//
//    private String generateToken() {
//        // length is bounded by 7
//        byte[] array = new byte[7];
//        new Random().nextBytes(array);
//        return new String(array, Charset.forName("UTF-8"));
//    }
//    private   byte[] restoreImageTest(byte[] screenshot,int xSize,int ySize){
//        byte[] bufferedImage = screenshot;
//        for (ImgDiffEntity imgDiffEntity : diffList) {
//            byte[] bytes = imgDiffEntity.getBytes();
//            int startX = imgDiffEntity.getStartX();
//            int startY = imgDiffEntity.getStartY();
//            int longXY = imgDiffEntity.getLongXY();
//            int newByteArrayCount = 0;
//            for(int y = startY;y<startY+longXY;y++){
//                for(int x = startX;x<startX+longXY;x++){
//                    int coorY = xSize * y;
//                    int coordInArray = coorY + x;
//                    screenshot[coordInArray] = bytes[newByteArrayCount];
//                    newByteArrayCount++;
//                }
//            }
//        }
//        return screenshot;
//
//
//    }
//
//    private BufferedImage createImageFromBytes(byte[] imageData) {
//        ByteArrayInputStream bais = new ByteArrayInputStream(imageData);
//        try {
//            return ImageIO.read(bais);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    /*
//    * recipient register, and start receive screenshots
//      until it will be unregistered
//    * */
//    public AuthInfo register(String dvId) throws ScreenCastException,IOException{
//        lock.lock();
//        try{
//            boolean isAllOkey = true;
//       //     unRegister(dvId); // first unregister listener if he already connected
//            // if server off - start it.
//            if(!serverWork)
//                isAllOkey = startServer();
//            if(!isAllOkey)
//                throw new ScreenCastException("Cant start Server");
//            String token = generateToken();
//            AuthInfo authInfo = new AuthInfo(token, port);
//            // add token in hashMap where key is clientId
//            listenersAuthInfo.put(dvId,authInfo);
//            return authInfo;
//        }finally {
//            lock.unlock();
//        }
//    }
//
//   /*
//    unregister client and stop server if he was last.
//   * */
//    public void unRegister(String dvId) throws IOException{
//        lock.lock();
//        try{
//            // remove token from store
//            listenersAuthInfo.remove(dvId);
//            // get Listener from listener store, and close it's input\output stream
//            ListenerInfo listenerInfo = listeners.get(dvId);
//            try {
//                listenerInfo.getIn().close();
//                listenerInfo.getOut().close();
//            }catch (Exception e){
//
//            }
//            // use future to cancel it job.
//            if(listenerInfo != null) {
//                Future<?> future = listenerInfo.getFuture();
//                if (future != null)
//                    future.cancel(true);
//            }
//            // remove it from listener store
//            listeners.remove(dvId);
//            // if stores empty, we have no client's and may stop server and loop
//            if(listenersAuthInfo.size() == 0 || listeners.size() == 0) {
//               stopServer();
//                stopLoop();
//            }
//        }finally {
//            lock.unlock();
//        }
//    }
//
//    /*
//    Create serverSocket and submit task to ServiceExecutor
//    * */
//    private boolean startServer() throws IOException {
//
//        serverSocket = new ServerSocket(port);
//        serverWork = true;
//        serverFuture = BackgroundService.submitNewTask(this::run);
//        return serverWork;
//    }
//
//    /*
//    cancel serverFuture task,
//    and if all okay - close socket and set it null
//    * */
//    private void stopServer() throws IOException {
//        if(serverFuture != null) {
//            serverFuture.cancel(true);
//            serverWork = false;
//            if(serverSocket != null)
//            serverSocket.close();
//            serverSocket = null;
//        }
//    }
//
//    /*
//    Actual server thread loop.
//    * */
//    @Override
//    public void run() {
//        try {
//            //work while serverWork bool true(which set in start/stop server method
//        while (serverWork){
//            Socket  connection = serverSocket.accept();
//            System.out.println("CONNECION ACCEPTED");
//            BufferedReader  inputStream = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//            DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
//            String id = getId(inputStream); // first client send id
//            String token = getToken(inputStream); // after he send token
//            boolean isAuth = authorization(id,token); // check credentials
//            if(!isAuth){ // if false, stop all connection with that user
//                inputStream.close();
//                outputStream.close();
//                connection.close();
//            }else{ // else add it to stores
//                addToListeners(connection,inputStream,outputStream,id,token);
//            }
//        } }
//        catch (IOException e) {
//            Loggout.e("ScreenRobot","Run method error when accepting socket",e);
//        }finally {
//            serverWork = false;
//            serverSocket = null;
//        }
//    }
//
//    /*
// Check if we know this user
// first receive deviceId, then token
// * */
//    private boolean authorization(String id,String token) {
//        AuthInfo authInfo = listenersAuthInfo.get(id);
//        return  authInfo.getToken().equals(token);
//    }
//
//    private String getId(BufferedReader inputStream) throws IOException{
//        return inputStream.readLine();
//    }
//
//    private String getToken(BufferedReader inputStream) throws IOException{
//        return inputStream.readLine();
//    }
//
//    private void addToListeners(Socket connection, BufferedReader inputStream, DataOutputStream outputStream, String id, String token) {
//        // listenerInfo represent listener connection, and it's message passing mechanism.
//        ListenerInfo listenerInfo = new ListenerInfo(inputStream, outputStream, connection, true, new LinkedBlockingQueue<>());
//        // each Listener, has own thread, in which he wait for message in blockingQueque, clientListener this class
//        ClientListener clientListener = new ClientListener(listenerInfo);
//        listeners.put(id,listenerInfo);
//
//        if(pool.isShutdown() || pool.isTerminated()) // if loop stopped start it
//            pool = Executors.newCachedThreadPool();
//        // start Listener thread, and store future in listenerInfo
//        Future<?> submit = pool.submit(clientListener);
//        listenerInfo.setFuture(submit);
//        if(!loopWork){ // if it first listener and getScreenLoop don't work, start it
//            startLoop();
//        }
//    }
//
//    /*
//    Start screen getting Loop
//    * */
//    private void startLoop() {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                robot.screenShotJNi.startListener(); // start
//            }
//        }).start();
//
//        if(screenLoop == null)
//            screenLoop = new ScreenLoop();
//        loopFuture = BackgroundService.submitNewTask(screenLoop::run); // submit screenLoop thread in main ExecutorService
//        loopWork = true;
//    }
//
//    private void stopLoop(){
//        loopWork = false;
//        robot.screenShotJNi.stopListener();
//        if(loopFuture != null)
//        loopFuture.cancel(true); // stop loop thread
//        pool.shutdownNow(); // also if we stopped loop, we didn't need listeners, stoop their threads
//        screenLoop = null;
//    }
//
//    /*
//     each Listener, has own thread, in which he wait for message in blockingQueque, clientListener this class
//    * */
//    class ClientListener implements Runnable{
//
//        ListenerInfo client;
//        boolean work;
//
//        public ClientListener(ListenerInfo info) {
//            this.client = info;
//            work = true;
//        }
//
//        @Override
//        public void run() {
//            BlockingQueue<Integer> blockingQueue = client.getBlockingQueue();
//            while (work){
//                try {
//                    // in thread we wait for command
//                    Integer code = blockingQueue.poll(5,TimeUnit.MILLISECONDS);
//                    if(code == null)
//                        continue;
//                    switch (code){
//                        case 1:
//                            sendScreen();
//                            break;
//                        case 2:
//                            sendDiff();
//                            break;
//                    }
//                } catch (InterruptedException | IOException e) {
//                   work = false;
//                }
//            }
//        }
//
//        private void sendDiff() throws IOException {
//            long time = System.currentTimeMillis();
//            int size = diffList.size();
//            if(size == 0)
//                return;
//            DataOutputStream out = client.getOut();
//            out.writeInt(2);
//            out.flush();
//            out.writeInt(size);
//            out.flush();
//            for (ImgDiffEntity diff : diffList) {
//                byte[] bytes = diff.getBytes();
//                out.writeInt(diff.getLongXY());
//                out.flush();
//                out.writeInt(diff.getStartX());
//                out.flush();
//                out.writeInt(diff.getStartY());
//                out.flush();
//                out.writeInt(bytes.length);
//                out.flush();
//                out.write(bytes);
//                out.flush();
//            }
//            long l = System.currentTimeMillis();
//            System.out.println("TIme passed 2@@@@ " +(l - time) + "        " + size);
//            lastOperationcomplete = true;
//        }
//
//        private void sendScreen() throws IOException {
//            long time = System.currentTimeMillis();
//            DataOutputStream out = client.getOut();
//            out.writeInt(1);
//            out.flush();
//            out.writeInt(currImage.length);
//            out.flush();
//            out.write(currImage,0,currImage.length);
//            out.flush();
//            long l = System.currentTimeMillis();
//            System.out.println("TIme passed " +(l - time));
//            lastOperationcomplete = true;
//        }
//    }
//
//    /*
//    Screen loop class, get screenshot's\diffs and send to listener's
//    * */
//    class ScreenLoop implements Runnable{
//
//        @Override
//        public void run() {
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            int diffMin = 100;
//            int countBaseFrame = 512; // how frequent we send full frame
//            byte[] lastImg = new byte[1];
//            lastOperationcomplete = true;
//            while (loopWork){
//                if(!lastOperationcomplete) {
//                    try {
//                        Thread.sleep(20);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    continue;
//                }
//                currImage = getScreenShot();
//                int x = screenShotJNi.getSizeX();
//                int y = screenShotJNi.getSizeY();
//                if(lastImg.length == 1)
//                    lastImg = currImage;
//                if(countBaseFrame == 512){
//                    lastImage = currImage;
//                    sendScreen(1);
//                } else{
//                        int diffs = getDiffs(diffMin, currImage,lastImg, x, y);
//                        sendScreen(2);
//                }
//                lastImg = currImage;
//                countBaseFrame--;
//                if(countBaseFrame < 0){
//                    countBaseFrame = 512;
//                }
//            }
//        }
//
//
//        private void sendScreen(int code) {
//            listeners.values().stream().forEach(info ->{
//                try {
//                    int operation;
//                    if(info.isFirstFrame()) { // if listener newbie send full image
//                        operation = 1;
//                        info.setFirstFrame(false);
//                    }
//                    else
//                        operation = code;
//                    info.getBlockingQueue().offer(operation,5,TimeUnit.MILLISECONDS);
//                } catch (InterruptedException e) {
//                }
//            });
//        }
//    }
//
//
//
//
//    static   ScreenRobot robot;
//    public static void main(String args[]) throws IOException {
//
//        robot = ScreenRobot.getInstance();
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                robot.screenShotJNi.startListener(); // start
//            }
//        }).start();
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        byte[] sc = robot.getScreenShot();
//        File outputfile = new File("/home/kirill/image.jpg");
//        ImageIO.write(robot.createImageFromBytes(sc), "jpg", outputfile);
//        robot.lastImage = sc;
//        try {
//            Thread.sleep(3000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        byte[] screenShot = robot.getScreenShot();
//        int y =  robot.screenShotJNi.getSizeY();
//        int x =  robot.screenShotJNi.getSizeX();
//        System.out.println(    robot.getDiffs(10,screenShot,  robot.lastImage,x,y));
//        outputfile = new File("/home/kirill/imageNewRestored.jpg");
//        ImageIO.write(robot.createImageFromBytes(robot.lastImage), "jpg", outputfile);
//        outputfile = new File("/home/kirill/imageNew.jpg");
//        ImageIO.write(robot.createImageFromBytes(screenShot), "jpg", outputfile);
//        byte[] bytes = robot.restoreImageTest(robot.lastImage, x, y);
//        for(int i = 0; i < screenShot.length;i++){
//            if(screenShot[i] != sc[i]){
//
//            }
//        }
//    }


}
