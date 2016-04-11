package org.lorainelab.igb.visualization;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Reference;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import java.util.Set;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
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
import org.lorainelab.igb.visualization.event.ClickDragZoomEvent;
import org.lorainelab.igb.visualization.event.ScaleEvent;
import org.lorainelab.igb.visualization.event.ScrollXUpdate;
import org.lorainelab.igb.visualization.menubar.MenuBarManager;
import org.lorainelab.igb.visualization.model.CoordinateTrackRenderer;
import org.lorainelab.igb.visualization.model.JumpZoomEvent;
import org.lorainelab.igb.visualization.model.TrackLabel;
import org.lorainelab.igb.visualization.model.TrackRenderer;
import static org.lorainelab.igb.visualization.model.TrackRenderer.MAX_ZOOM_MODEL_COORDINATES_X;
import org.lorainelab.igb.visualization.model.TrackRendererProvider;
import org.lorainelab.igb.visualization.model.View;
import org.lorainelab.igb.visualization.model.ViewPortManager;
import org.lorainelab.igb.visualization.tabs.TabPaneManager;
import static org.lorainelab.igb.visualization.util.CanvasUtils.exponentialScaleTransform;
import static org.lorainelab.igb.visualization.util.CanvasUtils.invertExpScaleTransform;
import static org.lorainelab.igb.visualization.util.CanvasUtils.linearScaleTransform;

@Component(immediate = true, provide = GenoVixFxController.class)
public class GenoVixFxController {

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
    private Pane xSliderPane;
    @FXML
    private Rectangle slider;
    @FXML
    private Rectangle leftSliderThumb;
    @FXML
    private Rectangle rightSliderThumb;

    @FXML
    private VBox root;

    @FXML
    private AnchorPane rightTabPaneContainer;

    @FXML
    private AnchorPane bottomTabPaneContainer;

    @FXML
    private HBox zoomSliderMiniMapWidgetContainer;

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
    private TrackRendererProvider trackRendererProvider;
    private EventBusService eventBusService;
    private TabPaneManager tabPaneManager;
    private ZoomSliderMiniMapWidget zoomSliderMiniMapWidget;
    private MenuBarManager menuBarManager;

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

    private void addMockData() {
        trackRenderers = trackRendererProvider.getTrackRenderers();
    }

