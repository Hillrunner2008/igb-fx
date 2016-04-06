package org.lorainelab.igb.visualization;

import java.net.URL;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        final URL resource = MainApp.class.getClassLoader().getResource("genoVizFx.fxml");
        FXMLLoader loader = new FXMLLoader(resource);
        Parent root = loader.load();
        GenoVixFxController controller = loader.getController();
        Scene scene = new Scene(root);
        scene.getStylesheets().add("/styles/Styles.css");
        
        stage.setTitle("GenoViz Fx");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
