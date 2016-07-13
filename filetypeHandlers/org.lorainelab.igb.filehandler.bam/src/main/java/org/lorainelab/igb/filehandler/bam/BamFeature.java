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
    private final int alignmentStart;
    private final int alignmentEnd;
    private final String readName;
    private final String chromosomeId;
    private String cigarString;
    private int mappingQuality;
    Set<AlignmentBlock> alignMentBlocks;

    public BamFeature(SAMRecord samRecord) {
        this.readName = samRecord.getReadName();
        this.chromosomeId = samRecord.getReferenceName();
        this.mappingQuality = samRecord.getMappingQuality();
        cigarString = samRecord.getCigarString();
        alignmentStart = samRecord.getAlignmentStart() - 1; // convert to interbase
        alignmentEnd = samRecord.getAlignmentEnd() - 1;
        if (!samRecord.getReadNegativeStrandFlag()) {
            strand = Strand.POSITIVE;
            range = Range.closedOpen(alignmentStart, alignmentEnd);
        } else {
            strand = Strand.NEGATIVE;
            range = Range.closedOpen(alignmentStart, alignmentEnd);
        }
        alignMentBlocks = setAnnotationBlocks(samRecord);
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
        return Optional.ofNullable(readName);
    }

    @Override
    public String getChromosomeId() {
        return chromosomeId;
    }

    public Optional<String> getReadSequence() {
        return Optional.ofNullable("");
    }

    public int getMappingQuality() {
        return mappingQuality;
    }

    public String getCigarString() {
        return cigarString;
    }

//    public String getAlignmentBlockSequence(AlignmentBlock alignmentBlock) {
//        Range<Integer> alignmentBlockRange = alignmentBlock.getRange();
//        final int sequenceStartPos = alignmentBlockRange.lowerEndpoint() - alignmentStart;
//        final int sequenceEndPos = sequenceStartPos + alignmentBlockRange.upperEndpoint() - alignmentBlockRange.lowerEndpoint();
//        return samRecord.getReadString().substring(sequenceStartPos, sequenceEndPos);
//    }
    public Map<String, String> getTooltipData() {
        HashMap<String, String> tooltipData = Maps.newHashMap();
        tooltipData.put("forward", strand == Strand.POSITIVE ? "true" : "false");
        tooltipData.put("cigar", getCigarString());
        tooltipData.put("name", readName);
        tooltipData.put("reference name", chromosomeId);
        tooltipData.put("start", alignmentStart + 1 + "");
        tooltipData.put("end", alignmentEnd + 1 + "");
        return tooltipData;
    }

    public Set<AlignmentBlock> getAnnotationBlocks() {
        return alignMentBlocks;
    }

    private Set<AlignmentBlock> setAnnotationBlocks(SAMRecord samRecord) {
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
