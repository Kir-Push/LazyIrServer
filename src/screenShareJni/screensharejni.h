#ifndef SCREENSHAREJNI_H
#define SCREENSHAREJNI_H

#include "jni.h"
#include <QClipboard>
#include "screensharejni_global.h"

class SCREENSHAREJNISHARED_EXPORT ScreenShareJni : public QObject
{

public:
    ScreenShareJni();

public slots:
    void onStarted();

private:
};




#endif // SCREENSHAREJNI_H
