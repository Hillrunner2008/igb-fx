package org.lorainelab.igb.data.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javafx.geometry.Rectangle2D;
import org.lorainelab.igb.data.model.glyph.CompositionGlyph;
import static org.lorainelab.igb.data.model.glyph.Glyph.SLOT_HEIGHT;

/**
 *
 * @author dcnorris
 */
public class Slot {

    private RangeMap<Double, CompositionGlyph> glyphs;
    private double slotYoffset;
    private double maxX;

    public Slot() {
        slotYoffset = 0;
        maxX = -1;
        glyphs = TreeRangeMap.create();
    }

    public double getSlotYoffset() {
        return slotYoffset;
    }

    public void setSlotYoffset(double slotYoffset) {
        this.slotYoffset = slotYoffset;
    }

    public double getMaxX() {
        return maxX;
    }

    public void addGlyph(CompositionGlyph glyph) {
        glyphs.put(Range.closed(glyph.getBoundingRect().getMinX(), glyph.getBoundingRect().getMaxX()), glyph);
        maxX = glyph.getBoundingRect().getMaxX();
    }

    //subRangeMap returns in O(1) time, and the RangeMap it returns has O(log n) additive cost for each of its query operations 
    //that is, all of its operations still take O(log n), just with a higher constant factor.
    public List<CompositionGlyph> getGlyphsInXrange(Range<Double> queryRange) {
        return Lists.newArrayList(glyphs.subRangeMap(queryRange).asMapOfRanges().values());
    }

    //complexity is O(log n) in x dimension, but linear in y
    //we should be able to generalize about y ranges based on their slot, 
    //so it should be possible to eliminate based on row and not filter using slow rectangle intersection math in y dimension inside this 
    public List<CompositionGlyph> getGlyphsInView(View view) {
        if (isSlotInView(view)) {
            return getGlyphsInXrange(view.getXrange());
        } else {
            return Collections.EMPTY_LIST;
        }
    }

    public Collection<CompositionGlyph> getAllGlyphs() {
        return glyphs.asMapOfRanges().values();
    }

    public Rectangle2D getSlotBoundingViewRect(View view) {
        Rectangle2D viewBoundingRect = view.getBoundingRect();
        Range<Double> viewYRange = Range.closed(viewBoundingRect.getMinY(), viewBoundingRect.getMaxY());
        final Range<Double> slotYrange = Range.closed(slotYoffset, SLOT_HEIGHT + slotYoffset);
        if (slotYrange.isConnected(viewYRange)) {
            Range<Double> intersection = slotYrange.intersection(viewYRange);
            return new Rectangle2D(viewBoundingRect.getMinX(), intersection.lowerEndpoint(), viewBoundingRect.getWidth(), intersection.upperEndpoint()-intersection.lowerEndpoint());
        } else {
            return Rectangle2D.EMPTY;
        }
    }

    private boolean isSlotInView(View view) {
        Rectangle2D viewBoundingRect = view.getBoundingRect();
        Range<Double> viewYRange = Range.closed(viewBoundingRect.getMinY(), viewBoundingRect.getMaxY());
        return Range.closed(slotYoffset, SLOT_HEIGHT + slotYoffset).isConnected(viewYRange);
    }
}
