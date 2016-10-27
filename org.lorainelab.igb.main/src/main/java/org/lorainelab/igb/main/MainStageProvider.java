package org.lorainelab.igb.main;

import javafx.application.HostServices;
import javafx.stage.Stage;
import org.lorainelab.igb.stage.provider.api.StageProvider;

public class MainStageProvider implements StageProvider {

    Stage stage;
    Stage splashStage;
    HostServices hostServices;

    public MainStageProvider(Stage stage, Stage splashStage, HostServices hostServices) {
        this.stage = stage;
        this.splashStage = splashStage;
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

    @Override
    public Stage getSplashStage() {
        return splashStage;
    }
}
