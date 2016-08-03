/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.visualization.component;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Reference;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.lorainelab.igb.visualization.OverlayCanvasRegion;
import org.lorainelab.igb.visualization.PrimaryCanvasRegion;
import org.lorainelab.igb.visualization.model.CanvasPaneModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jeckstei
 */
@aQute.bnd.annotation.component.Component(immediate = true, provide = ZoomStripe.class)
public class ZoomStripe implements Widget {

    private static final Logger LOG = LoggerFactory.getLogger(ZoomStripe.class);
    private CanvasPaneModel canvasPaneModel;
    private PrimaryCanvasRegion primaryCanvasRegion;
    private OverlayCanvasRegion overlayCanvasRegion;

    @Activate
    public void activate() {
    }

    @Reference
    public void setCanvasPaneModel(CanvasPaneModel canvasPaneModel) {
        this.canvasPaneModel = canvasPaneModel;
    }

    @Override
    public void render() {
        double zoomStripeCoordinate = canvasPaneModel.getZoomStripeCoordinate().doubleValue();
        if (zoomStripeCoordinate >= 0) {
            double modelWidth = canvasPaneModel.getModelWidth().get();

            double xFactor = canvasPaneModel.getxFactor().get();
            double scrollX = canvasPaneModel.getScrollX().get();
            double canvasWidth = primaryCanvasRegion.getWidth();
            final double visibleVirtualCoordinatesX = Math.floor(canvasWidth / xFactor);
            double xOffset = Math.round((scrollX / 100) * (modelWidth - visibleVirtualCoordinatesX));
            double maxXoffset = modelWidth - visibleVirtualCoordinatesX;
            xOffset = Math.min(maxXoffset, xOffset);
            GraphicsContext gc = overlayCanvasRegion.getCanvas().getGraphicsContext2D();
            gc.save();
            gc.setStroke(Color.rgb(0, 0, 0, .3));
            gc.scale(xFactor, 1);
            double x = (canvasPaneModel.getZoomStripeCoordinate().doubleValue()) - xOffset;
            double width = canvasWidth / xFactor;
            if (width > 500) {
                gc.setLineWidth(width * 0.002);
            }
            if (x >= 0 && x <= width) {
                gc.strokeLine(x + .5, 0, x + .5, overlayCanvasRegion.getCanvas().getHeight());
            }
            gc.restore();
        }
    }

    @Reference
    public void setPrimaryCanvasRegion(PrimaryCanvasRegion primaryCanvasRegion) {
        this.primaryCanvasRegion = primaryCanvasRegion;
    }

    @Reference
    public void setOverlayCanvasRegion(OverlayCanvasRegion overlayCanvasRegion) {
        this.overlayCanvasRegion = overlayCanvasRegion;
    }

}
