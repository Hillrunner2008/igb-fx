package org.lorainelab.igb.data.model.glyph;

import java.util.Optional;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.lorainelab.igb.data.model.View;

/**
 *
 * @author dcnorris
 */
public class GraphGlyph implements Glyph {

    private final Rectangle2D boundingRect;

    public GraphGlyph(Rectangle2D boundingRect) {
        this.boundingRect = boundingRect;
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
        Rectangle2D viewRect = view.getBoundingRect();
        Rectangle2D trackRect = view.getCanvasContext().getBoundingRect();
        double x = boundingRect.getMinX();
        double maxX = boundingRect.getMaxX();
        double width = boundingRect.getWidth();
        double height = boundingRect.getHeight();
        if (x < viewRect.getMinX()) {
            double offSet = viewRect.getMinX() - x;
            width = width - offSet;
            x = 0;
        } else {
            x = x - viewRect.getMinX();
        }
        if (maxX > viewRect.getMaxX()) {
            int offSet = (int) (maxX - viewRect.getMaxX());
            width = width - offSet;
        }
        gc.setFill(getFill());
        gc.setStroke(getStrokeColor());
        final double tracksHeight = view.getCanvasContext().getBoundingRect().getHeight() / view.getYfactor();
        double cutOff = tracksHeight - view.getBoundingRect().getHeight();
        double y;
            final double tracksMinY = trackRect.getMinY() / view.getYfactor();
        if (view.isIsNegative()) {
            y = tracksMinY;
        } else {
            y = trackRect.getMaxY() / view.getYfactor() - boundingRect.getHeight();
        }
        if (y < tracksMinY) {
            double offSet = (tracksMinY - y);
            height = height - offSet;
            y = 0;
        } else {
            if (view.isIsNegative()) {
            } else {
                y -= cutOff;
            }
        }
        gc.fillRect(x, y, width, height);
    }

    public Optional<Rectangle2D> getViewBoundingRect(View view, Rectangle2D slotBoundingViewRect) {
        final double tracksHeight = view.getCanvasContext().getBoundingRect().getHeight() / view.getYfactor();
        double cutOff = tracksHeight - view.getBoundingRect().getHeight();
        Rectangle2D viewRect = view.getBoundingRect();
        Rectangle2D boundingRect = getBoundingRect();
        double x = boundingRect.getMinX();
        double maxX = boundingRect.getMaxX();
        double y = boundingRect.getMinY() + slotBoundingViewRect.getMinY();
        double width = boundingRect.getWidth();
        double height = boundingRect.getHeight();

        if (x < viewRect.getMinX()) {
            double offSet = viewRect.getMinX() - x;
            width = width - offSet;
            x = 0;
        } else {
            x = x - viewRect.getMinX();
        }
        if (maxX > viewRect.getMaxX()) {
            int offSet = (int) (maxX - viewRect.getMaxX());
            width = width - offSet;
        }
        if (y < viewRect.getMinY()) {
            double offSet = (viewRect.getMinY() - y - cutOff);
            height = height - offSet;
            y = 0;
        } else {
            y = y - viewRect.getMinY();
            if (view.isIsNegative()) {
                height = height - cutOff;
            } else {
                y -= cutOff;
            }
        }
        if (width <= 0 || height <= 0) {
            return Optional.empty();
        }
        return Optional.of(new Rectangle2D(x, y, width, height));
    }
}
