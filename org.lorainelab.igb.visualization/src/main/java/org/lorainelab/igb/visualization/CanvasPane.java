package org.lorainelab.igb.visualization;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
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
    private PrimaryCanvasRegion primaryCanvas;
    private OverlayCanvasRegion overlayCanvas;

    @Activate
    public void activate() {
        Platform.runLater(() -> {
            getChildren().add(primaryCanvas);
//            getChildren().add(overlayCanvas);
        });
    }

    @Reference
    public void setPrimaryCanvas(PrimaryCanvasRegion primaryCanvas) {
        this.primaryCanvas = primaryCanvas;
    }

//    @Reference
//    public void setOverlayCanvas(OverlayCanvasRegion overlayCanvas) {
//        this.overlayCanvas = overlayCanvas;
//    }

    //TODO remove this compiler hack 
    public Canvas getCanvas() {
        return null;
    }
}
