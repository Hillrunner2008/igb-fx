/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.data.model.glyph;

import com.google.common.collect.Range;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.lorainelab.igb.data.model.Chromosome;
import org.lorainelab.igb.data.model.View;
import static org.lorainelab.igb.data.model.glyph.Glyph.MAX_GLYPH_HEIGHT;
import static org.lorainelab.igb.data.model.glyph.Glyph.SHARED_RECT;
import static org.lorainelab.igb.data.model.glyph.Glyph.SLOT_HEIGHT;
import org.lorainelab.igb.data.model.shapes.Rectangle;
import org.lorainelab.igb.data.model.util.ColorUtils;
import org.lorainelab.igb.data.model.util.DrawUtils;
import org.lorainelab.igb.data.model.util.FontReference;
import org.lorainelab.igb.data.model.util.FontUtils;
import static org.lorainelab.igb.data.model.util.Palette.DEFAULT_GLYPH_FILL;
import static org.lorainelab.igb.data.model.util.Palette.getBaseColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jeckstei
 */
public class RectangleGlyph implements Glyph {

    private static final Logger LOG = LoggerFactory.getLogger(RectangleGlyph.class);
    public static final double THICK_RECTANGLE_HEIGHT = MAX_GLYPH_HEIGHT;
    public static final double DEFAULT_RECTANGLE_HEIGHT = THICK_RECTANGLE_HEIGHT * .75;

    private Color fill = DEFAULT_GLYPH_FILL.get();
    private Color strokeColor = DEFAULT_GLYPH_FILL.get();

    private final Rectangle2D boundingRect;
    private final Optional<Function<String, String>> innerTextRefSeqTranslator;
    private final Optional<Range<Integer>> innerTextReferenceSequenceRange;
    boolean colorByBase;
    private final boolean isSelectable;
    private boolean isSelected;
    private boolean maskBasePairMatches;
    private GlyphAlignment glyphAlignment;

    public RectangleGlyph(Rectangle rectShape) {
        double height = DEFAULT_RECTANGLE_HEIGHT;
        if (rectShape.getAttributes().contains(org.lorainelab.igb.data.model.shapes.Rectangle.Attribute.THICK)) {
            height = THICK_RECTANGLE_HEIGHT;
        } else if (rectShape.getAttributes().contains(org.lorainelab.igb.data.model.shapes.Rectangle.Attribute.INSERTION)) {
            height = SLOT_HEIGHT / 10;
        }
        boundingRect = new Rectangle2D(rectShape.getOffset(), 0, rectShape.getWidth(), height);
        innerTextRefSeqTranslator = rectShape.getInnerTextRefSeqTranslator();
        innerTextReferenceSequenceRange = rectShape.getInnerTextReferenceSequenceRange();
        colorByBase = rectShape.isMirrorReferenceSequence();
        isSelectable = rectShape.isSelectable();
        isSelected = false;
        maskBasePairMatches = rectShape.isMaskBasePairMatches();
        glyphAlignment = GlyphAlignment.BOTTOM;
    }

    @Override
    public Color getFill() {
        return fill;
    }

    @Override
    public Color getStrokeColor() {
        return strokeColor;
    }

    @Override
    public boolean isSelectable() {
        return isSelectable;
    }

    @Override
    public boolean isSelected() {
        return isSelected;
    }

    @Override
    public void setIsSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public void setFill(Color fill) {
        this.fill = fill;
    }

    public void setStrokeColor(Color strokeColor) {
        this.strokeColor = strokeColor;
    }

    @Override
    public Rectangle2D getBoundingRect() {
        return boundingRect;
    }

    @Override
    public void draw(GraphicsContext gc, View view, double slotMinY) {
        calculateDrawRect(view, slotMinY).ifPresent(sharedRect -> {
            Rectangle2D viewRect = view.modelCoordRect();
            gc.setFill(fill);
            gc.setStroke(strokeColor);
            final double y = sharedRect.getMinY();
            DrawUtils.scaleToVisibleRec(view, SHARED_RECT);

            if (view.modelCoordRect().getWidth() < 250) {
                gc.fillRect(SHARED_RECT.x, SHARED_RECT.y, SHARED_RECT.width, SHARED_RECT.height);
                drawText(view, viewRect, gc, sharedRect);
            } else {
                gc.fillRect(SHARED_RECT.x, SHARED_RECT.y, SHARED_RECT.width, SHARED_RECT.height);
            }
        });
    }

