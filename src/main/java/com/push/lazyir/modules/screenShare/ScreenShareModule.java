package com.push.lazyir.modules.screenShare;
import com.push.lazyir.devices.Cacher;
import com.push.lazyir.devices.NetworkPackage;
import com.push.lazyir.modules.Module;
import com.push.lazyir.modules.screenShare.enity.AuthInfo;
import com.push.lazyir.service.main.BackgroundService;

import javax.inject.Inject;
import java.io.IOException;

public class ScreenShareModule extends Module {
    private static final String REGISTER = "register";
    private static final String UNREGISTER = "unregister";
    private static final String TOKEN = "token";
 //   private static ScreenRobot screenRobot = ScreenRobot.getInstance();

@Inject
    public ScreenShareModule(BackgroundService backgroundService, Cacher cacher) {
        super(backgroundService, cacher);
    }

    @Override
    public void execute(NetworkPackage np) {
        String cmd = np.getData();
        try {
            switch (cmd) {
                case REGISTER:
                    register(np);
                    break;
                case UNREGISTER:
                    unRegister(np);
                    break;
                default:
                    break;
            }
        }catch (IOException e){
            //todo
        }catch (ScreenCastException e){
            //todo
        }

    }

    /*
    Register client in screenRobot, and send token in answer (authClass)
    * */
    private void register(NetworkPackage np) throws IOException, ScreenCastException {
//        unRegister(np);
//        AuthInfo register = screenRobot.register(np.getId());
//        NetworkPackage msg = cacher.getOrCreatePackage(ScreenShareModule.class.getSimpleName(), TOKEN);
//        msg.setObject(TOKEN,register);
//        String message = msg.getMessage();
//        System.out.println(message);
//        sendMsg(message);
    }


    /*
    * Client send unregister command, so we pass it's id to screenID, which do actually job
    * */
    private void unRegister(NetworkPackage np) throws IOException {
//        screenRobot.unRegister(np.getId());
    }
}
