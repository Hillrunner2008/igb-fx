/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.data.model.glyph;

import java.util.Objects;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.lorainelab.igb.data.model.View;

/**
 *
 * @author jeckstei
 */
public class LineGlyph implements Glyph {

    int start;
    int width;
    private Rectangle2D boundingRect;
    private double y;

    public LineGlyph(int start, int width, double y) {
        this.start = start;
        this.width = width;
        this.y = y;
        boundingRect = new Rectangle2D(start, y, width, 1);
    }

    @Override
    public Color getFill() {
        return Color.BLACK;
    }

    @Override
    public Color getStrokeColor() {
        return Color.BLACK;
    }

    @Override
    public Rectangle2D getBoundingRect() {
        return boundingRect;
    }

    @Override
    public void draw(GraphicsContext gc, View view, Rectangle2D slotBoundingViewRect) {
        getViewBoundingRect(view, slotBoundingViewRect).ifPresent(drawRect -> {
            double x = drawRect.getMinX();
            double y = drawRect.getMinY();
            double width = drawRect.getWidth();
            double height = drawRect.getHeight();
            gc.save();
            gc.setFill(Color.BLACK);
            gc.setStroke(Color.BLACK);
            gc.strokeLine(x, y, x + width, y);
            gc.restore();
        });

    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + this.start;
        hash = 47 * hash + this.width;
        hash = 47 * hash + Objects.hashCode(this.boundingRect);
        hash = 47 * hash + (int) (Double.doubleToLongBits(this.y) ^ (Double.doubleToLongBits(this.y) >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LineGlyph other = (LineGlyph) obj;
        if (this.start != other.start) {
            return false;
        }
        if (this.width != other.width) {
            return false;
        }
        if (Double.doubleToLongBits(this.y) != Double.doubleToLongBits(other.y)) {
            return false;
        }
        if (!Objects.equals(this.boundingRect, other.boundingRect)) {
            return false;
        }
        return true;
    }

}
