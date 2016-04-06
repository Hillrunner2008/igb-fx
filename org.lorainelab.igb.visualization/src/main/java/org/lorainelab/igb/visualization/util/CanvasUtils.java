package org.lorainelab.igb.visualization.util;

import org.lorainelab.igb.visualization.CanvasPane;
import static org.lorainelab.igb.visualization.model.TrackRenderer.MAX_ZOOM_MODEL_COORDINATES_X;

/**
 *
 * @author dcnorris
 */
public class CanvasUtils {

    public static double exponentialScaleTransform(CanvasPane pane, double value) {
        double minScaleX = pane.getWidth() / pane.getModelWidth();
        double maxScaleX = pane.getWidth() / MAX_ZOOM_MODEL_COORDINATES_X;
        ExponentialTransform transform = new ExponentialTransform(minScaleX, maxScaleX);
        return transform.transform(value);
    }

    public static double invertExpScaleTransform(CanvasPane pane, double value) {
        double minScaleX = pane.getWidth() / pane.getModelWidth();
        double maxScaleX = pane.getWidth() / MAX_ZOOM_MODEL_COORDINATES_X;
        ExponentialTransform transform = new ExponentialTransform(minScaleX, maxScaleX);
        return transform.inverseTransform(value);
    }

    public static double linearScaleTransform(CanvasPane pane, double value) {
        double minScaleX = pane.getModelWidth();
        double maxScaleX = MAX_ZOOM_MODEL_COORDINATES_X;
        return pane.getWidth() / (maxScaleX + (minScaleX - maxScaleX) * (1 - (value / 100)));
    }
}
