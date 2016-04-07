package org.lorainelab.igb.demo;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import com.google.common.collect.Sets;
import java.util.Set;
import javafx.embed.swing.JFXPanel;
import org.lorainelab.igb.visualization.CanvasPane;
import org.lorainelab.igb.visualization.model.CoordinateTrackRenderer;
import org.lorainelab.igb.visualization.model.RefrenceSequenceProvider;
import org.lorainelab.igb.visualization.model.Track;
import org.lorainelab.igb.visualization.model.TrackRenderer;
import org.lorainelab.igb.visualization.model.TrackRendererProvider;
import org.lorainelab.igb.visualization.model.ZoomableTrackRenderer;

@Component
public class DemoTrackRendererProvider implements TrackRendererProvider {

    Set<TrackRenderer> trackRenderers;
    RefrenceSequenceProvider refrenceSequenceProvider;
    private CanvasPane canvasPane;
    private Track negativeStrandTrack;
    private Track positiveStrandTrack;

    public DemoTrackRendererProvider() {
        trackRenderers = Sets.newLinkedHashSet();
    }

    @Activate
    public void activate() {
        new JFXPanel(); // runtime initializer, do not remove
        modelWidth = refrenceSequenceProvider.getReferenceDna().length();
            positiveStrandTrack = new Track(false, "RefGene (+)", 5);
            negativeStrandTrack = new Track(true, "RefGene (-)", 5);
            ZoomableTrackRenderer bedFileTrack = new ZoomableTrackRenderer(canvasPane, positiveStrandTrack, modelWidth);
            CoordinateTrackRenderer coordinateTrack = new CoordinateTrackRenderer(canvasPane, refrenceSequenceProvider);
            ZoomableTrackRenderer negativeStrandBedFile = new ZoomableTrackRenderer(canvasPane, negativeStrandTrack, modelWidth);
            trackRenderers.add(bedFileTrack);
            trackRenderers.add(coordinateTrack);
            trackRenderers.add(negativeStrandBedFile);
    }
    private int modelWidth;

    @Override
    public Set<TrackRenderer> getTrackRenderers() {
        return trackRenderers;
    }

    @Reference
    public void setRefrenceSequenceProvider(RefrenceSequenceProvider refrenceSequenceProvider) {
        this.refrenceSequenceProvider = refrenceSequenceProvider;
    }

    @Reference
    public void setCanvasPane(CanvasPane canvasPane) {
        this.canvasPane = canvasPane;
    }

    @Override
    public int getModelWidth() {
        return modelWidth;
    }

    public Track getNegativeStrandTrack() {
        return negativeStrandTrack;
    }

    public Track getPositiveStrandTrack() {
        return positiveStrandTrack;
    }

}
