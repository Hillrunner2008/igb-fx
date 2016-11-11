package org.lorainelab.igb.data.model;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
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
import static org.lorainelab.igb.data.model.util.Palette.DEFAULT_GLYPH_FILL;
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
    private List<Slot> slots;

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
        slots = Lists.newCopyOnWriteArrayList();
        glyphs = Lists.newCopyOnWriteArrayList();

    }

    @Override
    public void draw(GraphicsContext gc, View view, CanvasContext canvasContext) {
        double trackPositionOffset = canvasContext.getBoundingRect().getMinY() / view.getYfactor();
        gc.save();
        //NOTE: Rounding issues prevent us from using translation to take care of view offsets in the x coordinate system
        // everything works fine until x coordinate is large (e.g. > 10_000_000), and then the larger numbers don't render correctly on the canvas
        //i.e. we can't do this gc.translate(-view.getBoundingRect().getMinX(), trackPositionOffset);
        gc.translate(0, trackPositionOffset);

        for (int slotPosition = 0; slotPosition < slots.size(); slotPosition++) {
            Slot slot = slots.get(slotPosition);
            Rectangle2D slotBoundingViewRect = slot.getSlotBoundingRect(view.modelCoordRect(), isNegative);
            final List<CompositionGlyph> glyphsInView = slot.getGlyphsInView(view);
            if (!glyphsInView.isEmpty()) {
                try {
                    experimentalOptimizedRender(glyphsInView, gc, view, slotBoundingViewRect, isSummaryRow(slotPosition));
                } catch (Exception ex) {
                    LOG.error(ex.getMessage(), ex);
                }
            }
        }
        gc.restore();
    }

    private boolean isSummaryRow(int slotPosition) {
        return slotPosition >= stackHeight - 1;
    }

    private void experimentalOptimizedRender(List<CompositionGlyph> glyphsInView, GraphicsContext gc, View view, Rectangle2D slotBoundingRect, boolean isSummaryRow) {
        double xPixelsPerCoordinate = view.modelCoordRect().getWidth() / view.getCanvasContext().getBoundingRect().getWidth();
        double modelCoordinatesPerScreenXPixel = view.modelCoordRect().getWidth() / view.getCanvasContext().getBoundingRect().getWidth();
        //combine nearby rectangles to optimize rendering... assuming this will be less expensive, needs testing
        if (xPixelsPerCoordinate < 1_000) {
            glyphsInView.stream().forEach(glyph -> glyph.draw(gc, view, slotBoundingRect.getMinY(), isSummaryRow));
        } else {
            gc.beginPath();
            gc.setFill(DEFAULT_GLYPH_FILL.get());
            for (int i = 0; i < glyphsInView.size();) {
                CompositionGlyph glyph = glyphsInView.get(i);
                boolean isSelected = glyph.isSelected();
                final double slotMinY = slotBoundingRect.getMinY();
                Rectangle.Double drawRect = glyph.calculateDrawRect(view, slotMinY).orElse(null);
                if (drawRect != null) {
                    SCRATCH_RECT.setRect(drawRect);
                    if (widthLessThanPixel(drawRect, modelCoordinatesPerScreenXPixel)) {
                        drawRect.setRect(drawRect.x, drawRect.y, modelCoordinatesPerScreenXPixel, drawRect.height);
                    }
                    double maxX = SCRATCH_RECT.getMaxX();
                    while (i + 1 < glyphsInView.size()) {
                        final CompositionGlyph nextGlyph = glyphsInView.get(i + 1);
                        Rectangle2D nextRenderRect = nextGlyph.getBoundingRect();
                        if ((nextRenderRect.getMinX() - view.getMutableCoordRect().getMinX()) / xPixelsPerCoordinate < (SCRATCH_RECT.getMaxX() / xPixelsPerCoordinate) + 1) {
                            isSelected = isSelected | nextGlyph.isSelected();
                            maxX = Math.min(nextRenderRect.getMaxX(), view.getMutableCoordRect().getMaxX()) - view.getMutableCoordRect().getMinX();
                        } else {
                            SCRATCH_RECT.setRect(SCRATCH_RECT.getMinX(), SCRATCH_RECT.getMinY(), maxX - SCRATCH_RECT.getMinX(), SCRATCH_RECT.getHeight());
                            DrawUtils.scaleToVisibleRec(view, SCRATCH_RECT);
                            java.awt.geom.Rectangle2D.Double drawSummaryRect = glyph.getDrawSummaryRect(gc, SCRATCH_RECT, slotMinY);
                            gc.moveTo(drawSummaryRect.getMinX(), drawSummaryRect.y);
                            gc.lineTo(drawSummaryRect.getMaxX(), drawSummaryRect.y);
                            gc.lineTo(drawSummaryRect.getMaxX(), drawSummaryRect.getMaxY());
                            gc.lineTo(drawSummaryRect.getMinX(), drawSummaryRect.getMaxY());
                            gc.lineTo(drawSummaryRect.getMinX(), drawSummaryRect.getMinY());
                            if (isSelected) {
                                glyph.drawSummarySelectionRectangle(gc, view, SCRATCH_RECT, slotMinY);
                            }
                            break;
                        }
                        i++;
                    }
                }
                i++;
            }
            gc.fill();
        }
    }

    private static boolean widthLessThanPixel(java.awt.geom.Rectangle2D.Double drawRect, double modelCoordinatesPerScreenXPixel) {
        return drawRect.width < modelCoordinatesPerScreenXPixel;
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

    private void buildSlots() {
        final Stopwatch stopwatch = Stopwatch.createStarted();
        Collections.sort(glyphs, MIN_X_COMPARATOR);
        for (CompositionGlyph glyph : glyphs) {
            int slotPosition = 0;
            for (int i = 0; i < slots.size(); i++) {
                double max = ((Slot) slots.get(i)).getMaxX();
                if (glyph.getBoundingRect().getMinX() > max) {
                    break;
                } else {
                    slotPosition++;
                    if (slotPosition >= stackHeight && isLimitedStackHeight()) {
                        break;
                    }
                }
            }
            if (slotPosition <= stackHeight || stackHeight == 0) {
                if (slots.isEmpty()) {
                    Slot row = new Slot();
                    row.addGlyph(glyph);
                    slots.add(row);
                } else if (slotPosition < slots.size() - 1) {
                    Slot row = slots.get(slotPosition);
                    row.addGlyph(glyph);
                } else {
                    Slot row = new Slot();
                    row.addGlyph(glyph);
                    slots.add(row);
                }
            }
        }
        LOG.info("STOPWATCH METRICS for buildSlots {}", stopwatch.stop());

        updateSlotCount();
        modelHeight = SLOT_HEIGHT * slotCount;

        for (int slotPosition = 0; slotPosition < slots.size(); slotPosition++) {
            Slot row = slots.get(slotPosition);
            row.setSlotYoffset(getSlotOffset(slotPosition));
        }
    }

    private void updateSlotCount() {
        int max = slots.size();
        slotCount = max;
        if (isLimitedStackHeight()) {
            if (stackHeight < slotCount) {
                slotCount = stackHeight;
            }
        }
    }

    private boolean isLimitedStackHeight() {
        return stackHeight != 0;
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
            slots.clear();
            buildSlots();
        }
    }

    public void clearGlyphs() {
        glyphs.clear();
        slots.clear();
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
        Rectangle2D viewBoundingRect = view.modelCoordRect();

        List<CompositionGlyph> selections = new ArrayList<>();
        for (int slotPosition = 0; slotPosition < slots.size(); slotPosition++) {
            Slot slot = slots.get(slotPosition);
            final Range<Double> mouseEventXrange = Range.closed(selectionRectangle.getMinX(), selectionRectangle.getMaxX());
            final Stream<CompositionGlyph> glyphsInXRange = slot.getGlyphsInXrange(mouseEventXrange).stream();
            Rectangle2D slotBoundingViewRect = slot.getSlotBoundingRect(viewBoundingRect, isNegative);
            glyphsInXRange.filter(glyph -> {
                final Range<Double> mouseEventYrange = Range.closed(selectionRectangle.getMinY(), selectionRectangle.getMaxY());
                final Rectangle.Double glyphViewRect = glyph.calculateDrawRect(view, slotBoundingViewRect.getMinY()).orElse(null);
                if (glyphViewRect == null) {
                    return false;
                }
                final double minY = glyphViewRect.getMinY() * view.getYfactor();
                final double maxY = glyphViewRect.getMaxY() * view.getYfactor();
                return Range.closed(minY, maxY).isConnected(mouseEventYrange);
            }).forEach(glyph -> selections.add(glyph));
        }

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
        return glyphs.stream().filter(g -> g.isSelected()).collect(toList());
    }

    @Override
    public List<CompositionGlyph> getGlyphsInView(View view) {
        List<CompositionGlyph> inView = Lists.newArrayList();
        for (int slotPosition = 0; slotPosition < slots.size(); slotPosition++) {
            Slot slot = slots.get(slotPosition);
            slot.getGlyphsInView(view).forEach(inView::add);
        }
        return inView;
    }

    @Override
    public void addGlyphs(Collection<CompositionGlyph> glyphs) {
        this.glyphs.addAll(glyphs);
        slots.clear();
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
