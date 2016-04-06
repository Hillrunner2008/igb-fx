package org.lorainelab.igb.data.model.bam;

import com.google.common.collect.Sets;
import htsjdk.samtools.Cigar;
import htsjdk.samtools.CigarElement;
import htsjdk.samtools.SAMRecord;
import java.util.Optional;
import java.util.Set;
import org.lorainelab.igb.data.model.Feature;
import org.lorainelab.igb.data.model.Range;
import org.lorainelab.igb.data.model.Strand;
import org.lorainelab.igb.data.model.bam.AlignmentBlock.AlignmentType;

public class BamFeature implements Feature {

    private final Range range;
    private final Strand strand;
    private final SAMRecord samRecord;
    private final int alignmentStart;
    private final int alignmentEnd;

    public BamFeature(SAMRecord samRecord) {
        this.samRecord = samRecord;
        alignmentStart = samRecord.getAlignmentStart() - 1; // convert to interbase
        alignmentEnd = samRecord.getAlignmentEnd() - 1;
        if (!samRecord.getReadNegativeStrandFlag()) {
            strand = Strand.POSITIVE;
            range = new Range(alignmentStart, alignmentEnd);
        } else {
            strand = Strand.NEGATIVE;
            range = new Range(alignmentStart, alignmentEnd);
        }
    }

    @Override
    public Range getRange() {
        return range;
    }

    @Override
    public Strand getStrand() {
        return strand;
    }

    @Override
    public Optional<String> getId() {
        return Optional.ofNullable(samRecord.getReadName());
    }

    @Override
    public String getChromosomeId() {
        return samRecord.getReferenceName();
    }

    public String getReadSequence() {
        return samRecord.getReadString();
    }

    public int getMappingQuality() {
        return samRecord.getMappingQuality();
    }

    public String getCigarString() {
        return samRecord.getCigarString();
    }

    public String getAlignmentBlockSequence(AlignmentBlock alignmentBlock) {
        Range alignmentBlockRange = alignmentBlock.getRange();
        final int sequenceStartPos = alignmentBlockRange.getStart() - alignmentStart;
        final int sequenceEndPos = sequenceStartPos + alignmentBlockRange.getLength();
        return samRecord.getReadString().substring(sequenceStartPos, sequenceEndPos);
    }

    public Set<AlignmentBlock> getAnnotationBlocks() {
        Set<AlignmentBlock> blocks = Sets.newLinkedHashSet();
        Cigar cigar = samRecord.getCigar();
        int start = alignmentStart;
        for (CigarElement cigarElement : cigar.getCigarElements()) {
            switch (cigarElement.getOperator()) {
                case D:
                    blocks.add(new AlignmentBlock(new Range(start, start + cigarElement.getLength()), AlignmentType.DELETION));
                    start += cigarElement.getLength();
                    break;
                case I:
                    blocks.add(new AlignmentBlock(new Range(start, start + cigarElement.getLength()), AlignmentType.INSERTION));
                    break;
                case M:
                    blocks.add(new AlignmentBlock(new Range(start, start + cigarElement.getLength()), AlignmentType.MATCH));
                    start += cigarElement.getLength();
                    break;
                case N:
                    blocks.add(new AlignmentBlock(new Range(start, start + cigarElement.getLength()), AlignmentType.GAP));
                    start += cigarElement.getLength();
                    break;
                case P:
                    //?
                    break;
            }
        }
        return blocks;
    }

}
