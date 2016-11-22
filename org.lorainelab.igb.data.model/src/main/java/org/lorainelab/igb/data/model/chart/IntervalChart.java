package org.lorainelab.igb.data.model.chart;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import com.vividsolutions.jts.geom.Coordinate;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
public class IntervalChart {

    private static final Logger LOG = LoggerFactory.getLogger(IntervalChart.class);
    private int[] x;
    private int[] w;
    private double[] y;
    private Rectangle.Double dataBounds;

    public IntervalChart(int[] x, int[] w, double[] y) {
        checkNotNull(x);
        checkNotNull(w);
        checkNotNull(y);
        checkState(x.length == w.length && w.length == y.length);
        this.x = x;
        this.w = w;
        this.y = y;
        dataBounds = new Rectangle2D.Double();

        for (int i = 0; i < x.length; i++) {
            dataBounds.add(x[i], y[i]);
        }
    }

    public List<Coordinate> getDataInRange(Rectangle2D.Double modelCoordRect, double modelCoordinatesPerPixel) {
        if (x.length == 0) {
            return Collections.EMPTY_LIST;
        }
        int startIndex = findStartIndex(modelCoordRect.getMinX());
        int endIndex = findEndIndex(modelCoordRect.getMaxX());

        final List<Coordinate> coordinates = new ArrayList<>();
        for (int i = startIndex; i <= endIndex; i++) {
            final int cx = Math.max(x[i], (int) modelCoordRect.getMinX());
            double cy = y[i];
            double cw = w[i];

            if (cw < modelCoordinatesPerPixel) {
                double canvasPositionX = (cx / modelCoordinatesPerPixel) + 1;
                while (i + 1 <= endIndex && (int) x[i + 1] / modelCoordinatesPerPixel < canvasPositionX) {
                    final int nextXPositionWidth = w[i + 1];
                    if (nextXPositionWidth < modelCoordinatesPerPixel) {
                        cw += w[i + 1];
                        final double clippedToMaxYValue = y[i + 1];
                        cy = Math.max(clippedToMaxYValue, cy);
                        i++;
                    } else {
                        break;
                    }
                }
            }
            if (cx + cw > modelCoordRect.getMaxX()) {
                cw = modelCoordRect.getMaxX() - cx;
            }
            coordinates.add(new Coordinate(cx, cy, cw));
        }
        return coordinates;
    }

    private double clipToYBounds(Rectangle2D.Double modelCoordRect, double value) {
        double minY = modelCoordRect.y;
        double maxY = modelCoordRect.getMaxY();
        return Math.max(Math.min(value, maxY), minY);

    }

    public Rectangle2D.Double getDataBounds() {
        return dataBounds;
    }

    private int findStartIndex(double xmin) {
        int index = Arrays.binarySearch(x, (int) Math.floor(xmin));
        if (index >= 0) {
            return index;
        }
        index = Math.max(0, (-index - 2));
        return index;
    }

    private int findEndIndex(double xmax) {
        int index = Arrays.binarySearch(x, (int) Math.ceil(xmax));
        if (index >= 0) {
            return index;
        }
        index = -index - 1;
        index = Math.min(index, x.length - 1);
        index = Math.max(0, index);
        return index;

    }

}
