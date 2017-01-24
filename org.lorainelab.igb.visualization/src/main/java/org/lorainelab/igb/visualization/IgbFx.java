package org.lorainelab.igb.visualization;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Reference;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.lorainelab.igb.stage.provider.api.StageProvider;
import static org.lorainelab.igb.utils.FXUtilities.runAndWait;
import org.lorainelab.igb.visualization.ui.Root;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true)
public class IgbFx {

    private static final Logger LOG = LoggerFactory.getLogger(IgbFx.class);
    private Stage stage;
    private Root root;
    private StageProvider stageProvider;

    @Activate
    public void activate(BundleContext bc) {
        Platform.setImplicitExit(false);
        runAndWait(() -> {
            Stage splashStage = stageProvider.getSplashStage();

            Platform.runLater(() -> {
                Scene scene = new Scene(root);
                stage.setMinWidth(800);
                stage.setMinHeight(400);
//                root.getStyleClass().add("theme-dark");
                scene.getStylesheets().add(bc.getBundle().getEntry("styles/dark-theme.css").toExternalForm());
                //For runtime hot reloading this is left in place commented out
//            try {
//                scene.getStylesheets().add(new File("/home/dcnorris/NetBeansProjects/igb-fx/org.lorainelab.igb.visualization/src/main/resources/styles/dark-theme.css").toURL().toExternalForm());
//            } catch (MalformedURLException ex) {
//                LOG.error(ex.getMessage(), ex);
//            }
                stage.setTitle("IGBfx");
                stage.setScene(scene);

            });
            PauseTransition delay = new PauseTransition(Duration.seconds(1.5));
            delay.setOnFinished(event -> {
                Platform.runLater(() -> {
                    splashStage.close();
                    stage.show();
                });
            });
            delay.play();
        });
    }

    @Deactivate
    public void deactivate() {
        runAndWait(() -> {
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
