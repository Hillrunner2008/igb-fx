/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.visualization.component;

import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.eventbus.Subscribe;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.SetChangeListener;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import org.lorainelab.igb.data.model.Chromosome;
import org.lorainelab.igb.data.model.DataSet;
import org.lorainelab.igb.data.model.GenomeVersion;
import org.lorainelab.igb.data.model.Track;
import org.lorainelab.igb.visualization.CanvasPane;
import org.lorainelab.igb.visualization.component.api.Component;
import org.lorainelab.igb.visualization.event.ScaleEvent;
import org.lorainelab.igb.visualization.model.CoordinateTrackRenderer;
import static org.lorainelab.igb.visualization.model.TrackRenderer.MAX_ZOOM_MODEL_COORDINATES_X;
import org.lorainelab.igb.visualization.model.ViewPortManager;
import org.lorainelab.igb.visualization.model.ZoomableTrackRenderer;
import org.lorainelab.igb.visualization.store.AppStore;
import org.lorainelab.igb.visualization.store.AppStoreEvent;
import static org.lorainelab.igb.visualization.util.BoundsUtil.enforceRangeBounds;
import static org.lorainelab.igb.visualization.util.CanvasUtils.exponentialScaleTransform;
import static org.lorainelab.igb.visualization.util.CanvasUtils.invertExpScaleTransform;
import static org.lorainelab.igb.visualization.util.CanvasUtils.linearScaleTransform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jeckstei
 */
public class App extends Component<AppProps, AppState> {

    private static final Logger LOG = LoggerFactory.getLogger(App.class);
    private final TrackContainer trackContainer;
    private double lastHSliderFire = -1;
    private ViewPortManager viewPortManager;
    private static final int TOTAL_SLIDER_THUMB_WIDTH = 30;
    private boolean ignoreHSliderEvent = false;
    private boolean ignoreScrollXEvent = false;

    public App() {
        this.trackContainer = new TrackContainer();
        this.state = AppState.factory();
        AppStore.getStore().subscribe(this);
    }

    @Override
    public App beforeComponentReady() {
        initializeCanvas();
        initializeChromosomeSelectionListener();
        initializeGenomeVersionSelectionListener();
        return this;
    }

    

    @Subscribe
    private void subscribeToAppStore(AppStoreEvent event) {

        AppState state = AppState.factory()
                .setScrollX(
                        AppStore.getStore().getScrollX()
                ).
                setScrollY(
                        AppStore.getStore().getScrollY()
                ).
                setScrollYVisibleAmount(
                        AppStore.getStore().getScrollYVisibleAmount()
                ).
                sethSlider(
                        AppStore.getStore().gethSlider()
                ).
                setvSlider(
                        AppStore.getStore().getvSlider()
                ).
                setTrackRenderers(
                        AppStore.getStore().getTrackRenderers()
                ).
                setLoadedDataSets(
                        AppStore.getStore().getLoadedDataSets()
                ).
                setSelectedGenomeVersion(
                        AppStore.getStore().getSelectedGenomeVersion()
                ).
                setSelectedChromosome(
                        AppStore.getStore().getSelectedChromosome()
                );
        this.setState(state);

    }

    private void updateCanvasContexts() {
        viewPortManager.refresh(this.getProps().getvSlider().getValue(), this.getProps().getScrollY().getValue());

        this.setState(this.getState().setTotalTrackHeight(viewPortManager.getTotalTrackSize()));
        updateScrollY();
        //updateTrackLabels();
    }

    private void updateScrollY() {
        double sum = this.getState().getTrackRenderers().stream()
                .map(trackRenderer -> trackRenderer.getCanvasContext())
                .filter(canvasContext -> canvasContext.isVisible())
                .mapToDouble(canvasContext -> canvasContext.getBoundingRect().getHeight())
                .sum();
        this.setState(AppState.factory().setScrollYVisibleAmount((sum / this.getState().getTotalTrackHeight()) * 100));
    }

