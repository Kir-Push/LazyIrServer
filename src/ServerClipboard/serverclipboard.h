#ifndef SERVERCLIPBOARD_H
#define SERVERCLIPBOARD_H

#include "serverclipboard_global.h"
#include "jni.h"
#include <QClipboard>

class SERVERCLIPBOARDSHARED_EXPORT ServerClipboard : public QObject
{
    Q_OBJECT
  public:
      explicit ServerClipboard(QObject *parent = 0);
      void startListener(int argc, char *argv[]);
      void stopListener();
      JavaVM* cachedJVM;

      bool ichanged =false;
      jstring getClipboardData();
      void setClipboardData(JNIEnv *env,jstring text);

      public slots:
      void clipbDataChanged();


  private:

      QClipboard *clipboard;
};

#endif // SERVERCLIPBOARD_H
