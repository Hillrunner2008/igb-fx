package org.lorainelab.igb.main;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import java.io.IOException;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.lorainelab.igb.stage.provider.api.StageProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// This class is needed to work around the problem of being unable to initialize fields from a javafx runtime context
@Component(immediate = true, provide = StageProviderRegistrationManager.class)
public class StageProviderRegistrationManager {

    private static final Logger LOG = LoggerFactory.getLogger(StageProviderRegistrationManager.class);
    private ServiceRegistration<StageProvider> registerService;
    private BundleContext bundleContext;
    private Stage stage;

    @Activate
    public void activate(BundleContext bundleContext) {
        LOG.info("StageProviderRegistrationManager activated");
        this.bundleContext = bundleContext;
    }

    public void registerStageProvider(Stage stage, Stage splashStage, HostServices hostServices) {
        LOG.info("registering StageProvider");
        this.stage = stage;
        try {
            registerService = bundleContext.registerService(StageProvider.class, new MainStageProvider(stage, splashStage, hostServices), null);
            LOG.info("StageProvider registered");
        } catch (Throwable t) {
            LOG.error(t.getMessage(), t);
        }
    }

    @Deactivate
    public void deactivate() throws IOException {
        LOG.info("StageProviderRegistrationManager deactivated");
        Platform.runLater(() -> {
            stage.hide();

        });
        registerService.unregister();
    }

}