    private void initializeCanvas() {
        Canvas canvas = this.getProps().getCanvasPane().getCanvas();
        viewPortManager = new ViewPortManager(canvas, this.getState().getTrackRenderers(), 0, 0);
        this.getProps().getvSlider().valueProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            updateTrackRenderers();
        });
        this.getProps().gethSlider().valueProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {

            if (ignoreHSliderEvent) {
                ignoreHSliderEvent = false;
                return;
            }
            final boolean isSnapEvent = newValue.doubleValue() % this.getProps().gethSlider().getMajorTickUnit() == 0;
            if (lastHSliderFire < 0 || Math.abs(lastHSliderFire - newValue.doubleValue()) > 1 || isSnapEvent) {
                //updateTrackRenderers();
                AppStore.getStore().updateHSlider(newValue.doubleValue());
                updateTrackRenderers();
                syncWidgetSlider();
                lastHSliderFire = newValue.doubleValue();
            }
        });
        this.getProps().gethSliderWidget().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            //resetZoomStripe();
            boolean isNearMaxZoom = newValue.doubleValue() > 98;
            if (lastHSliderFire < 0 || Math.abs(lastHSliderFire - newValue.doubleValue()) > 1 || isNearMaxZoom) {
                final double xFactor = linearScaleTransform(this.getProps().getCanvasPane(), newValue.doubleValue());
//                this.getState().getTrackRenderers().forEach(trackRenderer -> {
//                    trackRenderer.scaleCanvas(xFactor, this.getProps().getScrollX().get(), this.getProps().getScrollY().getValue());
//                });
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

        this.getProps().getScrollX().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            final double boundedScrollValue = enforceRangeBounds(newValue.doubleValue(), 0, 100);
            if (boundedScrollValue != newValue.doubleValue()) {
                this.getProps().getScrollX().setValue(boundedScrollValue);
                return;
            }
            if (ignoreScrollXEvent) {
                ignoreScrollXEvent = false;
            } else {
                updateTrackRenderers();
            }
        });

        this.getProps().getScrollY().valueProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            updateTrackRenderers();
        });
        this.getProps().getLoadDataButton().setOnAction(action -> {
            Chromosome selectedChromosome = this.getState().getSelectedChromosome();
            GenomeVersion selectedGenomeVersion = this.getState().getSelectedGenomeVersion();
            Optional.ofNullable(selectedGenomeVersion).ifPresent(genomeVersion -> {
                Optional.ofNullable(selectedChromosome).ifPresent(chr -> {
                    genomeVersion.getLoadedDataSets().forEach(dataSet -> {
                        CompletableFuture.supplyAsync(() -> {
                            dataSet.loadRegion(selectedChromosome.getName(), getCurrentRange());
                            return null;
                        }).thenRun(() -> {
                            Platform.runLater(() -> {
                                updateTrackRenderers();
                            });
                        });
                    });
                });
            });
        });
        this.getProps().getLoadSequenceButton().setOnAction(action -> {
            Chromosome selectedChromosome = this.getState().getSelectedChromosome();
            Optional.ofNullable(selectedChromosome).ifPresent(chr -> {
                CompletableFuture.supplyAsync(() -> {
                    chr.loadRegion(getCurrentRange());
                    return null;
                }).thenRun(() -> {
                    Platform.runLater(() -> {
                        updateTrackRenderers();
                    });
                }).exceptionally(ex -> {
                    LOG.error(ex.getMessage(), ex);
                    return null;
                });
            });
        });

        //fixes initialization race condition
