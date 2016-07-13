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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jeckstei
 */
public class RectangleGlyph implements Glyph {

    private static final Logger LOG = LoggerFactory.getLogger(RectangleGlyph.class);
    private Color fill = Color.BLACK;

    private Color strokeColor = Color.BLACK;

    private Rectangle2D boundingRect;
    private Rectangle2D renderBoundingRect;
    private Optional<Function<String, String>> innerTextRefSeqTranslator;
    private Optional<Range<Integer>> innerTextReferenceSequenceRange;
    boolean colorByBase;

    public RectangleGlyph(Rectangle rectShape) {
        int height = 10;
        double y = 20;
        if (rectShape.getAttributes().contains(org.lorainelab.igb.data.model.shapes.Rectangle.Attribute.THICK)) {
            height = 15;
            y = 17.5;
        } else if (rectShape.getAttributes().contains(org.lorainelab.igb.data.model.shapes.Rectangle.Attribute.INSERTION)) {
            height = 3;
        }
        boundingRect = new Rectangle2D(rectShape.getOffset(), y, rectShape.getWidth(), height);
        innerTextRefSeqTranslator = rectShape.getInnerTextRefSeqTranslator();
        innerTextReferenceSequenceRange = rectShape.getInnerTextReferenceSequenceRange();
        colorByBase = rectShape.getColorByBase();
    }

    @Override
    public Color getFill() {
        return fill;
    }

    @Override
    public Color getStrokeColor() {
        return strokeColor;
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
    public void draw(GraphicsContext gc, View view, double additionalYoffset) {
        Rectangle2D viewRect = view.getBoundingRect();
        Optional<Rectangle2D> viewBoundingRect = getViewBoundingRect(viewRect, additionalYoffset);
        if (viewBoundingRect.isPresent()) {
            gc.setFill(fill);
            gc.setStroke(strokeColor);
            final double y = viewBoundingRect.get().getMinY();
            gc.fillRect(viewBoundingRect.get().getMinX(), y, viewBoundingRect.get().getWidth(), viewBoundingRect.get().getHeight());
            if (view.getBoundingRect().getWidth() < 150) {
                innerTextRefSeqTranslator.ifPresent(translationFunction -> {
                    Chromosome chromosome = view.getChromosome();

                    String sequence;
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
                    Range<Integer> basePairRange = Range.closed((int) boundingRect.getMinX() + (int) startOffset, (int) boundingRect.getMaxX() - (int) endOffset);
                    if (innerTextReferenceSequenceRange.isPresent()) {
                        //TODO handle offsets
                        sequence = new String(chromosome.getSequence(innerTextReferenceSequenceRange.get().lowerEndpoint(),
                                innerTextReferenceSequenceRange.get().upperEndpoint() - innerTextReferenceSequenceRange.get().lowerEndpoint()));
                        innerText = translationFunction.apply(sequence);
                        int startPos = (int) boundingRect.getMinX() - innerTextReferenceSequenceRange.get().lowerEndpoint();
                        innerText = innerText.substring(startPos, startPos + (int) boundingRect.getWidth());
                    } else {
                        sequence = new String(chromosome.getSequence(basePairRange.lowerEndpoint(), basePairRange.upperEndpoint() - basePairRange.lowerEndpoint()));
                        innerText = translationFunction.apply(sequence);
                    }

                    int size = 15;
                    synchronized (gc) {// should not be needed, but I am currently seeing rendering issues that appear directly related to race conditions on this function
                        gc.save();

                        double textScale = .5;
                        gc.scale(textScale, textScale);
                        gc.setFont(Font.font("Monospaced", FontWeight.MEDIUM, size));
                        FontMetrics fm = Toolkit.getToolkit().getFontLoader().getFontMetrics(gc.getFont());
                        double textHeight = fm.getAscent();
                        double textYPosition = (y / textScale) + textHeight;
                        double textYOffset = (viewBoundingRect.get().getHeight() / textScale - fm.getLineHeight()) / 2;
                        textYPosition += textYOffset;
                        gc.scale(1 / textScale, 1 / textScale);
                        double i = 0;
                        double minX = viewBoundingRect.get().getMinX();
                        int baseWidth = 1;
                        for (char c : innerText.toUpperCase().toCharArray()) {
                            if (colorByBase) {
                                gc.setFill(getBaseColor(c));
                            } else {
                                gc.setFill(fill);
                            }
                            if (startOffset % 1 > 0 && i == 0) {
                                gc.fillRect(minX, y, 1 - startOffset % 1, viewBoundingRect.get().getHeight());
                                i += (1 - startOffset % 1);
                                continue;
                            } else {
                                gc.fillRect(minX + i, y, 1, viewBoundingRect.get().getHeight());
                            }
                            gc.setFill(Color.BLACK);
                            gc.scale(textScale, textScale);
                            double x = ((minX + i) / textScale) + (1 / textScale) * .1;
                            double maxWidth = (1 / textScale) * .8;
                            gc.fillText("" + c, x, textYPosition, maxWidth);
                            gc.scale(1 / textScale, 1 / textScale);
                            i++;
                        }
                        gc.restore();
                    }
                });
            }
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
