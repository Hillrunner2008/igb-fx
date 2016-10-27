package org.lorainelab.igb.visualization.util;

import org.lorainelab.igb.utils.ExponentialTransform;

/**
 *
 * @author dcnorris
 */
public class CanvasUtils {

    public static double exponentialScaleTransform(double canvasWidth, double modelWidth, double value) {
        double minScaleX = canvasWidth / modelWidth;
        double maxScaleX = 20;
        ExponentialTransform transform = new ExponentialTransform(minScaleX, maxScaleX);
        return transform.transform(value);
    }

    public static double invertExpScaleTransform(double canvasWidth, double modelWidth, double value) {
        double minScaleX = canvasWidth / modelWidth;
        double maxScaleX = 20;
        ExponentialTransform transform = new ExponentialTransform(minScaleX, maxScaleX);
        return transform.inverseTransform(value);
    }

    public static double linearScaleTransform(double canvasWidth, double modelWidth, double value) {
        double minScaleX = modelWidth;
        double maxScaleX = 20;
        return canvasWidth / (maxScaleX + (minScaleX - maxScaleX) * (1 - (value / 100)));
    }
}
