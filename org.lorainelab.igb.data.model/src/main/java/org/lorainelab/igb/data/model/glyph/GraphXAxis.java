package org.lorainelab.igb.data.model.glyph;

import javafx.scene.canvas.GraphicsContext;
import org.lorainelab.igb.data.model.View;
import static org.lorainelab.igb.data.model.util.Palette.GRAPH_GRID_FILL;

/**
 *
 * @author dcnorris
 */
public class GraphXAxis {

    public static void drawXAxisGridLines(GraphicsContext gc, View view) {
        java.awt.geom.Rectangle2D.Double modelCoordRect = view.getMutableCoordRect();
        try {
            gc.save();
            gc.setGlobalAlpha(.7);
            gc.setFill(GRAPH_GRID_FILL);
            gc.setStroke(GRAPH_GRID_FILL);
            final double maxY = (view.getCanvasContext().getBoundingRect().getMaxY());
            double majorTickInterval = AxisUtil.getMajorTick(modelCoordRect.getWidth());
            long startMajor;
            if ((modelCoordRect.getMinX() % majorTickInterval) == 0) {
                startMajor = (long) modelCoordRect.getMinX();
            } else {
                startMajor = (long) (modelCoordRect.getMinX() + majorTickInterval - (modelCoordRect.getMinX() % majorTickInterval));
            }
            for (long i = startMajor; i < (modelCoordRect.getMaxX() + 1); i += majorTickInterval) {
                double x = i - modelCoordRect.getMinX();
                gc.fillRect(x, modelCoordRect.getMinY(), 1 / view.getXfactor(), modelCoordRect.getHeight());
            }
        } finally {
            gc.restore();
        }
    }

}
