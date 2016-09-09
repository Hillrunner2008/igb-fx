/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.data.model.glyph;

import com.google.common.collect.Range;
import com.sun.javafx.tk.FontMetrics;
import com.sun.javafx.tk.Toolkit;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.lorainelab.igb.data.model.Chromosome;
import org.lorainelab.igb.data.model.View;
import static org.lorainelab.igb.data.model.sequence.BasePairColorReference.getBaseColor;
import org.lorainelab.igb.data.model.shapes.Rectangle;
import org.lorainelab.igb.data.model.util.ColorUtils;
import org.lorainelab.igb.data.model.util.DrawUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jeckstei
 */
public class RectangleGlyph implements Glyph {

    private static final Logger LOG = LoggerFactory.getLogger(RectangleGlyph.class);
    public static final int THICK_RECTANGLE_HEIGHT = 15;
    private Color fill = Color.WHITE;

    private Color strokeColor = Color.BLACK;

    private final Rectangle2D boundingRect;
    private final Optional<Function<String, String>> innerTextRefSeqTranslator;
    private final Optional<Range<Integer>> innerTextReferenceSequenceRange;
    boolean colorByBase;
    private final boolean isSelectable;
    private boolean isSelected;
    private boolean maskBasePairMatches;

    public RectangleGlyph(Rectangle rectShape) {
        int height = 10;
        double y = 20;
        if (rectShape.getAttributes().contains(org.lorainelab.igb.data.model.shapes.Rectangle.Attribute.THICK)) {
            height = THICK_RECTANGLE_HEIGHT;
            y = MIN_Y_OFFSET;
        } else if (rectShape.getAttributes().contains(org.lorainelab.igb.data.model.shapes.Rectangle.Attribute.INSERTION)) {
            height = 3;
        }
        boundingRect = new Rectangle2D(rectShape.getOffset(), y, rectShape.getWidth(), height);
        innerTextRefSeqTranslator = rectShape.getInnerTextRefSeqTranslator();
        innerTextReferenceSequenceRange = rectShape.getInnerTextReferenceSequenceRange();
        colorByBase = rectShape.isMirrorReferenceSequence();
        isSelectable = rectShape.isSelectable();
        isSelected = false;
        maskBasePairMatches = rectShape.isMaskBasePairMatches();
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
    public void draw(GraphicsContext gc, View view, Rectangle2D slotBoundingViewRect) {
        calculateDrawRect(view, slotBoundingViewRect).ifPresent(sharedRect -> {
            Rectangle2D viewRect = view.getBoundingRect();
            gc.setFill(fill);
            gc.setStroke(strokeColor);
            final double y = sharedRect.getMinY();
            DrawUtils.scaleToVisibleRec(view, SHARED_RECT);
            gc.fillRect(SHARED_RECT.x, SHARED_RECT.y, SHARED_RECT.width, SHARED_RECT.height);
            if (view.getBoundingRect().getWidth() < 150) {
                drawText(view, viewRect, gc, y, sharedRect);
            }
        });
    }

    private void drawText(View view, Rectangle2D viewRect, GraphicsContext gc, final double y, java.awt.Rectangle.Double viewBoundingRect) {
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
            int size = (int) boundingRect.getHeight();
            synchronized (gc) {// should not be needed, but I am currently seeing rendering issues that appear directly related to race conditions on this function
                gc.save();
                double textScale = .5;
                gc.scale(textScale, textScale);
                gc.setFont(Font.font("Monospaced", FontWeight.MEDIUM, size));
                FontMetrics fm = Toolkit.getToolkit().getFontLoader().getFontMetrics(gc.getFont());
                double textHeight = fm.getAscent();
                double textYPosition = (y / textScale) + textHeight;
                double textYOffset = (viewBoundingRect.getHeight() / textScale - fm.getLineHeight()) / 2;
                textYPosition += textYOffset;
                gc.scale(1 / textScale, 1 / textScale);
                double i = 0;
                double minX = viewBoundingRect.getMinX();
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
                    if (startOffset % 1 > 0 && i == 0) {
                        gc.fillRect(minX, y, 1 - startOffset % 1, viewBoundingRect.getHeight());
                        i += (1 - startOffset % 1);
                        continue;
                    } else {
                        gc.fillRect(minX + i, y, 1, viewBoundingRect.getHeight());
                    }
                    if (!charMatch) {
                        if (colorByBase) {
                            gc.setFill(Color.BLACK);
                        } else {
                            gc.setFill(ColorUtils.getEffectiveContrastColor(fill));
                        }
                        gc.scale(textScale, textScale);
                        double x = ((minX + i) / textScale) + (1 / textScale) * .1;
                        double maxWidth = (1 / textScale) * .8;
                        gc.fillText("" + c, x, textYPosition, maxWidth);
                        gc.scale(1 / textScale, 1 / textScale);
                    }
                    i++;
                }
                gc.restore();
            }
        });
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
