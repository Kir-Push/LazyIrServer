#include <QApplication>
#include "serverclipboard.h"
#include "com_push_lazyir_modules_clipboard_ClipboardJni.h"
#include "QDebug"

  #include <QMimeData>

 bool working;
ServerClipboard::ServerClipboard(QObject *parent) : QObject(parent)
{

}

void ServerClipboard::startListener(int argc, char *argv[])
{
    if(working)
    {
        return;
    }
     working = true;
    QApplication app(argc, argv);
    clipboard = QGuiApplication::clipboard();
    connect(clipboard,SIGNAL(dataChanged()),this,SLOT(clipbDataChanged()));
       app.exec();
}

void ServerClipboard::stopListener()
{
QObject::disconnect(clipboard,SIGNAL(dataChanged()),this,SLOT(clipbDataChanged()));
working = false;
QApplication::quit();
}

jstring ServerClipboard::getClipboardData()
{
    JNIEnv *localEnv;
      cachedJVM->AttachCurrentThread((void **)&localEnv, NULL);
    const QMimeData *mimeData = clipboard->mimeData();
    return localEnv->NewStringUTF(mimeData->text().toUtf8().data());
}

void ServerClipboard::setClipboardData(JNIEnv *env,jstring text)
{
    ichanged = true;
     QString qstr(env->GetStringUTFChars(text, 0));
    clipboard->setText(qstr);
}
void ServerClipboard::clipbDataChanged()
{
    if(ichanged)
    {
        ichanged = false;
        return;
    }
    const QMimeData *mimeData = clipboard->mimeData();
    JNIEnv *localEnv;
      cachedJVM->AttachCurrentThread((void **)&localEnv, NULL);
    jclass strClass = localEnv->FindClass("com/push/lazyir/modules/clipboard/ClipboardJni");
    if(!strClass) {
         qInfo() << "Could not find the CEC class.";
     }
    jmethodID ctorID = localEnv->GetStaticMethodID(strClass, "clipboardChanged", "(Ljava/lang/String;)V");

    if(!ctorID) {
         qInfo() << "Could not find the ctorID class.";
     }
    jstring encoding = localEnv->NewStringUTF(mimeData->text().toUtf8().data());
    localEnv->CallStaticVoidMethod(strClass,ctorID,encoding);
}
