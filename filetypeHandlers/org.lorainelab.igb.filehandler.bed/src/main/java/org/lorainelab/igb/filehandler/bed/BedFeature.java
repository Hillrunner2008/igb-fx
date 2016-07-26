package org.lorainelab.igb.filehandler.bed;

import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.lorainelab.igb.data.model.Feature;
import org.lorainelab.igb.data.model.Strand;

public class BedFeature implements Feature {

    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getIntegerInstance(Locale.ENGLISH);
    protected String label;
    protected Map<String, String> props;
    protected int cdsStart = -1;
    protected int cdsEnd = -1;
    protected RangeSet<Integer> exons;
    protected RangeSet<Integer> cdsBlocks;
    protected Range<Integer> range;
    protected Strand strand;
    private String id;
    private final String chrId;
    private String description;
    private String score;
    private int cdsBlockLength;

    public BedFeature(String chrId, Range range, Strand strand) {
        this.chrId = chrId;
        this.range = range;
        this.strand = strand;
        exons = TreeRangeSet.create();
        cdsBlockLength = -1;
    }

    public Map<String, String> getTooltipData() {
        Map<String, String> data = Maps.newLinkedHashMap();
        if (strand.equals(Strand.POSITIVE)) {
            data.put("id", id);
            data.put("description", description);
            data.put("start", NUMBER_FORMAT.format(range.lowerEndpoint()) + "");
            data.put("end", NUMBER_FORMAT.format(range.upperEndpoint()) + "");
            data.put("length", NUMBER_FORMAT.format(range.upperEndpoint() - range.lowerEndpoint()) + "");
            data.put("strand", strand.getName());
            data.put(CDS_START, NUMBER_FORMAT.format(cdsStart) + "");
            data.put(CDS_END, NUMBER_FORMAT.format(cdsEnd) + "");
            data.put("chromosome", chrId);
            data.put("score", score);
            data.put("forward", "true");
        } else {
            data.put("id", id);
            data.put("description", description);
            data.put("start", NUMBER_FORMAT.format(range.upperEndpoint()) + "");
            data.put("end", NUMBER_FORMAT.format(range.lowerEndpoint()) + "");
            data.put("length", NUMBER_FORMAT.format(range.upperEndpoint() - range.lowerEndpoint()) + "");
            data.put("strand", strand.getName());
            data.put(CDS_START, NUMBER_FORMAT.format(cdsEnd) + "");
            data.put(CDS_END, NUMBER_FORMAT.format(cdsStart) + "");
            data.put("chromosome", chrId);
            data.put("score", score);
            data.put("forward", "false");
        }
        return data;
    }
    public static final String CDS_END = "cds end";
    public static final String CDS_START = "cds start";

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Map<String, String> getProps() {
        return props;
    }

    public void setProps(Map<String, String> props) {
        this.props = props;
    }

    public int getCdsStart() {
        return cdsStart;
    }

    public void setCdsStart(int cdsStart) {
        this.cdsStart = cdsStart;
    }

    public int getCdsEnd() {
        return cdsEnd;
    }

    public void setCdsEnd(int cdsEnd) {
        this.cdsEnd = cdsEnd;
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
        return Optional.ofNullable(id);
    }

    public void setId(String id) {
        this.id = id;
    }

    public Set<Range<Integer>> getIntrons() {
        return exons.complement()
                .subRangeSet(exons.span())
                .asRanges()
                .stream().map(intron -> {
                    return intron;
                }).map(intron -> Range.closedOpen(intron.lowerEndpoint(), intron.upperEndpoint()))
                .collect(Collectors.toSet());
    }

    @Override
    public String getChromosomeId() {
        return chrId;
    }

    public RangeSet<Integer> getExons() {
        return exons;
    }

    public RangeSet<Integer> getCdsBlocks() {
        if (cdsBlocks == null) {
            cdsBlocks = TreeRangeSet.create();
            Range<Integer> cdsRange = Range.closed(cdsStart - range.lowerEndpoint(), cdsEnd - range.lowerEndpoint());
            exons.asRanges().stream()
                    .map(exon -> Range.closed(exon.lowerEndpoint(), exon.upperEndpoint()))
                    .filter(exonRange -> exonRange.isConnected(cdsRange))
                    .map(exonRange -> exonRange.intersection(cdsRange))
                    .forEach(cdsBlock -> cdsBlocks.add(cdsBlock));
        }
        return cdsBlocks;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

}
