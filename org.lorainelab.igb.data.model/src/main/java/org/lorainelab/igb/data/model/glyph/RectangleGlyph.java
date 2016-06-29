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
import javafx.scene.paint.Paint;
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
    private Paint fill = Color.BLACK;

    private Paint strokeColor = Color.BLACK;

    private Rectangle2D boundingRect;
    private Rectangle2D renderBoundingRect;
    private Optional<Function<String, String>> innerTextRefSeqTranslator;
    private Optional<Range<Integer>> innerTextReferenceSequenceRange;
    boolean colorByBase;

    public RectangleGlyph(Rectangle rectShape) {
        int height = 10;
        if (rectShape.getAttributes().contains(org.lorainelab.igb.data.model.shapes.Rectangle.Attribute.thick)) {
            height = 15;
        }
        double y = height == 15 ? 17.5 : 20;
        boundingRect = new Rectangle2D(rectShape.getOffset(), y, rectShape.getWidth(), height);
        innerTextRefSeqTranslator = rectShape.getInnerTextRefSeqTranslator();
        innerTextReferenceSequenceRange = rectShape.getInnerTextReferenceSequenceRange();
        colorByBase = rectShape.getColorByBase();
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
                    if (innerTextReferenceSequenceRange.isPresent()) {
                        sequence = new String(chromosome.getSequence(innerTextReferenceSequenceRange.get().lowerEndpoint(),
                                innerTextReferenceSequenceRange.get().upperEndpoint() - innerTextReferenceSequenceRange.get().lowerEndpoint()));
                        innerText = translationFunction.apply(sequence);
                        int startPos = (int) boundingRect.getMinX() - innerTextReferenceSequenceRange.get().lowerEndpoint();
                        innerText = innerText.substring(startPos, startPos + (int) boundingRect.getWidth());
                    } else {
                        sequence = new String(chromosome.getSequence((int) boundingRect.getMinX(), (int) boundingRect.getWidth()));
                        innerText = translationFunction.apply(sequence);
                    }

                    synchronized (gc) {// should not be needed, but I am currently seeing rendering issues that appear directly related to race conditions on this function
                        gc.save();

                        int size = 14;
                        double textScale = .5;
                        gc.scale(textScale, textScale);
                        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, size));
                        FontMetrics fm = Toolkit.getToolkit().getFontLoader().getFontMetrics(gc.getFont());
                        double textHeight = fm.getAscent();
                        double textYPosition = (y / textScale) + textHeight;
                        double textYOffset = (viewBoundingRect.get().getHeight() / textScale - fm.getLineHeight()) / 2;
                        textYPosition += textYOffset;
                        gc.scale(1 / textScale, 1 / textScale);
                        if (colorByBase) {
                            int i = 0;
                            for (char c : innerText.toCharArray()) {
                                gc.setFill(getBaseColor(c));
                                gc.fillRect(viewBoundingRect.get().getMinX() + i, y, 1, viewBoundingRect.get().getHeight());
                                gc.setFill(Color.BLACK);
                                gc.scale(textScale, textScale);
                                double x = (viewBoundingRect.get().getMinX() + i) / textScale;
                                double maxWidth = 1 / textScale;
                                gc.fillText("" + c, x, textYPosition, maxWidth);
                                gc.scale(1 / textScale, 1 / textScale);
                                i++;
                            }
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
