package org.lorainelab.igb.data.model.util;

import java.util.Optional;
import javafx.geometry.Rectangle2D;

/**
 *
 * @author dcnorris
 */
public class RectangleUtils {

    public static Optional<Rectangle2D> intersect(Rectangle2D src1, Rectangle2D src2) {
        double x = Math.max(src1.getMinX(), src2.getMinX());
        double y = Math.max(src1.getMinY(), src2.getMinY());
        double maxx = Math.min(src1.getMaxX(), src2.getMaxX());
        double maxy = Math.min(src1.getMaxY(), src2.getMaxY());
        if (maxx - x <= 0 || maxy - y <= 0) {
            return Optional.empty();
        }
        return Optional.of(new Rectangle2D(x, y, maxx - x, maxy - y));
    }
}
