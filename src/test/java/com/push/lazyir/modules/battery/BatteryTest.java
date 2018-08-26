package com.push.lazyir.modules.battery;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.push.lazyir.devices.Device;
import com.push.lazyir.gui.GuiCommunicator;
import com.push.lazyir.service.main.BackgroundService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class BatteryTest {


    BackgroundService backgroundServiceMock;
    GuiCommunicator guiCommunicatorMock;
    Device deviceMock;
    Battery battery;
    @BeforeEach
    void setUp() {
        backgroundServiceMock = mock(BackgroundService.class);
        guiCommunicatorMock = mock(GuiCommunicator.class);
        deviceMock = mock(Device.class);
//        battery = new Battery(guiCommunicatorMock,backgroundServiceMock,deviceMock);
    }

    @Test
    void execute() {
//        String test ="testData";
//        String test2 = "testDataSecond";
//        doAnswer((invocation)->{
//            assertEquals(invocation.getArguments()[0].toString().equals(test),invocation.getArguments()[1].toString().equals(test2));
//            return "";
//        }).when(guiCommunicatorMock).batteryStatus(anyString(),anyString(),any());
//        NetworkPackageOld orCreatePackage = NetworkPackageOld.CacherOld.getOrCreatePackage(Battery.class.getSimpleName(),"data");
//        orCreatePackage.setValue(Battery.api.percentage.name(),test);
//        orCreatePackage.setValue(Battery.api.status.name(),test2);
//        battery.execute(orCreatePackage);
    }
}