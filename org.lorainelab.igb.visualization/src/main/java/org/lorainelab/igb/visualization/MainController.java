package org.lorainelab.igb.visualization;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Reference;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.SetChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.Slider;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Transform;
import org.controlsfx.control.PlusMinusSlider;
import org.controlsfx.control.PlusMinusSlider.PlusMinusEvent;
import org.lorainelab.igb.data.model.Chromosome;
import org.lorainelab.igb.data.model.DataSet;
import org.lorainelab.igb.data.model.GenomeVersion;
import org.lorainelab.igb.data.model.Track;
import org.lorainelab.igb.data.model.View;
import org.lorainelab.igb.selections.SelectionInfoService;
import org.lorainelab.igb.visualization.event.ClickDragZoomEvent;
import org.lorainelab.igb.visualization.event.ScaleEvent;
import org.lorainelab.igb.visualization.event.ScrollScaleEvent;
import org.lorainelab.igb.visualization.event.ScrollScaleEvent.Direction;
import org.lorainelab.igb.visualization.event.ScrollXUpdate;
import org.lorainelab.igb.visualization.event.SelectionChangeEvent;
import org.lorainelab.igb.visualization.footer.Footer;
import org.lorainelab.igb.visualization.menubar.MenuBarManager;
import org.lorainelab.igb.visualization.model.CoordinateTrackRenderer;
import org.lorainelab.igb.visualization.model.JumpZoomEvent;
import org.lorainelab.igb.visualization.model.TrackLabel;
import org.lorainelab.igb.visualization.model.TrackRenderer;
import static org.lorainelab.igb.visualization.model.TrackRenderer.MAX_ZOOM_MODEL_COORDINATES_X;
import org.lorainelab.igb.visualization.model.ViewPortManager;
import org.lorainelab.igb.visualization.model.ZoomableTrackRenderer;
import org.lorainelab.igb.visualization.tabs.TabPaneManager;
import org.lorainelab.igb.visualization.toolbar.ToolBarProvider;
import static org.lorainelab.igb.visualization.util.BoundsUtil.enforceRangeBounds;
import static org.lorainelab.igb.visualization.util.CanvasUtils.exponentialScaleTransform;
import static org.lorainelab.igb.visualization.util.CanvasUtils.invertExpScaleTransform;
import static org.lorainelab.igb.visualization.util.CanvasUtils.linearScaleTransform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true, provide = MainController.class)
public class MainController {

    private static final Logger LOG = LoggerFactory.getLogger(MainController.class);
    private static final int H_SLIDER_MAX = 100;

    @FXML
    private Slider hSlider;
    @FXML
    private Slider vSlider;
    @FXML
    private StackPane canvasContainer;
    @FXML
    private HBox labelHbox;
    @FXML
    private ScrollBar scrollY;
    @FXML
    private Pane xSliderPane;
    @FXML
    private Rectangle slider;
    @FXML
    private Rectangle leftSliderThumb;
    @FXML
    private Rectangle rightSliderThumb;
    @FXML
    private Button loadDataButton;
    @FXML
    private Button loadSequenceButton;

    @FXML
    private VBox root;

    @FXML
    private AnchorPane rightTabPaneContainer;

    @FXML
    private AnchorPane bottomTabPaneContainer;

    @FXML
    private PlusMinusSlider plusMinusSlider;

    private ToolBarProvider toolbarProvider;
    private Pane labelPane;
    private Set<TrackRenderer> trackRenderers;
    private DoubleProperty scrollX;
    private DoubleProperty hSliderWidget;
    private double lastDragX;
    private EventBus eventBus;
    private boolean ignoreScrollXEvent;
    private boolean ignoreHSliderEvent;
    private ViewPortManager viewPortManager;
    private double zoomStripeCoordinate;
    private CanvasPane canvasPane;
    private Canvas canvas;
    private double totalTrackHeight;
    private EventBusService eventBusService;
    private TabPaneManager tabPaneManager;
    private ZoomSliderMiniMapWidget zoomSliderMiniMapWidget;
    private MenuBarManager menuBarManager;
    private SelectionInfoService selectionInfoService;
    private GenomeVersion selectedGenomeVersion;
    private Chromosome selectedChromosome;
    private Footer footer;