    private void drawText(View view, Rectangle2D viewRect, GraphicsContext gc, java.awt.Rectangle.Double sharedRect) {
        try {
            gc.save();
            gc.scale(1, 1 / view.getYfactor());
            innerTextRefSeqTranslator.ifPresent(translationFunction -> {
                Chromosome chromosome = view.getChromosome();
                String innerText;
                double startOffset = 0;
                double endOffset = 0;
                if (boundingRect.getMinX() < viewRect.getMinX()) {
                    //left side is cut off
                    startOffset = viewRect.getMinX() - boundingRect.getMinX();
                }
                if ((int) boundingRect.getMaxX() > viewRect.getMaxX()) {
                    //right side is cut off
                    endOffset = boundingRect.getMaxX() - viewRect.getMaxX();
                }
                int startPos = (int) startOffset;
                int endPos = (int) (startPos + boundingRect.getWidth() - (int) startOffset - endOffset);
                Range<Integer> basePairRange = Range.closed((int) boundingRect.getMinX() + (int) startOffset, (int) boundingRect.getMaxX() - (int) endOffset);
                String sequence = new String(chromosome.getSequence(basePairRange.lowerEndpoint(), basePairRange.upperEndpoint() - basePairRange.lowerEndpoint()));
                if (innerTextReferenceSequenceRange.isPresent()) {
                    //TODO handle offsets
                    String requestedSequence = new String(chromosome.getSequence(innerTextReferenceSequenceRange.get().lowerEndpoint(),
                            innerTextReferenceSequenceRange.get().upperEndpoint() - innerTextReferenceSequenceRange.get().lowerEndpoint()));
                    innerText = translationFunction.apply(requestedSequence);
                } else {
                    innerText = translationFunction.apply(sequence);
                }
                if (innerText.length() > sequence.length()) {
                    innerText = innerText.substring(startPos, endPos);
                }
                double modelHeight = view.getCanvasContext().getTrackHeight() / view.getYfactor();
                double yCoordsPerPixel = view.getCanvasContext().getBoundingRect().getHeight() / modelHeight;
                double availableLabelHeight = Math.ceil((boundingRect.getHeight() * yCoordsPerPixel) * view.getYfactor());
                final boolean textVisible = availableLabelHeight >= 12;
                //cap to preserve aspect ratio

                FontReference fontReference = FontUtils.BASE_PAIR_FONT;
                gc.setFont(fontReference.getFont());

                double charWidth = fontReference.getCharWidth() / view.getXfactor();
                double minY = sharedRect.getMinY();
                double height = sharedRect.getHeight();
                final double textHeight = fontReference.getAscent();
                double center = minY + sharedRect.getHeight() / 2;
                double scaledTextminY = (center * view.getYfactor()) - (textHeight / 2);
                scaledTextminY += textHeight - 1;
                double i = 0;
                double minX = sharedRect.getMinX();

                int baseWidth = 1;
                final char[] innerTextChars = innerText.toUpperCase().toCharArray();
                final char[] seqChars = sequence.toUpperCase().toCharArray();
                for (int j = 0; j < innerTextChars.length; j++) {
                    boolean charMatch = false;
                    char c = innerTextChars[j];
                    if (colorByBase || maskBasePairMatches) {
                        if (innerTextChars.length == seqChars.length && c == seqChars[j]) {
                            charMatch = true;
                            gc.setFill(fill);
                        } else {
                            gc.setFill(getBaseColor(c));
                        }
                    } else {
                        gc.setFill(fill);
                    }
                    if (!gc.getFill().equals(fill)) {
                        if (startOffset % 1 > 0 && i == 0) {
                            gc.fillRect(minX, sharedRect.getMinY() * view.getYfactor(), 1 - startOffset % 1, sharedRect.getHeight() * view.getYfactor());
                            i += (1 - startOffset % 1);
                            continue;
                        } else {
                            gc.fillRect(minX + i, sharedRect.getMinY() * view.getYfactor(), 1, sharedRect.getHeight() * view.getYfactor());
                        }
                    }
                    if (!charMatch && textVisible) {
                        if (colorByBase || maskBasePairMatches) {
                            gc.setFill(Color.BLACK);
                        } else {
                            gc.setFill(ColorUtils.getEffectiveContrastColor(fill));
                        }
                        double x = minX + i + (charWidth / 2);
                        double maxWidth = .8;
                        gc.fillText("" + c, x, scaledTextminY, .5);

                    }
                    i++;
                }
            });
            gc.scale(1, view.getYfactor());
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
        } finally {
            gc.restore();
        }

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

    @Override
    public GlyphAlignment getGlyphAlignment() {
        return glyphAlignment;
    }

    @Override
    public void setGlyphAlignment(GlyphAlignment alignment) {
        this.glyphAlignment = alignment;
    }

}
