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
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.SetChangeListener;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
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
import javafx.scene.transform.Transform;
import javafx.util.Duration;
import org.controlsfx.control.PlusMinusSlider;
import org.controlsfx.control.PlusMinusSlider.PlusMinusEvent;
import org.controlsfx.control.RangeSlider;
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
import org.lorainelab.igb.visualization.menubar.MenuBarManager;
import org.lorainelab.igb.visualization.model.CoordinateTrackRenderer;
import org.lorainelab.igb.visualization.model.JumpZoomEvent;
import org.lorainelab.igb.visualization.model.TrackLabel;
import org.lorainelab.igb.visualization.model.TrackRenderer;
import static org.lorainelab.igb.visualization.model.TrackRenderer.MAX_ZOOM_MODEL_COORDINATES_X;
import org.lorainelab.igb.visualization.model.ViewPortManager;
import org.lorainelab.igb.visualization.model.ZoomableTrackRenderer;
import org.lorainelab.igb.visualization.tabs.TabPaneManager;
import static org.lorainelab.igb.visualization.util.CanvasUtils.exponentialScaleTransform;
import static org.lorainelab.igb.visualization.util.CanvasUtils.invertExpScaleTransform;
import static org.lorainelab.igb.visualization.util.CanvasUtils.linearScaleTransform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true, provide = GenoVixFxController.class)
public class GenoVixFxController {

    private static final Logger LOG = LoggerFactory.getLogger(GenoVixFxController.class);
    private static final int H_SLIDER_MAX = 100;

    @FXML
    private Slider hSlider;
    @FXML
    private Slider vSlider;
    @FXML
    private StackPane stackPane;
    @FXML
    private HBox labelHbox;
    @FXML
    private ScrollBar scrollY;
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

    @FXML
    private RangeSlider rangeSlider;

    @FXML
    private ProgressBar memoryProgressBar;
    @FXML
    private Label memoryLabel;

    @FXML
    private FontAwesomeIconView gcTrashIcon;

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