    public MainController() {
        trackRenderers = Sets.newHashSet();
        scrollX = new SimpleDoubleProperty(0);
        hSliderWidget = new SimpleDoubleProperty(0);
        ignoreScrollXEvent = false;
        ignoreHSliderEvent = false;
        zoomStripeCoordinate = -1;
        lastDragX = 0;
    }

    @Activate
    public void activate() {
        eventBus = eventBusService.getEventBus();
        eventBus.register(this);
    }

    private void initializeChromosomeSelectionListener() {
        selectionInfoService.getSelectedChromosome().addListener((observable, oldValue, newValue) -> {
            newValue.ifPresent(newChromosomeSelection -> {
                if (selectedChromosome != newChromosomeSelection) {
                    selectedChromosome = newChromosomeSelection;
                    selectionInfoService.getSelectedGenomeVersion().get().ifPresent(gv -> {
                        if (selectedGenomeVersion == gv) {
                            trackRenderers.clear();
                            labelPane.getChildren().clear();
                            hSlider.setValue(0);
                            vSlider.setValue(0);
                            scrollY.setValue(0);
                            scrollY.setVisibleAmount(100);
                            Platform.runLater(() -> {
                                updateTrackRenderers(gv);
                            });
                        }
                    });
                }
            });
        });
    }

    private void initializeGenomeVersionSelectionListener() {
        selectionInfoService.getSelectedGenomeVersion().addListener((observable, oldValue, newValue) -> {
            trackRenderers.clear();
            labelPane.getChildren().clear();
            hSlider.setValue(0);
            vSlider.setValue(0);
            scrollY.setValue(0);
            scrollY.setVisibleAmount(100);
            newValue.ifPresent(genomeVersion -> {
                if (selectedGenomeVersion != genomeVersion) {
                    selectedGenomeVersion = genomeVersion;
                    Platform.runLater(() -> {
                        updateTrackRenderers(genomeVersion);
                    });
                }
            });
        });
    }

    private void updateTrackRenderers(GenomeVersion gv) {
        if (gv.getSelectedChromosomeProperty().get().isPresent()) {
            final Chromosome chromosome = gv.getSelectedChromosomeProperty().get().get();
            final CoordinateTrackRenderer coordinateTrackRenderer = new CoordinateTrackRenderer(canvasPane, chromosome);
            coordinateTrackRenderer.setWeight(getMinWeight());
            trackRenderers.add(coordinateTrackRenderer);
            loadDataSets(gv, chromosome);
        } else {
            gv.getReferenceSequenceProvider().getChromosomes().stream().findFirst().ifPresent(chr -> {
                final CoordinateTrackRenderer coordinateTrackRenderer = new CoordinateTrackRenderer(canvasPane, chr);
                coordinateTrackRenderer.setWeight(getMinWeight());
                trackRenderers.add(coordinateTrackRenderer);
                loadDataSets(gv, chr);
            });
        }
    }

