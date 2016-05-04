package org.lorainelab.igb.main;

import javafx.application.HostServices;
import javafx.stage.Stage;
import org.lorainelab.igb.stage.provider.api.StageProvider;

public class MainStageProvider implements StageProvider {

    Stage stage;
    HostServices hostServices;

    public MainStageProvider(Stage stage, HostServices hostServices) {
        this.stage = stage;
        this.hostServices = hostServices;
    }

    @Override
    public Stage getStage() {
        return stage;
    }

    @Override
    public HostServices getHostServices() {
        return hostServices;
    }
}
