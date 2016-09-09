package org.lorainelab.igb.data.model.glyph;

import static com.google.common.base.Preconditions.*;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import java.awt.Rectangle;
import java.util.Map;
import java.util.Optional;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.lorainelab.igb.data.model.View;
import static org.lorainelab.igb.data.model.util.RectangleUtils.intersect;

/**
 *
 * @author dcnorris
 */
public class GraphGlyph implements Glyph {

    private final Rectangle.Double bounds;
    private final Rectangle2D boundingRect;
    private final RangeMap<Double, Double> graphIntervals;

    public GraphGlyph(RangeMap<Double, Double> graphIntervals) {
        checkNotNull(graphIntervals);
        this.graphIntervals = graphIntervals;
        bounds = new Rectangle.Double();

        for (Map.Entry<Range<Double>, Double> entry : graphIntervals.asMapOfRanges().entrySet()) {
            Range<Double> r = entry.getKey();
            bounds.add(new Rectangle.Double(r.lowerEndpoint(), 0, r.upperEndpoint() - r.lowerEndpoint(), entry.getValue()));
        }
        this.boundingRect = new Rectangle2D(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    @Override
    public Color getFill() {
        return Color.BLUEVIOLET;
    }

    @Override
    public Color getStrokeColor() {
        return Color.BLUEVIOLET;
    }

    @Override
    public Rectangle2D getBoundingRect() {
        return boundingRect;
    }

    @Override
    public void draw(GraphicsContext gc, View view, Rectangle2D slotBoundingViewRect) {
        double modelCoordinatesPerScreenXPixel = view.getBoundingRect().getWidth() / view.getCanvasContext().getBoundingRect().getWidth();
        double modelCoordinatesPerScreenYPixel = view.getBoundingRect().getHeight() / view.getCanvasContext().getBoundingRect().getHeight();

        Rectangle2D viewRect = view.getBoundingRect();
        double viewMinx = viewRect.getMinX();
        gc.save();
        gc.setFill(getFill());
        gc.setStroke(getStrokeColor());
        for (Map.Entry<Range<Double>, Double> entry : graphIntervals.subRangeMap(view.getXrange()).asMapOfRanges().entrySet()) {
            Range<Double> r = entry.getKey();
            final double x = r.lowerEndpoint();
            double height = entry.getValue();
            double width = r.upperEndpoint() - r.lowerEndpoint() + 1;
            final double y = view.getCanvasContext().getBoundingRect().getMinY() / view.getYfactor();
            SHARED_RECT.setRect(x, y - height, width, height);
            if (SHARED_RECT.intersects(view.getMutableBoundingRect())) {
                intersect(view.getMutableBoundingRect(), SHARED_RECT, SHARED_RECT);
                width = SHARED_RECT.width;
                height = SHARED_RECT.height;
                if (width > 0 && width < modelCoordinatesPerScreenXPixel) {
                    width = modelCoordinatesPerScreenXPixel;
                }
                if (height > 0 && height < modelCoordinatesPerScreenYPixel) {
                    height = modelCoordinatesPerScreenYPixel;
                }
                gc.fillRect(SHARED_RECT.x - viewMinx, SHARED_RECT.y - viewRect.getMinY(), width, height);
            }
        }
        gc.restore();

    }

    public Optional<Rectangle.Double> calculateDrawRect(View view, Rectangle2D slotBoundingViewRect) {
        SHARED_RECT.setRect(view.getMutableBoundingRect());
        return Optional.of(SHARED_RECT);
    }

}
