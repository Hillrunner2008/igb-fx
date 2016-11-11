package org.lorainelab.igb.data.model;

import cern.colt.list.IntArrayList;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import java.util.ArrayList;
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

    private List<CompositionGlyph> glyphs;
    private IntArrayList x = new IntArrayList();
    private double slotYoffset;
    private double maxX;

    public Slot() {
        slotYoffset = 0;
        maxX = -1;
        glyphs = Lists.newArrayList();
    }

    public double getSlotYoffset() {
        return slotYoffset;
    }

    public void setSlotYoffset(double slotYoffset) {
        this.slotYoffset = slotYoffset;
        x.trimToSize();
        x.sort();
    }

    public double getMaxX() {
        return maxX;
    }

    public void addGlyph(CompositionGlyph glyph) {
        synchronized (glyphs) {
            glyphs.add(glyph);
            x.add((int) glyph.getBoundingRect().getMinX());
            maxX = glyph.getBoundingRect().getMaxX();
        }
    }

    public List<CompositionGlyph> getGlyphsInXrange(Range<Double> queryRange) {
        final List<CompositionGlyph> glyphsInRange = new ArrayList<>();
        synchronized (glyphs) {
            int startIndex = findStartIndex(queryRange.lowerEndpoint());
            int endIndex = findEndIndex(queryRange.upperEndpoint());
            glyphsInRange.addAll(glyphs.subList(startIndex, endIndex));
            if (!glyphsInRange.isEmpty()) {
                Range<Double> startIndexGlyphRange = Range.closed(glyphsInRange.get(0).getBoundingRect().getMinX(), glyphsInRange.get(0).getBoundingRect().getMaxX());
                if (!startIndexGlyphRange.isConnected(queryRange)) {
                    glyphsInRange.remove(0);
                }
                if (glyphsInRange.size() > 1) {
                    Range<Double> endIndexGlyphRange = Range.closed(glyphsInRange.get(glyphsInRange.size()).getBoundingRect().getMinX(), glyphsInRange.get(0).getBoundingRect().getMaxX());
                    if (!endIndexGlyphRange.isConnected(queryRange)) {
                        glyphsInRange.remove(glyphsInRange.size());
                    }
                }
            }
        }
        return glyphsInRange;
    }

    // no check for trimming first and last index to range is done, this is great for general rendering speed.
    public List<CompositionGlyph> getGlyphsInXrangeQuick(Range<Double> queryRange) {
        List<CompositionGlyph> subList = new ArrayList<>();
        synchronized (glyphs) {
            int startIndex = findStartIndex(queryRange.lowerEndpoint());
            int endIndex = findEndIndex(queryRange.upperEndpoint());
            subList.addAll(glyphs.subList(startIndex, endIndex));
        }
        return subList;
    }

    private int findStartIndex(double xmin) {
        int index = x.binarySearch((int) Math.floor(xmin));
        if (index >= 0) {
            return index;
        }
        index = Math.max(0, (-index - 2));
        return index;
    }

    private int findEndIndex(double xmax) {
        int index = x.binarySearch((int) Math.ceil(xmax));
        if (index >= 0) {
            return index;
        }
        index = -index - 1;
        index = Math.min(index, x.size() - 1);
        index = Math.max(0, index);
        return index;

    }

    //complexity is O(log n) in x dimension, but linear in y
    //we should be able to generalize about y ranges based on their slot, 
    //so it should be possible to eliminate based on row and not filter using slow rectangle intersection math in y dimension inside this 
    public List<CompositionGlyph> getGlyphsInView(View view) {
        if (isSlotInView(view)) {
            return getGlyphsInXrangeQuick(view.getXrange());
        } else {
            return Collections.EMPTY_LIST;
        }
    }

    public Collection<CompositionGlyph> getAllGlyphs() {
        return glyphs;
    }

    public Rectangle2D getSlotBoundingRect(Rectangle2D viewBoundingRect, boolean isNegative) {
        Range<Double> viewYRange = Range.closed(viewBoundingRect.getMinY(), viewBoundingRect.getMaxY());
        final Range<Double> slotYrange = Range.closed(slotYoffset, SLOT_HEIGHT + slotYoffset);
        if (slotYrange.isConnected(viewYRange)) {
            return new Rectangle2D(viewBoundingRect.getMinX(), slotYoffset, viewBoundingRect.getWidth(), SLOT_HEIGHT);
        } else {
            return Rectangle2D.EMPTY;
        }
    }

    private boolean isSlotInView(View view) {
        Rectangle2D viewBoundingRect = view.modelCoordRect();
        Range<Double> viewYRange = Range.closed(viewBoundingRect.getMinY(), viewBoundingRect.getMaxY());
        return Range.closed(slotYoffset, SLOT_HEIGHT + slotYoffset).isConnected(viewYRange);
    }
}
