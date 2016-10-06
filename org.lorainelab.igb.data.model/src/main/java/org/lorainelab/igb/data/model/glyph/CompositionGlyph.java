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
import org.lorainelab.igb.data.model.View;
import org.lorainelab.igb.data.model.util.FontReference;
import org.lorainelab.igb.data.model.util.FontUtils;
import static org.lorainelab.igb.data.model.util.Palette.DEFAULT_GLYPH_FILL;
import static org.lorainelab.igb.data.model.util.Palette.DEFAULT_LABEL_COLOR;
import static org.lorainelab.igb.data.model.util.Palette.SELECTION_COLOR;

/**
 *
 * @author dcnorris
 */
public class CompositionGlyph implements Glyph {

    private static final double SELECTION_RECTANGLE_WIDTH = 0.25;

    private RangeMap<Double, Glyph> xRange;
    private final Map<String, String> tooltipData;
    private static Rectangle.Double SCRATCH_RECT = new Rectangle.Double(0, 0, 0, 0);

    private boolean isNegative;
    private final String label;
    boolean isSelected = false;
    private Rectangle2D boundingRect;
    private GlyphAlignment glyphAlignment;

    public CompositionGlyph(String label, Map<String, String> tooltipData, List<Glyph> children) {
        isNegative = tooltipData.containsKey("forward") && tooltipData.get("forward").equals("false");
        glyphAlignment = isNegative ? GlyphAlignment.TOP_CENTER : GlyphAlignment.BOTTOM_CENTER;
        this.xRange = TreeRangeMap.<Double, Glyph>create();
        children.stream().forEach(child -> {
            this.xRange.put(Range.closed(child.getBoundingRect().getMinX(), child.getBoundingRect().getMaxX()), child);
            child.setGlyphAlignment(glyphAlignment);
        });
        this.label = label;
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
        Rectangle2D viewRect = view.getBoundingRect();
        final RangeMap<Double, Glyph> intersectionRangeMapX = xRange.subRangeMap(view.getXrange());
        if (!intersectionRangeMapX.asMapOfRanges().isEmpty()) {
            double minX = Double.MAX_VALUE;
            double maxX = Double.MIN_VALUE;
            for (Glyph g : intersectionRangeMapX.asMapOfRanges().values()) {
                final double glyphMinX = g.getBoundingRect().getMinX();
                final double glyphMaxX = g.getBoundingRect().getMaxX();
                minX = Math.min(minX, glyphMinX);
                maxX = Math.max(maxX, glyphMaxX);
            }
            minX = Math.max(minX, viewRect.getMinX());
            maxX = Math.min(maxX, viewRect.getMaxX());
            double width = maxX - minX;

            //translate to view
            minX = minX - viewRect.getMinX();

            double minY = slotBoundingRect.getMinY();
            double maxY = slotBoundingRect.getMaxY();
            double height = maxY - minY;

            //translate to view
            minY = minY - viewRect.getMinY();

            if (width <= 0 || height <= 0) {
                return Optional.empty();
            }

            SCRATCH_RECT.setRect(minX, minY, width, height);

            return Optional.of(SCRATCH_RECT);
        }

        return Optional.empty();
    }

    @Override
    public GlyphAlignment getGlyphAlignment() {
        return glyphAlignment;
    }

    @Override
    public void setGlyphAlignment(GlyphAlignment alignment) {
        this.glyphAlignment = alignment;
    }

    public void draw(GraphicsContext gc, View view, Rectangle2D slotBoundingViewRect, boolean isSummaryRow) {
        Optional<Rectangle.Double> glyphViewIntersectionBoundsWrapper = calculateDrawRect(view, slotBoundingViewRect);
        if (!glyphViewIntersectionBoundsWrapper.isPresent()) {
            return;
        }
        Rectangle.Double glyphViewIntersectionBounds = glyphViewIntersectionBoundsWrapper.get();
        drawChildren(gc, view, slotBoundingViewRect);
        if (!isSummaryRow) {
            drawLabel(view, gc, glyphViewIntersectionBounds);
        }
        drawSelectionRectangle(gc, view, glyphViewIntersectionBounds, slotBoundingViewRect);
    }

