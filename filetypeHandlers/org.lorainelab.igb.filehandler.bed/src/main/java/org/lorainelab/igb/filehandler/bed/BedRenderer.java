package org.lorainelab.igb.filehandler.bed;

import com.google.common.collect.Range;
import java.util.stream.Stream;
import org.lorainelab.igb.data.model.Strand;
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
                        bedFeature.getIntrons().stream().map(intron -> Line.start(intron.lowerEndpoint(), intron.upperEndpoint() - intron.lowerEndpoint()).build())
                ),
                layer(
                        bedFeature.getRange().lowerEndpoint(),
                        bedFeature.getExons().asRanges().stream().map(exon -> Rectangle.start(exon.lowerEndpoint(), exon.upperEndpoint() - exon.lowerEndpoint()).build()),
                        calculateCds(bedFeature)
                )
        );
    }

    private Stream<Shape> calculateCds(BedFeature bedFeature) {
        final Strand strand = bedFeature.getStrand();
        Range<Integer> cdsRange = Range.closed(bedFeature.getCdsStart() - bedFeature.getRange().lowerEndpoint(), bedFeature.getCdsEnd() - bedFeature.getRange().lowerEndpoint());

        return bedFeature.getExons().asRanges().stream()
                .map(exon -> Range.closed(exon.lowerEndpoint(), exon.upperEndpoint()))
                .filter(exonRange -> exonRange.isConnected(cdsRange))
                .map(exonRange -> exonRange.intersection(cdsRange))
                .map(intersectingRange -> Rectangle.start(intersectingRange.lowerEndpoint(), intersectingRange.upperEndpoint() - intersectingRange.lowerEndpoint())
                .addAttribute(Rectangle.Attribute.THICK)
                .setInnerTextReferenceSequenceRange(bedFeature.getRange())
                .setInnerTextRefSeqTranslator(referenceSequence -> {
                    StringBuilder trimmedToExons = new StringBuilder();
                    Range<Integer> cds = Range.closed(bedFeature.getCdsStart(), bedFeature.getCdsEnd());
                    bedFeature.getExons().asRanges().forEach(exon -> {
                        trimmedToExons.append(referenceSequence.substring(exon.lowerEndpoint(), exon.upperEndpoint()));
                    });
                    String aminoAcidSequence = AminoAcid.getAminoAcid(trimmedToExons.toString(), strand == Strand.POSITIVE);
//                            int startIndex = intersectingRange.lowerEndpoint() - bedFeature.getCdsStart() + bedFeature.getRange().lowerEndpoint();
//                            int endIndex = intersectingRange.upperEndpoint() - bedFeature.getCdsStart() + bedFeature.getRange().lowerEndpoint();
//                            int width = endIndex - startIndex;
//                            endIndex = startIndex + Math.floorDiv(width, 3);
//                            startIndex += startIndex % 3;
//                            if (width < aminoAcidSequence.length()) {
//                                int remainder = width % 3;
//                                if (remainder > 1) {
//                                    //allow look ahead to next exon
//                                    return aminoAcidSequence.substring(startIndex, endIndex);
//                                }
//                            }

                    return aminoAcidSequence;
                })
                .build());
    }

}
