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
    private Rectangle2D renderBoundingRect;

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
    public void draw(GraphicsContext gc, View view, double additionalYoffset) {
        Rectangle2D viewRect = view.getBoundingRect();
        Rectangle2D drawRect = getRenderBoundingRect();
        double x = drawRect.getMinX()+.5;
        double y = drawRect.getMinY();
        double width = drawRect.getWidth()-1;
        double height = drawRect.getHeight();
        if (x < viewRect.getMinX()) {
            int offSet = (int) (viewRect.getMinX() - x);
            width = width - offSet;
            x = 0;
        } else {
            x = x - viewRect.getMinX();
        }
        if (y < viewRect.getMinY()) {
            int offSet = (int) (viewRect.getMinY() - y);
            height = height - offSet;
            y = height + offSet;
        } else {
            y = y - viewRect.getMinY();
        }

        gc.setFill(Color.BLACK);
        gc.setStroke(Color.BLACK);
        gc.strokeLine(x, y + additionalYoffset, x + width, y + additionalYoffset);
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

    @Override
    public Rectangle2D getRenderBoundingRect() {
        if (renderBoundingRect == null) {
            return boundingRect;
        }
        return renderBoundingRect;
    }

    @Override
    public void setRenderBoundingRect(Rectangle2D renderBoundingRect) {
        this.renderBoundingRect = renderBoundingRect;
    }

}