    private void initCanvasMouseListeners() {
//        canvasPane.setOnMouseClicked((MouseEvent event) -> {
//            boolean isDoubleClick = event.getClickCount() >= 2;
//
//            Point2D mouseEventLocation = new Point2D(event.getX(), event.getY());
//            trackRenderers.keySet().stream().sorted().map(key -> trackRenderers.get(key))
//                    .filter(trackRenderer -> trackRenderer.getCanvasContext().isPresent())
//                    .filter(trackRenderer -> trackRenderer.getCanvasContext().get().getBoundingRect().contains(mouseEventLocation))
//                    .forEach(trackRender -> {
//                        if (isDoubleClick) {
//                            resetZoomStripe();
//                            trackRender.handleMouseDoubleClickEvent(event);
//                        } else {
//                            trackRender.handleMouseClickEvent(event);
//                        }
//                    });
//
//            if (!isDoubleClick) {
//                Optional<TrackRenderer> trc = trackRenderers.keySet().stream().map(key -> trackRenderers.get(key))
//                        .filter(trackRenderer -> trackRenderer.getCanvasContext().isPresent())
//                        .filter(trackRenderer -> trackRenderer.getCanvasContext().get().getBoundingRect().contains(mouseEventLocation))
//                        .findFirst();
//                if (trc.isPresent()) {
//           zoomStripeCoordinate         View view = trc.get().getView();
//                    double offsetX = view.getBoundingRect().getMinX();
//                    zoomStripeCoordinate = (event.getX() / view.getXfactor()) + offsetX;
//                    refreshTrackRenderers();
//                    eventBus.post(new ZoomStripeEvent(zoomStripeCoordinate));
//                }
//            }
//        });
//        canvasPane.setOnMouseDragEntered((MouseEvent event) -> {
//            Point2D mouseEventLocation = new Point2D(event.getX(), event.getY());
//            trackRenderers.keySet().stream().sorted().map(key -> trackRenderers.get(key))
//                    .filter(trackRenderer -> trackRenderer.getCanvasContext().isPresent())
//                    .filter(trackRenderer -> trackRenderer.getCanvasContext().get().getBoundingRect().contains(mouseEventLocation))
//                    .forEach(trackRender -> {
//                        trackRender.handleMouseDragEnteredEvent(event);
//                    });
//        });
//        canvasPane.setOnMouseDragExited((MouseEvent event) -> {
//            Point2D mouseEventLocation = new Point2D(event.getX(), event.getY());
//            trackRenderers.keySet().stream().sorted().map(key -> trackRenderers.get(key)).forEach(trackRender -> {
//                trackRender.handleMouseDragExitedEvent(event);
//            });
//        });
//        canvasPane.setOnMouseDragOver((MouseEvent event) -> {
//            Point2D mouseEventLocation = new Point2D(event.getX(), event.getY());
//            trackRenderers.keySet().stream().sorted().map(key -> trackRenderers.get(key))
//                    .filter(trackRenderer -> trackRenderer.getCanvasContext().isPresent())
//                    .filter(trackRenderer -> trackRenderer.getCanvasContext().get().getBoundingRect().contains(mouseEventLocation))
//                    .forEach(trackRender -> {
//                        trackRender.handleMouseClickEvent(event);
//                    });
//        });
//        canvasPane.setOnMouseDragReleased((MouseEvent event) -> {
//            Point2D mouseEventLocation = new Point2D(event.getX(), event.getY());
//            trackRenderers.keySet().stream().sorted().map(key -> trackRenderers.get(key))
//                    .filter(trackRenderer -> trackRenderer.getCanvasContext().isPresent())
//                    .filter(trackRenderer -> trackRenderer.getCanvasContext().get().getBoundingRect().contains(mouseEventLocation))
//                    .forEach(trackRender -> {
//                        trackRender.handleMouseDragReleasedEvent(event);
//                    });
//        });
//        canvasPane.setOnMouseDragged((MouseEvent event) -> {
//            Point2D mouseEventLocation = new Point2D(event.getX(), event.getY());
//            trackRenderers.keySet().stream().sorted().map(key -> trackRenderers.get(key))
//                    .filter(trackRenderer -> trackRenderer.getCanvasContext().isPresent())
//                    .filter(trackRenderer -> trackRenderer.getCanvasContext().get().getBoundingRect().contains(mouseEventLocation))
//                    .forEach(trackRender -> {
//                        trackRender.handleMouseDraggedEvent(event);
//                    });
//        });
//        canvasPane.setOnMouseEntered((MouseEvent event) -> {
//            Point2D mouseEventLocation = new Point2D(event.getX(), event.getY());
//            trackRenderers.keySet().stream().sorted().map(key -> trackRenderers.get(key))
//                    .filter(trackRenderer -> trackRenderer.getCanvasContext().isPresent())
//                    .filter(trackRenderer -> trackRenderer.getCanvasContext().get().getBoundingRect().contains(mouseEventLocation))
//                    .forEach(trackRender -> {
//                        trackRender.handleMouseEnteredEvent(event);
//                    });
//        });
//        canvasPane.setOnMouseExited((MouseEvent event) -> {
////            Point2D mouseEventLocation = new Point2D(event.getX(), event.getY());
//            trackRenderers.keySet().stream().sorted().map(key -> trackRenderers.get(key)).forEach(trackRender -> {
//                trackRender.handleMouseExited(event);
//            });
////            trackRenderers.stream()
////                    .filter(trackRenderer -> trackRenderer.getCanvasContext().isPresent())
////                    .filter(trackRenderer -> trackRenderer.getCanvasContext().get().getBoundingRect().contains(mouseEventLocation))
////                    .forEach(trackRender -> {
////                        trackRender.handleMouseExited(event);
////                    });
//        });
//        canvasPane.setOnMouseMoved((MouseEvent event) -> {
//            Point2D mouseEventLocation = new Point2D(event.getX(), event.getY());
//            trackRenderers.keySet().stream().sorted().map(key -> trackRenderers.get(key))
//                    .filter(trackRenderer -> trackRenderer.getCanvasContext().isPresent())
//                    .filter(trackRenderer -> trackRenderer.getCanvasContext().get().getBoundingRect().contains(mouseEventLocation))
//                    .forEach(trackRender -> {
//                        trackRender.handleMouseMovedEvent(event);
//                    });
//        });
//        canvasPane.setOnMouseMoved((MouseEvent event) -> {
//            Point2D mouseEventLocation = new Point2D(event.getX(), event.getY());
//            trackRenderers.keySet().stream().sorted().map(key -> trackRenderers.get(key))
//                    .filter(trackRenderer -> trackRenderer.getCanvasContext().isPresent())
//                    .filter(trackRenderer -> !trackRenderer.getCanvasContext().get().getBoundingRect().contains(mouseEventLocation))
//                    .forEach(trackRender -> {
//                        trackRender.handleMouseExited(event);
//                    });
//        });
//        canvasPane.setOnMousePressed((MouseEvent event) -> {
//            Point2D mouseEventLocation = new Point2D(event.getX(), event.getY());
//            trackRenderers.keySet().stream().sorted().map(key -> trackRenderers.get(key))
//                    .filter(trackRenderer -> trackRenderer.getCanvasContext().isPresent())
//                    .filter(trackRenderer -> trackRenderer.getCanvasContext().get().getBoundingRect().contains(mouseEventLocation))
//                    .forEach(trackRender -> {
//                        trackRender.handleMousePressedEvent(event);
//                    });
//        });
//        canvasPane.setOnMouseReleased((MouseEvent event) -> {
//            Point2D mouseEventLocation = new Point2D(event.getX(), event.getY());
//            trackRenderers.keySet().stream().sorted().map(key -> trackRenderers.get(key))
//                    .filter(trackRenderer -> trackRenderer.getCanvasContext().isPresent())
//                    .filter(trackRenderer -> trackRenderer.getCanvasContext().get().getBoundingRect().contains(mouseEventLocation))
//                    .forEach(trackRender -> {
//                        trackRender.handleMouseReleased(event);
//                    });
//        });
//
//        canvasPane.addEventHandler(MOUSE_STATIONARY_BEGIN, event -> {
//            Point2D mouseEventLocation = event.getPosition();
//            trackRenderers.keySet().stream().sorted().map(key -> trackRenderers.get(key))
//                    .filter(trackRenderer -> trackRenderer.getCanvasContext().isPresent())
//                    .filter(trackRenderer -> trackRenderer.getCanvasContext().get().getBoundingRect().contains(mouseEventLocation))
//                    .forEach(trackRender -> {
//                        trackRender.handleMouseStopped(event);
//                    });
//        });
//
//        canvasPane.addEventHandler(MOUSE_STATIONARY_END, event -> {
//            trackRenderers.keySet().stream().sorted().map(key -> trackRenderers.get(key)).forEach(trackRender -> {
//                trackRender.handleMouseStarted(event);
//            });
//        });

        canvas.setOnScroll(scrollEvent -> {
            final boolean isForwardScroll = scrollEvent.getDeltaY() > 0.0;
            if (isForwardScroll) {
                hSlider.increment();
            } else {
                hSlider.decrement();
            }
        });
    }

