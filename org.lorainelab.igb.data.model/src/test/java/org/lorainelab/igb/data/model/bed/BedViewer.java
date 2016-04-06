package org.lorainelab.igb.data.model.bed;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.lorainelab.igb.data.model.shapes.Composition;
import org.lorainelab.igb.data.model.shapes.Line;
import org.lorainelab.igb.data.model.shapes.Rectangle;
import org.lorainelab.igb.data.model.shapes.Shape;
import org.lorainelab.igb.data.model.shapes.factory.GenovizFxFactory;
import org.lorainelab.igb.data.view.Layer;
import org.lorainelab.igb.data.view.Renderer;
import org.lorainelab.igb.visualization.GenoVixFxController;
import org.lorainelab.igb.visualization.model.CompositionGlyph;
import org.lorainelab.igb.visualization.model.Glyph;
import org.lorainelab.igb.visualization.model.Track;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BedViewer extends Application {

    private static final Logger LOG = LoggerFactory.getLogger(BedViewer.class);
    private GenoVixFxController controller;
    private Track positiveStrandTrack;
    private Track negativeStrandTrack;

    private List<Shape> shapes = Lists.newArrayList();

    public static void main(String[] args) throws InterruptedException {
        launch();
    }

    @Override
    public void start(Stage stage) throws Exception {
        final URL resource = GenoVixFxController.class.getClassLoader().getResource("genoVizFx.fxml");
        FXMLLoader loader = new FXMLLoader(resource);
        Parent root = loader.load();
        controller = loader.getController();
        positiveStrandTrack = controller.getPositiveStrandTrack();
        negativeStrandTrack = controller.getNegativeStrandTrack();
        BedParser bedParser = new BedParser();
        List<BedFeature> annotations = bedParser.getAnnotations();
        List<CompositionGlyph> primaryGlyphs = Lists.newArrayList();

        String[] label = {""};
        Map[] tooltipData = {Maps.newConcurrentMap()};
        annotations.stream().map((BedFeature annotation) -> {
            BedRenderer view = new BedRenderer();
            final Composition composition = view.render(annotation);
            composition.getLabel().ifPresent(compositionLabel -> label[0] = compositionLabel);
            tooltipData[0] = composition.getTooltipData();
            return composition.getLayers();
        }).forEach(layersList -> {
            List<Glyph> children = Lists.newArrayList();
            layersList.stream().forEach((Layer layer) -> {
                getShapes(layer).forEach(shape -> {
                    if (Rectangle.class.isAssignableFrom(shape.getClass())) {
                        children.add(GenovizFxFactory.generateRectangleGlyph((Rectangle) shape));
                    }
                    if (Line.class.isAssignableFrom(shape.getClass())) {
                        children.add(GenovizFxFactory.generateLine((Line) shape));
                    }
                });
            });
            primaryGlyphs.add(GenovizFxFactory.generateCompositionGlyph(label[0], tooltipData[0], children));
        });
        primaryGlyphs.stream().forEach(glyph -> {
            if (glyph.getTooltipData().get("forward").equals("true")) {
                positiveStrandTrack.getGlyphs().add(glyph);
            } else {
                negativeStrandTrack.getGlyphs().add(glyph);
            }
        });
        //TODO this is a temporary hack for the demo... We will need the track to internally manage the slot data structure
        positiveStrandTrack.buildSlots();
        negativeStrandTrack.buildSlots();
        Scene scene = new Scene(root);
        scene.getStylesheets().add("/styles/Styles.css");
        stage.setTitle("JavaFx IGB");
        stage.setScene(scene);
        stage.show();
    }

    //@Test
    public void viewBedFile() {
        BedParser bedParser = new BedParser();
        List<BedFeature> annotations = bedParser.getAnnotations();

        annotations.stream().map((BedFeature annotation) -> {
            BedRenderer view = new BedRenderer();
            return view.render(annotation);
        });

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

    class BedRenderer implements Renderer<BedFeature> {

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
}
