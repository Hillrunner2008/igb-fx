package org.lorainelab.igb.data.model.glyph;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.util.Optional;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Paint;
import org.lorainelab.igb.data.model.View;

/**
 *
 * @author jeckstei
 */
public interface Glyph {

    final int SLOT_HEIGHT = 30;
    final double MODEL_HEIGHT_PADDING = 17.5;

    Paint getFill();

    Paint getStrokeColor();

    Rectangle2D getBoundingRect();

    void setBoundingRect(Rectangle2D boundingRect);

    default boolean isSelectable() {
        return false;
    }

    default boolean isSelected() {
        return false;
    }

    default void setIsSelected(boolean isSelected) {
        //do nothing
    }

    void draw(GraphicsContext gc, View view, double additionalYoffset);

    default Optional<Rectangle2D> getViewBoundingRect(Rectangle2D view, double additionalYoffset) {
        Rectangle2D boundingRect = getBoundingRect();
        double x = boundingRect.getMinX();
        double maxX = boundingRect.getMaxX();
        double y = boundingRect.getMinY();
        double width = boundingRect.getWidth();
        double height = boundingRect.getHeight();
        if (x < view.getMinX()) {
            int offSet = (int) (view.getMinX() - x);
            width = width - offSet;
            x = 0;
        } else {
            x = x - view.getMinX();
        }
        if (maxX > view.getMaxX()) {
            int offSet = (int) (maxX - view.getMaxX());
            width = width - offSet;
        }
        if (y < view.getMinY()) {
            double offSet = (view.getMinY() - y);
            height = height - offSet;
            y = 0;
        } else {
            y = y - view.getMinY();
        }
        if (width <= 0 || height <= 0) {
            return Optional.empty();
        }
        return Optional.of(new Rectangle2D(x, y + additionalYoffset, width, height));
    }
}