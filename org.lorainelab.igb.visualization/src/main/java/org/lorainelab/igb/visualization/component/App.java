/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.visualization.component;

import com.google.common.collect.Lists;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.SetChangeListener;
import org.lorainelab.igb.data.model.Chromosome;
import org.lorainelab.igb.data.model.DataSet;
import org.lorainelab.igb.data.model.GenomeVersion;
import org.lorainelab.igb.data.model.Track;
import org.lorainelab.igb.visualization.component.App.AppState;
import org.lorainelab.igb.visualization.component.api.Component;
import org.lorainelab.igb.visualization.component.api.State;
import org.lorainelab.igb.visualization.model.CoordinateTrackRenderer;
import org.lorainelab.igb.visualization.model.ViewPortManager;
import org.lorainelab.igb.visualization.model.ZoomableTrackRenderer;
import static org.lorainelab.igb.visualization.util.BoundsUtil.enforceRangeBounds;
import static org.lorainelab.igb.visualization.util.CanvasUtils.exponentialScaleTransform;
import static org.lorainelab.igb.visualization.util.CanvasUtils.linearScaleTransform;

/**
 *
 * @author jeckstei
 */
public class App extends Component<AppProps, AppState> {

    private final TrackContainer trackContainer;
    private double lastHSliderFire = -1;
    private ViewPortManager viewPortManager;

    public App() {
        this.init();
        this.trackContainer = new TrackContainer();
    }
    
    protected class AppState implements State {
        
        private double totalTrackHeight;

        public double getTotalTrackHeight() {
            return totalTrackHeight;
        }

        public void setTotalTrackHeight(double totalTrackHeight) {
            this.totalTrackHeight = totalTrackHeight;
        }
        
    }
    

    private void updateCanvasContexts() {
        viewPortManager.refresh(this.getProps().getvSlider().getValue(), this.getProps().getScrollY().getValue());
        
        AppState state = new AppState();
        state.setTotalTrackHeight(viewPortManager.getTotalTrackSize());
        this.setState(state);
        //updateScrollY();
        //updateTrackLabels();
    }

