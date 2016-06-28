package org.lorainelab.igb.data.model.bam.view;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import javafx.application.Application;
import javafx.stage.Stage;
import org.junit.Test;
import org.lorainelab.igb.data.model.bam.AlignmentBlock;
import org.lorainelab.igb.data.model.bam.BamFeature;
import org.lorainelab.igb.data.model.bam.BamParser;
import org.lorainelab.igb.data.model.shapes.Composition;
import org.lorainelab.igb.data.model.shapes.Line;
import org.lorainelab.igb.data.model.shapes.Rectangle;
import org.lorainelab.igb.data.model.shapes.Shape;
import org.lorainelab.igb.data.model.view.Layer;
import org.lorainelab.igb.data.model.view.Renderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BamViewer extends Application {

    private static final Logger logger = LoggerFactory.getLogger(BamViewer.class);

    private List<Shape> shapes = Lists.newArrayList();

    @Override
    public void start(Stage stage) throws Exception {
        BamParser bamParser = new BamParser();
        List<BamFeature> annotations = bamParser.getAnnotations();
        annotations.stream().map((BamFeature annotation) -> {
            BamRenderer view = new BamRenderer();
            return view.render(annotation).getLayers();
        }).forEach(layers -> {
            layers.forEach((Layer layer) -> {
                getShapes(layer).forEach(shape -> {
                    System.out.println(shape.getClass());
                    shapes.add(shape);
                });
            });
        });

//        final URL resource = GenoVixFxController.class.getClassLoader().getResource("genoVizFx.fxml");
//        FXMLLoader loader = new FXMLLoader(resource);
//        Parent root = loader.load();
//        controller = loader.getController();
//        final Track track = controller.getPositiveStrandTrack();
//        Scene scene = new Scene(root);
//        stage.setTitle("GenoViz Fx");
//        stage.setScene(scene);
//        stage.show();
        shapes.forEach(shape -> {
            if (Rectangle.class.isAssignableFrom(shape.getClass())) {
                System.out.println(shape.getClass());
//                track.getGlyphs().add(GenovizFxFactory.generateRectangleGlyph((Rectangle) shape));
            }
        });
    }

    @Test
    public void testFx() throws InterruptedException {

        launch();
        //controller.render();
    }

    private List<Shape> getShapes(Layer layer) {
        List<Shape> toReturn = Lists.newArrayList();
        layer.getItems().forEach(s -> {
            if (s instanceof Layer) {
                toReturn.addAll(getShapes((Layer) s));
            } else {
                toReturn.add(s);
            }
        });
        return toReturn;
    }

    class BamRenderer implements Renderer<BamFeature> {

        @Override
        public Composition render(BamFeature bamFeature) {
            return composition("test",
                    Maps.newConcurrentMap(),
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
                            .build();
                case GAP:
                    return Line.start(alignmentBlock.getRange().lowerEndpoint(), alignmentBlock.getRange().lowerEndpoint()
                    ).build();
                case INSERTION:
                    return Rectangle.start(alignmentBlock.getRange().lowerEndpoint(), alignmentBlock.getRange().upperEndpoint() - alignmentBlock.getRange().lowerEndpoint())
                            .build();
                case MATCH:
                    return Rectangle.start(alignmentBlock.getRange().lowerEndpoint(), alignmentBlock.getRange().upperEndpoint() - alignmentBlock.getRange().lowerEndpoint()).build();
                case PADDING:
                    return Rectangle.start(alignmentBlock.getRange().lowerEndpoint(), alignmentBlock.getRange().upperEndpoint() - alignmentBlock.getRange().lowerEndpoint()).build();
                default:
                    return Rectangle.start(alignmentBlock.getRange().lowerEndpoint(), alignmentBlock.getRange().upperEndpoint() - alignmentBlock.getRange().lowerEndpoint()).build();
            }
        }

    }
}