    public GenoVixFxController() {
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
                updatedRangeSlider();
                updateTrackRenderers();
                lastHSliderFire = newValue.doubleValue();
            }
        });
        hSliderWidget.addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            resetZoomStripe();
            if (lastHSliderFire < 0 || Math.abs(lastHSliderFire - newValue.doubleValue()) > 1) {
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
                updatedRangeSlider();
                updateTrackRenderers();
            });
        });

        canvas.heightProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            Platform.runLater(() -> {
                updatedRangeSlider();
                updateTrackRenderers();
            });
        });

        scrollX.addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
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
//            updatedRangeSlider();
            updateTrackRenderers();
        });

    }

    public void drawZoomCoordinateLine() {
        canvasPane.drawZoomCoordinateLine();
    }

    public void resetZoomStripe() {
        canvasPane.resetZoomStripe();
    }

    private void updateTrackRenderers() {
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
        setupMemoryInfoWidget();
        setupPlusMinusSlider();
        setupRangeSlider();
        addZoomSliderMiniMapWidget();
        addMenuBar();
        addTabPanes();
        labelPane = new Pane();
        HBox.setHgrow(labelPane, Priority.ALWAYS);
        labelHbox.getChildren().add(labelPane);
        canvas = canvasPane.getCanvas();
        stackPane.getChildren().add(canvasPane);
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
                            dataSet.loadRegion(selectedChromosome.getName(), Range.closedOpen(0, 20000));
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
                        trackLabelNode.setOnDragDropped(new EventHandler<DragEvent>() {
                            @Override
                            public void handle(DragEvent event) {
                                final Object dropLocationSource = event.getSource();
                                if (dropLocationSource instanceof StackPane) {
                                    StackPane dropLocationLabelNode = StackPane.class.cast(dropLocationSource);
                                    boolean droppedAbove = event.getY() < (dropLocationLabelNode.getHeight() / 2);
                                    double dropLocationMinY = dropLocationLabelNode.getBoundsInParent().getMinY();
                                    Dragboard db = event.getDragboard();
                                    Object dragboardContent = db.getContent(DataFormat.PLAIN_TEXT);
                                    if (dragboardContent instanceof Double) {
                                        double eventTriggerMinY = (Double) dragboardContent;
                                        if (dropLocationMinY != eventTriggerMinY) {

                                            Iterator<TrackRenderer> iterator = trackRenderers.iterator();
                                            while (iterator.hasNext()) {
                                                TrackRenderer trackRenderer = iterator.next();
                                                if (trackRenderer.getCanvasContext().isVisible()) {
                                                    StackPane labelNode = trackRenderer.getTrackLabel().getContent();
                                                    if (labelNode.getBoundsInParent().getMinY() == eventTriggerMinY) {

                                                    }
                                                }
                                            }

                                            Lists.newArrayList(trackRenderers).stream()
                                                    .filter(trackRenderer -> trackRenderer.getCanvasContext().isVisible())
                                                    .forEach(draggedTrackRenderer -> {
                                                        StackPane labelNode = draggedTrackRenderer.getTrackLabel().getContent();
                                                        if (labelNode.getBoundsInParent().getMinY() == eventTriggerMinY) {
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
                                                        }
                                                    });

                                        }
                                    }

                                }
                                event.consume();
                            }
                        });
                    });
        });

    }

    @Subscribe
    private void jumpZoom(JumpZoomEvent jumpZoomEvent) {
        Rectangle2D focusRect = jumpZoomEvent.getRect();
        TrackRenderer eventLocationReference = jumpZoomEvent.getTrackRenderer();
        View view = eventLocationReference.getView();
        if (focusRect.intersects(view.getBoundingRect())) {
            double modelWidth = eventLocationReference.getModelWidth();
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
            scrollX.setValue(scrollXValue);
        }

    }

    @Subscribe
    private void handleScrollXUpdateEvent(ScrollXUpdate e) {
        ignoreScrollXEvent = true;
        scrollX.setValue(e.getScrollX());
        //syncSlider();
    }

    @Subscribe
    private void clickDragZoomListener(ClickDragZoomEvent event) {
        final Rectangle2D zoomFocus = new Rectangle2D(event.getStartX(), 0, event.getEndX() - event.getStartX(), Double.MAX_VALUE);
        trackRenderers.stream()
                .filter(track -> track instanceof CoordinateTrackRenderer)
                .findFirst().ifPresent(coordinateRenderer -> {
                    jumpZoom(new JumpZoomEvent(zoomFocus, coordinateRenderer));
                });
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

//    @Reference
//    public void setZoomSliderMiniMapWidget(ZoomSliderMiniMapWidget zoomSliderMiniMapWidget) {
//        this.zoomSliderMiniMapWidget = zoomSliderMiniMapWidget;
//    }
    private void addTabPanes() {
        rightTabPaneContainer.getChildren().add(tabPaneManager.getRightTabPane());
        bottomTabPaneContainer.getChildren().add(tabPaneManager.getBottomTabPane());
    }

    private void addZoomSliderMiniMapWidget() {
//        zoomSliderMiniMapWidgetContainer.getChildren().add(zoomSliderMiniMapWidget.getContent());
    }

    @Deactivate
    private void deactivate() {
        root.getChildren().clear();
    }

    private void addMenuBar() {
        root.getChildren().add(0, menuBarManager.getMenuBar());
    }

    private Range<Integer> getCurrentRange() {
        final double xFactor = exponentialScaleTransform(canvasPane, hSlider.getValue());
        final double visibleVirtualCoordinatesX = Math.floor(canvasPane.getWidth() / xFactor);
        double xOffset = Math.round((scrollX.doubleValue() / 100) * (canvasPane.getModelWidth() - visibleVirtualCoordinatesX));
        return Range.closedOpen((int) xOffset, (int) xOffset + (int) visibleVirtualCoordinatesX);
    }

    private void setupPlusMinusSlider() {
        plusMinusSlider.setOnValueChanged(new EventHandler<PlusMinusEvent>() {
            @Override
            public void handle(PlusMinusEvent event) {
                final double adjustedEventValue = getAdjustedScrollValue(event.getValue());
                double updatedScrollXValue = scrollX.doubleValue() + adjustedEventValue;
                if (updatedScrollXValue < 0) {
                    updatedScrollXValue = 0;
                } else if (updatedScrollXValue > 100) {
                    updatedScrollXValue = 100;
                }
                if (updatedScrollXValue != scrollX.doubleValue()) {
                    if (Platform.isFxApplicationThread()) {
                        scrollX.setValue(updatedScrollXValue);
                        updatedRangeSlider();
                    } else {
                        final double updatedScrollXValueFinal = updatedScrollXValue;//...
                        Platform.runLater(() -> {
                            scrollX.setValue(updatedScrollXValueFinal);
                            updatedRangeSlider();
                        });
                    }
                }
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
        );
    }

    private void setupMemoryInfoWidget() {
        gcTrashIcon.setOnMouseClicked(e -> System.gc());
        Timeline memoryInfoTimeline = new Timeline(new KeyFrame(Duration.seconds(2), (event) -> {
            final int freeMemory = (int) (Runtime.getRuntime().freeMemory() / (1024 * 1024));
            final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / (1024 * 1024));
            final int totalMemory = (int) (Runtime.getRuntime().totalMemory() / (1024 * 1024));
            final double usedPercentage = (double) (totalMemory - freeMemory) / (double) maxMemory;
            final String memoryLabelText = (totalMemory - freeMemory) + "M of " + maxMemory + 'M';
            if (Platform.isFxApplicationThread()) {
                memoryProgressBar.setProgress(usedPercentage);
                memoryLabel.setText(memoryLabelText);
            } else {
                Platform.runLater(() -> {
                    memoryProgressBar.setProgress(usedPercentage);
                    memoryLabel.setText(memoryLabelText);
                });
            }
        }));
        memoryInfoTimeline.setCycleCount(Timeline.INDEFINITE);
        memoryInfoTimeline.play();
    }

    private void setupRangeSlider() {
        rangeSlider.setMin(0);
        rangeSlider.setMax(100);
        rangeSlider.setLowValue(0);
        rangeSlider.setHighValue(100);
        rangeSlider.setBlockIncrement(2);
        rangeSlider.lowValueProperty().addListener((observable, oldValue, newValue) -> {
            double updatedScrollX = newValue.doubleValue();
            if (updatedScrollX < 0.1) {
                updatedScrollX = 0;
            } else if (updatedScrollX > 99.8) {
                updatedScrollX = 100;
            }
            resetZoomStripe();
            scrollX.setValue(newValue);
            updateTrackRenderers();
        });
        rangeSliderHighValueListener = (observable, oldValue, newValue) -> {
            final double updatedHsliderPosition = 100 - (newValue.doubleValue() - rangeSlider.lowValueProperty().doubleValue());
            hSlider.setValue(updatedHsliderPosition);
            resetZoomStripe();
        };
        rangeSlider.highValueProperty().addListener(rangeSliderHighValueListener);
    }
    private ChangeListener<Number> rangeSliderHighValueListener;

    private void updatedRangeSlider() {
        double modelWidth = canvasPane.getModelWidth();
        final double xFactor = exponentialScaleTransform(canvasPane, hSlider.getValue());
        final double visibleXCoordinates = Math.floor(canvasPane.getWidth() / xFactor);
        double percentageInView = visibleXCoordinates / modelWidth;
        rangeSlider.highValueProperty().removeListener(rangeSliderHighValueListener);
        rangeSlider.highValueProperty().set(rangeSlider.lowValueProperty().doubleValue() + (percentageInView * 100));
        rangeSlider.highValueProperty().addListener(rangeSliderHighValueListener);
    }

}
