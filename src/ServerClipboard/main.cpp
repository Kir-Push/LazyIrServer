#include <QApplication>
#include "serverclipboard.h"

int main(int argc, char *argv[])
{

    ServerClipboard w;
    w.startListener(argc,argv);
}
