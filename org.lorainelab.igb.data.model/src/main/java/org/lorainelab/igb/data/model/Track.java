package org.lorainelab.igb.data.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.concurrent.ConcurrentHashMap;
import static java.util.stream.Collectors.toList;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import org.lorainelab.igb.data.model.glyph.CompositionGlyph;
import static org.lorainelab.igb.data.model.glyph.CompositionGlyph.DEFAULT_COLOR;
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
        // everything works fine until x coordinate get large, and then the larger numbers don't render correctly on the canvas
        //i.e. we can't do this gc.translate(-view.getBoundingRect().getMinX(), -view.getBoundingRect().getMinY() + additionalYOffset);
        gc.translate(0, additionalYOffset);
        slotMap.entrySet().stream()
                .forEach(entry -> {
                    //TODO, could easily filter based on rows y coordinate range if we didn't want to support slop row, just modify getSlotOffset to not fix for slop and then filter on view range
                    //we may also consider using this approach to create customer slop row visualization
                    double slotYOffset = entry.getValue().getSlotYoffset();
                    gc.save();
                    gc.translate(0, slotYOffset);
                    experimentalOptimizedRender(entry.getValue().getGlyphsInView(view), gc, view, canvasContext);
                    gc.restore();
                });
        gc.restore();

    }

    private void experimentalOptimizedRender(List<CompositionGlyph> glyphsInView, GraphicsContext gc, View view, CanvasContext canvasContext) {
        double xPixelsPerCoordinate = view.getBoundingRect().getWidth() / canvasContext.getBoundingRect().getWidth();
        //combine nearby rectangles to optimize rendering... assuming this will be less expensive, needs testing
        if (xPixelsPerCoordinate < 10_000) {
            glyphsInView.stream().forEach(glyph -> glyph.draw(gc, view));
        } else {
            for (int i = 0; i < glyphsInView.size();) {
                CompositionGlyph glyph = glyphsInView.get(i);
                boolean isSelected = glyph.isSelected();
                Rectangle2D renderRect = glyph.getViewBoundingRect(view).get();// get() call is not unsafe in this instance, and breaking would be preferred if assumption not met
                if (renderRect.getWidth() / xPixelsPerCoordinate > 10) {
                    glyph.draw(gc, view);
                } else {
                    double maxX = renderRect.getMaxX();
                    while (i + 1 < glyphsInView.size()) {
                        final CompositionGlyph nextGlyph = glyphsInView.get(i + 1);
                        isSelected = isSelected | nextGlyph.isSelected();
                        Rectangle2D nextRenderRect = nextGlyph.getViewBoundingRect(view).get();
                        if (nextRenderRect.getMinX() / xPixelsPerCoordinate < (renderRect.getMaxX() / xPixelsPerCoordinate) + 1) {
                            maxX = nextRenderRect.getMaxX();
                        } else {
                            final Rectangle2D drawRect = new Rectangle2D(renderRect.getMinX(), renderRect.getMinY(), maxX - renderRect.getMinX(), renderRect.getHeight());
                            drawSummaryRectangle(gc, drawRect);
                            if (isSelected) {
                                glyph.drawSummarySelectionRectangle(gc, view, drawRect);
                            }
                            break;
                        }
                        i++;
                    }
                }
                i++;
            }

        }
    }

    private void drawSummaryRectangle(GraphicsContext gc, Rectangle2D drawRect) {
        gc.save();
        gc.setFill(DEFAULT_COLOR);
        gc.setStroke(DEFAULT_COLOR);
        if (isNegative) {
            gc.fillRect(drawRect.getMinX(), drawRect.getMinY(), drawRect.getWidth(), drawRect.getHeight() / 2);
        } else {
            gc.fillRect(drawRect.getMinX(), drawRect.getMinY()+ (drawRect.getHeight() / 2), drawRect.getWidth(), drawRect.getHeight() / 2);

        }
        gc.restore();
    }

    private double getSlotOffset(int slot) {
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
                        final int slot = slotToadd;
                        Slot bin = slotMap.computeIfAbsent(slotToadd, e -> {
                            return new Slot();
                        });
                        bin.addGlyph(glyph);
                    }
                });
        updateSlotCount();
        slotMap.entrySet().forEach(entry -> entry.getValue().setSlotYoffset(getSlotOffset(entry.getKey())));
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
