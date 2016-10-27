package org.lorainelab.igb.main;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.prefs.Preferences;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import org.lorainelab.igb.preferences.PreferenceUtils;
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
    private Stage splashStage;
    private Preferences modulePreferencesNode;
    private Stage primaryStage;
    public static final String WIDTH_KEY = "stageWidth";
    public static final String HEIGHT_KEY = "stageHeight";

    public Launcher() {
        modulePreferencesNode = PreferenceUtils.getPackagePrefsNode(Launcher.class);
    }

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

    private final EventHandler<WindowEvent> onClose = (WindowEvent event) -> {
        if (event.getEventType().equals(WindowEvent.WINDOW_CLOSE_REQUEST)) {
            LOG.info("Received request to shutdown container");
            CompletableFuture.runAsync(() -> {
                try {
                    modulePreferencesNode.put(HEIGHT_KEY, primaryStage.getHeight() + "");
                    modulePreferencesNode.put(WIDTH_KEY, primaryStage.getWidth() + "");
                    BundleContext bc = FrameworkUtil.getBundle(Launcher.class).getBundleContext();
                    Bundle bundle = bc.getBundle(0);
                    bundle.stop();
                } catch (Exception e) {
                    System.err.println("Error when shutting down Apache Karaf");
                }
            });
        }
    };

    private void handleRestartEvent() {
        LOG.info("Received handleRestartEvent request");
        Platform.runLater(() -> {
            Stage newStage = new Stage();
            Stage newsplashStage = new Stage();
            newStage.setOnCloseRequest(onClose);
            initPrimaryStage(newStage);
            stageRegistrationManager.registerStageProvider(newStage, newsplashStage, getHostServices());
        });
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        LOG.info("Starting Fx Application");
        final BundleContext bundleContext = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        ServiceReference<StageProviderRegistrationManager> serviceReference = bundleContext.getServiceReference(StageProviderRegistrationManager.class);
        StageProviderRegistrationManager stageRegistrationManager = bundleContext.getService(serviceReference);
        initSplashScreen();
        initPrimaryStage(primaryStage);
        stageRegistrationManager.registerStageProvider(primaryStage, splashStage, getHostServices());
        primaryStage.setOnCloseRequest(onClose);
    }

    private void initPrimaryStage(Stage pStage) {
        this.primaryStage = pStage;
        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
        double width = primaryScreenBounds.getWidth();
        double height = primaryScreenBounds.getHeight();
        try {
            double sHeight = Double.parseDouble(modulePreferencesNode.get(HEIGHT_KEY, ""));
            double sWidth = Double.parseDouble(modulePreferencesNode.get(WIDTH_KEY, ""));
            if (sHeight / height < 0.4 || sWidth / width < 0.4 || sHeight > height || sWidth > width) {
                primaryStage.setHeight(height * .8);
                primaryStage.setWidth(width * .8);
            } else {
                primaryStage.setHeight(sHeight);
                primaryStage.setWidth(sWidth);
            }
        } catch (Exception ex) {
            primaryStage.setHeight(height);
            primaryStage.setWidth(width);
        }
    }

    public void initSplashScreen() throws IOException {
        splashStage = new Stage(StageStyle.TRANSPARENT);
        splashStage.setAlwaysOnTop(true);
        Platform.runLater(() -> {
            try {
                final URL resource = Launcher.class.getClassLoader().getResource("splashScreen.fxml");
                FXMLLoader fxmlLoader = new FXMLLoader(resource);
                fxmlLoader.setClassLoader(this.getClass().getClassLoader());
                fxmlLoader.setController(this);
                StackPane stack = fxmlLoader.load();
                splashStage.setScene(new Scene(stack, 550, 250));
                splashStage.show();
            } catch (IOException ex) {
                LOG.error(ex.getMessage(), ex);
            }
        });
    }

    @Reference
    public void setStageRegistrationManger(StageProviderRegistrationManager registrationManager) {
        this.stageRegistrationManager = registrationManager;
    }
}
