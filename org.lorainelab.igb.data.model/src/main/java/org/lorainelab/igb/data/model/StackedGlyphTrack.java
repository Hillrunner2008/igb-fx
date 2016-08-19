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
import static org.lorainelab.igb.data.model.glyph.Glyph.MIN_X_COMPARATOR;
import static org.lorainelab.igb.data.model.glyph.Glyph.MIN_Y_OFFSET;
import static org.lorainelab.igb.data.model.glyph.Glyph.SLOT_HEIGHT;
import static org.lorainelab.igb.data.model.glyph.RectangleGlyph.THICK_RECTANGLE_HEIGHT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
public class StackedGlyphTrack implements Track {

    private static final Logger LOG = LoggerFactory.getLogger(StackedGlyphTrack.class);
    private final String trackLabel;
    private List<CompositionGlyph> glyphs;
    //A KD-Tree is something we may consider as an alternative to the Slot classes RangeMap's 
    //subRangeMap Interval Tree based solution. KD-Tree could handle rectangle's need for 4D search
    private Map<Integer, Slot> slotMap;

    private final boolean isNegative;
    private double modelHeight;
    private int stackHeight;
    private int slotCount;

    public StackedGlyphTrack(boolean isNegative, String trackLabel, int stackHeight) {
        this.isNegative = isNegative;
        this.trackLabel = trackLabel;
        this.stackHeight = Math.max(stackHeight, 0);
        //handles case when stackHeight is set to 0 i.e. unlimited
        this.modelHeight = Math.max(SLOT_HEIGHT * stackHeight, SLOT_HEIGHT);
        slotMap = new ConcurrentHashMap<>();
        glyphs = Lists.newArrayList();
    }

    @Override
    public List<CompositionGlyph> getGlyphs() {
        return glyphs;
    }

    @Override
    public void draw(GraphicsContext gc, View view, CanvasContext canvasContext) {
        double trackPositionOffset = canvasContext.getBoundingRect().getMinY() / view.getYfactor();
        gc.save();
        //NOTE: Rounding issues prevent us from using translation to take care of view offsets in the x coordinate system
        // everything works fine until x coordinate get large, and then the larger numbers don't render correctly on the canvas
        //i.e. we can't do this gc.translate(-view.getBoundingRect().getMinX(), -view.getBoundingRect().getMinY() + additionalYOffset);
        gc.translate(0, trackPositionOffset);
        slotMap.entrySet().stream()
                .forEach(entry -> {
                    //TODO, could easily filter based on rows y coordinate range if we didn't want to support slop row, just modify getSlotOffset to not fix for slop and then filter on view range
                    //we may also consider using this approach to create customer slop row visualization
                    double slotYOffset = entry.getValue().getSlotYoffset();
                    gc.save();
//                    gc.translate(0, slotYOffset);
//                    if (!isNegative) {
//                        if (entry.getKey() == 0) {
                    Rectangle2D slotBoundingViewRect = entry.getValue().getSlotBoundingViewRect(view.getBoundingRect(), isNegative);
                    

//                            System.out.println(slotBoundingViewRect.toString());
//                        }
//                    }
                    experimentalOptimizedRender(entry.getValue().getGlyphsInView(view), gc, view, slotBoundingViewRect);
                    gc.restore();
                });
        gc.restore();

    }