    private void loadDataSets(GenomeVersion gv, final Chromosome chromosome) {
        gv.getLoadedDataSets().forEach(dataSet -> {
            Track positiveStrandTrack = dataSet.getPositiveStrandTrack(chromosome.getName());
            Track negativeStrandTrack = dataSet.getNegativeStrandTrack(gv.getSelectedChromosomeProperty().get().get().getName());
            final ZoomableTrackRenderer positiveStrandTrackRenderer = new ZoomableTrackRenderer(canvasPane, positiveStrandTrack, chromosome.getLength());
            positiveStrandTrackRenderer.setWeight(getMinWeight());
            final ZoomableTrackRenderer negativeStrandTrackRenderer = new ZoomableTrackRenderer(canvasPane, negativeStrandTrack, chromosome.getLength());
            negativeStrandTrackRenderer.setWeight(getMaxWeight());
            trackRenderers.add(positiveStrandTrackRenderer);
            trackRenderers.add(negativeStrandTrackRenderer);
            updateTrackRenderers();
        });
        gv.getLoadedDataSets().addListener((SetChangeListener.Change<? extends DataSet> change) -> {
            Platform.runLater(() -> {
                if (change.wasAdded()) {
                    Track positiveStrandTrack = change.getElementAdded().getPositiveStrandTrack(chromosome.getName());
                    Track negativeStrandTrack = change.getElementAdded().getNegativeStrandTrack(gv.getSelectedChromosomeProperty().get().get().getName());
                    final ZoomableTrackRenderer positiveStrandTrackRenderer = new ZoomableTrackRenderer(canvasPane, positiveStrandTrack, chromosome.getLength());
                    positiveStrandTrackRenderer.setWeight(getMinWeight());
                    final ZoomableTrackRenderer negativeStrandTrackRenderer = new ZoomableTrackRenderer(canvasPane, negativeStrandTrack, chromosome.getLength());
                    negativeStrandTrackRenderer.setWeight(getMaxWeight());
                    trackRenderers.add(positiveStrandTrackRenderer);
                    trackRenderers.add(negativeStrandTrackRenderer);
                    updateTrackRenderers();
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

    @Subscribe
    private void handleScrollScaleEvent(ScrollScaleEvent event) {
        if (event.getDirection().equals(Direction.INCREMENT)) {
            hSlider.increment();
        } else {
            hSlider.decrement();
        }
    }

    private double lastHSliderFire = -1;

    @FXML
    private void initialize() {
        initializeGuiComponents();
        initializeChromosomeSelectionListener();
        initializeGenomeVersionSelectionListener();
        initializeZoomScrollBar();
        viewPortManager = new ViewPortManager(canvas, trackRenderers, 0, 0);

        vSlider.valueProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            updateTrackRenderers();
        });
        hSlider.valueProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {

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

    private void initializeZoomScrollBar() {
        slider.setOnMousePressed((MouseEvent event) -> {
            lastDragX = event.getX();
        });
        leftSliderThumb.setOnMousePressed((MouseEvent event) -> {
            lastDragX = event.getX();
        });
        rightSliderThumb.setOnMousePressed((MouseEvent event) -> {
            lastDragX = event.getX();
        });

        rightSliderThumb.setOnMouseDragged((MouseEvent event) -> {
            double increment = Math.round(event.getX() - lastDragX);
            double newSliderValue = slider.getWidth() + increment;
            double newRightThumbValue = rightSliderThumb.getX() + increment;
            if (newSliderValue < TOTAL_SLIDER_THUMB_WIDTH) {
                newSliderValue = TOTAL_SLIDER_THUMB_WIDTH;
                newRightThumbValue = rightSliderThumb.getX() - slider.getWidth() + newSliderValue;
            }
            if (newSliderValue > xSliderPane.getWidth()) {
                double tmp = slider.getWidth() + (xSliderPane.getWidth() - slider.getWidth() - slider.getX());
                double diff = Math.abs(newSliderValue - tmp);
                newRightThumbValue -= diff;
                newSliderValue = tmp;
            }
            if (newSliderValue >= 0 && newSliderValue <= (xSliderPane.getWidth() - slider.getX())) {
                slider.setWidth(newSliderValue);
                rightSliderThumb.setX(newRightThumbValue);
                double max = xSliderPane.getWidth() - TOTAL_SLIDER_THUMB_WIDTH;
                double current = slider.getWidth() - TOTAL_SLIDER_THUMB_WIDTH;

                double maxSlider = xSliderPane.getWidth() - slider.getWidth();
                double currentSlider = slider.getX();
                double newScrollX;
                if (maxSlider < 0) {
                    newScrollX = 0;
                } else {
                    newScrollX = (currentSlider / maxSlider) * 100;
                }
                ignoreScrollXEvent = true;
                scrollX.setValue(newScrollX);
                hSliderWidget.setValue((1 - (current / max)) * 100);
            }
            lastDragX = event.getX();
        });

        leftSliderThumb.setOnMouseDragged((MouseEvent event) -> {
            double increment = Math.round(event.getX() - lastDragX);
            double newSliderValue = slider.getX() + increment;
            double newLeftThumbValue = leftSliderThumb.getX() + increment;
            double newSliderWidth = (slider.getWidth() - increment);
            if (newSliderWidth < TOTAL_SLIDER_THUMB_WIDTH) {
                newSliderWidth = TOTAL_SLIDER_THUMB_WIDTH;
                newSliderValue = slider.getX() + slider.getWidth() - newSliderWidth;
                newLeftThumbValue = leftSliderThumb.getX() + slider.getWidth() - newSliderWidth;
            }
            if (newSliderValue < 0) {
                newSliderValue = 0;
                newLeftThumbValue = 0;
                newSliderWidth = slider.getWidth() + slider.getX();
            }
            if (newSliderValue > xSliderPane.getWidth()) {
                newSliderValue = xSliderPane.getWidth();
            }
            if (newSliderValue >= 0 && newSliderValue <= xSliderPane.getWidth()) {
                slider.setX(newSliderValue);
                slider.setWidth(newSliderWidth);
                leftSliderThumb.setX(newLeftThumbValue);
                double max = xSliderPane.getWidth() - TOTAL_SLIDER_THUMB_WIDTH;
                double current = slider.getWidth() - TOTAL_SLIDER_THUMB_WIDTH;

                double maxSlider = xSliderPane.getWidth() - slider.getWidth();
                double currentSlider = slider.getX();
                double newScrollX;
                if (maxSlider <= 0) {
                    newScrollX = 0;
                } else {
                    newScrollX = (currentSlider / maxSlider) * 100;
                }
                ignoreScrollXEvent = true;
                scrollX.setValue(newScrollX);
                hSliderWidget.setValue((1 - (current / max)) * 100);
            }
            lastDragX = event.getX();
        });

        slider.setOnMouseDragged((MouseEvent event) -> {

            double increment = Math.round(event.getX() - lastDragX);
            double newSliderValue = slider.getX() + increment;
            double newRightThumbValue = rightSliderThumb.getX() + increment;
            double newLeftThumbValue = leftSliderThumb.getX() + increment;
            if (newSliderValue < 0) {
                newSliderValue = 0;
                newLeftThumbValue = 0;
                newRightThumbValue = rightSliderThumb.getX() - slider.getX();
            } else if (newSliderValue > (xSliderPane.getWidth() - slider.getWidth())) {
                newSliderValue = (xSliderPane.getWidth() - slider.getWidth());
                newLeftThumbValue = newSliderValue;
                newRightThumbValue = rightSliderThumb.getX() + xSliderPane.getWidth() - slider.getX() - slider.getWidth();

            }
            slider.setX(newSliderValue);
            leftSliderThumb.setX(newLeftThumbValue);
            rightSliderThumb.setX(newRightThumbValue);
            double max = xSliderPane.getWidth() - slider.getWidth();
            double current = slider.getX();
            resetZoomStripe();
            scrollX.setValue((current / max) * 100);
            lastDragX = event.getX();
        });
    }
    private static final int TOTAL_SLIDER_THUMB_WIDTH = 30;

    public void drawZoomCoordinateLine() {
        canvasPane.drawZoomCoordinateLine();
    }

    public void resetZoomStripe() {
        canvasPane.resetZoomStripe();
    }

    private void updateTrackRenderers() {
        canvasPane.clear();
        updateCanvasContexts();
        trackRenderers.forEach(trackRenderer -> trackRenderer.scaleCanvas(exponentialScaleTransform(canvasPane, hSlider.getValue()), scrollX.get(), scrollY.getValue()));
        eventBus.post(new ScaleEvent(hSlider.getValue(), vSlider.getValue(), scrollX.getValue(), scrollY.getValue()));
        drawZoomCoordinateLine();
    }

    private void updateScrollY() {
        double sum = trackRenderers.stream()
                .map(trackRenderer -> trackRenderer.getCanvasContext())
                .filter(canvasContext -> canvasContext.isVisible())
                .mapToDouble(canvasContext -> canvasContext.getBoundingRect().getHeight())
                .sum();
        scrollY.setVisibleAmount((sum / totalTrackHeight) * 100);
    }

    private void initializeGuiComponents() {
        setupPlusMinusSlider();
        addMenuBar();
        addTopToolbar();
        addFooter();
        addTabPanes();
        labelPane = new Pane();
        HBox.setHgrow(labelPane, Priority.ALWAYS);
        labelHbox.getChildren().add(labelPane);
        canvas = canvasPane.getCanvas();
        canvasContainer.getChildren().add(canvasPane);
        scrollY.setBlockIncrement(.01);
        hSlider.setValue(0);
        hSlider.setMin(0);
        hSlider.setMax(H_SLIDER_MAX);
        hSlider.setBlockIncrement(2);
        hSlider.setMajorTickUnit(1);
        hSlider.setMinorTickCount(0);
        hSlider.setSnapToTicks(true);
        vSlider.setMin(0);
        vSlider.setValue(0);
        vSlider.setMax(100);
        scrollY.setMin(0);
        scrollY.setMax(100);
        scrollY.setVisibleAmount(100);
        loadDataButton.setOnAction(action -> {
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
        loadSequenceButton.setOnAction(action -> {
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
    }

    private void updateCanvasContexts() {
        viewPortManager.refresh(vSlider.getValue(), scrollY.getValue());
        totalTrackHeight = viewPortManager.getTotalTrackSize();
        updateScrollY();
        updateTrackLabels();
    }

    private void updateTrackLabels() {
        Platform.runLater(() -> {
            labelPane.getChildren().clear();
            trackRenderers.stream()
                    .filter(trackRenderer -> trackRenderer.getCanvasContext().isVisible())
                    .forEach(trackRenderer -> {
                        TrackLabel trackLabel = trackRenderer.getTrackLabel();
                        trackLabel.setDimensions(labelPane);
                        StackPane content = trackLabel.getContent();
                        labelPane.getChildren().add(content);
                    });
            labelPane.getChildren().stream()
                    .filter(node -> node instanceof StackPane)
                    .map(node -> (StackPane) node)
                    .forEach((StackPane trackLabelNode) -> {
                        trackLabelNode.setOnDragDetected(
                                (MouseEvent event) -> {
                                    if (event.getSource() instanceof StackPane) {
                                        Pane rootPane = (Pane) trackLabelNode.getScene().getRoot();
                                        rootPane.setOnDragOver(dragEvent -> {
                                            dragEvent.acceptTransferModes(TransferMode.ANY);
                                            dragEvent.consume();
                                        });
                                        SnapshotParameters snapshotParams = new SnapshotParameters();
                                        snapshotParams.setTransform(Transform.scale(0.75, 0.75));
                                        WritableImage snapshot = trackLabelNode.snapshot(snapshotParams, null);
                                        Dragboard db = trackLabelNode.startDragAndDrop(TransferMode.MOVE);
                                        ClipboardContent clipboardContent = new ClipboardContent();
                                        clipboardContent.put(DataFormat.PLAIN_TEXT, trackLabelNode.getBoundsInParent().getMinY());
                                        db.setDragView(snapshot, 5, 5);
                                        db.setContent(clipboardContent);
                                    }
                                    event.consume();
                                }
                        );
                        trackLabelNode.setOnDragDropped((DragEvent event) -> {
                            if (event.getSource() instanceof StackPane) {
                                StackPane dropLocationLabelNode = StackPane.class.cast(event.getSource());
                                boolean droppedAbove = event.getY() < (dropLocationLabelNode.getHeight() / 2);
                                double dropLocationMinY = dropLocationLabelNode.getBoundsInParent().getMinY();
                                Object dragboardContent = event.getDragboard().getContent(DataFormat.PLAIN_TEXT);
                                if (dragboardContent instanceof Double) {
                                    double eventTriggerMinY = (Double) dragboardContent;
                                    if (dropLocationMinY != eventTriggerMinY) {
                                        Lists.newArrayList(trackRenderers).stream()
                                                .filter(trackRenderer -> trackRenderer.getCanvasContext().isVisible())
                                                .filter(draggedTrackRenderer -> draggedTrackRenderer.getTrackLabel().getContent().getBoundsInParent().getMinY() == eventTriggerMinY)
                                                .findFirst()
                                                .ifPresent(draggedTrackRenderer -> {
                                                    Lists.newArrayList(trackRenderers).stream()
                                                            .filter(trackRenderer -> trackRenderer.getTrackLabel().getContent() == dropLocationLabelNode)
                                                            .findFirst()
                                                            .ifPresent(droppedTrackRenderer -> {
                                                                int droppedIndex = droppedTrackRenderer.getWeight();
                                                                if (droppedAbove) {
                                                                    trackRenderers.remove(draggedTrackRenderer);
                                                                    draggedTrackRenderer.setWeight(droppedIndex - 1);
                                                                    trackRenderers.add(draggedTrackRenderer);
                                                                } else {
                                                                    trackRenderers.remove(draggedTrackRenderer);
                                                                    draggedTrackRenderer.setWeight(droppedIndex + 1);
                                                                    trackRenderers.add(draggedTrackRenderer);
                                                                }
                                                                updateTrackRenderers();
                                                            });
                                                });
                                    }
                                }

                            }
                            event.consume();
                        });
                    });
        });

    }

    private void refreshSliderWidget() {
        if (xSliderPane.getWidth() > 0) {
            double max = xSliderPane.getWidth() - slider.getWidth();
            double current = slider.getX();
            double newXValue = (max * scrollX.getValue() / 100);

            if (newXValue <= 0) {
                newXValue = 0;
            }
            if (slider.getWidth() >= xSliderPane.getWidth()) {
                double newWidth = xSliderPane.getWidth();
                if (newWidth < TOTAL_SLIDER_THUMB_WIDTH) {
                    newWidth = TOTAL_SLIDER_THUMB_WIDTH;
                }
                double oldWidth = slider.getWidth();
                slider.setWidth(newWidth);
                rightSliderThumb.setX(rightSliderThumb.getX() + newWidth - oldWidth);
            }
            if (scrollX.getValue() >= 0 && xSliderPane.getWidth() > TOTAL_SLIDER_THUMB_WIDTH) {
                slider.setX(newXValue);
                leftSliderThumb.setX(newXValue);
                double maxPaneWidth = xSliderPane.getWidth() - TOTAL_SLIDER_THUMB_WIDTH;
                double newSliderWidth = -maxPaneWidth * ((hSliderWidget.getValue() / 100) - 1) + TOTAL_SLIDER_THUMB_WIDTH;
                double rightThumbX = rightSliderThumb.getX() + newXValue - current - slider.getWidth() + newSliderWidth;
                rightSliderThumb.setX(rightThumbX);
                slider.setWidth(newSliderWidth);

            }
        }
    }

    @Subscribe
    private void jumpZoom(JumpZoomEvent jumpZoomEvent) {
        Rectangle2D focusRect = jumpZoomEvent.getRect();
        TrackRenderer eventLocationReference = jumpZoomEvent.getTrackRenderer();
        View view = eventLocationReference.getView();
        if (focusRect.intersects(view.getBoundingRect())) {
            double modelWidth = canvasPane.getModelWidth();
            double minX = Math.max(focusRect.getMinX(), view.getBoundingRect().getMinX());
            double maxX = Math.min(focusRect.getMaxX(), view.getBoundingRect().getMaxX());
            double width = maxX - minX;
            if (width < MAX_ZOOM_MODEL_COORDINATES_X) {
                width = Math.max(width * 1.1, MAX_ZOOM_MODEL_COORDINATES_X);
                minX = Math.max((minX + focusRect.getWidth() / 2) - (width / 2), 0);
            }
            final double scaleXalt = eventLocationReference.getCanvasContext().getBoundingRect().getWidth() / width;
            resetZoomStripe();
            hSlider.setValue(invertExpScaleTransform(canvasPane, scaleXalt));
            double scrollXValue = (minX / (modelWidth - width)) * 100;
            scrollXValue = enforceRangeBounds(scrollXValue, 0, 100);
            scrollX.setValue(scrollXValue);
        }
    }

    @Subscribe
    private void handleScrollXUpdateEvent(ScrollXUpdate e) {
        ignoreScrollXEvent = true;
        double scrollXValue = enforceRangeBounds(e.getScrollX(), 0, 100);
        scrollX.setValue(scrollXValue);
    }

    @Subscribe
    private void clickDragZoomListener(ClickDragZoomEvent event) {
        double x1 = event.getStartX();
        final double x2 = event.getEndX() - event.getStartX();
        final Rectangle2D zoomFocus = new Rectangle2D(x1, 0, x2, Double.MAX_VALUE);
        trackRenderers.stream()
                .filter(track -> track instanceof CoordinateTrackRenderer)
                .findFirst().ifPresent(coordinateRenderer -> {
                    jumpZoom(new JumpZoomEvent(zoomFocus, coordinateRenderer));
                });
    }

    private void syncWidgetSlider() {
        double minScaleX = canvasPane.getModelWidth();
        double maxScaleX = MAX_ZOOM_MODEL_COORDINATES_X - 1;
        final double scaleRange = maxScaleX - minScaleX;
        final double xFactor = exponentialScaleTransform(canvasPane, hSlider.getValue());
        final double current = Math.floor(canvasPane.getWidth() / xFactor);
        double scaledPercentage = (current - minScaleX) / scaleRange;

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
        hSlider.setValue(invertExpScaleTransform(canvasPane, xFactor));
    }

    public DoubleProperty getHSliderValue() {
        return hSlider.valueProperty();
    }

    public DoubleProperty getXScrollPosition() {
        return scrollX;
    }

    @Reference
    public void setCanvasPane(CanvasPane canvasPane) {
        this.canvasPane = canvasPane;
    }

    @Reference
    public void setEventBusService(EventBusService eventBusService) {
        this.eventBusService = eventBusService;
    }

    @Reference
    public void setTabPaneManager(TabPaneManager tabPaneManager) {
        this.tabPaneManager = tabPaneManager;
    }

    @Reference
    public void setMenuBarManager(MenuBarManager menuBarManager) {
        this.menuBarManager = menuBarManager;
    }

    @Reference
    public void setSelectionInfoService(SelectionInfoService selectionInfoService) {
        this.selectionInfoService = selectionInfoService;
    }

    @Subscribe
    private void updateGlyphSelections(SelectionChangeEvent event) {
        selectionInfoService.getSelectedGlyphs().clear();
        trackRenderers.stream()
                .filter(renderer -> renderer instanceof ZoomableTrackRenderer)
                .map(renderer -> ZoomableTrackRenderer.class.cast(renderer))
                .forEach(renderer -> {
                    selectionInfoService.getSelectedGlyphs().addAll(
                            renderer.getTrack().getGlyphs()
                            .stream()
                            .filter(glyph -> glyph.isSelected())
                            .collect(Collectors.toList())
                    );
                });
    }

    private void addTabPanes() {
        rightTabPaneContainer.getChildren().add(tabPaneManager.getRightTabPane());
        bottomTabPaneContainer.getChildren().add(tabPaneManager.getBottomTabPane());
    }

    @Reference
    public void setToolbarProvider(ToolBarProvider toolbarProvider) {
        this.toolbarProvider = toolbarProvider;
    }

    @Reference
    public void setFooter(Footer footer) {
        this.footer = footer;
    }

    @Deactivate
    private void deactivate() {
        root.getChildren().clear();
    }

    private void addMenuBar() {
        root.getChildren().add(0, menuBarManager.getMenuBar());
    }

    private void addTopToolbar() {
        root.getChildren().add(1, toolbarProvider.getTopToolbar());
    }

    private void addFooter() {
        root.getChildren().add(3, footer);
    }

    private Range<Integer> getCurrentRange() {
        final double xFactor = exponentialScaleTransform(canvasPane, hSlider.getValue());
        final double visibleVirtualCoordinatesX = Math.floor(canvasPane.getWidth() / xFactor);
        double xOffset = Math.round((scrollX.doubleValue() / 100) * (canvasPane.getModelWidth() - visibleVirtualCoordinatesX));
        return Range.closedOpen((int) xOffset, (int) xOffset + (int) visibleVirtualCoordinatesX);
    }

    private void setupPlusMinusSlider() {
        plusMinusSlider.setOnValueChanged((PlusMinusEvent event) -> {
            final double updatedScrollXValue = getUpdatedScrollxValue(event.getValue());
            if (updatedScrollXValue != scrollX.doubleValue()) {
                if (Platform.isFxApplicationThread()) {
                    scrollX.setValue(updatedScrollXValue);
                    resetZoomStripe();
                    syncWidgetSlider();
                } else {
                    Platform.runLater(() -> {
                        scrollX.setValue(updatedScrollXValue);
                        resetZoomStripe();
                        syncWidgetSlider();
                    });
                }
            }
        });
    }

    private double getUpdatedScrollxValue(double eventValue) {
        double updatedScrollXValue = scrollX.doubleValue() + getAdjustedScrollValue(eventValue);
        updatedScrollXValue = enforceRangeBounds(updatedScrollXValue, 0, 100);
        return updatedScrollXValue;
    }

    private double getAdjustedScrollValue(double value) {
        if (value < -0.8 || value > 0.8) {
            return value;
        } else if ((value < 0 && value > -0.1) || value < .1) {
            return value / 10000;
        } else if ((value < 0 && value > -0.2) || value < .2) {
            return value / 2000;
        }
        return value / 50;
    }

}
