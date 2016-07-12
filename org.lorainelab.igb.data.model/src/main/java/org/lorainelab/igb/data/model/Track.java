package org.lorainelab.igb.data.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Ordering;
import com.google.common.collect.SortedSetMultimap;
import com.google.common.collect.TreeMultimap;
import com.google.common.primitives.Ints;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    private SortedSetMultimap<Integer, CompositionGlyph> slotMap;

    private final boolean isNegative;
    private DoubleProperty modelHeight;
    private int stackHeight;
    private static final int PADDING = 5;

    public Track(boolean isNegative, String trackLabel, int stackHeight) {
        this.isNegative = isNegative;
        this.trackLabel = trackLabel;
        this.stackHeight = Math.max(stackHeight, 0);
        //handles case when stackHeight is set to 0 i.e. unlimited
        this.modelHeight = new SimpleDoubleProperty(Math.max(SLOT_HEIGHT * stackHeight, SLOT_HEIGHT));
        slotMap = Multimaps.synchronizedSortedSetMultimap(TreeMultimap.create(Ordering.natural(), MIN_X_COMPARATOR));
        glyphs = Lists.newArrayList();
    }

    public List<CompositionGlyph> getGlyphs() {
        return glyphs;
    }

    public void draw(GraphicsContext gc, View view, CanvasContext canvasContext) {
        final double additionalYOffset = canvasContext.getBoundingRect().getMinY() / view.getYfactor();
        //TODO look into why concurrency issues are possible at this location during zooming
        slotMap.values().stream()
                .filter(glyph -> view.getBoundingRect().intersects(glyph.getRenderBoundingRect()))
                .forEach(glyph -> glyph.draw(gc, view, additionalYOffset));

    }

    private void setGlyphPosition(CompositionGlyph compositionGlyph, int slot, int maxStackHeight) {
        for (Glyph glyph : compositionGlyph.getChildren()) {
            Rectangle2D boundingRect = glyph.getBoundingRect();
            final int modelHeight = SLOT_HEIGHT * maxStackHeight;
            final double x = boundingRect.getMinX();
            final double width = boundingRect.getWidth();
            final double height = boundingRect.getHeight();
            if (isNegative) {
                final double y = boundingRect.getMinY() + (slot * SLOT_HEIGHT) - PADDING;
                glyph.setRenderBoundingRect(new Rectangle2D(x, y, width, height));
            } else {
                double slotStartingY = (maxStackHeight - slot) * SLOT_HEIGHT;
                double originalY = boundingRect.getMinY();
                final double y = originalY + slotStartingY;
                glyph.setRenderBoundingRect(new Rectangle2D(x, y, width, height));
            }
        }
        compositionGlyph.refreshBounds();
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
                            if (slotToadd >= stackHeight && isLimitedStackHeight()) {
                                break;
                            }
                        }
                    }
                    if (slotToadd <= stackHeight || stackHeight == 0) {
                        slotMap.put(slotToadd, glyph);
                    }
                });

        Optional<Integer> max = slotMap.keys().stream().max((x, y) -> Ints.compare(x, y));
        if (max.isPresent()) {
            Integer slotCount = max.get();
            if (isLimitedStackHeight()) {
                if (stackHeight < max.get()) {
                    slotCount = stackHeight;
                }
            }
            for (Map.Entry<Integer, CompositionGlyph> entry : slotMap.entries()) {
                int slotToadd = entry.getKey();
                if (slotToadd < stackHeight || stackHeight == 0) {

                    setGlyphPosition(entry.getValue(), slotToadd, slotCount);
                } else {
                    //add to slop row
                    setGlyphPosition(entry.getValue(), stackHeight - 1, slotCount);
                }
            }
            slotCount++; // for slop row
            modelHeight.set(SLOT_HEIGHT * slotCount);
        }
    }

    private boolean isLimitedStackHeight() {
        return stackHeight != 0;
    }

    public SortedSetMultimap<Integer, CompositionGlyph> getSlotMap() {
        return slotMap;
    }

    public String getTrackLabel() {
        return trackLabel;
    }

    public DoubleProperty modelHeightProperty() {
        return modelHeight;
    }

    public void setMaxStackHeight(int maxStackHeight) {
        final int updatedStackHeight = Math.max(maxStackHeight, 0);
        if (updatedStackHeight != stackHeight) {
            this.stackHeight = updatedStackHeight;
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
