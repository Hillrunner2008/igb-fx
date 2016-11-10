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
import static org.lorainelab.igb.data.model.util.Palette.DEFAULT_LINE_FILL;

/**
 *
 * @author jeckstei
 */
public class LineGlyph implements Glyph {

    int start;
    int width;
    private Rectangle2D boundingRect;
    private GlyphAlignment glyphAlignment;

    public LineGlyph(int start, int width) {
        this.start = start;
        this.width = width;
        boundingRect = new Rectangle2D(start, 0, width, 1);
        glyphAlignment = glyphAlignment.BOTTOM;
    }

    @Override
    public Color getFill() {
        return DEFAULT_LINE_FILL.get();
    }

    @Override
    public Color getStrokeColor() {
        return DEFAULT_LINE_FILL.get();
    }

    @Override
    public Rectangle2D getBoundingRect() {
        return boundingRect;
    }

    @Override
    public void draw(GraphicsContext gc, View view, double slotMinY) {
        calculateDrawRect(view, slotMinY).ifPresent(drawRect -> {
            double x = drawRect.getMinX();
            double y = drawRect.getMinY();
            double width = drawRect.getWidth();
            double height = drawRect.getHeight();
            gc.save();
            gc.setFill(getFill());
            gc.setStroke(getStrokeColor());
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
        if (!Objects.equals(this.boundingRect, other.boundingRect)) {
            return false;
        }
        return true;
    }

    @Override
    public GlyphAlignment getGlyphAlignment() {
        return glyphAlignment;
    }

    @Override
    public void setGlyphAlignment(GlyphAlignment alignment) {
        this.glyphAlignment = alignment;
    }

}
