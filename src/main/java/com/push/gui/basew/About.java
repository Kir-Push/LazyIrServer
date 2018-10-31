package com.push.gui.basew;

import com.push.gui.utils.GuiUtils;
import com.push.lazyir.service.managers.settings.LocalizationManager;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.io.IOException;

@Slf4j
public class About {
    private boolean  opened;
    private final LocalizationManager localizationMng;
    private GuiUtils guiUtils;
    @Inject
    public About(final LocalizationManager localizationMng,GuiUtils guiUtils) {
        this.localizationMng = localizationMng;
        this.guiUtils = guiUtils;
    }

    public void showWindow(){
        if(opened) {
            return;
        }
        opened = true;
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Thread.currentThread().getContextClassLoader().getResource("fxml/about.fxml"));
            AnchorPane rootLayout = loader.load();
            Scene scene = new Scene(rootLayout);
            ImageView imageView = (ImageView) scene.lookup("#imgAbout");
            imageView.setImage(guiUtils.getImage("information",128,128));

            Label appName =(Label) scene.lookup("#appName");
            appName.setText("LazyIr 1.0");

            Label credits = (Label) scene.lookup("#credits");
            String creditsText = localizationMng.get("aboutCredits");
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

            Stage stage = new Stage();
            stage.setOnCloseRequest(event -> opened = false);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            opened = false;
            log.error("ShowWindow",e);
        }
    }
}
