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

        return bedFeature.getCdsBlocks().asRanges().stream()
                .map(cdsBlock -> Rectangle.start(cdsBlock.lowerEndpoint(), cdsBlock.upperEndpoint() - cdsBlock.lowerEndpoint())
                .addAttribute(Rectangle.Attribute.THICK)
                .setInnerTextReferenceSequenceRange(bedFeature.getRange())
                .setInnerTextRefSeqTranslator(referenceSequence -> {
                    StringBuilder trimmedToExons = new StringBuilder();
                    for (Range<Integer> cdsRect : bedFeature.getCdsBlocks().asRanges()) {
                        trimmedToExons.append(referenceSequence.substring(cdsRect.lowerEndpoint(), cdsRect.upperEndpoint()));
                    }
                    String aminoAcidSequence = AminoAcid.getAminoAcid(trimmedToExons.toString(), strand == Strand.POSITIVE);
                    int totalDownstreamCdsBlockLength = bedFeature.getCdsBlocks().asRanges().stream()
                            .filter(range -> range.upperEndpoint() < cdsBlock.lowerEndpoint())
                            .mapToInt(cdsRect -> cdsRect.upperEndpoint() - cdsRect.lowerEndpoint()).sum();

                    return aminoAcidSequence.substring(totalDownstreamCdsBlockLength, totalDownstreamCdsBlockLength + cdsBlock.upperEndpoint() - cdsBlock.lowerEndpoint());

                })
                .build());
    }

}
