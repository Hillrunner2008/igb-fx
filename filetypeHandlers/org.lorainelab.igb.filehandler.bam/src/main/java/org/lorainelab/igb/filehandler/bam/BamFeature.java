package org.lorainelab.igb.filehandler.bam;

import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import htsjdk.samtools.Cigar;
import htsjdk.samtools.CigarElement;
import htsjdk.samtools.SAMRecord;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.lorainelab.igb.data.model.Feature;
import org.lorainelab.igb.data.model.Strand;
import org.lorainelab.igb.filehandler.bam.AlignmentBlock.AlignmentType;

public class BamFeature implements Feature {

    private final Range<Integer> range;
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
            range = Range.closedOpen(alignmentStart, alignmentEnd);
        } else {
            strand = Strand.NEGATIVE;
            range = Range.closedOpen(alignmentStart, alignmentEnd);
        }
    }

    @Override
    public Range<Integer> getRange() {
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
        Range<Integer> alignmentBlockRange = alignmentBlock.getRange();
        final int sequenceStartPos = alignmentBlockRange.lowerEndpoint() - alignmentStart;
        final int sequenceEndPos = sequenceStartPos + alignmentBlockRange.upperEndpoint() - alignmentBlockRange.lowerEndpoint();
        return samRecord.getReadString().substring(sequenceStartPos, sequenceEndPos);
    }
    
    public Map<String, String> getTooltipData() {
        HashMap<String, String> tooltipData = Maps.newHashMap();
        tooltipData.put("forward", Boolean.TRUE.toString());
        tooltipData.put("cigar", getCigarString());
        tooltipData.put("name", samRecord.getReadName());
        tooltipData.put("reference name", samRecord.getReferenceName());
        tooltipData.put("start", samRecord.getAlignmentStart()+"");
        tooltipData.put("end", samRecord.getAlignmentEnd()+"");
        return tooltipData;
    }

    public Set<AlignmentBlock> getAnnotationBlocks() {
        Set<AlignmentBlock> blocks = Sets.newLinkedHashSet();
        Cigar cigar = samRecord.getCigar();
        int start = alignmentStart;
        for (CigarElement cigarElement : cigar.getCigarElements()) {
            switch (cigarElement.getOperator()) {
                case D:
                    blocks.add(new AlignmentBlock(Range.closedOpen(start, start + cigarElement.getLength()), AlignmentType.DELETION));
                    start += cigarElement.getLength();
                    break;
                case I:
                    blocks.add(new AlignmentBlock(Range.closedOpen(start, start + cigarElement.getLength()), AlignmentType.INSERTION));
                    break;
                case M:
                    blocks.add(new AlignmentBlock(Range.closedOpen(start, start + cigarElement.getLength()), AlignmentType.MATCH));
                    start += cigarElement.getLength();
                    break;
                case N:
                    blocks.add(new AlignmentBlock(Range.closedOpen(start, start + cigarElement.getLength()), AlignmentType.GAP));
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
