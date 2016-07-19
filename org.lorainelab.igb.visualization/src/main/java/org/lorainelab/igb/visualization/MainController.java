package org.lorainelab.igb.visualization;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Reference;
import com.google.common.collect.Sets;
import com.google.common.eventbus.Subscribe;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.geometry.Side;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import org.controlsfx.control.PlusMinusSlider;
import org.lorainelab.igb.data.model.Chromosome;
import org.lorainelab.igb.data.model.DataSet;
import org.lorainelab.igb.data.model.GenomeVersion;
import org.lorainelab.igb.search.api.SearchService;
import org.lorainelab.igb.search.api.model.Document;
import org.lorainelab.igb.search.api.model.IndexIdentity;
import org.lorainelab.igb.selections.SelectionInfoService;
import org.lorainelab.igb.visualization.component.App;
import org.lorainelab.igb.visualization.component.AppProps;
import org.lorainelab.igb.visualization.component.api.Component;
import org.lorainelab.igb.visualization.component.api.Props;
import org.lorainelab.igb.visualization.component.api.State;
import org.lorainelab.igb.visualization.event.SelectionChangeEvent;
import org.lorainelab.igb.visualization.footer.Footer;
import org.lorainelab.igb.visualization.menubar.MenuBarManager;
import org.lorainelab.igb.visualization.model.TrackRenderer;
import org.lorainelab.igb.visualization.model.ZoomableTrackRenderer;
import org.lorainelab.igb.visualization.store.AppStore;
import org.lorainelab.igb.visualization.tabs.TabPaneManager;
import org.lorainelab.igb.visualization.toolbar.ToolBarManager;
import static org.lorainelab.igb.visualization.util.BoundsUtil.enforceRangeBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@aQute.bnd.annotation.component.Component(immediate = true, provide = MainController.class)
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
    private TextField search;
    private ContextMenu searchAutocomplete;
    private final SortedSet<String> autocompleteEntries;

    @FXML
    private VBox root;

    @FXML
    private AnchorPane rightTabPaneContainer;

    @FXML
    private AnchorPane bottomTabPaneContainer;

    @FXML
    private PlusMinusSlider plusMinusSlider;

    private ToolBarManager toolbarProvider;
    private Pane labelPane;
    private Set<TrackRenderer> trackRenderers;
    private Set<DataSet> loadedDataSets;
    private DoubleProperty scrollX;
    private DoubleProperty hSliderWidget;
    private double lastDragX;
    //private EventBus eventBus;
    private boolean ignoreScrollXEvent;
    private boolean ignoreHSliderEvent;

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
    private SearchService searchService;
    //Apps
    private Component app;

    public MainController() {
        trackRenderers = Sets.newConcurrentHashSet();
        loadedDataSets = Sets.newConcurrentHashSet();
        scrollX = new SimpleDoubleProperty(0);
        hSliderWidget = new SimpleDoubleProperty(0);
        ignoreScrollXEvent = false;
        ignoreHSliderEvent = false;
        zoomStripeCoordinate = -1;
        lastDragX = 0;
        autocompleteEntries = Sets.newTreeSet();
        searchAutocomplete = new ContextMenu();
        searchAutocomplete.hide();
        app = new App();

    }

    private void startApp() {
        AppProps props = new AppProps(
                hSlider,
                scrollX,
                scrollY,
                zoomStripeCoordinate,
                canvasPane,
                selectionInfoService,
                selectedChromosome,
                selectedGenomeVersion,
                vSlider,
                totalTrackHeight,
                hSliderWidget,
                slider,
                xSliderPane,
                leftSliderThumb,
                rightSliderThumb,
                labelPane,
                loadDataButton,
                loadSequenceButton
        );
        renderComponents(
                app.withAttributes(props).beforeComponentReady()
        );
    }

    @Activate
    public void activate() {

//        eventBus = eventBusService.getEventBus();
//        eventBus.register(this);
    }

    private void renderComponents(Component<Props, State> component) {
        component.render().forEach(child -> {
            renderComponents(child);
        });
    }

    @Reference
    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    private void initializeSearch() {
        Platform.runLater(() -> {
            search.setOnKeyReleased(e -> {
                if (search.getText().length() == 0) {
                    searchAutocomplete.hide();
                } else {
                    LinkedList<Document> searchResult = new LinkedList<>();
                    Optional<IndexIdentity> resourceIndexIdentity
                            = searchService.getResourceIndexIdentity(
                                    selectedGenomeVersion.getSpeciesName());
                    if (resourceIndexIdentity.isPresent()) {
                        //TODO: refactor to boolean queries in search module
                        searchService.search("(chromosomeId:" + selectedChromosome.getName() + ") AND (id:" + search.getText() + "*)",
                                resourceIndexIdentity.get()).stream()
                                .forEach(doc -> searchResult.add(doc));
                    }
                    if (searchResult.size() > 0) {
                        populatePopup(searchResult);
                        if (!searchAutocomplete.isShowing()) {
                            searchAutocomplete.show(search, Side.BOTTOM, 0, 0);
                        }
                    } else {
                        searchAutocomplete.hide();
                    }
                }

            });
        });
    }

    private void populatePopup(List<Document> searchResult) {
        List<CustomMenuItem> menuItems = new LinkedList<>();
        int maxEntries = 10;
        int count = Math.min(searchResult.size(), maxEntries);
        for (int i = 0; i < count; i++) {
            final Document result = searchResult.get(i);
            Label entryLabel = new Label(result.getFields().get("id"));
            CustomMenuItem item = new CustomMenuItem(entryLabel, true);
            item.setOnAction((ActionEvent actionEvent) -> {
                Platform.runLater(() -> {
                    search.setText(result.getFields().get("id"));
                    searchAutocomplete.hide();
                    int start = Integer.parseInt(result.getFields().get("start"));
                    int end = Integer.parseInt(result.getFields().get("end"));
                    trackRenderers.stream().findFirst().ifPresent(trackRender -> {
                        Rectangle2D oldRect = trackRender.getCanvasContext().getBoundingRect();
                        Rectangle2D rect = new Rectangle2D(start, oldRect.getMinY(), end - start, oldRect.getHeight());
                        LOG.info("start: {} end: {}", start, end);
//                        eventBus.post(new JumpZoomEvent(rect, trackRender));
                    });
                });

            });
            menuItems.add(item);
        }
        searchAutocomplete.getItems().clear();
        searchAutocomplete.getItems().addAll(menuItems);

    }

