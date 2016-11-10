package org.lorainelab.igb.data.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import gnu.trove.procedure.TIntProcedure;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;
import static java.util.stream.Collectors.toList;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import net.sf.jsi.SpatialIndex;
import net.sf.jsi.rtree.RTree;
import org.lorainelab.igb.data.model.glyph.CompositionGlyph;
import org.lorainelab.igb.data.model.glyph.Glyph;
import static org.lorainelab.igb.data.model.glyph.Glyph.MIN_X_COMPARATOR;
import static org.lorainelab.igb.data.model.glyph.Glyph.SLOT_HEIGHT;
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
    private SpatialIndex si;
    private final boolean isNegative;
    private double modelHeight;
    private int stackHeight;
    private int slotCount;
    private final DataSet dataSet;
    private BooleanProperty isHeightLocked;
    private double lockedHeight;
    private TreeMap<Integer, Integer> slotMaxXReference;

    public StackedGlyphTrack(boolean isNegative, String trackLabel, int stackHeight, DataSet dataSet) {
        slotMaxXReference = Maps.newTreeMap();
        this.lockedHeight = 25;
        this.isHeightLocked = new SimpleBooleanProperty(false);
        this.dataSet = dataSet;
        this.isNegative = isNegative;
        this.trackLabel = trackLabel;
        this.stackHeight = Math.max(stackHeight, 0);
        //handles case when stackHeight is set to 0 i.e. unlimited
        this.modelHeight = Math.max(SLOT_HEIGHT * stackHeight, SLOT_HEIGHT);
        glyphs = Lists.newArrayList();
        si = new RTree();
        si.init(null);
    }

    @Override
    public void draw(GraphicsContext gc, View view, CanvasContext canvasContext) {
        Rectangle2D modelCoordRect = view.modelCoordRect();
        double trackPositionOffset = canvasContext.getBoundingRect().getMinY() / view.getYfactor();
        gc.save();
        //NOTE: Rounding issues prevent us from using translation to take care of view offsets in the x coordinate system
        // everything works fine until x coordinate is large (e.g. > 10_000_000), and then the larger numbers don't render correctly on the canvas
        //i.e. we can't do this gc.translate(-view.getBoundingRect().getMinX(), trackPositionOffset);
        gc.translate(0, trackPositionOffset);
        SaveToListProcedure toListProcedure = new SaveToListProcedure();
        net.sf.jsi.Rectangle viewBounds = toJsiRect(modelCoordRect);
        si.intersects(viewBounds, toListProcedure);
        List<Integer> idsInView = toListProcedure.getIds();
        List<CompositionGlyph> glyphToDraw = idsInView.stream().map(id -> glyphs.get(id)).collect(toList());
        //TODO implement this again since it is no longer possible to assume glyphs are in the same row in this method
//        experimentalOptimizedRender(glyphToDraw, gc, view, isNegative);
        glyphToDraw.stream().forEach(glyph -> glyph.draw(gc, view, getSlotOffset(glyph.getRow()), isSummaryRow(glyph.getRow())));
        gc.restore();
    }

    private net.sf.jsi.Rectangle toJsiRect(Rectangle2D modelCoordRect) {
        final net.sf.jsi.Rectangle viewBounds = new net.sf.jsi.Rectangle((float) modelCoordRect.getMinX(), (float) modelCoordRect.getMinY(), (float) modelCoordRect.getMaxX(), (float) modelCoordRect.getMaxY());
        return viewBounds;
    }

