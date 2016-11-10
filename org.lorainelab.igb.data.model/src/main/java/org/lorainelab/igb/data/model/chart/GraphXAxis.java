package org.lorainelab.igb.data.model.chart;

import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import org.lorainelab.igb.data.model.View;
import static org.lorainelab.igb.data.model.util.Palette.GRAPH_GRID_FILL;

/**
 *
 * @author dcnorris
 */
public class GraphXAxis {

    public static void drawXAxisGridLines(GraphicsContext gc, View view, Rectangle2D boundingRect) {
        final Rectangle2D canvasCoordRect = view.getCanvasContext().getBoundingRect();
        java.awt.geom.Rectangle2D.Double modelCoordRect = view.getMutableCoordRect();
        try {
            gc.save();
            gc.setGlobalAlpha(.7);
            gc.setFill(GRAPH_GRID_FILL.get());

            double minTrackY = 0; //already translated to canvas rect minY
            double maxTrackY = canvasCoordRect.getHeight() / view.getYfactor();
            double majorTickInterval = AxisUtil.getMajorTick(modelCoordRect.getWidth());
            double startMajor;
            if ((modelCoordRect.getMinX() % majorTickInterval) == 0) {
                startMajor = modelCoordRect.getMinX();
            } else {
                startMajor = modelCoordRect.getMinX() + majorTickInterval - (modelCoordRect.getMinX() % majorTickInterval);
            }
            for (double i = startMajor; i < (modelCoordRect.getMaxX() + 1); i += majorTickInterval) {
                double x = i - modelCoordRect.getMinX();
                gc.fillRect(x, minTrackY, 1 / view.getXfactor(), modelCoordRect.getHeight());
            }
        } finally {
            gc.restore();
        }
    }

}
