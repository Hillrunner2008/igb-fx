package org.lorainelab.igb.main;

import aQute.bnd.annotation.component.Component;
import java.util.concurrent.Executors;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.stage.Stage;
import org.lorainelab.igb.visualization.StageProvider;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class Launcher extends Application {

    private static final Logger LOG = LoggerFactory.getLogger(Launcher.class);

    public void activate() throws InterruptedException {
        Executors.defaultThreadFactory().newThread(() -> {
            Thread.currentThread().setContextClassLoader(
                    this.getClass().getClassLoader());
            LOG.info("Launching IGB FX");
            launch();
        }).start();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FrameworkUtil.getBundle(this.getClass()).getBundleContext()
                .registerService(StageProvider.class, new MainStageProvider(primaryStage), null);
    }

}
