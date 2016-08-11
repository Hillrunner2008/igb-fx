package org.lorainelab.igb.data.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.concurrent.ConcurrentHashMap;
import static java.util.stream.Collectors.toList;
import javafx.scene.canvas.GraphicsContext;
import org.lorainelab.igb.data.model.glyph.CompositionGlyph;
import static org.lorainelab.igb.data.model.glyph.Glyph.MIN_OFFSET;
import static org.lorainelab.igb.data.model.glyph.Glyph.MIN_X_COMPARATOR;
import static org.lorainelab.igb.data.model.glyph.Glyph.SLOT_HEIGHT;
import static org.lorainelab.igb.data.model.glyph.RectangleGlyph.THICK_RECTANGLE_HEIGHT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
public class Track {

    private static final Logger LOG = LoggerFactory.getLogger(Track.class);
    private final String trackLabel;
    private List<CompositionGlyph> glyphs;
    //A KD-Tree is something we may consider as an alternative to the Slot classes RangeMap's 
    //subRangeMap Interval Tree based solution. KD-Tree could handle rectangle's need for 4D search
    private Map<Integer, Slot> slotMap;

    private final boolean isNegative;
    private double modelHeight;
    private int stackHeight;
    private int slotCount;

    public Track(boolean isNegative, String trackLabel, int stackHeight) {
        this.isNegative = isNegative;
        this.trackLabel = trackLabel;
        this.stackHeight = Math.max(stackHeight, 0);
        //handles case when stackHeight is set to 0 i.e. unlimited
        this.modelHeight = Math.max(SLOT_HEIGHT * stackHeight, SLOT_HEIGHT);
        slotMap = new ConcurrentHashMap<>();
        glyphs = Lists.newArrayList();
    }

    public List<CompositionGlyph> getGlyphs() {
        return glyphs;
    }

    public void draw(GraphicsContext gc, View view, CanvasContext canvasContext) {
        double additionalYOffset = canvasContext.getBoundingRect().getMinY() / view.getYfactor();
        //TODO look into why concurrency issues are possible at this location during zooming
        gc.save();
        //NOTE: Rounding issues prevent us from using translation to take care of view offsets in the x coordinate system
        // everything works fine until x coordinates get large, and then the larger numbers don't render correctly on the canvas
        //i.e. we can't do this gc.translate(-view.getBoundingRect().getMinX(), -view.getBoundingRect().getMinY() + additionalYOffset);
        gc.translate(0, additionalYOffset);
        slotMap.entrySet().stream()
                .forEach(entry -> {
                    //TODO, could easily filter based on rows y coordinate range if we didn't want to support slop row, just modify getSlotOffset to not fix for slop and then filter on view range
                    //we may also consider using this approach to create customer slop row visualization
                    double slotOffset = getSlotOffset(entry.getKey());
                    gc.save();
                    gc.translate(0, slotOffset);
                    entry.getValue().getGlyphsInView(view).stream().forEach(glyph -> glyph.draw(gc, view));
                    gc.restore();
                });
        gc.restore();

    }

    public double getSlotOffset(int slot) {
        if (slot >= stackHeight && stackHeight != 0) {
            //add to slop row
            slot = stackHeight - 1;
        }
        if (isNegative) {
            return -MIN_OFFSET + (slot * SLOT_HEIGHT);
        } else {
            final double minPositiveStrandOffset = -MIN_OFFSET - THICK_RECTANGLE_HEIGHT;
            return minPositiveStrandOffset + (slotCount - slot) * SLOT_HEIGHT;
        }
    }

    public void buildSlots() {
        glyphs.stream()
                .sorted(MIN_X_COMPARATOR)
                .forEachOrdered(glyph -> {
                    int slotToadd = 0;
                    List<Integer> keys = slotMap.keySet().stream().sorted(Ordering.natural()).collect(toList());
                    for (int key : keys) {
                        double max = slotMap.get(key).getMaxX();
                        if (glyph.getBoundingRect().getMinX() > max) {
                            break;
                        } else {
                            slotToadd++;
                            if (slotToadd >= stackHeight && isLimitedStackHeight()) {
                                break;
                            }
                        }
                    }
                    if (slotToadd <= stackHeight || stackHeight == 0) {
                        Slot bin = slotMap.computeIfAbsent(slotToadd, e -> {
                            return new Slot();
                        });
                        bin.addGlyph(glyph);
                    }
                });

        updateSlotCount();
        modelHeight = SLOT_HEIGHT * slotCount + 1;// +1 for slop row

    }

    private void updateSlotCount() {
        OptionalInt max = slotMap.keySet().stream().mapToInt(key -> key).max();
        slotCount = max.orElse(0);
        if (isLimitedStackHeight()) {
            if (stackHeight < slotCount) {
                slotCount = stackHeight;
            }
        }
    }

    private boolean isLimitedStackHeight() {
        return stackHeight != 0;
    }

    public Map<Integer, Slot> getSlotMap() {
        return slotMap;
    }

    public String getTrackLabel() {
        return trackLabel;
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

    public double getModelHeight() {
        return modelHeight;
    }

}
