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
    static final double MIN_OFFSET = 17.5;

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

    void draw(GraphicsContext gc, View view);

    default Optional<Rectangle2D> getViewBoundingRect(View view) {
        Rectangle2D boundingRect = getBoundingRect();
        Rectangle2D viewBoundingRect = view.getBoundingRect();
        if (boundingRect.intersects(viewBoundingRect)) {
            double x = Math.max(boundingRect.getMinX(), viewBoundingRect.getMinX());
            double y = Math.max(boundingRect.getMinY(), viewBoundingRect.getMinY());
            double maxx = Math.min(boundingRect.getMaxX(), viewBoundingRect.getMaxX());
            double maxy = Math.min(boundingRect.getMaxY(), viewBoundingRect.getMaxY());
            return Optional.of(new Rectangle2D(x - viewBoundingRect.getMinX(), y, maxx - x, maxy - y));
        }
        return Optional.empty();
    }

    static Rectangle2D intersect(Rectangle2D src1, Rectangle2D src2) {
        double x = Math.max(src1.getMinX(), src2.getMinX());
        double y = Math.max(src1.getMinY(), src2.getMinY());
        double maxx = Math.min(src1.getMaxX(), src2.getMaxX());
        double maxy = Math.min(src1.getMaxY(), src2.getMaxY());
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
