package org.lorainelab.igb.main;

import javafx.stage.Stage;
import org.lorainelab.igb.visualization.StageProvider;

public class MainStageProvider implements StageProvider {

    Stage stage;

    public MainStageProvider(Stage stage) {
        this.stage = stage;
    }

    @Override
    public Stage getStage() {
        return stage;
    }

}
