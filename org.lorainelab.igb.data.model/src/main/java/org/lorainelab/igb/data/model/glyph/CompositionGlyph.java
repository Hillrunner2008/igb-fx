package org.lorainelab.igb.data.model.glyph;

import com.google.common.base.Strings;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;
import java.awt.Rectangle;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.lorainelab.igb.data.model.View;
import static org.lorainelab.igb.data.model.util.Palette.DEFAULT_GLYPH_FILL;
import static org.lorainelab.igb.data.model.util.Palette.DEFAULT_LABEL_COLOR;

/**
 *
 * @author dcnorris
 */
public class CompositionGlyph implements Glyph {

    private RangeMap<Double, Glyph> xRange;
    private final Map<String, String> tooltipData;
    private static Rectangle.Double SCRATCH_RECT = new Rectangle.Double(0, 0, 0, 0);

    private boolean isNegative;
    private final String label;
    boolean isSelected = false;
    private Rectangle2D boundingRect;

    public CompositionGlyph(String label, Map<String, String> tooltipData, List<Glyph> children) {
        this.xRange = TreeRangeMap.<Double, Glyph>create();
        children.stream().forEach(child -> {
            this.xRange.put(Range.closed(child.getBoundingRect().getMinX(), child.getBoundingRect().getMaxX()), child);
        });
        this.label = label;
        isNegative = tooltipData.containsKey("forward") && tooltipData.get("forward").equals("false");
        this.tooltipData = tooltipData;
    }

    public boolean isNegative() {
        return isNegative;
    }

    public Map<String, String> getTooltipData() {
        return tooltipData;
    }

    @Override
    public Color getFill() {
        return DEFAULT_LABEL_COLOR;
    }

