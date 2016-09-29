package org.lorainelab.igb.data.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.collect.Range;
import java.awt.Rectangle;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.concurrent.ConcurrentHashMap;
import static java.util.stream.Collectors.toList;
import java.util.stream.Stream;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import org.lorainelab.igb.data.model.glyph.CompositionGlyph;
import org.lorainelab.igb.data.model.glyph.Glyph;
import static org.lorainelab.igb.data.model.glyph.Glyph.MIN_X_COMPARATOR;
import static org.lorainelab.igb.data.model.glyph.Glyph.SLOT_HEIGHT;
import org.lorainelab.igb.data.model.util.DrawUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
public class StackedGlyphTrack implements Track {

    private static final Logger LOG = LoggerFactory.getLogger(StackedGlyphTrack.class);
    private static Rectangle.Double SCRATCH_RECT = new Rectangle.Double(0, 0, 0, 0);
    private final String trackLabel;
    private List<CompositionGlyph> glyphs;
    //A KD-Tree is something we may consider as an alternative to the Slot classes RangeMap's 
    private Map<Integer, Slot> slotMap;

    private final boolean isNegative;
    private double modelHeight;
    private int stackHeight;
    private int slotCount;
    private final DataSet dataSet;
    private BooleanProperty isHeightLocked;
    private double lockedHeight;

    public StackedGlyphTrack(boolean isNegative, String trackLabel, int stackHeight, DataSet dataSet) {
        this.lockedHeight = 25;
        this.isHeightLocked = new SimpleBooleanProperty(false);
        this.dataSet = dataSet;
        this.isNegative = isNegative;
        this.trackLabel = trackLabel;
        this.stackHeight = Math.max(stackHeight, 0);
        //handles case when stackHeight is set to 0 i.e. unlimited
        this.modelHeight = Math.max(SLOT_HEIGHT * stackHeight, SLOT_HEIGHT);
        slotMap = new ConcurrentHashMap<>();
        glyphs = Lists.newArrayList();

    }

    @Override
    public void draw(GraphicsContext gc, View view, CanvasContext canvasContext) {
        double trackPositionOffset = canvasContext.getBoundingRect().getMinY() / view.getYfactor();
        gc.save();
        //NOTE: Rounding issues prevent us from using translation to take care of view offsets in the x coordinate system
        // everything works fine until x coordinate is large (e.g. > 10_000_000), and then the larger numbers don't render correctly on the canvas
        //i.e. we can't do this gc.translate(-view.getBoundingRect().getMinX(), trackPositionOffset);
        gc.translate(0, trackPositionOffset);
        for (Map.Entry<Integer, Slot> entry : slotMap.entrySet()) {
            Rectangle2D slotBoundingViewRect = entry.getValue().getSlotBoundingRect(view.getBoundingRect(), isNegative);
            final List<CompositionGlyph> glyphsInView = entry.getValue().getGlyphsInView(view);
            if (!glyphsInView.isEmpty()) {
                experimentalOptimizedRender(glyphsInView, gc, view, slotBoundingViewRect, isSummaryRow(entry));
            }
        }
        gc.restore();
    }

    private boolean isSummaryRow(Map.Entry<Integer, Slot> entry) {
        return entry.getKey() >= stackHeight - 1;
    }