//    @Subscribe
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
//
//    @Subscribe
//    public void handleClickDragEndEvent(ClickDragEndEvent event) {
//        if (canvasPane.getHeight() > viewPortManager.getTotalTrackSize()) {
//            canvasPane.clear();
//            updateTrackRenderers();
//        }
//    }
    @FXML
    private void initialize() {
        initializeGuiComponents();
        //initializeChromosomeSelectionListener();
        //initializeGenomeVersionSelectionListener();
        //initializeZoomScrollBar();
        //initializeSearch();
        startApp();

    }

//    private void initializeZoomScrollBar() {
//        slider.setOnMousePressed((MouseEvent event) -> {
//            lastDragX = event.getX();
//        });
//        leftSliderThumb.setOnMousePressed((MouseEvent event) -> {
//            lastDragX = event.getX();
//        });
//        rightSliderThumb.setOnMousePressed((MouseEvent event) -> {
//            lastDragX = event.getX();
//        });
//
//        rightSliderThumb.setOnMouseDragged((MouseEvent event) -> {
//            double increment = Math.round(event.getX() - lastDragX);
//            double newSliderValue = slider.getWidth() + increment;
//            double newRightThumbValue = rightSliderThumb.getX() + increment;
//            if (newSliderValue < TOTAL_SLIDER_THUMB_WIDTH) {
//                newSliderValue = TOTAL_SLIDER_THUMB_WIDTH;
//                newRightThumbValue = rightSliderThumb.getX() - slider.getWidth() + newSliderValue;
//            }
//            if (newSliderValue > xSliderPane.getWidth()) {
//                double tmp = slider.getWidth() + (xSliderPane.getWidth() - slider.getWidth() - slider.getX());
//                double diff = Math.abs(newSliderValue - tmp);
//                newRightThumbValue -= diff;
//                newSliderValue = tmp;
//            }
//            if (newSliderValue >= 0 && newSliderValue <= (xSliderPane.getWidth() - slider.getX())) {
//                slider.setWidth(newSliderValue);
//                rightSliderThumb.setX(newRightThumbValue);
//                double max = xSliderPane.getWidth() - TOTAL_SLIDER_THUMB_WIDTH;
//                double current = slider.getWidth() - TOTAL_SLIDER_THUMB_WIDTH;
//
//                double maxSlider = xSliderPane.getWidth() - slider.getWidth();
//                double currentSlider = slider.getX();
//                double newScrollX;
//                if (maxSlider < 0) {
//                    newScrollX = 0;
//                } else {
//                    newScrollX = (currentSlider / maxSlider) * 100;
//                }
//                ignoreScrollXEvent = true;
//                scrollX.setValue(newScrollX);
//                hSliderWidget.setValue((1 - (current / max)) * 100);
//            }
//            lastDragX = event.getX();
//        });
//
//        leftSliderThumb.setOnMouseDragged((MouseEvent event) -> {
//            double increment = Math.round(event.getX() - lastDragX);
//            double newSliderValue = slider.getX() + increment;
//            double newLeftThumbValue = leftSliderThumb.getX() + increment;
//            double newSliderWidth = (slider.getWidth() - increment);
//            if (newSliderWidth < TOTAL_SLIDER_THUMB_WIDTH) {
//                newSliderWidth = TOTAL_SLIDER_THUMB_WIDTH;
//                newSliderValue = slider.getX() + slider.getWidth() - newSliderWidth;
//                newLeftThumbValue = leftSliderThumb.getX() + slider.getWidth() - newSliderWidth;
//            }
//            if (newSliderValue < 0) {
//                newSliderValue = 0;
//                newLeftThumbValue = 0;
//                newSliderWidth = slider.getWidth() + slider.getX();
//            }
//            if (newSliderValue > xSliderPane.getWidth()) {
//                newSliderValue = xSliderPane.getWidth();
//            }
//            if (newSliderValue >= 0 && newSliderValue <= xSliderPane.getWidth()) {
//                slider.setX(newSliderValue);
//                slider.setWidth(newSliderWidth);
//                leftSliderThumb.setX(newLeftThumbValue);
//                double max = xSliderPane.getWidth() - TOTAL_SLIDER_THUMB_WIDTH;
//                double current = slider.getWidth() - TOTAL_SLIDER_THUMB_WIDTH;
//
//                double maxSlider = xSliderPane.getWidth() - slider.getWidth();
//                double currentSlider = slider.getX();
//                double newScrollX;
//                if (maxSlider <= 0) {
//                    newScrollX = 0;
//                } else {
//                    newScrollX = (currentSlider / maxSlider) * 100;
//                }
//                ignoreScrollXEvent = true;
//                scrollX.setValue(newScrollX);
//                hSliderWidget.setValue((1 - (current / max)) * 100);
//            }
//            lastDragX = event.getX();
//        });
//
//        slider.setOnMouseDragged((MouseEvent event) -> {
//
//            double increment = Math.round(event.getX() - lastDragX);
//            double newSliderValue = slider.getX() + increment;
//            double newRightThumbValue = rightSliderThumb.getX() + increment;
//            double newLeftThumbValue = leftSliderThumb.getX() + increment;
//            if (newSliderValue < 0) {
//                newSliderValue = 0;
//                newLeftThumbValue = 0;
//                newRightThumbValue = rightSliderThumb.getX() - slider.getX();
//            } else if (newSliderValue > (xSliderPane.getWidth() - slider.getWidth())) {
//                newSliderValue = (xSliderPane.getWidth() - slider.getWidth());
//                newLeftThumbValue = newSliderValue;
//                newRightThumbValue = rightSliderThumb.getX() + xSliderPane.getWidth() - slider.getX() - slider.getWidth();
//
//            }
//            slider.setX(newSliderValue);
//            leftSliderThumb.setX(newLeftThumbValue);
//            rightSliderThumb.setX(newRightThumbValue);
//            double max = xSliderPane.getWidth() - slider.getWidth();
//            double current = slider.getX();
//            resetZoomStripe();
//            scrollX.setValue((current / max) * 100);
//            lastDragX = event.getX();
//        });
//    }
    private static final int TOTAL_SLIDER_THUMB_WIDTH = 30;

