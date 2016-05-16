package org.lorainelab.igb.main;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Reference;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class Launcher extends Application {
    
    private static final Logger LOG = LoggerFactory.getLogger(Launcher.class);
    private StageProviderRegistrationManager stageRegistrationManager;
    public void activate() throws InterruptedException {
        Executors.defaultThreadFactory().newThread(() -> {
            Thread.currentThread().setContextClassLoader(
                    this.getClass().getClassLoader());
            LOG.info("Launching IGB FX");
            try {
                launch();
            } catch (IllegalStateException ex) {
                //Can be caused by relaunch when javafx process is already running
                handleRestartEvent();
            }
        }).start();
    }
    
    private void handleRestartEvent() {
        Platform.runLater(() -> {
            stageRegistrationManager.registerStageProvider(new Stage(), getHostServices());
        });
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        final BundleContext bundleContext = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        ServiceReference<StageProviderRegistrationManager> serviceReference = bundleContext.getServiceReference(StageProviderRegistrationManager.class);
        StageProviderRegistrationManager stageRegistrationManager = bundleContext.getService(serviceReference);
        stageRegistrationManager.registerStageProvider(primaryStage, getHostServices());
        primaryStage.setOnHiding(event -> {
            System.out.println("setOnHiding");
        });
        primaryStage.setOnHidden(event -> {
            System.out.println("setOnHidden");
        });
        primaryStage.setOnShown(event -> {
            System.out.println("setOnShown");
        });
        primaryStage.setOnShowing(event -> {
            System.out.println("setOnShowing");
        });
        primaryStage.setOnCloseRequest((WindowEvent event) -> {
             System.out.println("setOnCloseRequest");
            if (event.getEventType().equals(WindowEvent.WINDOW_CLOSE_REQUEST)) {
                LOG.info("Received request to shutdown container");
                CompletableFuture.runAsync(() -> {
                    try {
                        BundleContext bc = FrameworkUtil.getBundle(Launcher.class).getBundleContext();
                        Bundle bundle = bc.getBundle(0);
                        bundle.stop();
                    } catch (Exception e) {
                        System.err.println("Error when shutting down Apache Karaf");
                    }
                });
            }
        });
    }
    
    @Deactivate
    public void deactivate() {
    }
    
    @Reference
    public void setStageRegistrationManger(StageProviderRegistrationManager registrationManager) {
        this.stageRegistrationManager = registrationManager;
    }
}
