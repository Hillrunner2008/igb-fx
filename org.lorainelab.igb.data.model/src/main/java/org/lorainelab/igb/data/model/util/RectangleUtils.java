package org.lorainelab.igb.data.model.util;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import java.awt.Rectangle;
import java.util.Optional;
import javafx.geometry.Rectangle2D;

/**
 *
 * @author dcnorris
 */
public class RectangleUtils {

    public static Optional<Rectangle2D> intersect(Rectangle2D src1, Rectangle2D src2) {
        checkNotNull(src1);
        checkNotNull(src2);
        double x = Math.max(src1.getMinX(), src2.getMinX());
        double y = Math.max(src1.getMinY(), src2.getMinY());
        double maxx = Math.min(src1.getMaxX(), src2.getMaxX());
        double maxy = Math.min(src1.getMaxY(), src2.getMaxY());
        if (maxx - x <= 0 || maxy - y <= 0) {
            return Optional.empty();
        }
        return Optional.of(new Rectangle2D(x, y, maxx - x, maxy - y));
    }

    public static void intersect(Rectangle.Double src1, Rectangle.Double src2, Rectangle.Double dest) {
        checkNotNull(src1);
        checkNotNull(src2);
        checkNotNull(dest);
        checkArgument(src1.intersects(src2), "cannot create intersection between non connected rectangles {} {}", src1, src2);
        double x = Math.max(src1.getMinX(), src2.getMinX());
        double y = Math.max(src1.getMinY(), src2.getMinY());
        double maxx = Math.min(src1.getMaxX(), src2.getMaxX());
        double maxy = Math.min(src1.getMaxY(), src2.getMaxY());
//        if (maxx - x <= 0 || maxy - y <= 0) {
//            throw new IllegalArgumentException("cannot create intersection between non connected rectangles");
//        }
        dest.setRect(x, y, maxx - x, maxy - y);
    }
}
