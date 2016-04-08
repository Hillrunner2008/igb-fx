package org.lorainelab.igb.visualization.model;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.collect.TreeMultimap;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;

/**
 *
 * @author dcnorris
 */
public class Track {

    public static final int SLOT_HEIGHT = 30;
    public static final double MODEL_HEIGHT_PADDING = 17.5;
    private final String trackLabel;
    private List<CompositionGlyph> glyphs;
    private TreeMultimap<Integer, CompositionGlyph> slotMap;
    private static Comparator<CompositionGlyph> byMinX
            = (glyph1, glyph2) -> {
                return ComparisonChain.start()
                .compare(glyph1.getBoundingRect().getMinX(), glyph2.getBoundingRect().getMinX())
                .compare(glyph1.getBoundingRect().getWidth(), glyph2.getBoundingRect().getWidth())
                .compare(glyph1.getLabel(), glyph2.getLabel())
                .result();
            };
    private final boolean isNegative;
    private double modelHeight;
    private int maxStackHeight;

    public Track(boolean isNegative, String trackLabel, int stackHeight) {
        this.isNegative = isNegative;
        this.trackLabel = trackLabel;
        this.maxStackHeight = stackHeight;
        this.modelHeight = (Track.SLOT_HEIGHT * stackHeight) + MODEL_HEIGHT_PADDING;
        slotMap = TreeMultimap.create(Ordering.natural(), byMinX);
        glyphs = Lists.newArrayList();
    }

    public List<CompositionGlyph> getGlyphs() {
        return glyphs;
    }

    public void draw(GraphicsContext gc, View view, CanvasContext canvasContext) {
        final double additionalYOffset = canvasContext.getBoundingRect().getMinY() / view.getYfactor();
        slotMap.asMap().values().stream().flatMap(glyphs -> glyphs.stream())
                .filter(glyph -> view.getBoundingRect().intersects(glyph.getBoundingRect()))
                .forEach(glyph -> glyph.draw(gc, view, additionalYOffset));
    }

    private void incrementCompositionGlyphSlot(CompositionGlyph compositionGlyph, int slot) {
        compositionGlyph.getChildren().stream().forEach(glyph -> {
            Rectangle2D boundingRect = glyph.getBoundingRect();
            if (!isNegative) {
                glyph.setBoundingRect(
                        new Rectangle2D(
                                boundingRect.getMinX(),
                                boundingRect.getMinY() + (maxStackHeight - slot) * SLOT_HEIGHT,
                                boundingRect.getWidth(),
                                boundingRect.getHeight())
                );
            } else {
                glyph.setBoundingRect(
                        new Rectangle2D(
                                boundingRect.getMinX(),
                                boundingRect.getMinY() + (slot * SLOT_HEIGHT),
                                boundingRect.getWidth(),
                                boundingRect.getHeight())
                );
            }
        });
    }

    public void buildSlots() {
        Collections.sort(glyphs, byMinX);
        for (CompositionGlyph glyph : glyphs) {
            int slotToadd = 0;
            for (Integer key : slotMap.keySet()) {
                if (glyph.getBoundingRect().getMinX() > slotMap.get(key).last().getBoundingRect().getMaxX()) {
                    break;
                } else {
                    slotToadd++;
                    if (slotToadd > maxStackHeight) {
                        break;
                    }
                }
            }
            if (slotToadd <= maxStackHeight) {
                incrementCompositionGlyphSlot(glyph, slotToadd);
                slotMap.put(slotToadd, glyph);
            }
        }
    }

    public String getTrackLabel() {
        return trackLabel;
    }

    public double getModelHeight() {
        return modelHeight;
    }

    public void setMaxStackHeight(int maxStackHeight) {
        this.maxStackHeight = maxStackHeight;
        this.modelHeight = (Track.SLOT_HEIGHT * maxStackHeight) + MODEL_HEIGHT_PADDING;
        buildSlots();
    }

}
