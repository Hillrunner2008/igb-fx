package org.lorainelab.igb.filehandler.bigwig;

import com.google.common.collect.Range;
import java.util.Optional;
import org.broad.igv.bbfile.WigItem;
import org.lorainelab.igb.data.model.Feature;
import org.lorainelab.igb.data.model.Strand;

/**
 *
 * @author dcnorris
 */
public class BigWigFeature implements Feature {

    private final String chrId;
    private final Range range;
    private double yValue;

    BigWigFeature(String chrId, WigItem wigItem) {
        this.chrId = chrId;
        this.range = Range.closed(wigItem.getStartBase(), wigItem.getEndBase());
        yValue = wigItem.getWigValue();
    }

    public double getyValue() {
        return yValue;
    }

    @Override
    public Range<Integer> getRange() {
        return range;
    }

    @Override
    public Strand getStrand() {
        return Strand.POSITIVE; //TODO think about enum value for data not associated with strand
    }

    @Override
    public Optional<String> getId() {
        return Optional.empty();
    }

    @Override
    public String getChromosomeId() {
        return chrId;
    }

}
