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
    private String featureSequence;

    public BamFeature(SAMRecord samRecord, String refSeqString) {
        this.readName = samRecord.getReadName();
        this.chromosomeId = samRecord.getReferenceName();
        this.mappingQuality = samRecord.getMappingQuality();
        cigarString = samRecord.getCigarString();
        alignmentStart = samRecord.getAlignmentStart() - 1; // convert to interbase
        alignmentEnd = samRecord.getAlignmentEnd() - 1;
        if (!samRecord.getReadNegativeStrandFlag()) {
            strand = Strand.POSITIVE;
        } else {
            strand = Strand.NEGATIVE;
        }
        range = Range.closedOpen(alignmentStart, alignmentEnd);
        alignMentBlocks = setAnnotationBlocks(samRecord, refSeqString);
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
        return Optional.ofNullable(featureSequence);
    }

    public int getMappingQuality() {
        return mappingQuality;
    }

    public String getCigarString() {
        return cigarString;
    }

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

    private Set<AlignmentBlock> setAnnotationBlocks(SAMRecord samRecord, String refSeqString) {
        Set<AlignmentBlock> blocks = Sets.newLinkedHashSet();
        Cigar cigar = samRecord.getCigar();
        int start = alignmentStart;
        int insertionOffset = 0;
        for (CigarElement cigarElement : cigar.getCigarElements()) {
            switch (cigarElement.getOperator()) {
                case D:
                    blocks.add(new AlignmentBlock(Range.closedOpen(start, start + cigarElement.getLength()), AlignmentType.DELETION, false, null));
                    start += cigarElement.getLength();
                    break;
                case I:
                    blocks.add(new AlignmentBlock(Range.closedOpen(start, start + cigarElement.getLength()), AlignmentType.INSERTION, false, null));
                    break;
                case M:
                    final int startPosition = insertionOffset;
                    final String refBlockSequence = refSeqString.substring(startPosition, startPosition + cigarElement.getLength());
                    final String recordBlockSequence = samRecord.getReadString().substring(Math.max(startPosition - 1, 0), Math.max(startPosition - 1, 0) + cigarElement.getLength());
                    boolean exactMatch = refBlockSequence.equalsIgnoreCase(recordBlockSequence);
                    blocks.add(new AlignmentBlock(Range.closedOpen(start, start + cigarElement.getLength()), AlignmentType.MATCH, exactMatch, exactMatch ? null : recordBlockSequence));
                    start += cigarElement.getLength();
                    break;
                case N:
                    blocks.add(new AlignmentBlock(Range.closedOpen(start, start + cigarElement.getLength()), AlignmentType.GAP, false, null));
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
