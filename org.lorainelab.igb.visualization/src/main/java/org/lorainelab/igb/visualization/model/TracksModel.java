package org.lorainelab.igb.visualization.model;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import com.google.common.collect.Sets;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import org.lorainelab.igb.data.model.Chromosome;
import org.lorainelab.igb.data.model.DataSet;
import org.lorainelab.igb.data.model.GenomeVersion;
import org.lorainelab.igb.data.model.Track;
import org.lorainelab.igb.selections.SelectionInfoService;
import org.lorainelab.igb.visualization.PrimaryCanvasRegion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true, provide = TracksModel.class)
public class TracksModel {

    private static final Logger LOG = LoggerFactory.getLogger(TracksModel.class);
    private DoubleProperty totalTrackHeight;
    private ObservableSet<TrackRenderer> trackRenderers;
    private SelectionInfoService selectionInfoService;
    private Chromosome selectedChromosome;
    private GenomeVersion selectedGenomeVersion;
    private PrimaryCanvasRegion primaryCanvasRegion;

    public TracksModel() {
        totalTrackHeight = new SimpleDoubleProperty(0);
        trackRenderers = FXCollections.observableSet(Sets.newConcurrentHashSet());
    }

    @Activate
    public void activate() {
        initializeChromosomeSelectionListener();
        initializeGenomeVersionSelectionListener();
    }

    private void initializeChromosomeSelectionListener() {
        selectionInfoService.getSelectedChromosome().addListener((observable, oldValue, newValue) -> {
            newValue.ifPresent(newChromosomeSelection -> {
                if (selectedChromosome != newChromosomeSelection) {
                    selectionInfoService.getSelectedGenomeVersion().get().ifPresent(gv -> {
                        trackRenderers.clear();
                        final Chromosome chromosome = newChromosomeSelection;
                        CoordinateTrackRenderer coordinateTrackRenderer = new CoordinateTrackRenderer(primaryCanvasRegion.getCanvas(), chromosome);
                        coordinateTrackRenderer.setWeight(getMinWeight());
                        trackRenderers.add(coordinateTrackRenderer);
                        loadDataSets(gv, chromosome);
                    });
                }
            });
        });
    }

    private void initializeGenomeVersionSelectionListener() {
        selectionInfoService.getSelectedGenomeVersion().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(() -> {
                newValue.ifPresent(genomeVersion -> {
                    if (selectedGenomeVersion != genomeVersion) {
                        selectedGenomeVersion = genomeVersion;
                        initializeDataSetListener(selectedGenomeVersion);
                    }
                });
            });
        });
    }

    public ReadOnlyDoubleProperty getTotalTrackHeight() {
        return totalTrackHeight;
    }

    public void setTotalTrackHeight(double totalTrackHeight) {
        this.totalTrackHeight.set(totalTrackHeight);
    }

    public ObservableSet<TrackRenderer> getTrackRenderers() {
        return trackRenderers;
    }

    @Reference
    public void setSelectionInfoService(SelectionInfoService selectionInfoService) {
        this.selectionInfoService = selectionInfoService;
    }

    @Reference
    public void setPrimaryCanvasRegion(PrimaryCanvasRegion primaryCanvasRegion) {
        this.primaryCanvasRegion = primaryCanvasRegion;
    }

    private void loadDataSets(GenomeVersion gv, Chromosome chromosome) {
        gv.getLoadedDataSets().forEach(dataSet -> {
            Track positiveStrandTrack = dataSet.getPositiveStrandTrack(chromosome.getName());
            Track negativeStrandTrack = dataSet.getNegativeStrandTrack(gv.getSelectedChromosomeProperty().get().get().getName());
            final ZoomableTrackRenderer positiveStrandTrackRenderer = new ZoomableTrackRenderer(primaryCanvasRegion.getCanvas(), positiveStrandTrack, chromosome);
            positiveStrandTrackRenderer.setWeight(getMinWeight());
            final ZoomableTrackRenderer negativeStrandTrackRenderer = new ZoomableTrackRenderer(primaryCanvasRegion.getCanvas(), negativeStrandTrack, chromosome);
            negativeStrandTrackRenderer.setWeight(getMaxWeight());
            trackRenderers.add(positiveStrandTrackRenderer);
            trackRenderers.add(negativeStrandTrackRenderer);
        });
        if (gv.getLoadedDataSets().isEmpty()) {
            //updateCanvasContexts();
        }
    }

    private int getMinWeight() {
        int[] min = {0};
        trackRenderers.stream().mapToInt(t -> t.getWeight()).min().ifPresent(currentMin -> {
            min[0] = currentMin - 1;
        });
        return min[0];
    }

    private int getMaxWeight() {
        int[] max = {0};
        trackRenderers.stream().mapToInt(t -> t.getWeight()).max().ifPresent(currentMax -> {
            max[0] = currentMax + 1;
        });
        return max[0];
    }

    private void initializeDataSetListener(GenomeVersion gv) {
        gv.getLoadedDataSets().removeListener(selectedGenomeVersionDataSetListener);
        gv.getLoadedDataSets().addListener(selectedGenomeVersionDataSetListener);
    }
    private SetChangeListener<DataSet> selectedGenomeVersionDataSetListener = (SetChangeListener.Change<? extends DataSet> change) -> {
        selectionInfoService.getSelectedGenomeVersion().get().ifPresent(gv -> {
            if (change.wasAdded()) {
                if (gv.getSelectedChromosomeProperty().get().isPresent()) {
                    Chromosome selectedChromosome = gv.getSelectedChromosomeProperty().get().get();
                    final DataSet loadedDataSet = change.getElementAdded();
                    Track positiveStrandTrack = loadedDataSet.getPositiveStrandTrack(selectedChromosome.getName());
                    Track negativeStrandTrack = change.getElementAdded().getNegativeStrandTrack(gv.getSelectedChromosomeProperty().get().get().getName());
                    final ZoomableTrackRenderer positiveStrandTrackRenderer = new ZoomableTrackRenderer(primaryCanvasRegion.getCanvas(), positiveStrandTrack, selectedChromosome);
                    positiveStrandTrackRenderer.setWeight(getMinWeight());
                    final ZoomableTrackRenderer negativeStrandTrackRenderer = new ZoomableTrackRenderer(primaryCanvasRegion.getCanvas(), negativeStrandTrack, selectedChromosome);
                    negativeStrandTrackRenderer.setWeight(getMaxWeight());
                    trackRenderers.add(positiveStrandTrackRenderer);
                    trackRenderers.add(negativeStrandTrackRenderer);
                }
            } else {
                //todo implement remove
            }
        });
    };

}
