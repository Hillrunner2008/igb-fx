package org.lorainelab.igb.data.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.collect.TreeMultimap;
import java.util.List;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import org.lorainelab.igb.data.model.glyph.CompositionGlyph;
import org.lorainelab.igb.data.model.glyph.Glyph;
import static org.lorainelab.igb.data.model.glyph.Glyph.MIN_X_COMPARATOR;
import static org.lorainelab.igb.data.model.glyph.Glyph.MODEL_HEIGHT_PADDING;
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
    private double modelHeight;
    private int stackHeight;

    public Track(boolean isNegative, String trackLabel, int stackHeight) {
        this.isNegative = isNegative;
        this.trackLabel = trackLabel;
        this.stackHeight = stackHeight;
        this.modelHeight = (SLOT_HEIGHT * stackHeight) + MODEL_HEIGHT_PADDING;
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

    private void incrementCompositionGlyphSlot(CompositionGlyph compositionGlyph, int slot) {
        for (Glyph glyph : compositionGlyph.getChildren()) {
            Rectangle2D boundingRect = glyph.getBoundingRect();
            if (isNegative) {
                glyph.setRenderBoundingRect(
                        new Rectangle2D(
                                boundingRect.getMinX(),
                                boundingRect.getMinY() + (slot * SLOT_HEIGHT),
                                boundingRect.getWidth(),
                                boundingRect.getHeight())
                );
            } else {
                glyph.setRenderBoundingRect(
                        new Rectangle2D(
                                boundingRect.getMinX(),
                                boundingRect.getMinY() + ((stackHeight-1) - slot) * SLOT_HEIGHT,
                                boundingRect.getWidth(),
                                boundingRect.getHeight())
                );
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
                            if (slotToadd > stackHeight) {
                                break;
                            }
                        }
                    }
                    if (slotToadd <= stackHeight) {
                        incrementCompositionGlyphSlot(glyph, slotToadd);
                        slotMap.put(slotToadd, glyph);
                    }
                });
    }

    public TreeMultimap<Integer, CompositionGlyph> getSlotMap() {
        return slotMap;
    }

    public String getTrackLabel() {
        return trackLabel;
    }

    public double getModelHeight() {
        return modelHeight;
    }

    public void setMaxStackHeight(int maxStackHeight) {
        this.stackHeight = maxStackHeight;
        this.modelHeight = (SLOT_HEIGHT * maxStackHeight) + MODEL_HEIGHT_PADDING;
        buildSlots();
    }

    void clearGlyphs() {
        glyphs.clear();
        slotMap.clear();
    }

}
