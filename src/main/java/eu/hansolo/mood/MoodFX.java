package eu.hansolo.mood;

import com.gluonhq.charm.down.common.JavaFXPlatform;
import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.visual.Swatch;
import eu.hansolo.mood.mqtt.MqttManager;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;


public class MoodFX extends MobileApplication {
    public static final String MAIN_VIEW        = HOME_VIEW;
    public static final String CONFIG_VIEW      = "CONFIG";
    public static final Color  BACKGROUND_COLOR = Color.rgb(66, 71, 79);


    @Override public void init() {
        addViewFactory(MAIN_VIEW, () -> new MainView(MAIN_VIEW));
        addViewFactory(CONFIG_VIEW, () -> new ConfigView(CONFIG_VIEW));
    }

    @Override public void postInit(Scene scene) {
        scene.getStylesheets().add(MoodFX.class.getResource("styles.css").toExternalForm());
        Swatch.BLUE_GREY.assignTo(scene);
        ((Stage) scene.getWindow()).getIcons().add(new Image(MoodFX.class.getResourceAsStream("/icon.png")));

        // Size to FullScreen on Desktop and Embedded
        if (JavaFXPlatform.isDesktop()) {
            if (System.getProperty("os.arch").toUpperCase().contains("ARM")) {
                ((Stage) scene.getWindow()).setFullScreen(true);
                ((Stage) scene.getWindow()).setFullScreenExitHint("");
            } else {
                (scene.getWindow()).setWidth(500);
                (scene.getWindow()).setHeight(550);
            }
        }
    }

    @Override public void stop() {
        if (MqttManager.INSTANCE.isConnected()) MqttManager.INSTANCE.disconnect(0);
        System.exit(0);
    }
}
