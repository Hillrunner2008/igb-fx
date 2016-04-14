package org.lorainelab.igb.filehandler.bed;

import com.google.common.collect.Range;
import java.util.stream.Stream;
import org.lorainelab.igb.data.model.shapes.Composition;
import org.lorainelab.igb.data.model.shapes.Line;
import org.lorainelab.igb.data.model.shapes.Rectangle;
import org.lorainelab.igb.data.model.shapes.Shape;
import org.lorainelab.igb.data.model.view.Renderer;

public class BedRenderer implements Renderer<BedFeature> {

    @Override
    public Composition render(BedFeature bedFeature) {
        return composition(
                bedFeature.getLabel(),
                bedFeature.getTooltipData(),
                layer(
                        bedFeature.getRange().lowerEndpoint(),
                        bedFeature.getExons().asRanges().stream().map(exon -> Rectangle.start(exon.lowerEndpoint(), exon.upperEndpoint() - exon.lowerEndpoint()).build()),
                        bedFeature.getIntrons().stream().map(intron -> Line.start(intron.lowerEndpoint(), intron.upperEndpoint() - intron.lowerEndpoint()).build()),
                        calculateCds(bedFeature)
                )
        );
    }

    private Stream<Shape> calculateCds(BedFeature bedFeature) {
        Range<Integer> cdsRange = Range.closed(bedFeature.getCdsStart() - bedFeature.getRange().lowerEndpoint(), bedFeature.getCdsEnd() - bedFeature.getRange().lowerEndpoint());

        return bedFeature.getExons().asRanges().stream()
                .map(exon -> Range.closed(exon.lowerEndpoint(), exon.upperEndpoint()))
                .filter(exonRange -> exonRange.isConnected(cdsRange))
                .map(eoxnRange -> eoxnRange.intersection(cdsRange))
                .map(intersectingRange -> Rectangle.start(intersectingRange.lowerEndpoint(), intersectingRange.upperEndpoint() - intersectingRange.lowerEndpoint())
                        .addAttribute(Rectangle.Attribute.thick).build());
    }

}
