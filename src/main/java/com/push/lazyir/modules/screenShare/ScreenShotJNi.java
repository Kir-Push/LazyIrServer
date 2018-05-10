package com.push.lazyir.modules.screenShare;



public class ScreenShotJNi {


    public ScreenShotJNi(String libend){
       //     System.load(new File(ScreenShotJNi.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile().getPath()+ File.separator + "libServerScreenshot" + libend);
            System.load("/home/kirill/build-screenShareJni-Desktop_Qt_5_10_1_GCC_64bit-Release/libscreenShareJni.so.1.0.0"); // todo don't forget change to normal
    }

    native public byte[] getScreenShot();

    native public int getSizeX();

    native public int getSizeY();

    native public void  startListener();

    native public void stopListener();
}
