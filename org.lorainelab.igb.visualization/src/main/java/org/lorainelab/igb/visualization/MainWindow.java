package org.lorainelab.igb.visualization;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Reference;
import java.awt.SplashScreen;
import java.io.IOException;
import java.net.URL;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.lorainelab.igb.stage.provider.api.StageProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true)
public class MainWindow {

    private static final Logger LOG = LoggerFactory.getLogger(MainWindow.class);
    private MainController controller;
    private Stage stage;
    private VBox root;

    public MainWindow() {
    }

    @Activate
    public void activate() {
        try {
            initiailize();
        } catch (IOException ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }

    private void initiailize() throws IOException {
        SplashScreen splashScreen = SplashScreen.getSplashScreen();
        if (splashScreen != null) {
            splashScreen.close();
        }
        initializeFxRuntime();
        final URL resource = MainWindow.class.getClassLoader().getResource("main.fxml");
        FXMLLoader loader = new FXMLLoader(resource);
        loader.setClassLoader(this.getClass().getClassLoader());
        loader.setController(controller);
        root = loader.load();
        Scene scene = new Scene(root);
        stage.setTitle("JavaFx IGB");
        Platform.runLater(() -> {
            stage.setScene(scene);
            stage.show();
        });
    }

    private void initializeFxRuntime() {
        new JFXPanel(); // runtime initializer, do not remove
        Platform.setImplicitExit(false);
    }

    @Deactivate
    public void deactivate() {
        Platform.runLater(() -> {
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
        this.stage = stageProvider.getStage();
    }

    @Reference
    public void setController(MainController controller) {
        this.controller = controller;
    }
}
