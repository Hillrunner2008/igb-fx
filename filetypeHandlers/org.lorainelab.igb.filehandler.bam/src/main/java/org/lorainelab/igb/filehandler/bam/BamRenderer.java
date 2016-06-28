/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.filehandler.bam;

import org.lorainelab.igb.data.model.shapes.Composition;
import org.lorainelab.igb.data.model.shapes.Line;
import org.lorainelab.igb.data.model.shapes.Rectangle;
import org.lorainelab.igb.data.model.shapes.Shape;
import org.lorainelab.igb.data.model.view.Renderer;

/**
 *
 * @author jeckstei
 */
public class BamRenderer implements Renderer<BamFeature> {

    @Override
    public Composition render(BamFeature bamFeature) {
        String[] name = new String[] {"unknown"};
        bamFeature.getId().ifPresent(id -> {
           name[0] = id;
        });
        return composition(name[0],
                bamFeature.getTooltipData(),
                layer(
                        0,
                        shapes(
                                Rectangle.start(bamFeature.getRange().lowerEndpoint(), bamFeature.getRange().upperEndpoint() - bamFeature.getRange().lowerEndpoint())
                                .linkToModel(bamFeature).build()
                        )
                ),
                layer(
                        0,
                        bamFeature.getAnnotationBlocks().stream().map(alignmentBlock -> convertAlignmentBlockToRect(alignmentBlock))
                ));
    }

    private Shape convertAlignmentBlockToRect(AlignmentBlock alignmentBlock) {
        switch (alignmentBlock.getAlignmentType()) {
            case DELETION:
                return Rectangle.start(alignmentBlock.getRange().lowerEndpoint(), alignmentBlock.getRange().upperEndpoint() - alignmentBlock.getRange().lowerEndpoint())
                        .addAttribute(Rectangle.Attribute.deletion).build();
            case GAP:
                return Line.start(alignmentBlock.getRange().lowerEndpoint(), alignmentBlock.getRange().lowerEndpoint()
                ).build();
            case INSERTION:
                return Rectangle.start(alignmentBlock.getRange().lowerEndpoint(), alignmentBlock.getRange().upperEndpoint() - alignmentBlock.getRange().lowerEndpoint())
                        .addAttribute(Rectangle.Attribute.insertion).build();
            case MATCH:
                return Rectangle.start(alignmentBlock.getRange().lowerEndpoint(), alignmentBlock.getRange().upperEndpoint() - alignmentBlock.getRange().lowerEndpoint()).build();
            case PADDING:
                return Rectangle.start(alignmentBlock.getRange().lowerEndpoint(), alignmentBlock.getRange().upperEndpoint() - alignmentBlock.getRange().lowerEndpoint()).build();
            default:
                return Rectangle.start(alignmentBlock.getRange().lowerEndpoint(), alignmentBlock.getRange().upperEndpoint() - alignmentBlock.getRange().lowerEndpoint()).build();
        }
    }

}
