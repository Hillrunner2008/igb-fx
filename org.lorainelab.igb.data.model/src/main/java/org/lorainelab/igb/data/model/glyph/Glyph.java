package org.lorainelab.igb.data.model.glyph;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import com.google.common.collect.ComparisonChain;
import java.util.Comparator;
import java.util.Optional;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.lorainelab.igb.data.model.View;

/**
 *
 * @author jeckstei
 */
public interface Glyph {

    static final int SLOT_HEIGHT = 30;
    static final double MIN_Y_OFFSET = 17.5;
    static final double MAX_SHAPE_HEIGHT = 15;

    Color getFill();

    Color getStrokeColor();

    Rectangle2D getBoundingRect();

    default boolean isSelectable() {
        return false;
    }

    default boolean isSelected() {
        return false;
    }

    default void setIsSelected(boolean isSelected) {
        //do nothing
    }

    void draw(GraphicsContext gc, View view, Rectangle2D slotBoundingViewRect);

    default Optional<Rectangle2D> getViewBoundingRect(View view, Rectangle2D slotBoundingViewRect) {
        double cutOff = SLOT_HEIGHT - slotBoundingViewRect.getHeight();
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
                y -= 17.5;
            } else {
                y -= cutOff;
            }
        }
        if (width <= 0 || height <= 0) {
            return Optional.empty();
        }
        return Optional.of(new Rectangle2D(x, y, width, height));
    }

    public static Rectangle2D intersect(Rectangle2D src1, Rectangle2D src2) {
        double x = Math.max(src1.getMinX(), src2.getMinX());
        double y = Math.max(src1.getMinY(), src2.getMinY());
        double maxx = Math.min(src1.getMaxX(), src2.getMaxX());
        double maxy = Math.min(src1.getMaxY(), src2.getMaxY());
        if (maxx - x <= 0 || maxy - y <= 0) {
            return null;
        }
        return new Rectangle2D(x, y, maxx - x, maxy - y);
    }
    static Comparator<CompositionGlyph> MIN_X_COMPARATOR
            = (glyph1, glyph2) -> {
                return ComparisonChain.start()
                        .compare(glyph1.getBoundingRect().getMinX(), glyph2.getBoundingRect().getMinX())
                        .compare(glyph1.getBoundingRect().getWidth(), glyph2.getBoundingRect().getWidth())
                        .compare(glyph1.getLabel(), glyph2.getLabel())
                        .result();
            };
}
