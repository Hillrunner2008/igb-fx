/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.data.model.glyph;

import java.util.Objects;
import java.util.Optional;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import org.lorainelab.igb.data.model.View;

/**
 *
 * @author jeckstei
 */
public class RectangleGlyph implements Glyph {

    private Paint fill = Color.BLACK;

    private Paint strokeColor = Color.BLACK;

    private Rectangle2D boundingRect;

    public RectangleGlyph(double x, double y, int width, int height) {
        boundingRect = new Rectangle2D(x, y, width, height);
    }

    @Override
    public Paint getFill() {
        return fill;
    }

    @Override
    public Paint getStrokeColor() {
        return strokeColor;
    }

    public void setFill(Paint fill) {
        this.fill = fill;
    }

    public void setStrokeColor(Paint strokeColor) {
        this.strokeColor = strokeColor;
    }

    @Override
    public Rectangle2D getBoundingRect() {
        return boundingRect;
    }

    @Override
    public void draw(GraphicsContext gc, View view, double additionalYoffset) {
        Rectangle2D viewRect = view.getBoundingRect();
        //System.out.println("++++++++++miny: " + viewRect.getMinY() + " maxy: " + viewRect.getMaxY());
        Optional<Rectangle2D> viewBoundingRect = getViewBoundingRect(viewRect, additionalYoffset);
        if (viewBoundingRect.isPresent()) {
            gc.setFill(fill);
            gc.setStroke(strokeColor);
            final double y = viewBoundingRect.get().getMinY();
            //System.out.println("---------miny: " + viewBoundingRect.getMinY() + " height: " + height + " offset: " + additionalYoffset);
            gc.fillRect(viewBoundingRect.get().getMinX(), y, viewBoundingRect.get().getWidth(), viewBoundingRect.get().getHeight());
        }
    }

    @Override
    public void setBoundingRect(Rectangle2D boundingRect) {
        this.boundingRect = boundingRect;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.fill);
        hash = 29 * hash + Objects.hashCode(this.strokeColor);
        hash = 29 * hash + Objects.hashCode(this.boundingRect);
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
        final RectangleGlyph other = (RectangleGlyph) obj;
        if (!Objects.equals(this.fill, other.fill)) {
            return false;
        }
        if (!Objects.equals(this.strokeColor, other.strokeColor)) {
            return false;
        }
        if (!Objects.equals(this.boundingRect, other.boundingRect)) {
            return false;
        }
        return true;
    }

}