//        Platform.runLater(() -> {
//            refreshSliderWidget();
//            updateTrackRenderers();
//        });
    }

    private Range<Integer> getCurrentRange() {
        CanvasPane canvasPane = this.getProps().getCanvasPane();
        double hSlider = this.getState().gethSlider();
        double scrollX = this.getState().getScrollX();
        final double xFactor = exponentialScaleTransform(canvasPane, hSlider);
        final double visibleVirtualCoordinatesX = Math.floor(canvasPane.getWidth() / xFactor);
        double xOffset = Math.round((scrollX / 100) * (canvasPane.getModelWidth() - visibleVirtualCoordinatesX));
        return Range.closedOpen((int) xOffset, (int) xOffset + (int) visibleVirtualCoordinatesX);
    }

    private void syncWidgetSlider() {
        double minScaleX = this.getProps().getCanvasPane().getModelWidth();
        double maxScaleX = MAX_ZOOM_MODEL_COORDINATES_X - 1;
        final double scaleRange = maxScaleX - minScaleX;
        final double xFactor = exponentialScaleTransform(this.getProps().getCanvasPane(), this.getProps().gethSlider().getValue());
        final double current = Math.floor(this.getProps().getCanvasPane().getWidth() / xFactor);
        double scaledPercentage = (current - minScaleX) / scaleRange;

        Rectangle slider = this.getProps().getSlider();
        Pane xSliderPane = this.getProps().getxSliderPane();
        DoubleProperty scrollX = this.getProps().getScrollX();
        Rectangle leftSliderThumb = this.getProps().getLeftSliderThumb();
        Rectangle rightSliderThumb = this.getProps().getRightSliderThumb();
        double oldWidth = slider.getWidth();
        double oldX = slider.getX();
        double width = ((1 - scaledPercentage) * (xSliderPane.getWidth() - TOTAL_SLIDER_THUMB_WIDTH)) + TOTAL_SLIDER_THUMB_WIDTH;
        double x = ((scrollX.getValue() / 100)) * (xSliderPane.getWidth() - width);
        slider.setX(x);
        leftSliderThumb.setX(x);
        slider.setWidth(width);
        rightSliderThumb.setX(rightSliderThumb.getX() - (oldWidth + oldX - width - x));
    }

    private void syncHSlider(double xFactor) {
        ignoreHSliderEvent = true;
        AppStore.getStore().updateHSlider(invertExpScaleTransform(this.getProps().getCanvasPane(), xFactor));
    }

    public DoubleProperty getHSliderValue() {
        return this.getProps().gethSlider().valueProperty();
    }

    public DoubleProperty getXScrollPosition() {
        return this.getProps().getScrollX();
    }

    private void initializeChromosomeSelectionListener() {
        this.getProps().getSelectionInfoService().getSelectedChromosome().addListener((observable, oldValue, newValue) -> {
            newValue.ifPresent(newChromosomeSelection -> {
                Chromosome selectedChromosome = this.getProps().getSelectedChromosome();
                if (selectedChromosome != newChromosomeSelection) {
                    AppStore.getStore().setSelectedChromosome(newChromosomeSelection);
                    this.getProps().getSelectionInfoService().getSelectedGenomeVersion().get().ifPresent(gv -> {
                        if (this.getProps().getSelectedGenomeVersion() == gv) {
                            Platform.runLater(() -> {
                                //TODO: handle this comp
                                this.getProps().getLabelPane().getChildren().clear();
                                AppStore.getStore().update(
                                        this.getState().getScrollX(),
                                        0,
                                        0,
                                        0,
                                        0,
                                        true
                                );
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
                this.getProps().getLabelPane().getChildren().clear();
                AppStore.getStore().update(
                        this.getState().getScrollX(),
                        0,
                        0,
                        0,
                        0,
                        true
                );
                newValue.ifPresent(genomeVersion -> {
                    if (this.getProps().getSelectedGenomeVersion() != genomeVersion) {
                        AppStore.getStore().setSelectedGenomeVersion(genomeVersion);
                        updateTrackRenderers(genomeVersion);
                    }
                });
            });
        });
    }

    private void updateTrackRenderers(GenomeVersion gv) {
        if (gv.getSelectedChromosomeProperty().get().isPresent()) {
            if (!this.getState().getTrackRenderers().stream().anyMatch(renderer -> renderer instanceof CoordinateTrackRenderer)) {
                final Chromosome chromosome = gv.getSelectedChromosomeProperty().get().get();
                final CoordinateTrackRenderer coordinateTrackRenderer = new CoordinateTrackRenderer(this.getProps().getCanvasPane(), chromosome);
                coordinateTrackRenderer.setWeight(getMinWeight());
                AppStore.getStore().addTrackRenderer(coordinateTrackRenderer);
                loadDataSets(gv, chromosome);
            }
        }
    }

    private void updateTrackRenderers() {
        
        updateCanvasContexts();

//        eventBus.post();
        //drawZoomCoordinateLine();
    }

//    private void handleScrollScaleEvent(ScrollScaleEvent event) {
//        Platform.runLater(() -> {
//            if (event.getDirection().equals(Direction.INCREMENT)) {
//                hSlider.increment();
//            } else {
//                hSlider.decrement();
//            }
//        });
//
//    }
    private void loadDataSets(GenomeVersion gv, final Chromosome chromosome) {
        gv.getLoadedDataSets().forEach(dataSet -> {
            Track positiveStrandTrack = dataSet.getPositiveStrandTrack(chromosome.getName());
            Track negativeStrandTrack = dataSet.getNegativeStrandTrack(gv.getSelectedChromosomeProperty().get().get().getName());
            CanvasPane canvasPane = this.getProps().getCanvasPane();
            final ZoomableTrackRenderer positiveStrandTrackRenderer = new ZoomableTrackRenderer(canvasPane, positiveStrandTrack, chromosome);
            positiveStrandTrackRenderer.setWeight(getMinWeight());
            final ZoomableTrackRenderer negativeStrandTrackRenderer = new ZoomableTrackRenderer(canvasPane, negativeStrandTrack, chromosome);
            negativeStrandTrackRenderer.setWeight(getMaxWeight());
            AppStore.getStore().addTrackRenderer(positiveStrandTrackRenderer, negativeStrandTrackRenderer);
            updateTrackRenderers();
        });
        gv.getLoadedDataSets().addListener((SetChangeListener.Change<? extends DataSet> change) -> {
            Platform.runLater(() -> { // there is a bug causing this event to fire multiple times for a single addition to the observable collection
                if (change.wasAdded()) {
                    final DataSet loadedDataSet = change.getElementAdded();
                    if (!this.getState().getLoadedDataSets().contains(loadedDataSet)) {
                        CanvasPane canvasPane = this.getProps().getCanvasPane();
                        AppStore.getStore().addDataSet(loadedDataSet);
                        Track positiveStrandTrack = loadedDataSet.getPositiveStrandTrack(chromosome.getName());
                        Track negativeStrandTrack = change.getElementAdded().getNegativeStrandTrack(gv.getSelectedChromosomeProperty().get().get().getName());
                        final ZoomableTrackRenderer positiveStrandTrackRenderer = new ZoomableTrackRenderer(canvasPane, positiveStrandTrack, chromosome);
                        positiveStrandTrackRenderer.setWeight(getMinWeight());
                        final ZoomableTrackRenderer negativeStrandTrackRenderer = new ZoomableTrackRenderer(canvasPane, negativeStrandTrack, chromosome);
                        negativeStrandTrackRenderer.setWeight(getMaxWeight());
                        AppStore.getStore().addTrackRenderer(positiveStrandTrackRenderer, negativeStrandTrackRenderer);
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
        this.getState().getTrackRenderers().stream().mapToInt(t -> t.getWeight()).min().ifPresent(currentMin -> {
            min[0] = currentMin - 1;
        });
        return min[0];
    }

    private int getMaxWeight() {
        int[] max = {0};
        this.getState().getTrackRenderers().stream().mapToInt(t -> t.getWeight()).max().ifPresent(currentMax -> {
            max[0] = currentMax + 1;
        });
        return max[0];
    }

    private void refreshSliderWidget() {
//        if (xSliderPane.getWidth() > 0) {
//            double max = xSliderPane.getWidth() - slider.getWidth();
//            double current = slider.getX();
//            double newXValue = (max * scrollX.getValue() / 100);
//
//            if (newXValue <= 0) {
//                newXValue = 0;
//            }
//            if (slider.getWidth() >= xSliderPane.getWidth()) {
//                double newWidth = xSliderPane.getWidth();
//                if (newWidth < TOTAL_SLIDER_THUMB_WIDTH) {
//                    newWidth = TOTAL_SLIDER_THUMB_WIDTH;
//                }
//                double oldWidth = slider.getWidth();
//                slider.setWidth(newWidth);
//                rightSliderThumb.setX(rightSliderThumb.getX() + newWidth - oldWidth);
//            }
//            if (scrollX.getValue() >= 0 && xSliderPane.getWidth() > TOTAL_SLIDER_THUMB_WIDTH) {
//                slider.setX(newXValue);
//                leftSliderThumb.setX(newXValue);
//                double maxPaneWidth = xSliderPane.getWidth() - TOTAL_SLIDER_THUMB_WIDTH;
//                double newSliderWidth = -maxPaneWidth * ((hSliderWidget.getValue() / 100) - 1) + TOTAL_SLIDER_THUMB_WIDTH;
//                double rightThumbX = rightSliderThumb.getX() + newXValue - current - slider.getWidth() + newSliderWidth;
//                rightSliderThumb.setX(rightThumbX);
//                slider.setWidth(newSliderWidth);
//
//            }
//        }
    }

    @Override
    public List<Component> render() {
        this.getProps().getCanvasPane().clear();
        this.getState().getTrackRenderers().forEach(trackRenderer -> trackRenderer.scaleCanvas(
                exponentialScaleTransform(
                        this.getProps().getCanvasPane(),
                        this.getState().gethSlider()
                ),
                this.getState().getScrollX(),
                this.getState().getScrollY()
        )
        );
        ScaleEvent scaleEvent = new ScaleEvent(this.getState().gethSlider(), this.getState().getvSlider(), this.getState().getScrollX(), this.getState().getScrollY());
        this.getProps().getCanvasPane().handleScaleEvent(scaleEvent);
        TrackContainerProps trackContainerProps = new TrackContainerProps();
        LOG.info("render");
        return Lists.newArrayList(
                trackContainer.withAttributes(trackContainerProps)
        );
    }

}
