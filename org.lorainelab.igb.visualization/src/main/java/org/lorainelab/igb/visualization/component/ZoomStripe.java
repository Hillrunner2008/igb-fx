/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.visualization.component;

import com.google.common.collect.Lists;
import java.util.List;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.lorainelab.igb.visualization.component.api.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jeckstei
 */
public class ZoomStripe extends Component<ZoomStripeProps, ZoomStripeState> {

    private static final Logger LOG = LoggerFactory.getLogger(ZoomStripe.class);

    @Override
    public Component beforeComponentReady() {
        //
        return this;
    }

    @Override
    public List<Component> render() {
        //LOG.info("render zoomstrip");
        double zoomStripeCoordinate = this.getProps().getZoomStripeCoordinate();
        if (zoomStripeCoordinate >= 0) {
            double modelWidth = this.getProps().getModelWidth();
            double modelHeight = this.getProps().getModelHeight();
            double xFactor = this.getProps().getxFactor();
            double scrollX = this.getProps().getScrollX();
            final double visibleVirtualCoordinatesX = Math.floor(this.getProps().getCanvasWidth() / xFactor);
            double xOffset = Math.round((scrollX / 100) * (modelWidth - visibleVirtualCoordinatesX));
            Rectangle2D viewBoundingRectangle = new Rectangle2D(0, 0, modelWidth, modelHeight);
            double zoomStripePositionPercentage = (zoomStripeCoordinate - viewBoundingRectangle.getMinX()) / viewBoundingRectangle.getWidth();
            xOffset = Math.max(zoomStripeCoordinate - (visibleVirtualCoordinatesX * zoomStripePositionPercentage), 0);
            double maxXoffset = modelWidth - visibleVirtualCoordinatesX;
            xOffset = Math.min(maxXoffset, xOffset);
            GraphicsContext gc = this.getProps().getCanvas().getGraphicsContext2D();
            gc.save();
            gc.setStroke(Color.rgb(0, 0, 0, .3));
            gc.scale(this.getProps().getxFactor(), 1);
            double x = (this.getProps().getZoomStripeCoordinate()) - xOffset;
            double width = this.getProps().getCanvas().getWidth() / this.getProps().getxFactor();
            if (width > 500) {
                gc.setLineWidth(width * 0.002);
            }
            if (x >= 0 && x <= width) {
                gc.strokeLine(x + .5, 0, x + .5, this.getProps().getCanvas().getHeight());
            }
            gc.restore();
        }
        return Lists.newArrayList();
    }

}
