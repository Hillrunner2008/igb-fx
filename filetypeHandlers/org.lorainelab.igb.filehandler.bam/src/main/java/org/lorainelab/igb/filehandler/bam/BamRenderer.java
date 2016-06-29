/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.filehandler.bam;

import javafx.scene.paint.Color;
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
                        bamFeature.getRange().lowerEndpoint(),
                        bamFeature.getAnnotationBlocks().stream().map(alignmentBlock -> convertAlignmentBlockToRect(alignmentBlock))
                ));
    }

    private Shape convertAlignmentBlockToRect(AlignmentBlock alignmentBlock) {
        switch (alignmentBlock.getAlignmentType()) {
            case DELETION:
                return Rectangle.start(alignmentBlock.getRange().lowerEndpoint(), alignmentBlock.getRange().upperEndpoint() - alignmentBlock.getRange().lowerEndpoint())
                        .build();
            case GAP:
                return Line.start(alignmentBlock.getRange().lowerEndpoint(), alignmentBlock.getRange().upperEndpoint() - alignmentBlock.getRange().lowerEndpoint()
                ).build();
            case INSERTION:
                return Rectangle.start(
                        alignmentBlock.getRange().lowerEndpoint(), 
                        alignmentBlock.getRange().upperEndpoint() - alignmentBlock.getRange().lowerEndpoint())
                        .setColor(Color.CHOCOLATE)
                        .build();
            case MATCH:
                return Rectangle.start(
                        alignmentBlock.getRange().lowerEndpoint(), 
                        alignmentBlock.getRange().upperEndpoint() - alignmentBlock.getRange().lowerEndpoint()
                ).setInnerTextRefSeqTranslator(seq -> seq).build();
            case PADDING:
                return Rectangle.start(alignmentBlock.getRange().lowerEndpoint(), alignmentBlock.getRange().upperEndpoint() - alignmentBlock.getRange().lowerEndpoint()).build();
            default:
                return Rectangle.start(alignmentBlock.getRange().lowerEndpoint(), alignmentBlock.getRange().upperEndpoint() - alignmentBlock.getRange().lowerEndpoint()).build();
        }
    }

}
