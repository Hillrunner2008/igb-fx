package org.lorainelab.igb.data.model.bam;

import org.lorainelab.igb.data.model.Range;

/**
 *
 * @author dcnorris
 */
public class AlignmentBlock {

    private final Range range;
    private final AlignmentType alignmentType;

    public enum AlignmentType {
        INSERTION,
        DELETION,
        MATCH,
        GAP,
        PADDING
    }

    public AlignmentBlock(Range range, AlignmentType alignmentType) {
        this.range = range;
        this.alignmentType = alignmentType;
    }

    public Range getRange() {
        return range;
    }

    public AlignmentType getAlignmentType() {
        return alignmentType;
    }

}
