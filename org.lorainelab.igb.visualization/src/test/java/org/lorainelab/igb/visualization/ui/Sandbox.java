package org.lorainelab.igb.visualization.ui;

import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

/**
 *
 * @author dcnorris
 */
public class Sandbox extends ApplicationTest {

    @Test
    public void vboxScaleTest() {

    }

    @Override
    public void start(Stage stage) throws Exception {

        VBox vbox = new VBox();
        Pane p = new Pane();
        p.setPrefHeight(100);
        vbox.getChildren().add(p);
        p.scaleYProperty().set(2);
        stage.setScene(new Scene(vbox));
        stage.show();
        System.out.println(p.boundsInLocalProperty().get().getHeight());
        
    }

}
