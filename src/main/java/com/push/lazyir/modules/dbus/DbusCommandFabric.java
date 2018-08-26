package com.push.lazyir.modules.dbus;

public class DbusCommandFabric {

    private DbusCommandFabric() {
    }

    private static String varCommandInt = "dbus-send --session --dest=%s --type=method_call --print-reply /org/mpris/MediaPlayer2 org.mpris.MediaPlayer2.Player.%s int64:%s";
    private static String baseCommand = "dbus-send --session --dest=%s --type=method_call --print-reply /org/mpris/MediaPlayer2 org.mpris.MediaPlayer2.Player.%s";
    private static String getCommandString = "dbus-send --session --dest=%s --type=method_call --print-reply /org/mpris/MediaPlayer2 org.freedesktop.DBus.Properties.Get string:org.mpris.MediaPlayer2.Player string:%s";
    private static String volume = "dbus-send --session --dest=%s --type=method_call --print-reply /org/mpris/MediaPlayer2  org.freedesktop.DBus.Properties.Set string:org.mpris.MediaPlayer2.Player string:Volume variant:double:%s";
    private static String getAllMpris = "dbus-send --session --dest=org.freedesktop.DBus --type=method_call --print-reply /org/freedesktop/DBus org.freedesktop.DBus.ListNames | grep org.mpris.MediaPlayer2";


    static String getGetAll() {
        return getAllMpris;
    }

    static String seek(String player,String arg)
    {
        return String.format(varCommandInt,player,"Seek",arg);
    }

    static String next(String player)
    {
        return String.format(baseCommand,player,"Next");
    }

    static String previous(String player)
    {
        return String.format(baseCommand,player,"Previous");
    }

    static String stop(String player)
    {
        return String.format(baseCommand,player,"Stop");
    }

    static String playPause(String player)
    {
        return String.format(baseCommand,player,"PlayPause");
    }

    static String openUri(String player,String uri)
    {
        return String.format(baseCommand,player,"OpenUri string:"+uri);
    }

    static String setPosition(String player,String path,String position) {
        return String.format(varCommandInt,player,"SetPosition Object Path:"+path,position);
    }

    static String setVolume(String player,String value)
    {
        return String.format(volume,player,value);
    }

    static String getVolume(String player)
    {
        return String.format(getCommandString,player,"Volume");
    }

    static String getMetadata(String player)
    {
        return String.format(getCommandString,player,"Metadata");
    }

    static String getPlaybackstatus(String player)
    {
        return String.format(getCommandString,player,"PlaybackStatus");
    }

    static String getPosition(String player)
    {
        return String.format(getCommandString,player,"Position");
    }

}