    private void experimentalOptimizedRender(List<CompositionGlyph> glyphsInView, GraphicsContext gc, View view, Rectangle2D slotBoundingViewRect) {
        double xPixelsPerCoordinate = view.getBoundingRect().getWidth() / view.getCanvasContext().getBoundingRect().getWidth();
        //combine nearby rectangles to optimize rendering... assuming this will be less expensive, needs testing
        if (xPixelsPerCoordinate < 10_000) {
            glyphsInView.stream().forEach(glyph -> glyph.draw(gc, view, slotBoundingViewRect));
        } else {
            for (int i = 0; i < glyphsInView.size();) {
                CompositionGlyph glyph = glyphsInView.get(i);
                boolean isSelected = glyph.isSelected();
                Rectangle2D renderRect = glyph.getViewBoundingRect(view, slotBoundingViewRect).orElse(null);
                if (renderRect != null) {
                    if (renderRect.getWidth() / xPixelsPerCoordinate > 10) {
                        glyph.draw(gc, view, slotBoundingViewRect);
                    } else {
                        double maxX = renderRect.getMaxX();
                        while (i + 1 < glyphsInView.size()) {
                            final CompositionGlyph nextGlyph = glyphsInView.get(i + 1);
                            isSelected = isSelected | nextGlyph.isSelected();
                            Rectangle2D nextRenderRect = nextGlyph.getViewBoundingRect(view, slotBoundingViewRect).orElse(null);
                            if (nextRenderRect != null && nextRenderRect.getMinX() / xPixelsPerCoordinate < (renderRect.getMaxX() / xPixelsPerCoordinate) + 1) {
                                maxX = nextRenderRect.getMaxX();
                            } else {
                                final Rectangle2D drawRect = new Rectangle2D(renderRect.getMinX(), renderRect.getMinY(), maxX - renderRect.getMinX(), renderRect.getHeight());
                                glyph.drawSummaryRectangle(gc, drawRect);
                                if (isSelected) {
                                    glyph.drawSummarySelectionRectangle(gc, view, drawRect);
                                }
                                break;
                            }
                            i++;
                        }
                    }
                }
                i++;
            }

        }
    }

    private double getSlotOffset(int slot) {
        if (slot >= stackHeight && stackHeight != 0) {
            //add to slop row
            slot = stackHeight - 1;
        }
        if (isNegative) {
            return slot * SLOT_HEIGHT;
        } else {
            final double minPositiveStrandOffset = -MIN_Y_OFFSET - THICK_RECTANGLE_HEIGHT;
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
                        slotMap.computeIfAbsent(slotToadd, e -> {
                            return new Slot();
                        }).addGlyph(glyph);
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

    public boolean isNegative() {
        return isNegative;
    }

    @Override
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

    @Override
    public double getModelHeight() {
        return modelHeight;
    }

    @Override
    public void processSelectionRectangle(Rectangle2D selectionRectangle, Rectangle2D viewBoundingRect) {
//        List<CompositionGlyph> selections = getSlotMap().entrySet().stream().flatMap(entry -> {
//            double slotOffset = entry.getValue().getSlotYoffset();
//            final Range<Double> mouseEventXrange = Range.closed(selectionRectangle.getMinX(), selectionRectangle.getMaxX());
//            final Stream<CompositionGlyph> glyphsInXRange = entry.getValue().getGlyphsInXrange(mouseEventXrange).stream();
//
//            Rectangle2D slotBoundingViewRect = entry.getValue().getSlotBoundingViewRect(viewBoundingRect, isNegative);
//            return glyphsInXRange.filter(glyph -> {
//                final Range<Double> mouseEventYrange = Range.closed(selectionRectangle.getMinY(), selectionRectangle.getMaxY());
//                final Rectangle2D glyphViewRect = glyph.getViewBoundingRect(view, slotBoundingViewRect).orElse(null);
//                if (glyphViewRect == null) {
//                    return false;
//                }
//                return Range.closed(glyphViewRect.getMinY() + slotOffset, glyphViewRect.getMaxY() + slotOffset).isConnected(mouseEventYrange);
//            });
//        }).collect(toList());
////
//        if (selections.size() > 1) {
//            selections.forEach(glyph -> glyph.setIsSelected(true));
//        } else {
//            selections.forEach(glyph -> {
//                boolean subSelectionActive = false;
//                for (Glyph g : glyph.getChildren()) {
//                    if (g.isSelectable()) {
//                        if (g.getBoundingRect().intersects(selectionRectangle)) {
//                            g.setIsSelected(true);
//                            subSelectionActive = true;
//                            break;
//                        }
//                    }
//                }
//                glyph.setIsSelected(true);//set this flag regardless of subselection 
//            });
//        }
    }

    @Override
    public List<CompositionGlyph> getSelectedGlyphs() {
        return slotMap.entrySet().stream()
                .flatMap(entry -> entry.getValue().getAllGlyphs().stream())
                .filter(glyph -> glyph.isSelected()).collect(toList());
    }

}
