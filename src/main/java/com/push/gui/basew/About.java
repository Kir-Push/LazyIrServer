package com.push.gui.basew;

import com.push.gui.controllers.MainController;
import com.push.gui.utils.GuiUtils;
import com.push.lazyir.Loggout;
import com.push.lazyir.service.BackgroundService;
import com.push.lazyir.service.settings.LocalizationManager;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;


public class About {
    private static volatile boolean  opened = false;

    public static void showWindow(String id,MainController mainController){
        if(opened){
            return;
        }
        opened = true;
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainWin.class.getClassLoader().getResource("fxml/about.fxml"));
            AnchorPane rootLayout = (AnchorPane) loader.load();
            Scene scene = new Scene(rootLayout);
            Stage stage = new Stage();

            LocalizationManager localizationManager = BackgroundService.getLocalizationManager();

            ImageView imageView = (ImageView) scene.lookup("#imgAbout");
            imageView.setImage(GuiUtils.getImage("information",128,128));

            Label appName =(Label) scene.lookup("#appName");
            appName.setText("LazyDroid 1.0");

            Label credits = (Label) scene.lookup("#credits");
            String creditsText = localizationManager.get("aboutCredits");
            credits.setText(creditsText + ": " + "\n" +
            "Icon \"flaticon.com/free-icon/user_149071\" made by  smashicons.com \n"+
                            "Icon \"flaticon.com/free-icon/chat_437654\" made by  iconfinder.com/vasabii \n" +
                            "Icon \"flaticon.com/free-icon/right-arrow_254038\" made by  freepik.com\n" +
                            "Icon \"flaticon.com/free-icon/telephone_724642\" made by  icon54.com\n" +
                            "Icon \"flaticon.com/free-icon/information_189865\" made by  roundicons.com\n" +
                            "Icon \"flaticon.com/free-icon/chat_134932\" made by  smashicons.com\n\n" +
                    "If something missed or you have questions, please contact me: "
            );
            Label contact = (Label) scene.lookup("#contacts");
            contact.setText("Email: Ashparenij2@gmail.com");
            stage.setOnCloseRequest(event -> {
                opened = false;
            });
            stage.setScene(scene);
            stage.show();
        }catch (Exception e){
            opened = false;
            Loggout.e("AboutWindow","Exception ",e);
        }
    }
}
