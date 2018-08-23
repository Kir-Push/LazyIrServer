package com.push.lazyir.service.main;

public enum BackgroundServiceCmds {
    startUdpListener,
    stopUdpListener,
    stopTcpListener,
    startListeningTcp,
    startSendPeriodicallyUdp,
    stopSendingPeriodicallyUdp,
    closeAllTcpConnections,
    stopSftpServer,
    removeClipBoardListener,
    startClipboardListener,
    unregisterBatteryRecever,
    registerBatteryReceiver,
    onZeroConnections,
    submitTask,
    sendToAll,
    destroy,
    startTasks,
    getAllNotifs
}