//    public void drawZoomCoordinateLine() {
//        canvasPane.drawZoomCoordinateLine();
//    }

    public void resetZoomStripe() {
        canvasPane.resetZoomStripe();
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
//       
    }

//    private void updateTrackLabels() {
//        Platform.runLater(() -> {
//            labelPane.getChildren().clear();
//            trackRenderers.stream()
//                    .filter(trackRenderer -> trackRenderer.getCanvasContext().isVisible())
//                    .forEach(trackRenderer -> {
//                        TrackLabel trackLabel = trackRenderer.getTrackLabel();
//                        trackLabel.setDimensions(labelPane);
//                        StackPane content = trackLabel.getContent();
//                        labelPane.getChildren().add(content);
//                    });
//            labelPane.getChildren().stream()
//                    .filter(node -> node instanceof StackPane)
//                    .map(node -> (StackPane) node)
//                    .forEach((StackPane trackLabelNode) -> {
//                        trackLabelNode.setOnDragDetected(
//                                (MouseEvent event) -> {
//                                    if (event.getSource() instanceof StackPane) {
//                                        Pane rootPane = (Pane) trackLabelNode.getScene().getRoot();
//                                        rootPane.setOnDragOver(dragEvent -> {
//                                            dragEvent.acceptTransferModes(TransferMode.ANY);
//                                            dragEvent.consume();
//                                        });
//                                        SnapshotParameters snapshotParams = new SnapshotParameters();
//                                        snapshotParams.setTransform(Transform.scale(0.75, 0.75));
//                                        WritableImage snapshot = trackLabelNode.snapshot(snapshotParams, null);
//                                        Dragboard db = trackLabelNode.startDragAndDrop(TransferMode.MOVE);
//                                        ClipboardContent clipboardContent = new ClipboardContent();
//                                        clipboardContent.put(DataFormat.PLAIN_TEXT, trackLabelNode.getBoundsInParent().getMinY());
//                                        db.setDragView(snapshot, 5, 5);
//                                        db.setContent(clipboardContent);
//                                    }
//                                    event.consume();
//                                }
//                        );
//                        trackLabelNode.setOnDragDropped((DragEvent event) -> {
//                            if (event.getSource() instanceof StackPane) {
//                                StackPane dropLocationLabelNode = StackPane.class.cast(event.getSource());
//                                boolean droppedAbove = event.getY() < (dropLocationLabelNode.getHeight() / 2);
//                                double dropLocationMinY = dropLocationLabelNode.getBoundsInParent().getMinY();
//                                Object dragboardContent = event.getDragboard().getContent(DataFormat.PLAIN_TEXT);
//                                if (dragboardContent instanceof Double) {
//                                    double eventTriggerMinY = (Double) dragboardContent;
//                                    if (dropLocationMinY != eventTriggerMinY) {
//                                        Lists.newArrayList(trackRenderers).stream()
//                                        .filter(trackRenderer -> trackRenderer.getCanvasContext().isVisible())
//                                        .filter(draggedTrackRenderer -> draggedTrackRenderer.getTrackLabel().getContent().getBoundsInParent().getMinY() == eventTriggerMinY)
//                                        .findFirst()
//                                        .ifPresent(draggedTrackRenderer -> {
//                                            Lists.newArrayList(trackRenderers).stream()
//                                            .filter(trackRenderer -> trackRenderer.getTrackLabel().getContent() == dropLocationLabelNode)
//                                            .findFirst()
//                                            .ifPresent(droppedTrackRenderer -> {
//                                                int droppedIndex = droppedTrackRenderer.getWeight();
//                                                if (droppedAbove) {
//                                                    trackRenderers.remove(draggedTrackRenderer);
//                                                    draggedTrackRenderer.setWeight(droppedIndex - 1);
//                                                    trackRenderers.add(draggedTrackRenderer);
//                                                } else {
//                                                    trackRenderers.remove(draggedTrackRenderer);
//                                                    draggedTrackRenderer.setWeight(droppedIndex + 1);
//                                                    trackRenderers.add(draggedTrackRenderer);
//                                                }
//                                                updateTrackRenderers();
//                                            });
//                                        });
//                                    }
//                                }
//
//                            }
//                            event.consume();
//                        });
//                    });
//        });
//
//    }
//    @Subscribe
//    private void jumpZoom(JumpZoomEvent jumpZoomEvent) {
//        Rectangle2D focusRect = jumpZoomEvent.getRect();
//        TrackRenderer eventLocationReference = jumpZoomEvent.getTrackRenderer();
//        View view = eventLocationReference.getView();
//        double modelWidth = canvasPane.getModelWidth();
//        double minX = Math.max(focusRect.getMinX(), view.getBoundingRect().getMinX());
//        double maxX = Math.min(focusRect.getMaxX(), view.getBoundingRect().getMaxX());
//        double width = maxX - minX;
//        if (width < MAX_ZOOM_MODEL_COORDINATES_X) {
//            width = Math.max(width * 1.1, MAX_ZOOM_MODEL_COORDINATES_X);
//            minX = Math.max((minX + focusRect.getWidth() / 2) - (width / 2), 0);
//        }
//        final double scaleXalt = eventLocationReference.getCanvasContext().getBoundingRect().getWidth() / width;
//        resetZoomStripe();
//        hSlider.setValue(invertExpScaleTransform(canvasPane, scaleXalt));
//        double scrollPosition = (minX / (modelWidth - width)) * 100;
//        final double scrollXValue = enforceRangeBounds(scrollPosition, 0, 100);
//        scrollX.setValue(scrollXValue);
//    }
//
//    @Subscribe
//    private void handleScrollXUpdateEvent(ScrollXUpdate e) {
//        ignoreScrollXEvent = true;
//        double scrollXValue = enforceRangeBounds(e.getScrollX(), 0, 100);
//        scrollX.setValue(scrollXValue);
//    }
//
//    @Subscribe
//    private void clickDragZoomListener(ClickDragZoomEvent event) {
//        double x1 = event.getStartX();
//        final double x2 = event.getEndX() - event.getStartX();
//        final Rectangle2D zoomFocus = new Rectangle2D(x1, 0, x2, Double.MAX_VALUE);
//        trackRenderers.stream()
//                .filter(track -> track instanceof CoordinateTrackRenderer)
//                .findFirst().ifPresent(coordinateRenderer -> {
//                    jumpZoom(new JumpZoomEvent(zoomFocus, coordinateRenderer));
//                });
//    }
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

    @Reference(unbind = "removeMenuBarManager")
    public void setMenuBarManager(MenuBarManager menuBarManager) {
        this.menuBarManager = menuBarManager;
    }

    public void removeMenuBarManager(MenuBarManager menuBarManager) {
        try {
            root.getChildren().remove(menuBarManager.getMenuBar());
        } catch (Exception ex) {
            //do nothing
        }
    }

    @Reference
    public void setSelectionInfoService(SelectionInfoService selectionInfoService) {
        this.selectionInfoService = selectionInfoService;
    }

    @Subscribe
    private void updateGlyphSelections(SelectionChangeEvent event) {
        selectionInfoService.getSelectedGlyphs().clear();
        //TODO find a more performant way to update selections, possibly debouncing at this location would be sufficient
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
    public void setToolbarProvider(ToolBarManager toolbarProvider) {
        this.toolbarProvider = toolbarProvider;
    }

    @Reference(unbind = "removeFooter")
    public void setFooter(Footer footer) {
        this.footer = footer;
    }

    public void removeFooter(Footer footer) {
        try {
            root.getChildren().remove(footer);
        } catch (Exception ex) {
            //do nothing
        }
    }

    @Deactivate
    private void deactivate() {
        AppStore.getStore().unsubscribe(app);
        try {
            Platform.runLater(() -> {
                root.getChildren().clear();
            });
        } catch (Exception ex) {
            //do nothing
        }
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

    private void setupPlusMinusSlider() {
//        plusMinusSlider.setOnValueChanged((PlusMinusEvent event) -> {
//            final double updatedScrollXValue = getUpdatedScrollxValue(event.getValue());
//            if (updatedScrollXValue != scrollX.doubleValue()) {
//                if (Platform.isFxApplicationThread()) {
//                    scrollX.setValue(updatedScrollXValue);
//                    resetZoomStripe();
//                    syncWidgetSlider();
//                } else {
//                    Platform.runLater(() -> {
//                        scrollX.setValue(updatedScrollXValue);
//                        resetZoomStripe();
//                        syncWidgetSlider();
//                    });
//                }
//            }
//        });
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
