/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.visualization.widget;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.lorainelab.igb.visualization.model.CanvasModel;
import org.lorainelab.igb.visualization.ui.OverlayRegion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jeckstei
 */
@Component(immediate = true, provide = Widget.class)
public class ZoomStripe implements Widget {

    private static final Logger LOG = LoggerFactory.getLogger(ZoomStripe.class);
    private OverlayRegion overlayRegion;

    @Activate
    public void activate() {
    }

    @Override
    public void render(CanvasModel canvasModel) {
        double zoomStripeCoordinate = canvasModel.getZoomStripeCoordinate().doubleValue();
        if (zoomStripeCoordinate >= 0) {
            double modelWidth = canvasModel.getModelWidth().get();

            double xFactor = canvasModel.getxFactor().get();
            double scrollX = canvasModel.getScrollX().get();
            double canvasWidth = overlayRegion.getWidth();
            final double visibleVirtualCoordinatesX = Math.floor(canvasWidth / xFactor);
            double xOffset = Math.round((scrollX / 100) * (modelWidth - visibleVirtualCoordinatesX));
            double maxXoffset = modelWidth - visibleVirtualCoordinatesX;
            xOffset = Math.min(maxXoffset, xOffset);
            GraphicsContext gc = overlayRegion.getCanvas().getGraphicsContext2D();
            gc.save();
            gc.setStroke(Color.rgb(0, 0, 0, .3));
            gc.scale(xFactor, 1);
            double x = (canvasModel.getZoomStripeCoordinate().doubleValue()) - xOffset;
            double width = canvasWidth / xFactor;
            if (width > 500) {
                gc.setLineWidth(width * 0.002);
            }
            if (x >= 0 && x <= width) {
                gc.strokeLine(x + .5, 0, x + .5, overlayRegion.getCanvas().getHeight());
            }
            gc.restore();
        }
    }

    @Reference
    public void setOverlayRegion(OverlayRegion overlayRegion) {
        this.overlayRegion = overlayRegion;
    }

    @Override
    public int getZindex() {
        return 5;
    }

    @Override
    public boolean isOverlayWidget() {
        return true;
    }

}