    @Override
    public void draw(GraphicsContext gc, View view, Rectangle2D slotBoundingViewRect) {
        Optional<Rectangle.Double> glyphViewIntersectionBoundsWrapper = calculateDrawRect(view, slotBoundingViewRect);
        if (!glyphViewIntersectionBoundsWrapper.isPresent()) {
            return;
        }
        Rectangle.Double glyphViewIntersectionBounds = glyphViewIntersectionBoundsWrapper.get();
        drawChildren(gc, view, slotBoundingViewRect);
        drawLabel(view, gc, glyphViewIntersectionBounds);
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

    public void drawSummaryRectangle(GraphicsContext gc, Rectangle.Double glyphViewIntersectionBounds, Rectangle2D slotRect) {
        gc.save();
        gc.setFill(DEFAULT_GLYPH_FILL);
        gc.setStroke(DEFAULT_GLYPH_FILL);
        final double centerOffset = (SLOT_HEIGHT - glyphViewIntersectionBounds.getHeight() / 2) / 2;
        double minY;
        switch (glyphAlignment) {
            case BOTTOM_CENTER:
                double centerPos = slotRect.getMinY() + centerOffset;
                minY = centerPos + LABEL_OFFSET;
                break;
            case TOP_CENTER:
                double centerY = slotRect.getMinY() + centerOffset;
                minY = centerY - LABEL_OFFSET;
                break;
            default:
                minY = slotRect.getMinY() + centerOffset;
        }
        gc.fillRect(glyphViewIntersectionBounds.getMinX(), minY, glyphViewIntersectionBounds.getWidth(), glyphViewIntersectionBounds.getHeight() / 2);
        gc.restore();
    }

    public void drawSummarySelectionRectangle(GraphicsContext gc, View view, Rectangle.Double drawRect, Rectangle2D slotRect) {
        gc.save();
        gc.setFill(SELECTION_COLOR);
        double xToYRatio = view.getXfactor() / view.getYfactor();
        double vMargin = drawRect.getHeight() / 10;
        double minY = (drawRect.getMinY() - view.getBoundingRect().getMinY()) + vMargin;
        double minX = drawRect.getMinX();
        double maxX = drawRect.getMaxX();
        double maxY = drawRect.getMaxY() - view.getBoundingRect().getMinY() - vMargin;
        double width = drawRect.getWidth();
        double height = maxY - minY;
        gc.fillRect(minX, minY, width, SELECTION_RECTANGLE_WIDTH); //top
        gc.fillRect(minX, minY, SELECTION_RECTANGLE_WIDTH / xToYRatio, height); //left
        gc.fillRect(maxX, minY, SELECTION_RECTANGLE_WIDTH / xToYRatio, height + SELECTION_RECTANGLE_WIDTH);//right
        gc.fillRect(minX, maxY, width, SELECTION_RECTANGLE_WIDTH);//bottom
        gc.restore();
    }

    private void drawLabel(View view, GraphicsContext gc, Rectangle.Double glyphViewIntersectionBounds) {
        Rectangle2D viewBoundingRect = view.getBoundingRect();
        final String labelString = label;
        if (!Strings.isNullOrEmpty(labelString)) {
            if (viewBoundingRect.getWidth() < 700_000) {
                gc.save();
                gc.scale(1 / view.getXfactor(), 1 / view.getYfactor());

                double textScale = .5;
                gc.scale(textScale, textScale);
                double availableLabelHeight = (LABEL_HEIGHT * view.getYfactor()) / textScale;
                availableLabelHeight *= .75;
                FontReference fontReference = FontUtils.getFontByPixelHeight(availableLabelHeight);
                gc.setFont(fontReference.getFont());
                if (availableLabelHeight > 10) {

                    double textYOffset = getTextYOffset(fontReference, availableLabelHeight);
                    double textYPosition;
                    if (isNegative) {
                        textYPosition = ((glyphViewIntersectionBounds.getMaxY() * view.getYfactor()) / textScale);
                        textYPosition -= textYOffset;
                    } else {
                        textYPosition = (((glyphViewIntersectionBounds.getMinY()) * view.getYfactor()) / textScale);
                        textYPosition += textYOffset;
                    }

                    double x = (glyphViewIntersectionBounds.getMinX() * view.getXfactor()) / textScale;
                    double width = (glyphViewIntersectionBounds.getWidth() * view.getXfactor()) / textScale;
                    gc.setFill(getFill());
                    String drawLabel = labelString;
                    double textWidth = (com.sun.javafx.tk.Toolkit.getToolkit().getFontLoader().computeStringWidth(labelString, gc.getFont()));
                    while (textWidth > width && drawLabel.length() > 3) {
                        drawLabel = drawLabel.substring(0, drawLabel.length() - 2) + "\u2026";
                        textWidth = (com.sun.javafx.tk.Toolkit.getToolkit().getFontLoader().computeStringWidth(drawLabel, gc.getFont()));
                    }
                    x = (x + (width / 2)) - (textWidth / 2);

                    gc.fillText(drawLabel, x, textYPosition, width);
                }
                gc.restore();
            }
        }
    }

    private double getTextYOffset(FontReference fontReference, double availableLabelHeight) {
        double textHeightOffset = fontReference.getAscent() - fontReference.getDescent();
        if (textHeightOffset < availableLabelHeight) {
            textHeightOffset -= ((availableLabelHeight - textHeightOffset) / 4) * 3;
        }
        return textHeightOffset;
    }

    private void drawSelectionRectangle(GraphicsContext gc, View view, Rectangle.Double glyphViewIntersectionBounds, Rectangle2D slotRect) {
        if (isSelected()) {
            Rectangle.Double drawRect = glyphViewIntersectionBounds;
//            for (Glyph g : xRange.asMapOfRanges().values()) {
//                if (g.isSelectable() && g.isSelected()) {
//                    Optional<Rectangle.Double> viewBoundingRect = g.calculateDrawRect(view, slotBoundingRect);
//                    if (viewBoundingRect.isPresent()) {
//                        drawRect = viewBoundingRect.get();
//                        break;
//                    }
//                }
//            }
            gc.save();
            gc.setFill(SELECTION_COLOR);
            double xToYRatio = view.getXfactor() / view.getYfactor();
            double minY = slotRect.getMinY() - view.getBoundingRect().getMinY();
            double minX = drawRect.getMinX();
            double maxX = drawRect.getMaxX();
            double maxY = slotRect.getMaxY() - view.getBoundingRect().getMinY() - SLOT_PADDING;
            double width = drawRect.getWidth();
            double height = maxY - minY;
            gc.fillRect(minX, minY, width, SELECTION_RECTANGLE_WIDTH); //top
            gc.fillRect(minX, minY, SELECTION_RECTANGLE_WIDTH / xToYRatio, height); //left
            gc.fillRect(maxX, minY, SELECTION_RECTANGLE_WIDTH / xToYRatio, height + SELECTION_RECTANGLE_WIDTH);//right
            gc.fillRect(minX, maxY, width, SELECTION_RECTANGLE_WIDTH);//bottom
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
