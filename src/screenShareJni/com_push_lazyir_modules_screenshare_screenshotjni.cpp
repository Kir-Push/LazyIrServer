#include "screensharejni.h"
#include "com_push_lazyir_modules_screenShare_ScreenShotJNi.h"
#include <iostream>
#include <QApplication>
#include <QPixmap>
#include <QDesktopWidget>
#include <QScreen>
#include <QBuffer>

 QApplication * app;
 int xSize;
 int ySize;
JNICALL jbyteArray Java_com_push_lazyir_modules_screenShare_ScreenShotJNi_getScreenShot(JNIEnv *env, jobject myclass)
{

    QScreen *screen = QGuiApplication::primaryScreen();
    QPixmap pixmap = screen->grabWindow(0);
    xSize = pixmap.width();
    ySize = pixmap.height();
    QByteArray bArray;
    QBuffer buffer(&bArray);
    buffer.open(QIODevice::WriteOnly);
    pixmap.save(&buffer,"bmp");
    jbyteArray array = env->NewByteArray (bArray.length());
    env->SetByteArrayRegion (array, 0, bArray.length(), reinterpret_cast<jbyte*>(bArray.data()));
    return array;
}

JNICALL void Java_com_push_lazyir_modules_screenShare_ScreenShotJNi_startListener(JNIEnv *env, jobject myclass)
{
 char *ptr_array[20];
 int val = 5;
 app = new QApplication(val, ptr_array);
 app->exec();

}

JNICALL void Java_com_push_lazyir_modules_screenShare_ScreenShotJNi_stopListener(JNIEnv *env, jobject myclass)
{
    QApplication::quit();

}

JNICALL jint Java_com_push_lazyir_modules_screenShare_ScreenShotJNi_getSizeX (JNIEnv *, jobject)
{
    return xSize;
}

JNICALL jint Java_com_push_lazyir_modules_screenShare_ScreenShotJNi_getSizeY (JNIEnv *, jobject)
{
    return ySize;
}
