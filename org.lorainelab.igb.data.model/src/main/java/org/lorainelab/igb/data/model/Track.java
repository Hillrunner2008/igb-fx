package org.lorainelab.igb.data.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.collect.TreeMultimap;
import java.util.Iterator;
import java.util.List;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import org.lorainelab.igb.data.model.glyph.CompositionGlyph;
import org.lorainelab.igb.data.model.glyph.Glyph;
import static org.lorainelab.igb.data.model.glyph.Glyph.MIN_X_COMPARATOR;
import static org.lorainelab.igb.data.model.glyph.Glyph.SLOT_HEIGHT;

/**
 *
 * @author dcnorris
 */
public class Track {

    private final String trackLabel;
    private List<CompositionGlyph> glyphs;
    private TreeMultimap<Integer, CompositionGlyph> slotMap;

    private final boolean isNegative;
    private DoubleProperty modelHeight;
    private int stackHeight;

    public Track(boolean isNegative, String trackLabel, int stackHeight) {
        this.isNegative = isNegative;
        this.trackLabel = trackLabel;
        this.stackHeight = stackHeight;
        this.modelHeight = new SimpleDoubleProperty((SLOT_HEIGHT * stackHeight));
        slotMap = TreeMultimap.create(Ordering.natural(), MIN_X_COMPARATOR);
        glyphs = Lists.newArrayList();
    }

    public List<CompositionGlyph> getGlyphs() {
        return glyphs;
    }

    public void draw(GraphicsContext gc, View view, CanvasContext canvasContext) {
        final double additionalYOffset = canvasContext.getBoundingRect().getMinY() / view.getYfactor();
        slotMap.asMap().values().stream().flatMap(glyphs -> glyphs.stream())
                .filter(glyph -> view.getBoundingRect().intersects(glyph.getRenderBoundingRect()))
                .forEach(glyph -> glyph.draw(gc, view, additionalYOffset));
    }

    private void setGlyphPosition(CompositionGlyph compositionGlyph, int slot, int maxStackHeight) {
        for (Glyph glyph : compositionGlyph.getChildren()) {
            Rectangle2D boundingRect = glyph.getBoundingRect();
            final int modelHeight = SLOT_HEIGHT * maxStackHeight;
            double yOffset = modelHeight / maxStackHeight;
            final double x = boundingRect.getMinX();
            final double width = boundingRect.getWidth();
            final double height = boundingRect.getHeight();
            if (isNegative) {
                final double y = boundingRect.getMinY() + (slot * yOffset);
                glyph.setRenderBoundingRect(new Rectangle2D(x, y, width, height));
            } else {
                final double y = boundingRect.getMinY() + ((maxStackHeight - 1) - slot) * yOffset;
                glyph.setRenderBoundingRect(new Rectangle2D(x, y, width, height));
            }
        }
    }

    public void buildSlots() {
        glyphs.stream()
                .sorted(MIN_X_COMPARATOR)
                .forEachOrdered(glyph -> {
                    int slotToadd = 0;
                    for (Integer key : slotMap.keySet()) {
                        double rowMax = slotMap.get(key).last().getBoundingRect().getMaxX();
                        if (glyph.getBoundingRect().getMinX() > rowMax) {
                            break;
                        } else {
                            slotToadd++;
                            if (slotToadd >= stackHeight && stackHeight != 0) {
                                break;
                            }
                        }
                    }
                    if (slotToadd <= stackHeight || stackHeight == 0) {
                        slotMap.put(slotToadd, glyph);
                    }
                });
        final Iterator<Integer> descendingIterator = slotMap.keySet().descendingIterator();
        if (descendingIterator.hasNext()) {
            Integer maxStackHeight = descendingIterator.next();
            slotMap.entries().forEach(entry -> {
                int slotToadd = entry.getKey();
                if (slotToadd < stackHeight || stackHeight == 0) {
                    setGlyphPosition(entry.getValue(), slotToadd, stackHeight > 0 ? stackHeight : maxStackHeight);
                } else {
                    setGlyphPosition(entry.getValue(), stackHeight - 1, stackHeight > 0 ? stackHeight : maxStackHeight);
                }
            });
            modelHeight.set((SLOT_HEIGHT * maxStackHeight));
        }
    }

    public TreeMultimap<Integer, CompositionGlyph> getSlotMap() {
        return slotMap;
    }

    public String getTrackLabel() {
        return trackLabel;
    }

    public DoubleProperty getModelHeight() {
        return modelHeight;
    }

    public void setMaxStackHeight(int maxStackHeight) {
        if (maxStackHeight >= 0) {
            this.stackHeight = maxStackHeight;
            slotMap.clear();
            buildSlots();
        }
    }

    void clearGlyphs() {
        glyphs.clear();
        slotMap.clear();
    }

    public int getStackHeight() {
        return stackHeight;
    }

}
