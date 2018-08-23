
#include <iostream>
#include "com_push_lazyir_modules_clipboard_ClipboardJni.h"
#include "serverclipboard.h"
ServerClipboard* srv;
JNICALL void Java_com_push_lazyir_modules_clipboard_ClipboardJni_startListener(JNIEnv *env, jclass myclass)
{
    if(!worka)
    {
        worka = true;
        srv = new ServerClipboard();
        int status = env->GetJavaVM( & srv->cachedJVM);
           if(status != 0) {
               // Fail!
               exit(1);
           }
       char *argv = new char[1];
        srv->startListener(0,&argv);
    }
}

JNICALL void Java_com_push_lazyir_modules_clipboard_ClipboardJni_stopListener(JNIEnv *env, jclass myclass)
{
      srv->stopListener();
}

JNICALL jstring Java_com_push_lazyir_modules_clipboard_ClipboardJni_getClipboardText(JNIEnv *env, jclass myclass)
{
     if(!worka)
     {
         return NULL;
     }
  return srv->getClipboardData();
}

JNICALL void Java_com_push_lazyir_modules_clipboard_ClipboardJni_setClipboardText(JNIEnv *env, jclass myclass, jstring text)
{
   if(!worka)
       return;
   srv->setClipboardData(env,text);
}
