package org.lorainelab.igb.visualization.util;

/**
 *
 * @author dcnorris
 */
public class BoundsUtil {

    public static double enforceRangeBounds(double originalValue, double boundMin, double boundMax) {
        if (originalValue > boundMax) {
            return boundMax;
        } else if (originalValue < boundMin) {
            return boundMin;
        }
        return originalValue;
    }
}