    private void experimentalOptimizedRender(List<CompositionGlyph> glyphsInView, GraphicsContext gc, View view, Rectangle2D slotBoundingRect, boolean isSummaryRow) {
        double xPixelsPerCoordinate = view.getBoundingRect().getWidth() / view.getCanvasContext().getBoundingRect().getWidth();
        double modelCoordinatesPerScreenXPixel = view.getBoundingRect().getWidth() / view.getCanvasContext().getBoundingRect().getWidth();
        //combine nearby rectangles to optimize rendering... assuming this will be less expensive, needs testing
        if (xPixelsPerCoordinate < 10_000) {
            glyphsInView.stream().forEach(glyph -> glyph.draw(gc, view, slotBoundingRect, isSummaryRow));
        } else {
            for (int i = 0; i < glyphsInView.size();) {
                CompositionGlyph glyph = glyphsInView.get(i);
                boolean isSelected = glyph.isSelected();
                Rectangle.Double drawRect = glyph.calculateDrawRect(view, slotBoundingRect).orElse(null);
                if (drawRect != null) {
                    SCRATCH_RECT.setRect(drawRect);
                    if (SCRATCH_RECT.getWidth() / xPixelsPerCoordinate > 10) {
                        glyph.draw(gc, view, slotBoundingRect);
                    } else {
                        if (drawRect.width < modelCoordinatesPerScreenXPixel) {
                            drawRect.setRect(drawRect.x, drawRect.y, modelCoordinatesPerScreenXPixel, drawRect.height);
                        }
                        double maxX = SCRATCH_RECT.getMaxX();
                        while (i + 1 < glyphsInView.size()) {
                            final CompositionGlyph nextGlyph = glyphsInView.get(i + 1);
                            isSelected = isSelected | nextGlyph.isSelected();
                            Rectangle.Double nextRenderRect = nextGlyph.calculateDrawRect(view, slotBoundingRect).orElse(null);
                            if (nextRenderRect != null && nextRenderRect.width < modelCoordinatesPerScreenXPixel) {
                                nextRenderRect.setRect(nextRenderRect.x, nextRenderRect.y, modelCoordinatesPerScreenXPixel, nextRenderRect.height);
                            }
                            if (nextRenderRect != null && nextRenderRect.getMinX() / xPixelsPerCoordinate < (SCRATCH_RECT.getMaxX() / xPixelsPerCoordinate) + 1) {
                                maxX = nextRenderRect.getMaxX();
                            } else {
                                SCRATCH_RECT.setRect(SCRATCH_RECT.getMinX(), SCRATCH_RECT.getMinY(), maxX - SCRATCH_RECT.getMinX(), SCRATCH_RECT.getHeight());
                                DrawUtils.scaleToVisibleRec(view, SCRATCH_RECT);
                                glyph.drawSummaryRectangle(gc, SCRATCH_RECT);
                                if (isSelected) {
                                    glyph.drawSummarySelectionRectangle(gc, view, SCRATCH_RECT);
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
            return DefaultSlotPacker.packTopToBottom(slot);
        } else {
            return DefaultSlotPacker.packBottomToTop(slot, modelHeight);
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
        modelHeight = SLOT_HEIGHT * slotCount;
        slotMap.entrySet().forEach(entry -> entry.getValue().setSlotYoffset(getSlotOffset(entry.getKey())));

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

    public DataSet getDataSet() {
        return dataSet;
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

    public void clearGlyphs() {
        glyphs.clear();
        slotMap.clear();
    }

    public void clearSelections() {
        glyphs.stream()
                .forEach(glyph -> {
                    glyph.setIsSelected(false);
                    for (Glyph g : glyph.getChildren()) {
                        g.setIsSelected(false);
                    }
                });
    }

    public int getStackHeight() {
        return stackHeight;
    }

    @Override
    public double getModelHeight() {
        return modelHeight;
    }

    @Override
    public void processSelectionRectangle(Rectangle2D selectionRectangle, View view) {
        Rectangle2D viewBoundingRect = view.getBoundingRect();
        List<CompositionGlyph> selections = getSlotMap().entrySet().stream().flatMap(entry -> {
            double slotOffset = entry.getValue().getSlotYoffset();
            final Range<Double> mouseEventXrange = Range.closed(selectionRectangle.getMinX(), selectionRectangle.getMaxX());
            final Stream<CompositionGlyph> glyphsInXRange = entry.getValue().getGlyphsInXrange(mouseEventXrange).stream();

            Rectangle2D slotBoundingViewRect = entry.getValue().getSlotBoundingRect(viewBoundingRect, isNegative);
            return glyphsInXRange.filter(glyph -> {
                final Range<Double> mouseEventYrange = Range.closed(selectionRectangle.getMinY(), selectionRectangle.getMaxY());
                final Rectangle.Double glyphViewRect = glyph.calculateDrawRect(view, slotBoundingViewRect).orElse(null);
                if (glyphViewRect == null) {
                    return false;
                }
                return Range.closed(glyphViewRect.getMinY(), glyphViewRect.getMaxY()).isConnected(mouseEventYrange);
            });
        }).collect(toList());
//
        if (selections.size() > 1) {
            selections.forEach(glyph -> glyph.setIsSelected(true));
        } else {
            selections.forEach(glyph -> {
                boolean subSelectionActive = false;
                for (Glyph g : glyph.getChildren()) {
                    if (g.isSelectable()) {
                        if (g.getBoundingRect().intersects(selectionRectangle)) {
                            g.setIsSelected(true);
                            subSelectionActive = true;
                            break;
                        }
                    }
                }
                glyph.setIsSelected(true);//set this flag regardless of subselection 
            });
        }
    }

    @Override
    public List<CompositionGlyph> getSelectedGlyphs() {
        List<CompositionGlyph> selectedGlyphs = Lists.newArrayList();
        slotMap.entrySet().iterator().forEachRemaining(entry -> {
            entry.getValue().getAllGlyphs().iterator().forEachRemaining(glyph -> {
                if (glyph.isSelected()) {
                    selectedGlyphs.add(glyph);
                }
            });
        });
        return selectedGlyphs;
    }

    @Override
    public List<CompositionGlyph> getGlyphsInView(View view) {
        return slotMap.entrySet().stream().flatMap(slot -> slot.getValue().getGlyphsInView(view).stream()).collect(toList());
    }

    @Override
    public void addGlyphs(Collection<CompositionGlyph> glyphs) {
        this.glyphs.addAll(glyphs);
        slotMap.clear();
        if (!glyphs.isEmpty()) {
            buildSlots();
        }
    }

    @Override
    public BooleanProperty isHeightLocked() {
        return isHeightLocked;
    }

    @Override
    public boolean allowLockToggle() {
        return true;
    }

}
