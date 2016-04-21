package org.lorainelab.igb.main;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import java.io.IOException;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.lorainelab.igb.stage.provider.api.StageProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

// This class is needed to work around the problem of being unable to initialize fields from a javafx runtime context
@Component(immediate = true, provide = StageProviderRegistrationManager.class)
public class StageProviderRegistrationManager {

    private ServiceRegistration<StageProvider> registerService;
    private BundleContext bundleContext;
    private Stage stage;

    @Activate
    public void activate(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public void registerStageProvider(Stage stage) {
        this.stage = stage;
        registerService = bundleContext.registerService(StageProvider.class, new MainStageProvider(stage), null);
    }

    @Deactivate
    public void deactivate() throws IOException {
        Platform.runLater(() -> {
            stage.hide();
            
        });
        registerService.unregister();
    }

}
