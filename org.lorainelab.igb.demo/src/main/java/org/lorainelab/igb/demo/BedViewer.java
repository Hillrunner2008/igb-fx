package org.lorainelab.igb.demo;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Reference;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.lorainelab.igb.data.model.bed.BedFeature;
import org.lorainelab.igb.data.model.bed.BedParser;
import org.lorainelab.igb.data.model.bed.BedRenderer;
import org.lorainelab.igb.data.model.shapes.Composition;
import org.lorainelab.igb.data.model.shapes.Line;
import org.lorainelab.igb.data.model.shapes.Rectangle;
import org.lorainelab.igb.data.model.shapes.Shape;
import org.lorainelab.igb.data.model.shapes.factory.GenovizFxFactory;
import org.lorainelab.igb.data.model.view.Layer;
import org.lorainelab.igb.visualization.GenoVixFxController;
import org.lorainelab.igb.visualization.StageProvider;
import org.lorainelab.igb.visualization.model.CompositionGlyph;
import org.lorainelab.igb.visualization.model.Glyph;
import org.lorainelab.igb.visualization.model.Track;
import org.lorainelab.igb.visualization.model.TrackRendererProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true)
public class BedViewer {

    private static final Logger LOG = LoggerFactory.getLogger(BedViewer.class);
    private GenoVixFxController controller;
    private Track positiveStrandTrack;
    private Track negativeStrandTrack;
    private List<Shape> shapes = Lists.newArrayList();
    private TrackRendererProvider trackRendererProvider;
    private Stage stage;

    @Activate
    public void activate() throws IOException {
        initiailize();
    }

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

    @Reference
    public void setTrackRendererProvider(TrackRendererProvider trackRendererProvider) {
        this.trackRendererProvider = trackRendererProvider;
    }

    @Reference
    public void setController(GenoVixFxController controller) {
        this.controller = controller;
    }

    @Deactivate
    public void stopBundle() {
        Platform.exit();
    }

    private void initiailize() throws IOException {
        final URL resource = BedViewer.class.getClassLoader().getResource("genoVizFx.fxml");
        FXMLLoader loader = new FXMLLoader(resource);
        loader.setClassLoader(this.getClass().getClassLoader());
        loader.setController(controller);
        Parent root = loader.load();
        positiveStrandTrack = trackRendererProvider.getPositiveStrandTrack();
        negativeStrandTrack = trackRendererProvider.getNegativeStrandTrack();
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
            layersList
                    .stream().forEach((Layer layer) -> {
                        getShapes(layer).forEach(shape -> {
                            if (Rectangle.class
                                    .isAssignableFrom(shape.getClass())) {
                                children.add(GenovizFxFactory.generateRectangleGlyph((Rectangle) shape));

                            }
                            if (Line.class
                                    .isAssignableFrom(shape.getClass())) {
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
        Platform.runLater(() -> {
            stage.setScene(scene);
            stage.show();
        });
    }

    @Reference
    public void setStageProvider(StageProvider stageProvider) {
        this.stage = stageProvider.getStage();
    }
}