    private void init() {
        viewPortManager = new ViewPortManager(canvas, trackRenderers, 0, 0);

        this.getProps().getvSlider().valueProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            updateTrackRenderers();
        });
        this.getProps().gethSlider().valueProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {

            if (ignoreHSliderEvent) {
                ignoreHSliderEvent = false;
                return;
            }
            final boolean isSnapEvent = newValue.doubleValue() % hSlider.getMajorTickUnit() == 0;
            if (lastHSliderFire < 0 || Math.abs(lastHSliderFire - newValue.doubleValue()) > 1 || isSnapEvent) {
                updateTrackRenderers();
                syncWidgetSlider();
                lastHSliderFire = newValue.doubleValue();
            }
        });
        hSliderWidget.addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            resetZoomStripe();
            boolean isNearMaxZoom = newValue.doubleValue() > 98;
            if (lastHSliderFire < 0 || Math.abs(lastHSliderFire - newValue.doubleValue()) > 1 || isNearMaxZoom) {
                final double xFactor = linearScaleTransform(canvasPane, newValue.doubleValue());
                trackRenderers.forEach(trackRenderer -> {
                    trackRenderer.scaleCanvas(xFactor, scrollX.get(), scrollY.getValue());
                });
                syncHSlider(xFactor);
                lastHSliderFire = newValue.doubleValue();
            }
        });
        canvas.widthProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            Platform.runLater(() -> {
                refreshSliderWidget();
            });
            updateTrackRenderers();
        });

        canvas.heightProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            Platform.runLater(() -> {
                refreshSliderWidget();
            });
            updateTrackRenderers();
        });

        scrollX.addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            final double boundedScrollValue = enforceRangeBounds(newValue.doubleValue(), 0, 100);
            if (boundedScrollValue != newValue.doubleValue()) {
                scrollX.setValue(boundedScrollValue);
                return;
            }
            if (ignoreScrollXEvent) {
                ignoreScrollXEvent = false;
            } else {
                updateTrackRenderers();
            }
        });

        scrollY.valueProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            updateTrackRenderers();
        });
        //fixes initialization race condition
        Platform.runLater(() -> {
            refreshSliderWidget();
            updateTrackRenderers();
        });
    }

    private void initializeChromosomeSelectionListener() {
        this.getProps().getSelectionInfoService().getSelectedChromosome().addListener((observable, oldValue, newValue) -> {
            newValue.ifPresent(newChromosomeSelection -> {
                Chromosome selectedChromosome = this.getProps().getSelectedChromosome();
                if (selectedChromosome != newChromosomeSelection) {
                    selectedChromosome = newChromosomeSelection;
                    this.getProps().getSelectionInfoService().getSelectedGenomeVersion().get().ifPresent(gv -> {
                        if (this.getProps().getSelectedGenomeVersion() == gv) {
                            Platform.runLater(() -> {
                                trackRenderers.clear();
                                labelPane.getChildren().clear();
                                hSlider.setValue(0);
                                vSlider.setValue(0);
                                scrollY.setValue(0);
                                scrollY.setVisibleAmount(100);
                                updateTrackRenderers(gv);
                            });
                        }
                    });
                }
            });
        });
    }

    private void initializeGenomeVersionSelectionListener() {
        this.getProps().getSelectionInfoService().getSelectedGenomeVersion().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(() -> {
                trackRenderers.clear();
                labelPane.getChildren().clear();
                hSlider.setValue(0);
                vSlider.setValue(0);
                scrollY.setValue(0);
                scrollY.setVisibleAmount(100);
                newValue.ifPresent(genomeVersion -> {
                    if (this.getProps().getSelectedGenomeVersion() != genomeVersion) {
                        selectedGenomeVersion = genomeVersion;
                        updateTrackRenderers(genomeVersion);
                    }
                });
            });
        });
    }

    private void updateTrackRenderers(GenomeVersion gv) {
        if (gv.getSelectedChromosomeProperty().get().isPresent()) {
            if (!trackRenderers.stream().anyMatch(renderer -> renderer instanceof CoordinateTrackRenderer)) {
                final Chromosome chromosome = gv.getSelectedChromosomeProperty().get().get();
                final CoordinateTrackRenderer coordinateTrackRenderer = new CoordinateTrackRenderer(canvasPane, chromosome);
                coordinateTrackRenderer.setWeight(getMinWeight());
                trackRenderers.add(coordinateTrackRenderer);
                loadDataSets(gv, chromosome);
            }
        }
    }

    private void updateTrackRenderers() {
        canvasPane.clear();
        updateCanvasContexts();
        trackRenderers.forEach(trackRenderer -> trackRenderer.scaleCanvas(exponentialScaleTransform(canvasPane, hSlider.getValue()), scrollX.get(), scrollY.getValue()));
//        eventBus.post(new ScaleEvent(hSlider.getValue(), vSlider.getValue(), scrollX.getValue(), scrollY.getValue()));
        drawZoomCoordinateLine();
    }

    private void loadDataSets(GenomeVersion gv, final Chromosome chromosome) {
        gv.getLoadedDataSets().forEach(dataSet -> {
            Track positiveStrandTrack = dataSet.getPositiveStrandTrack(chromosome.getName());
            Track negativeStrandTrack = dataSet.getNegativeStrandTrack(gv.getSelectedChromosomeProperty().get().get().getName());
            final ZoomableTrackRenderer positiveStrandTrackRenderer = new ZoomableTrackRenderer(canvasPane, positiveStrandTrack, chromosome);
            positiveStrandTrackRenderer.setWeight(getMinWeight());
            final ZoomableTrackRenderer negativeStrandTrackRenderer = new ZoomableTrackRenderer(canvasPane, negativeStrandTrack, chromosome);
            negativeStrandTrackRenderer.setWeight(getMaxWeight());
            trackRenderers.add(positiveStrandTrackRenderer);
            trackRenderers.add(negativeStrandTrackRenderer);
            updateTrackRenderers();
        });
        gv.getLoadedDataSets().addListener((SetChangeListener.Change<? extends DataSet> change) -> {
            Platform.runLater(() -> { // there is a bug causing this event to fire multiple times for a single addition to the observable collection
                if (change.wasAdded()) {
                    final DataSet loadedDataSet = change.getElementAdded();
                    if (!loadedDataSets.contains(loadedDataSet)) {
                        loadedDataSets.add(loadedDataSet);
                        Track positiveStrandTrack = loadedDataSet.getPositiveStrandTrack(chromosome.getName());
                        Track negativeStrandTrack = change.getElementAdded().getNegativeStrandTrack(gv.getSelectedChromosomeProperty().get().get().getName());
                        final ZoomableTrackRenderer positiveStrandTrackRenderer = new ZoomableTrackRenderer(canvasPane, positiveStrandTrack, chromosome);
                        positiveStrandTrackRenderer.setWeight(getMinWeight());
                        final ZoomableTrackRenderer negativeStrandTrackRenderer = new ZoomableTrackRenderer(canvasPane, negativeStrandTrack, chromosome);
                        negativeStrandTrackRenderer.setWeight(getMaxWeight());
                        trackRenderers.add(positiveStrandTrackRenderer);
                        trackRenderers.add(negativeStrandTrackRenderer);
                        updateTrackRenderers();
                    }
                } else {
                    //todo implement remove
                }
            });
        });
        if (gv.getLoadedDataSets().isEmpty()) {
            updateTrackRenderers();
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

    @Override
    public List<Component> render() {
        TrackContainerProps trackContainerProps = new TrackContainerProps();

        return Lists.newArrayList(
                trackContainer.withAttributes(trackContainerProps)
        );
    }

}