//    private void experimentalOptimizedRender(List<CompositionGlyph> glyphsInView, GraphicsContext gc, View view, boolean isSummaryRow) {
//        double xPixelsPerCoordinate = view.modelCoordRect().getWidth() / view.getCanvasContext().getBoundingRect().getWidth();
//        double modelCoordinatesPerScreenXPixel = view.modelCoordRect().getWidth() / view.getCanvasContext().getBoundingRect().getWidth();
//        //combine nearby rectangles to optimize rendering... assuming this will be less expensive, needs testing
//        if (xPixelsPerCoordinate < 10_000) {
//            glyphsInView.stream().forEach(glyph -> glyph.draw(gc, view, getSlotOffset(glyph.getRow()), isSummaryRow));
//        } else {
//            for (int i = 0; i < glyphsInView.size();) {
//                CompositionGlyph glyph = glyphsInView.get(i);
//                boolean isSelected = glyph.isSelected();
//                Rectangle.Double drawRect = glyph.calculateDrawRect(view, getSlotOffset(glyph.getRow())).orElse(null);
//                if (drawRect != null) {
//                    SCRATCH_RECT.setRect(drawRect);
//                    if (SCRATCH_RECT.getWidth() / xPixelsPerCoordinate > 10) {
//                        try {
//                            glyph.draw(gc, view, getSlotOffset(glyph.getRow()));
//                        } catch (Exception ex) {
//                            LOG.error(ex.getMessage(), ex);
//                        }
//                    } else {
//                        if (drawRect.width < modelCoordinatesPerScreenXPixel) {
//                            drawRect.setRect(drawRect.x, drawRect.y, modelCoordinatesPerScreenXPixel, drawRect.height);
//                        }
//                        double maxX = SCRATCH_RECT.getMaxX();
//                        while (i + 1 < glyphsInView.size()) {
//                            final CompositionGlyph nextGlyph = glyphsInView.get(i + 1);
//                            Rectangle.Double nextRenderRect = nextGlyph.calculateDrawRect(view, slotMinY).orElse(null);
//                            if (nextRenderRect != null && nextRenderRect.width < modelCoordinatesPerScreenXPixel) {
//                                nextRenderRect.setRect(nextRenderRect.x, nextRenderRect.y, modelCoordinatesPerScreenXPixel, nextRenderRect.height);
//                            }
//                            if (nextRenderRect != null && nextRenderRect.getMinX() / xPixelsPerCoordinate < (SCRATCH_RECT.getMaxX() / xPixelsPerCoordinate) + 1) {
//                                isSelected = isSelected | nextGlyph.isSelected();
//                                maxX = nextRenderRect.getMaxX();
//                            } else {
//                                SCRATCH_RECT.setRect(SCRATCH_RECT.getMinX(), SCRATCH_RECT.getMinY(), maxX - SCRATCH_RECT.getMinX(), SCRATCH_RECT.getHeight());
//                                DrawUtils.scaleToVisibleRec(view, SCRATCH_RECT);
//                                glyph.drawSummaryRectangle(gc, SCRATCH_RECT, slotMinY);
//                                if (isSelected) {
//                                    glyph.drawSummarySelectionRectangle(gc, view, SCRATCH_RECT, slotMinY);
//                                }
//                                break;
//                            }
//                            i++;
//                        }
//                    }
//                }
//                i++;
//            }
//        }
//    }
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
        si = new RTree();
        si.init(null);
        slotMaxXReference.clear();
        glyphs.stream()
                .sorted(MIN_X_COMPARATOR)
                .forEachOrdered(glyph -> {
                    int slotPosition = 0;
                    for (int key : slotMaxXReference.keySet()) {
                        int maxX = slotMaxXReference.get(key);
                        if (glyph.getBoundingRect().getMinX() > maxX) {
                            break;
                        } else {
                            slotPosition++;
                            if (slotPosition >= stackHeight && isLimitedStackHeight()) {
                                break;
                            }
                        }
                    }
                    if (slotPosition <= stackHeight || stackHeight == 0) {
                        slotMaxXReference.put(slotPosition, (int) glyph.getBoundingRect().getMaxX());
                        glyph.setRow(slotPosition);
                    }
                });
        updateSlotCount();
        modelHeight = SLOT_HEIGHT * slotCount;

        for (int i = 0; i < glyphs.size(); i++) {
            CompositionGlyph cg = glyphs.get(i);
            Rectangle2D boundingRect = cg.getBoundingRect();
            final float minY = (float) getSlotOffset(cg.getRow());
            final float maxY = minY + (float) boundingRect.getHeight();
            final net.sf.jsi.Rectangle rowAdjustedBounds = new net.sf.jsi.Rectangle((float) boundingRect.getMinX(), minY, (float) boundingRect.getMaxX(), maxY);
            si.add(rowAdjustedBounds, i);
        }
    }

    private boolean isSummaryRow(int row) {
        return row >= stackHeight - 1;
    }

    private void updateSlotCount() {
        if (!slotMaxXReference.isEmpty()) {
            slotCount = slotMaxXReference.lastKey();
            if (isLimitedStackHeight()) {
                if (stackHeight < slotCount) {
                    slotCount = stackHeight;
                }
            }
        } else {
            slotCount = 0;
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
            buildSlots();
        }
    }

    public void clearGlyphs() {
        glyphs.clear();
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
        final SaveToListProcedure saveToListProcedure = new SaveToListProcedure();
        si.intersects(toJsiRect(viewBoundingRect), saveToListProcedure);
        saveToListProcedure.getIds().stream().map(id -> glyphs.get(id)).forEach(selections::add);

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
        return glyphs.stream().filter(cg -> cg.isSelected()).collect(toList());
    }

    @Override
    public List<CompositionGlyph> getGlyphsInView(View view) {
        List<CompositionGlyph> inView = Lists.newArrayList();
        final SaveToListProcedure saveToListProcedure = new SaveToListProcedure();
        si.intersects(toJsiRect(view.modelCoordRect()), saveToListProcedure);
        saveToListProcedure.getIds().stream().map(id -> glyphs.get(id)).forEach(inView::add);
        return inView;
    }

    @Override
    public void addGlyphs(Collection<CompositionGlyph> glyphs) {
        this.glyphs.addAll(glyphs);
        buildSlots();
    }

    @Override
    public BooleanProperty isHeightLocked() {
        return isHeightLocked;
    }

    @Override
    public boolean allowLockToggle() {
        return true;
    }

    class SaveToListProcedure implements TIntProcedure {

        private List<Integer> ids = new ArrayList<Integer>();

        public boolean execute(int id) {
            ids.add(id);
            return true;
        }

        private List<Integer> getIds() {
            return ids;
        }
    };
}
