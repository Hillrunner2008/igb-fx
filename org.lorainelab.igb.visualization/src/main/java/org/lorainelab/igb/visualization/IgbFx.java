package org.lorainelab.igb.visualization;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Reference;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.lorainelab.igb.stage.provider.api.StageProvider;
import static org.lorainelab.igb.utils.FXUtilities.runAndWait;
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
        Platform.setImplicitExit(false);
        runAndWait(() -> {
            stageProvider.getSplashStage().hide();
            Scene scene = new Scene(root);
            stage.setTitle("IGBfx");
            stage.setScene(scene);
        });
        //splitting this piece off since it has occasionally caused the stage to never start and block the bundle activation from completing
        Platform.runLater(() -> {
            stage.show();
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
