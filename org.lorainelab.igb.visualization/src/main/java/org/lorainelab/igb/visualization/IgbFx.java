package org.lorainelab.igb.visualization;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Reference;
import java.awt.SplashScreen;
import java.util.Optional;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.lorainelab.igb.stage.provider.api.StageProvider;
import org.lorainelab.igb.visualization.ui.Root;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true)
public class IgbFx {

    private static final Logger LOG = LoggerFactory.getLogger(IgbFx.class);
    private Stage stage;
    private Root root;
    private StageProvider stageProvider;

    @Activate
    public void activate() {
//        closeSplashScreen();
//        initializeFxRuntime();
        Platform.setImplicitExit(false);
        Platform.runLater(() -> {
            stageProvider.getSplashStage().hide();
            Scene scene = new Scene(root);
            stage.setTitle("IGBfx");
            stage.setScene(scene);
            stage.show();
        });
    }

    private void closeSplashScreen() {
        Optional.ofNullable(SplashScreen.getSplashScreen()).ifPresent(SplashScreen::close);
    }

    private void initializeFxRuntime() {
        new JFXPanel(); // runtime initializer, do not remove
        Platform.setImplicitExit(false);
    }

    @Deactivate
    public void deactivate() {
        Platform.runLater(() -> {
            try {
                stage.hide();
                root.getChildren().clear();
            } catch (Exception ex) {
                //do nothing
            }
        });
    }

    @Reference
    public void setStageProvider(StageProvider stageProvider) {
        this.stageProvider = stageProvider;
        this.stage = stageProvider.getStage();
    }

    @Reference
    public void setRoot(Root root) {
        this.root = root;
    }

}
