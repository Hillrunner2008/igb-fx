package org.lorainelab.igb.data.model.glyph;

import com.google.common.base.Strings;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.lorainelab.igb.data.model.View;

/**
 *
 * @author dcnorris
 */
public class CompositionGlyph implements Glyph {

    private final List<Glyph> children;
    private final Map<String, String> tooltipData;

    private final String label;
    boolean isSelected = false;

    public CompositionGlyph(String label, Map<String, String> tooltipData, List<Glyph> children) {
        this.children = children;
        this.label = label;
        this.tooltipData = tooltipData;
    }

    public Map<String, String> getTooltipData() {
        return tooltipData;
    }

    @Override
    public Paint getFill() {
        return Color.BLACK;
    }

    @Override
    public Paint getStrokeColor() {
        return Color.RED;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public Rectangle2D getBoundingRect() {
        double minX = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE;
        double maxY = Double.MIN_VALUE;
        for (Glyph g : children) {
            Rectangle2D rect = g.getRenderBoundingRect();
            minX = Math.min(minX, rect.getMinX());
            maxX = Math.max(maxX, rect.getMaxX());
            minY = Math.min(minY, rect.getMinY());
            maxY = Math.max(maxY, rect.getMaxY());
        }
        double width = maxX - minX;
        double height = maxY - minY;
        return new Rectangle2D(minX, minY, width, height);
    }

    @Override
    public Optional<Rectangle2D> getViewBoundingRect(Rectangle2D view, double additionalYoffset) {
        double minX = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE;
        double maxY = Double.MIN_VALUE;
        double glyphMinY = Double.MIN_VALUE;
        double maxGlyphheight = Double.MIN_VALUE;
        for (Glyph g : children) {
            if (g.getRenderBoundingRect().intersects(view)) {
                Optional<Rectangle2D> rect = g.getViewBoundingRect(view, additionalYoffset);
                if (rect.isPresent()) {
                    minX = Math.min(minX, rect.get().getMinX());
                    maxX = Math.max(maxX, rect.get().getMaxX());
                    maxY = Math.max(maxY, rect.get().getMaxY());
                    minY = Math.min(minY, rect.get().getMinY());
                }
            }
            //even if part of composition glyph is out of view, we will consider its minY and height
            Rectangle2D boundingRect = getRenderBoundingRect();
            double y = boundingRect.getMinY();
            double height = boundingRect.getHeight();
            if (y < view.getMinY()) {
                double offSet = (view.getMinY() - y);
                height = height - offSet;
                y = 0;
            } else {
                y = y - view.getMinY();
            }
            glyphMinY = Math.max(glyphMinY, y);
            maxGlyphheight = Math.max(maxGlyphheight, height);

        }
        double width = maxX - minX;
        double height = maxY - minY;
        if (width <= 0 || height <= 0) {
            return Optional.empty();
        }
        double y = (glyphMinY - maxGlyphheight) + additionalYoffset;
        return Optional.of(new Rectangle2D(minX, y, width, maxGlyphheight * LABEL_SPACE_SCALER));
    }
    private static final double LABEL_SPACE_SCALER = 2;

    @Override
    public void draw(GraphicsContext gc, View view, double additionalYoffset) {
        Rectangle2D viewBoundingRect = view.getBoundingRect();
        Optional<Rectangle2D> glyphViewIntersectionBoundsWrapper = getViewBoundingRect(viewBoundingRect, additionalYoffset);
        if (!glyphViewIntersectionBoundsWrapper.isPresent()) {
            return;
        }
        Rectangle2D glyphViewIntersectionBounds = glyphViewIntersectionBoundsWrapper.get();
        if (viewBoundingRect.getWidth() < 25_000_000) {
            children.stream()
                    .filter(glyph -> viewBoundingRect.intersects(glyph.getRenderBoundingRect()))
                    .forEach(child -> child.draw(gc, view, additionalYoffset));
        } else {
            gc.save();
            gc.setFill(Color.web("#3F51B5"));
            gc.setStroke(Color.web("#3F51B5"));
            gc.fillRect(glyphViewIntersectionBounds.getMinX(), glyphViewIntersectionBounds.getMinY() + (glyphViewIntersectionBounds.getHeight() / 2), glyphViewIntersectionBounds.getWidth(), glyphViewIntersectionBounds.getHeight() / 2);
            gc.restore();
        }
        if (!Strings.isNullOrEmpty(label)) {
            final double fontSize = Math.min((SLOT_HEIGHT * view.getYfactor()) * .35, 14.5);
            if (viewBoundingRect.getWidth() < 300_000 && fontSize > 8) {
                gc.save();
                gc.scale(1 / view.getXfactor(), 1 / view.getYfactor());
                double textScale = .8;
                gc.scale(textScale, textScale);
                double x = (glyphViewIntersectionBounds.getMinX() * view.getXfactor()) / textScale;
                double y = (glyphViewIntersectionBounds.getMinY() * view.getYfactor()) / textScale;
                double height = (glyphViewIntersectionBounds.getHeight() * view.getYfactor()) / textScale;
                double width = (glyphViewIntersectionBounds.getWidth() * view.getXfactor()) / textScale;
                gc.setFont(Font.font("Monospaced", FontWeight.NORMAL, fontSize));
                gc.setFill(Color.BLACK);
                double textHeight = ((com.sun.javafx.tk.Toolkit.getToolkit().getFontLoader().getFontMetrics(gc.getFont()).getLineHeight()));
                String drawLabel = label;
                double textWidth = (com.sun.javafx.tk.Toolkit.getToolkit().getFontLoader().computeStringWidth(label, gc.getFont()));
                while (textWidth > width && drawLabel.length() > 3) {
                    drawLabel = drawLabel.substring(0, drawLabel.length() - 2) + "\u2026";
                    textWidth = (com.sun.javafx.tk.Toolkit.getToolkit().getFontLoader().computeStringWidth(drawLabel, gc.getFont()));
                }
                x = (x + (width / 2)) - (textWidth / 2);
                y = (y + (textHeight / 2)) + (height / 4);
                gc.fillText(drawLabel, x, y);
                gc.restore();
            }
        }
        if (isSelected()) {
            gc.save();
            gc.setFill(Color.RED);

            double xToYRatio = view.getXfactor() / view.getYfactor();
            double rectWidth = 1;
            double width = view.getBoundingRect().getWidth();
            double height = view.getBoundingRect().getHeight();
            if (width > 1500 && height > 150) {
                rectWidth = 2;
            }
            double minY = glyphViewIntersectionBounds.getMinY();
            double minX = glyphViewIntersectionBounds.getMinX();
            double maxX = glyphViewIntersectionBounds.getMaxX();
            double maxY = glyphViewIntersectionBounds.getMaxY();
            gc.fillRect(minX, minY, glyphViewIntersectionBounds.getWidth(), rectWidth); //top
            gc.fillRect(minX, minY, rectWidth / xToYRatio, glyphViewIntersectionBounds.getHeight()); //left
            gc.fillRect(maxX, minY, rectWidth / xToYRatio, glyphViewIntersectionBounds.getHeight() + 1);//right
            gc.fillRect(minX, maxY, glyphViewIntersectionBounds.getWidth(), rectWidth);//bottom
            gc.restore();
        }
    }

    @Override
    public boolean isSelectable() {
        return true;
    }

    @Override
    public void setIsSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    @Override
    public boolean isSelected() {
        return isSelected;
    }

    public List<Glyph> getChildren() {
        return children;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.tooltipData);
        hash = 67 * hash + Objects.hashCode(this.label);
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
        final CompositionGlyph other = (CompositionGlyph) obj;
        if (!Objects.equals(this.label, other.label)) {
            return false;
        }
        if (!Objects.equals(this.tooltipData, other.tooltipData)) {
            return false;
        }
        return true;
    }

    @Override
    public Rectangle2D getRenderBoundingRect() {
        return getBoundingRect();
    }

    @Override
    public void setRenderBoundingRect(Rectangle2D rectangle2D) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
