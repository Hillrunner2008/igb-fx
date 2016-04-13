package org.lorainelab.igb.filehandler.bed;

import com.google.common.collect.Maps;
import com.google.common.collect.RangeSet;
import com.google.common.collect.Sets;
import com.google.common.collect.TreeRangeSet;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.lorainelab.igb.data.model.Feature;
import org.lorainelab.igb.data.model.Range;
import org.lorainelab.igb.data.model.Strand;

public class BedFeature implements Feature {

    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getIntegerInstance(Locale.ENGLISH);
    protected String label;
    protected Map<String, String> props;
    protected int cdsStart = -1;
    protected int cdsEnd = -1;
    protected Set<Range> exons;
    protected Range range;
    protected Strand strand;
    private String id;
    private final String chrId;
    private String description;
    private String score;

    public BedFeature(String chrId, Range range, Strand strand) {
        this.chrId = chrId;
        this.range = range;
        this.strand = strand;
        exons = Sets.newLinkedHashSet();
    }

    public Map<String, String> getTooltipData() {
        Map<String, String> data = Maps.newLinkedHashMap();
        if (strand.equals(Strand.POSITIVE)) {
            data.put("id", id);
            data.put("description", description);
            data.put("start", NUMBER_FORMAT.format(range.getStart()) + "");
            data.put("end", NUMBER_FORMAT.format(range.getEnd()) + "");
            data.put("length", NUMBER_FORMAT.format(range.getLength()) + "");
            data.put("strand", strand.getName());
            data.put("cds start", NUMBER_FORMAT.format(cdsStart) + "");
            data.put("cds end", NUMBER_FORMAT.format(cdsEnd) + "");
            data.put("chromosome", chrId);
            data.put("score", score);
            data.put("forward", "true");
        } else {
            data.put("id", id);
            data.put("description", description);
            data.put("start", NUMBER_FORMAT.format(range.getEnd()) + "");
            data.put("end", NUMBER_FORMAT.format(range.getStart()) + "");
            data.put("length", NUMBER_FORMAT.format(range.getLength()) + "");
            data.put("strand", strand.getName());
            data.put("cds start", NUMBER_FORMAT.format(cdsEnd) + "");
            data.put("cds end", NUMBER_FORMAT.format(cdsStart) + "");
            data.put("chromosome", chrId);
            data.put("score", score);
            data.put("forward", "false");
        }
        return data;
    }

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

    public Set<Range> getRanges() {
        return exons;
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
        return Optional.ofNullable(id);
    }

    public void setId(String id) {
        this.id = id;
    }

    public Set<Range> getIntrons() {
        RangeSet<Integer> rangeSet = TreeRangeSet.create();
        exons.stream().map(exon -> com.google.common.collect.Range.closed(exon.getStart(), exon.getEnd())).forEach(r -> rangeSet.add(r));
        return rangeSet.complement()
                .subRangeSet(rangeSet.span())
                .asRanges()
                .stream().map(intron -> {
                    return intron;
                }).map(intron -> new Range(intron.lowerEndpoint(), intron.upperEndpoint()))
                .collect(Collectors.toSet());
    }

    private Range getCds() {
        if (cdsStart == -1 || cdsEnd == -1) {
            return range;
        }
        return new Range(cdsStart, cdsEnd);
    }

    @Override
    public String getChromosomeId() {
        return chrId;
    }

    public Set<Range> getExons() {
        return exons;
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
