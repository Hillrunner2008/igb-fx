package org.lorainelab.igb.data.model.glyph;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import com.google.common.collect.ComparisonChain;
import java.awt.Rectangle;
import java.util.Comparator;
import java.util.Optional;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.lorainelab.igb.data.model.View;
import static org.lorainelab.igb.data.model.util.RectangleUtils.intersect;

/**
 *
 * @author jeckstei
 */
public interface Glyph {

    static final int SLOT_HEIGHT = 30;
    static Rectangle.Double SHARED_RECT = new Rectangle.Double(0, 0, 0, 0);

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

    default SlotAlignment getSlotAlignment() {
        return SlotAlignment.CENTER;
    }

    void draw(GraphicsContext gc, View view, Rectangle2D slotBoundingViewRect);

    // aligns glyph within slot and clips to view bounds
    default Optional<Rectangle.Double> calculateDrawRect(View view, Rectangle2D slotRect) {
        Rectangle2D viewRect = view.getBoundingRect();
        Rectangle2D boundingRect = getBoundingRect();
        double alignedMinY = getAlignedMinY(boundingRect, slotRect);
        SHARED_RECT.setRect(boundingRect.getMinX(), alignedMinY, boundingRect.getWidth(), boundingRect.getHeight());
        if (view.getMutableBoundingRect().intersects(SHARED_RECT)) {
            intersect(view.getMutableBoundingRect(), SHARED_RECT, SHARED_RECT);
            SHARED_RECT.setRect(SHARED_RECT.x - viewRect.getMinX(), SHARED_RECT.y - viewRect.getMinY(), SHARED_RECT.width, SHARED_RECT.height);
            return Optional.of(SHARED_RECT);
        }
        return Optional.empty();
    }

    default double getAlignedMinY(Rectangle2D boundingRect, Rectangle2D slotRect) {
        double y;
        switch (getSlotAlignment()) {
            case BOTTOM:
                y = SLOT_HEIGHT - boundingRect.getHeight();
                break;
            case CENTER:
                y = slotRect.getMinY() + (SLOT_HEIGHT - boundingRect.getHeight()) / 2;
                break;
            case TOP:
                y = 0;
                break;
            case CUSTOM:
                y = boundingRect.getMinY();
                break;
            default:
                y = slotRect.getMinY() + (SLOT_HEIGHT - boundingRect.getHeight()) / 2;
        }
        return y;
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
