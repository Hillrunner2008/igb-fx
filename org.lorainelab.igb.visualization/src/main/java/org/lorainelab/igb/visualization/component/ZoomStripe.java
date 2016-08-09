/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.visualization.component;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.lorainelab.igb.visualization.PrimaryCanvasRegion;
import org.lorainelab.igb.visualization.model.CanvasPaneModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jeckstei
 */
@Component(immediate = true, provide = Widget.class)
public class ZoomStripe implements Widget {

    private static final Logger LOG = LoggerFactory.getLogger(ZoomStripe.class);
    private PrimaryCanvasRegion primaryCanvasRegion;

    @Activate
    public void activate() {
    }

    @Override
    public void render(CanvasPaneModel canvasPaneModel) {
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
            GraphicsContext gc = primaryCanvasRegion.getCanvas().getGraphicsContext2D();
            gc.save();
            gc.setStroke(Color.rgb(0, 0, 0, .3));
            gc.scale(xFactor, 1);
            double x = (canvasPaneModel.getZoomStripeCoordinate().doubleValue()) - xOffset;
            double width = canvasWidth / xFactor;
            if (width > 500) {
                gc.setLineWidth(width * 0.002);
            }
            if (x >= 0 && x <= width) {
                gc.strokeLine(x + .5, 0, x + .5, primaryCanvasRegion.getCanvas().getHeight());
            }
            gc.restore();
        }
    }

    @Reference
    public void setOverlayCanvasRegion(PrimaryCanvasRegion primaryCanvasRegion) {
        this.primaryCanvasRegion = primaryCanvasRegion;
    }

    @Override
    public int getZindex() {
        return 5;
    }

}
