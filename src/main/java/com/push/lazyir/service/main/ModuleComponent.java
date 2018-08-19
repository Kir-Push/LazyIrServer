package com.push.lazyir.service.main;

import com.push.lazyir.modules.battery.Battery;
import com.push.lazyir.modules.clipboard.ClipBoard;
import com.push.lazyir.modules.command.SendCommand;
import com.push.lazyir.modules.dbus.Mpris;
import com.push.lazyir.modules.ir.IrModule;
import com.push.lazyir.modules.memory.Memory;
import com.push.lazyir.modules.notifications.call.CallModule;
import com.push.lazyir.modules.notifications.messengers.Messengers;
import com.push.lazyir.modules.notifications.notifications.ShowNotification;
import com.push.lazyir.modules.notifications.sms.SmsModule;
import com.push.lazyir.modules.ping.Ping;
import com.push.lazyir.modules.reminder.Reminder;
import com.push.lazyir.modules.screenShare.ScreenShareModule;
import com.push.lazyir.modules.share.ShareModule;
import com.push.lazyir.modules.sync.SynchroModule;
import com.push.lazyir.modules.touch.TouchControl;
import com.push.lazyir.utils.annotations.ModuleScope;
import dagger.Subcomponent;

@Subcomponent()
@ModuleScope()
public interface ModuleComponent {

    Battery provideBattery();
    ClipBoard provideClipBoard();
    SendCommand provideSendCommand();
    Mpris provideMpris();
    IrModule provideIrModule();
    Memory provideMemory();
    CallModule provideCallModule();
    Messengers provideMessengers();
    ShowNotification provideShowNotification();
    SmsModule provideSmsModule();
    Ping providePing();
    Reminder provideReminder();
    ScreenShareModule provideScreenShareModule();
    ShareModule provideShareModule();
    SynchroModule provideSynchroModule();
    TouchControl provideTouchControl();
}
