package org.lorainelab.igb.data.model;

import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;
import java.util.Collection;
import java.util.Set;
import static java.util.stream.Collectors.toSet;
import org.lorainelab.igb.data.model.glyph.CompositionGlyph;

/**
 *
 * @author dcnorris
 */
public class Slot {

    private RangeMap<Double, CompositionGlyph> glyphs;
    private double maxX;

    public Slot() {
        maxX = -1;
        glyphs = TreeRangeMap.create();
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
    private Collection<CompositionGlyph> getGlyphsInXrange(Range<Double> queryRange) {
        return glyphs.subRangeMap(queryRange).asMapOfRanges().values();
    }

    //complexity is O(log n) in x dimension, but linear in y
    //we should be able to generalize about y ranges based on their slot, 
    //so it should be possible to eliminate based on row and not filter using slow rectangle intersection math in y dimension inside this 
    public Set<CompositionGlyph> getGlyphsInView(View view) {
        final Collection<CompositionGlyph> glyphsInXRange = getGlyphsInXrange(view.getXrange());
        return glyphsInXRange.stream()
//                .filter(glyph -> view.getBoundingRect().intersects(glyph.getRenderBoundingRect()))
                .collect(toSet());

    }

    public Collection<CompositionGlyph> getAllGlyphs() {
        return glyphs.asMapOfRanges().values();
    }
}
