package org.lorainelab.igb.data.model.bed;

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
                        bedFeature.getRange().getStart(),
                        bedFeature.getExons().stream().map(exon -> Rectangle.start(exon.getStart(), exon.getLength()).build()),
                        bedFeature.getIntrons().stream().map(intron -> Line.start(intron.getStart(), intron.getLength()).build()),
                        calculateCds(bedFeature)
                )
        );
    }

    private Stream<Shape> calculateCds(BedFeature bedFeature) {
        Range<Integer> cdsRange = Range.closed(bedFeature.getCdsStart() - bedFeature.getRange().getStart(), bedFeature.getCdsEnd() - bedFeature.getRange().getStart());

        return bedFeature.getExons().stream()
                .map(exon -> Range.closed(exon.getStart(), exon.getEnd()))
                .filter(exonRange -> exonRange.isConnected(cdsRange))
                .map(eoxnRange -> eoxnRange.intersection(cdsRange))
                .map(intersectingRange -> Rectangle.start(intersectingRange.lowerEndpoint(), intersectingRange.upperEndpoint() - intersectingRange.lowerEndpoint())
                        .addAttribute(Rectangle.Attribute.thick).build());
    }

}
