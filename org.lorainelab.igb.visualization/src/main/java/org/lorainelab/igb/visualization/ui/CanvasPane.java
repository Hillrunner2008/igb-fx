package org.lorainelab.igb.visualization.ui;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import javafx.application.Platform;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true, provide = CanvasPane.class)
public class CanvasPane extends StackPane {

    private static final Logger LOG = LoggerFactory.getLogger(CanvasPane.class);
    private CanvasRegion canvasRegion;

    @Activate
    public void activate() {
        Platform.runLater(() -> {
            getChildren().add(canvasRegion);
        });
    }

    @Reference
    public void setCanvasRegion(CanvasRegion canvasRegion) {
        this.canvasRegion = canvasRegion;
    }

}
