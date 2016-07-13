/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.filehandler.bam;

import java.util.Optional;
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
        String[] name = new String[]{"unknown"};
        bamFeature.getId().ifPresent(id -> {
            name[0] = id;
        });
        return composition(name[0],
                bamFeature.getTooltipData(),
                layer(
                        0,
                        bamFeature.getAnnotationBlocks().stream()
                                .map(alignmentBlock -> convertAlignmentBlockToRect(alignmentBlock))
                                .filter(optionalShape -> optionalShape.isPresent())
                                .map(shape -> shape.get())
                ),
                layer(
                        0,
                        bamFeature.getAnnotationBlocks().stream()
                                .map(alignmentBlock -> convertInsertions(alignmentBlock))
                                .filter(optionalShape -> optionalShape.isPresent())
                                .map(shape -> shape.get())
                )
        );
    }

    private Optional<Shape> convertAlignmentBlockToRect(AlignmentBlock alignmentBlock) {
        switch (alignmentBlock.getAlignmentType()) {
            case DELETION:
                return Optional.of(Rectangle.start(alignmentBlock.getRange().lowerEndpoint(), alignmentBlock.getRange().upperEndpoint() - alignmentBlock.getRange().lowerEndpoint())
                        .setColor(Color.RED)
                        .setInnerTextRefSeqTranslator(seq -> "X")
                        .build());
            case MATCH:
                return Optional.of(Rectangle.start(
                        alignmentBlock.getRange().lowerEndpoint(),
                        alignmentBlock.getRange().upperEndpoint() - alignmentBlock.getRange().lowerEndpoint()
                )
                        .setColorByBase(true)
                        .setInnerTextRefSeqTranslator(seq -> seq)
                        .build());
            case PADDING:
                return Optional.of(Rectangle.start(alignmentBlock.getRange().lowerEndpoint(), alignmentBlock.getRange().upperEndpoint() - alignmentBlock.getRange().lowerEndpoint()).build());
            default:
                return Optional.empty();
        }
    }

    private Optional<Shape> convertInsertions(AlignmentBlock alignmentBlock) {
        switch (alignmentBlock.getAlignmentType()) {
            case GAP:
                return Optional.of(Line.start(alignmentBlock.getRange().lowerEndpoint(), alignmentBlock.getRange().upperEndpoint() - alignmentBlock.getRange().lowerEndpoint()
                ).build());
            case INSERTION:
                return Optional.of(Rectangle.start(
                        alignmentBlock.getRange().lowerEndpoint(),
                        2)
                        .setColor(Color.GOLD)
                        .addAttribute(Rectangle.Attribute.INSERTION)
                        .setInnerTextRefSeqTranslator(seq -> "><")
                        .build());
            default:
                return Optional.empty();
        }
    }

}
