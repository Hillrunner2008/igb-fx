package org.lorainelab.igb.data.model.chart;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import com.google.common.collect.Range;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
public class ChartData {

    private static final Logger LOG = LoggerFactory.getLogger(ChartData.class);
    private int[] x;
    private int[] w;
    private double[] y;
    private Rectangle.Double dataBounds;
    GeometryFactory gf = new GeometryFactory();

    public ChartData(int[] x, int[] w, double[] y) {
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

    public List<Coordinate> getDataInRange(Range<Double> range, double minY, double maxY, double modelCoordinatesPerPixel) {

        final List<Coordinate> coordinates = new ArrayList<>();
        int startIndex = findStartIndex(range.lowerEndpoint());
        int endIndex = findEndIndex(range.upperEndpoint());
        for (int i = startIndex; i <= endIndex; i++) {
            final int cx = x[i];
            double cy = Math.max(Math.min(y[i], maxY), minY);
            double cw = w[i];

            if (cw < modelCoordinatesPerPixel) {
                while (i + 1 <= endIndex && cw < modelCoordinatesPerPixel) {
                    final int nextXPosition = x[i + 1];
                    final int nextXPositionWidth = w[i + 1];
                    final double nextPixelPosition = nextXPosition / modelCoordinatesPerPixel;
                    //if nextPosition is within 1 pixel
                    if (nextPixelPosition < (cx / modelCoordinatesPerPixel) + 1) {
                        cw += nextXPositionWidth;
                        cy = Math.max(Math.min(y[i + 1], maxY), cy);
                    } else {
                        break;
                    }
                    i++;
                }
            }

            coordinates.add(new Coordinate(cx, cy, cw));
        }
        return coordinates;
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
