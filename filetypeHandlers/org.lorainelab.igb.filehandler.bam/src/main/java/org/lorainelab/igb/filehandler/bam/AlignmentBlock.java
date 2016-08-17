package org.lorainelab.igb.filehandler.bam;

import com.google.common.collect.Range;

/**
 *
 * @author dcnorris
 */
public class AlignmentBlock {

    private final Range<Integer> range;
    private final AlignmentType alignmentType;
    private final String seqString;
    private final boolean exactRefMatch;

    public enum AlignmentType {
        INSERTION,
        DELETION,
        MATCH,
        GAP,
        PADDING
    }

    public AlignmentBlock(Range<Integer> range, AlignmentType alignmentType, boolean exactRefMatch, String seqString) {
        this.range = range;
        this.alignmentType = alignmentType;
        this.seqString = seqString;
        this.exactRefMatch = exactRefMatch;
    }

    public Range<Integer> getRange() {
        return range;
    }

    public AlignmentType getAlignmentType() {
        return alignmentType;
    }

    public String getSeqString() {
        return seqString;
    }

    public boolean isExactRefMatch() {
        return exactRefMatch;
    }

}
