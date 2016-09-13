package org.lorainelab.igb.main;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
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
    private Stage splashStage;

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
        Platform.runLater(() -> {
            Stage newStage = new Stage();
            Stage newsplashStage = new Stage();
            newStage.setOnCloseRequest(onClose);
            stageRegistrationManager.registerStageProvider(newStage, newsplashStage, getHostServices());
        });
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        final BundleContext bundleContext = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        ServiceReference<StageProviderRegistrationManager> serviceReference = bundleContext.getServiceReference(StageProviderRegistrationManager.class);
        StageProviderRegistrationManager stageRegistrationManager = bundleContext.getService(serviceReference);
        initSplashScreen();
        stageRegistrationManager.registerStageProvider(primaryStage, splashStage, getHostServices());
        primaryStage.setOnCloseRequest(onClose);
    }

    public void initSplashScreen() {
        splashStage = new Stage(StageStyle.TRANSPARENT);
        splashStage.setAlwaysOnTop(true);
        ProgressBar bar = new ProgressBar();
        bar.setPadding(new Insets(0, 0, 10, 0));
        BorderPane p = new BorderPane();
        bar.prefWidthProperty().bind(p.widthProperty().subtract(20));
        BorderPane center = new BorderPane();
        Label label = new Label("Starting IGBfx");
        VBox vbox = new VBox(label, bar);
        center.setCenter(vbox);
        p.setBottom(center);
        BundleContext bc = FrameworkUtil.getBundle(Launcher.class).getBundleContext();
// doesn't show long enough to be useful        
//        bar.progressProperty().set(1);
//        int[] bundleCount = new int[]{bc.getBundles().length};
//        int[] startedCount = new int[]{1};
//        for (Bundle b : bc.getBundles()) {
//            //skip to features.xml
//            if (!b.getSymbolicName().equals("features.xml")) {
//                startedCount[0]++;
//                Platform.runLater(() -> {
//                    bar.progressProperty().set((double) startedCount[0] / (double) bundleCount[0]);
//                });
//                continue;
//            }
//            Optional.ofNullable(b.getBundleContext()).ifPresent(bundleContext -> {
//                bundleContext.addBundleListener(be -> {
//                    Platform.runLater(() -> {
//                        if (splashStage.isShowing()) {
//                            if (be.getType() == BundleEvent.STARTING) {
//                                label.setText(be.getBundle().getSymbolicName() + " starting");
//                            } else if (be.getType() == BundleEvent.STARTING) {
//                                label.setText(be.getBundle().getSymbolicName() + " started");
//                                startedCount[0]++;
//                                bar.progressProperty().set((double) startedCount[0] / (double) bundleCount[0]);
//                            }
//                        }
//                    });
//                });
//            });
//        }
        StackPane stack = new StackPane(new ImageView(bc.getBundle().getEntry("splash.png").toExternalForm()), p);
        splashStage.setScene(new Scene(stack, 600, 200));
        splashStage.show();
    }

    @Reference
    public void setStageRegistrationManger(StageProviderRegistrationManager registrationManager) {
        this.stageRegistrationManager = registrationManager;
    }
}
