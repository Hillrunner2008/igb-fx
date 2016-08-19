package org.lorainelab.igb.filehandler.bigwig;

import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.broad.igv.bbfile.WigItem;
import org.lorainelab.igb.data.model.Feature;
import org.lorainelab.igb.data.model.Strand;

/**
 *
 * @author dcnorris
 */
public class BigWigFeature implements Feature {

    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getIntegerInstance(Locale.ENGLISH);
    private final String chrId;
    private final Range range;
    private float yValue;

    BigWigFeature(String chrId, WigItem wigItem) {
        this.chrId = chrId;
        this.range = Range.closed(wigItem.getStartBase(), wigItem.getEndBase());
        yValue = wigItem.getWigValue();
    }

    public Map<String, String> getTooltipData() {
        Map<String, String> data = Maps.newLinkedHashMap();
        data.put("start", NUMBER_FORMAT.format(range.lowerEndpoint()) + "");
        data.put("end", NUMBER_FORMAT.format(range.upperEndpoint()) + "");
        data.put("y value", yValue + "");
        data.put("chromosome", chrId);
        data.put("forward", "true");
        return data;
    }

    public float getyValue() {
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
