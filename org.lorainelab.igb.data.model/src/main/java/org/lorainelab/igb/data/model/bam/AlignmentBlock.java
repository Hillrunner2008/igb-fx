package org.lorainelab.igb.data.model.bam;

import com.google.common.collect.Range;

/**
 *
 * @author dcnorris
 */
public class AlignmentBlock {

    private final Range<Integer> range;
    private final AlignmentType alignmentType;

    public enum AlignmentType {
        INSERTION,
        DELETION,
        MATCH,
        GAP,
        PADDING
    }

    public AlignmentBlock(Range<Integer> range, AlignmentType alignmentType) {
        this.range = range;
        this.alignmentType = alignmentType;
    }

    public Range<Integer> getRange() {
        return range;
    }

    public AlignmentType getAlignmentType() {
        return alignmentType;
    }

}