    @Override
    public Color getStrokeColor() {
        return DEFAULT_LABEL_COLOR;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public Rectangle2D getBoundingRect() {
        if (boundingRect == null) {
            boundingRect = calculateBoundingRect();
        }
        return boundingRect;
    }

    private Rectangle2D calculateBoundingRect() {
        double minX = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE;
        double maxY = Double.MIN_VALUE;
        for (Glyph g : xRange.asMapOfRanges().values()) {
            Rectangle2D rect = g.getBoundingRect();
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
    public Optional<Rectangle.Double> calculateDrawRect(View view, Rectangle2D slotBoundingRect) {
        final RangeMap<Double, Glyph> intersectionRangeMapX = xRange.subRangeMap(view.getXrange());
        if (!intersectionRangeMapX.asMapOfRanges().isEmpty()) {
            double minX = Double.MAX_VALUE;
            double maxX = Double.MIN_VALUE;
            double minY = Double.MAX_VALUE;
            double maxY = Double.MIN_VALUE;
            double glyphMinY = Double.MIN_VALUE;
            double maxGlyphheight = Double.MIN_VALUE;
            for (Glyph g : intersectionRangeMapX.asMapOfRanges().values()) {
                Optional<Rectangle.Double> rect = g.calculateDrawRect(view, slotBoundingRect);
                if (rect.isPresent()) {
                    minX = Math.min(minX, rect.get().getMinX());
                    maxX = Math.max(maxX, rect.get().getMaxX());
                    maxY = Math.max(maxY, rect.get().getMaxY());
                    minY = Math.min(minY, rect.get().getMinY());
                }
                //even if part of composition glyph is out of view, we will consider its minY and height
//                double y = boundingRect.getMinY();
//                double height = boundingRect.getHeight();
//                if (y < view.getBoundingRect().getMinY()) {
//                    double offSet = (view.getBoundingRect().getMinY() - y);
//                    height = height - offSet;
//                    y = 0;
//                } else {
//                    y = y - view.getBoundingRect().getMinY();
//                }
//                glyphMinY = Math.max(glyphMinY, y);
//                maxGlyphheight = Math.max(maxGlyphheight, height);

            }
            double width = maxX - minX;
            double height = maxY - minY;
            if (width <= 0 || height <= 0) {
                return Optional.empty();
            }
            double y;
            if (!isNegative) {
                y = (minY - height);
            } else {
                y = (minY);
            }
//            if (isPositiveStrand()) {
//                y = (glyphMinY - maxGlyphheight);
//            } else {
//                y = (glyphMinY);
//            }
            SCRATCH_RECT.setRect(minX, y, width, height * 2);
            return Optional.of(SCRATCH_RECT);
        }

        return Optional.empty();
    }

    public void draw(GraphicsContext gc, View view, Rectangle2D slotBoundingViewRect, boolean isSummaryRow) {
        Rectangle2D viewBoundingRect = view.getBoundingRect();
        Optional<Rectangle.Double> glyphViewIntersectionBoundsWrapper = calculateDrawRect(view, slotBoundingViewRect);
        if (!glyphViewIntersectionBoundsWrapper.isPresent()) {
            return;
        }
        Rectangle.Double glyphViewIntersectionBounds = glyphViewIntersectionBoundsWrapper.get();
        drawChildren(gc, view, slotBoundingViewRect);
        if (!isSummaryRow) {
            drawLabel(view, viewBoundingRect, gc, glyphViewIntersectionBounds);
        }
        drawSelectionRectangle(gc, view, glyphViewIntersectionBounds, slotBoundingViewRect);
    }

    @Override
    public void draw(GraphicsContext gc, View view, Rectangle2D slotBoundingViewRect) {
        Rectangle2D viewBoundingRect = view.getBoundingRect();
        Optional<Rectangle.Double> glyphViewIntersectionBoundsWrapper = calculateDrawRect(view, slotBoundingViewRect);
        if (!glyphViewIntersectionBoundsWrapper.isPresent()) {
            return;
        }
        Rectangle.Double glyphViewIntersectionBounds = glyphViewIntersectionBoundsWrapper.get();
        drawChildren(gc, view, slotBoundingViewRect);
        drawLabel(view, viewBoundingRect, gc, glyphViewIntersectionBounds);
        drawSelectionRectangle(gc, view, glyphViewIntersectionBounds, slotBoundingViewRect);
    }

    private void drawChildren(GraphicsContext gc, View view, Rectangle2D slotBoundingViewRect) {
        final RangeMap<Double, Glyph> intersectionRangeMapX = xRange.subRangeMap(view.getXrange());
        if (!intersectionRangeMapX.asMapOfRanges().isEmpty()) {
            final Collection<Glyph> childrenInView = intersectionRangeMapX.asMapOfRanges().values();
            //TODO implement z index based sorting
            childrenInView.stream().filter(glyph -> glyph instanceof LineGlyph).forEach(glyph -> glyph.draw(gc, view, slotBoundingViewRect));
            childrenInView.stream()
                    .filter(glyph -> glyph instanceof RectangleGlyph)
                    .map(glyph -> RectangleGlyph.class.cast(glyph))
                    .filter(rect -> rect.getBoundingRect().getHeight() == 10)
                    .forEach(glyph -> glyph.draw(gc, view, slotBoundingViewRect));
            childrenInView.stream()
                    .filter(glyph -> glyph instanceof RectangleGlyph)
                    .map(glyph -> RectangleGlyph.class.cast(glyph))
                    .filter(rect -> rect.getBoundingRect().getHeight() != 10)
                    .forEach(glyph -> glyph.draw(gc, view, slotBoundingViewRect));
            childrenInView.stream().filter(glyph -> glyph instanceof GraphGlyph).forEach(glyph -> glyph.draw(gc, view, slotBoundingViewRect));
        }
    }

    public void drawSummaryRectangle(GraphicsContext gc, Rectangle.Double glyphViewIntersectionBounds) {
        gc.save();
        gc.setFill(DEFAULT_GLYPH_FILL);
        gc.setStroke(DEFAULT_GLYPH_FILL);
        if (!isNegative) {
            gc.fillRect(glyphViewIntersectionBounds.getMinX(), glyphViewIntersectionBounds.getMinY() + (glyphViewIntersectionBounds.getHeight() / 2), glyphViewIntersectionBounds.getWidth(), glyphViewIntersectionBounds.getHeight() / 2);
        } else {
            gc.fillRect(glyphViewIntersectionBounds.getMinX(), glyphViewIntersectionBounds.getMinY(), glyphViewIntersectionBounds.getWidth(), glyphViewIntersectionBounds.getHeight() / 2);

        }
        gc.restore();
    }

    private void drawLabel(View view, Rectangle2D viewBoundingRect, GraphicsContext gc, Rectangle.Double glyphViewIntersectionBounds) {
        final String labelString = label;
        if (!Strings.isNullOrEmpty(labelString)) {
            final double fontSize = Math.min((glyphViewIntersectionBounds.getHeight() * view.getYfactor()) * .35, 10);
            if (viewBoundingRect.getWidth() < 100_000 && fontSize > 2) {
                gc.save();
                gc.scale(1 / view.getXfactor(), 1 / view.getYfactor());
                double textScale = .8;
                gc.scale(textScale, textScale);
                double x = (glyphViewIntersectionBounds.getMinX() * view.getXfactor()) / textScale;
                double y = (glyphViewIntersectionBounds.getMinY() * view.getYfactor()) / textScale;
                double height = (glyphViewIntersectionBounds.getHeight() * view.getYfactor()) / textScale;
                double width = (glyphViewIntersectionBounds.getWidth() * view.getXfactor()) / textScale;
                gc.setFont(Font.font("Monospaced", FontWeight.NORMAL, fontSize));
                gc.setFill(getFill());
                double textHeight = ((com.sun.javafx.tk.Toolkit.getToolkit().getFontLoader().getFontMetrics(gc.getFont()).getLineHeight()));
                String drawLabel = labelString;
                double textWidth = (com.sun.javafx.tk.Toolkit.getToolkit().getFontLoader().computeStringWidth(labelString, gc.getFont()));
                while (textWidth > width && drawLabel.length() > 3) {
                    drawLabel = drawLabel.substring(0, drawLabel.length() - 2) + "\u2026";
                    textWidth = (com.sun.javafx.tk.Toolkit.getToolkit().getFontLoader().computeStringWidth(drawLabel, gc.getFont()));
                }
                x = (x + (width / 2)) - (textWidth / 2);
                if (!isNegative) {
                    y = (y + (textHeight / 2)) + (height / 5);
                } else {
                    y = (y + (textHeight / 2)) + (height / 5) * 4;
                }
                gc.fillText(drawLabel, x, y);
                gc.restore();
            }
        }
    }

    private void drawSelectionRectangle(GraphicsContext gc, View view, Rectangle.Double glyphViewIntersectionBounds, Rectangle2D slotBoundingRect) {
        if (isSelected()) {
            Rectangle.Double drawRect = glyphViewIntersectionBounds;
            for (Glyph g : xRange.asMapOfRanges().values()) {
                if (g.isSelectable() && g.isSelected()) {
                    Optional<Rectangle.Double> viewBoundingRect = g.calculateDrawRect(view, slotBoundingRect);
                    if (viewBoundingRect.isPresent()) {
                        drawRect = viewBoundingRect.get();
                        break;
                    }
                }
            }
            gc.save();
            gc.setFill(Color.RED);
            double rectWidth = 0.25;
            double xToYRatio = view.getXfactor() / view.getYfactor();
            double minY = slotBoundingRect.getMinY();
            double minX = drawRect.getMinX();
            double maxX = drawRect.getMaxX();
            double maxY = slotBoundingRect.getMaxY();
            double width = drawRect.getWidth();
            double height = drawRect.getHeight();
            gc.fillRect(minX, minY, width, rectWidth); //top
            gc.fillRect(minX, minY, rectWidth / xToYRatio, height); //left
            gc.fillRect(maxX, minY, rectWidth / xToYRatio, height + rectWidth);//right
            gc.fillRect(minX, maxY, width, rectWidth);//bottom
            gc.restore();
        }
    }

    public void drawSummarySelectionRectangle(GraphicsContext gc, View view, Rectangle.Double drawRect) {
        gc.save();
        gc.setFill(Color.RED);
        double rectWidth = 0.25;
        double xToYRatio = view.getXfactor() / view.getYfactor();
        double minY = drawRect.getMinY();
        double minX = drawRect.getMinX();
        double maxX = drawRect.getMaxX();
        double maxY = drawRect.getMaxY();
        double width = drawRect.getWidth();
        double height = drawRect.getHeight();
        gc.fillRect(minX, minY, width, rectWidth); //top
        gc.fillRect(minX, minY, rectWidth / xToYRatio, height); //left
        gc.fillRect(maxX, minY, rectWidth / xToYRatio, height + rectWidth);//right
        gc.fillRect(minX, maxY, width, rectWidth);//bottom
        gc.restore();
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

    public Collection<Glyph> getChildren() {
        return xRange.asMapOfRanges().values();
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

}