    private double lastHSliderFire = -1;

    @FXML
    private void initialize() {
        initializeGuiComponents();
        addMockData();
        initializeZoomScrollBar();
        viewPortManager = new ViewPortManager(canvas, trackRenderers, 0, 0);

        vSlider.valueProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            scaleTrackRenderers();
        });
        hSlider.valueProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {

            if (ignoreHSliderEvent) {
                ignoreHSliderEvent = false;
                return;
            }
            final boolean isSnapEvent = newValue.doubleValue() % hSlider.getMajorTickUnit() == 0;
            if (lastHSliderFire < 0 || Math.abs(lastHSliderFire - newValue.doubleValue()) > 1 || isSnapEvent) {

                scaleTrackRenderers();
                syncWidgetSlider();
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
                refreshSliderWidget();
            });
            scaleTrackRenderers();
        });

        canvas.heightProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            Platform.runLater(() -> {
                refreshSliderWidget();
            });
            scaleTrackRenderers();
        });

        scrollX.addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            if (ignoreScrollXEvent) {
                ignoreScrollXEvent = false;
            } else {
                scaleTrackRenderers();
            }
        });

        scrollY.valueProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            scaleTrackRenderers();
        });
        //fixes initialization race condition
        Platform.runLater(() -> {
            refreshSliderWidget();
            scaleTrackRenderers();
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
            if (newSliderValue < 50) {
                newSliderValue = 50;
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
                double max = xSliderPane.getWidth() - 50;
                double current = slider.getWidth() - 50;

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

        leftSliderThumb.setOnMouseDragged((MouseEvent event) -> {
            double increment = Math.round(event.getX() - lastDragX);
            double newSliderValue = slider.getX() + increment;
            double newLeftThumbValue = leftSliderThumb.getX() + increment;
            double newSliderWidth = (slider.getWidth() - increment);
            if (newSliderWidth < 50) {
                newSliderWidth = 50;
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
                double max = xSliderPane.getWidth() - 50;
                double current = slider.getWidth() - 50;

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

    public void drawZoomCoordinateLine() {
        canvasPane.drawZoomCoordinateLine();
    }

    public void resetZoomStripe() {
        canvasPane.resetZoomStripe();
    }

    private void scaleTrackRenderers() {
        updateCanvasContexts();
        trackRenderers.forEach(trackRenderer -> trackRenderer.scaleCanvas(exponentialScaleTransform(canvasPane, hSlider.getValue()), scrollX.get(), scrollY.getValue()));
        eventBus.post(new ScaleEvent(hSlider.getValue(), vSlider.getValue(), scrollX.getValue(), scrollY.getValue()));
        drawZoomCoordinateLine();
    }

//    private void refreshTrackRenderers() {
//        trackRenderers.keySet().stream().sorted().map(key -> trackRenderers.get(key)).forEach(trackRenderer -> trackRenderer.render());
//        drawZoomCoordinateLine();
//    }
    private void updateScrollY() {
        double sum = trackRenderers.stream()
                .map(trackRenderer -> trackRenderer.getCanvasContext())
                .filter(canvasContext -> canvasContext.isVisible())
                .mapToDouble(canvasContext -> canvasContext.getBoundingRect().getHeight())
                .sum();
        scrollY.setVisibleAmount((sum / totalTrackHeight) * 100);
    }

    private void initializeGuiComponents() {
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
        initCanvasMouseListeners();
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
                            final Object dropLocationSource = event.getSource();
                            if (dropLocationSource instanceof StackPane) {
                                StackPane dropLocationLabelNode = StackPane.class.cast(dropLocationSource);
                                double dropLocationMinY = dropLocationLabelNode.getBoundsInParent().getMinY();
                                Dragboard db = event.getDragboard();
                                Object dragboardContent = db.getContent(DataFormat.PLAIN_TEXT);
                                if (dragboardContent instanceof Double) {
                                    double eventTriggerMinY = (Double) dragboardContent;
                                    if (dropLocationMinY != eventTriggerMinY) {
                                        trackRenderers.stream()
                                                .filter(trackRenderer -> trackRenderer.getCanvasContext().isVisible())
                                                .forEach(draggedTrackRenderer -> {
                                                    StackPane labelNode = draggedTrackRenderer.getTrackLabel().getContent();
                                                    if (labelNode.getBoundsInParent().getMinY() == eventTriggerMinY) {
                                                        trackRenderers.stream()
                                                                .filter(trackRenderer -> trackRenderer.getTrackLabel().getContent() == dropLocationLabelNode)
                                                                .findFirst()
                                                                .ifPresent(droppedTrackRenderer -> {
                                                                    Integer draggedIndex = draggedTrackRenderer.getWeight();
                                                                    Integer droppedIndex = droppedTrackRenderer.getWeight();
                                                                    draggedTrackRenderer.setWeight(droppedIndex);
                                                                    droppedTrackRenderer.setWeight(draggedIndex);
                                                                    scaleTrackRenderers();
                                                                });
                                            }
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
                if (newWidth < 50) {
                    newWidth = 50;
                }
                double oldWidth = slider.getWidth();
                slider.setWidth(newWidth);
                rightSliderThumb.setX(rightSliderThumb.getX() + newWidth - oldWidth);
            }
            if (scrollX.getValue() >= 0 && xSliderPane.getWidth() > 50) {
                slider.setX(newXValue);
                leftSliderThumb.setX(newXValue);
                double maxPaneWidth = xSliderPane.getWidth() - 50;
                double newSliderWidth = -maxPaneWidth * ((hSliderWidget.getValue() / 100) - 1) + 50;
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

    private void syncWidgetSlider() {
        double[] scaledPercentage = {0};
        trackRenderers.stream()
                .filter(tr -> !(tr instanceof CoordinateTrackRenderer))
                .filter(tr -> tr.getCanvasContext().isVisible()).findFirst()
                .ifPresent(trackRenderer -> {
                    double minScaleX = trackRenderer.getModelWidth();
                    double maxScaleX = MAX_ZOOM_MODEL_COORDINATES_X - 1;
                    final double scaleRange = maxScaleX - minScaleX;
                    final double current = trackRenderer.getView().getBoundingRect().getWidth();
                    scaledPercentage[0] = (current - minScaleX) / scaleRange;
                });
        double oldWidth = slider.getWidth();
        double oldX = slider.getX();
        double width = ((1 - scaledPercentage[0]) * (xSliderPane.getWidth() - 50)) + 50;
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
    public void setTrackRendererProvider(TrackRendererProvider trackRendererProvider) {
        this.trackRendererProvider = trackRendererProvider;
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

}
