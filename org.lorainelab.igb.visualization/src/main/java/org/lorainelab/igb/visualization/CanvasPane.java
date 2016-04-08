package org.lorainelab.igb.visualization;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import java.time.Duration;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import org.lorainelab.igb.visualization.event.MouseClickedEvent;
import org.lorainelab.igb.visualization.event.MouseDoubleClickEvent;
import org.lorainelab.igb.visualization.event.MouseDragEnteredEvent;
import org.lorainelab.igb.visualization.event.MouseDragExitedEvent;
import org.lorainelab.igb.visualization.event.MouseDragOverEvent;
import org.lorainelab.igb.visualization.event.MouseDragReleasedEvent;
import org.lorainelab.igb.visualization.event.MouseDraggedEvent;
import org.lorainelab.igb.visualization.event.MouseEnteredEvent;
import org.lorainelab.igb.visualization.event.MouseExitedEvent;
import org.lorainelab.igb.visualization.event.MouseMovedEvent;
import org.lorainelab.igb.visualization.event.MousePressedEvent;
import org.lorainelab.igb.visualization.event.MouseReleasedEvent;
import org.lorainelab.igb.visualization.event.MouseStationaryEndEvent;
import org.lorainelab.igb.visualization.event.MouseStationaryEvent;
import org.lorainelab.igb.visualization.event.MouseStationaryStartEvent;
import org.lorainelab.igb.visualization.event.ScaleEvent;
import org.lorainelab.igb.visualization.event.ZoomStripeEvent;
import org.lorainelab.igb.visualization.model.RefrenceSequenceProvider;
import org.lorainelab.igb.visualization.util.CanvasUtils;
import org.reactfx.EventStream;
import org.reactfx.EventStreams;
import org.reactfx.util.Either;

@Component(immediate = true, provide = CanvasPane.class)
public class CanvasPane extends Region {

    private Canvas canvas;
    private double modelWidth;
    private double xFactor;
    private double zoomStripeCoordinate;
    private double xOffset;
    private double visibleVirtualCoordinatesX;
    private EventBus eventBus;
    private GenoVixFxController controller;
    private RefrenceSequenceProvider refrenceSequenceProvider;
    private EventBusService eventBusService;

    public CanvasPane() {
    }

    @Activate
    public void activate() {
        eventBus = eventBusService.getEventBus();
        eventBus.register(this);
        this.modelWidth = refrenceSequenceProvider.getReferenceDna().length();
        canvas = new Canvas();
        getChildren().add(canvas);
        canvas.widthProperty().addListener(observable -> {
            draw();
            xFactor = canvas.getWidth() / modelWidth;
        });
        canvas.heightProperty().addListener(observable -> draw());
        zoomStripeCoordinate = -1;
        initializeMouseEventHandlers();
    }

    private Point2D getLocalPoint2DFromMouseEvent(MouseEvent event) {
        return new Point2D(event.getX(), event.getY());
    }

    private Point2D getScreenPoint2DFromMouseEvent(MouseEvent event) {
        return new Point2D(event.getScreenX(), event.getScreenY());
    }

    private void initializeMouseEventHandlers() {
        canvas.setOnMouseClicked((MouseEvent event) -> {
            boolean isDoubleClick = false;
            eventBus.post(new MouseClickedEvent(
                    getLocalPoint2DFromMouseEvent(event),
                    getScreenPoint2DFromMouseEvent(event))
            );
            if (event.getClickCount() == 2) {
                isDoubleClick = true;
                resetZoomStripe();
                eventBus.post(new MouseDoubleClickEvent(
                        getLocalPoint2DFromMouseEvent(event),
                        getScreenPoint2DFromMouseEvent(event))
                );
            }
            if (!isDoubleClick) {
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
                drawZoomCoordinateLine(event);
            }
        });
        canvas.setOnMouseDragEntered((MouseEvent event) -> {
            eventBus.post(new MouseDragEnteredEvent(
                    getLocalPoint2DFromMouseEvent(event),
                    getScreenPoint2DFromMouseEvent(event))
            );
        });
        canvas.setOnMouseDragExited((MouseEvent event) -> {
            eventBus.post(new MouseDragExitedEvent(
                    getLocalPoint2DFromMouseEvent(event),
                    getScreenPoint2DFromMouseEvent(event))
            );
        });
        canvas.setOnMouseDragOver((MouseEvent event) -> {
            eventBus.post(new MouseDragOverEvent(
                    getLocalPoint2DFromMouseEvent(event),
                    getScreenPoint2DFromMouseEvent(event))
            );
        });
        canvas.setOnMouseDragReleased((MouseEvent event) -> {
            eventBus.post(new MouseDragReleasedEvent(
                    getLocalPoint2DFromMouseEvent(event),
                    getScreenPoint2DFromMouseEvent(event))
            );
        });
        canvas.setOnMouseDragged((MouseEvent event) -> {
            eventBus.post(new MouseDraggedEvent(
                    getLocalPoint2DFromMouseEvent(event),
                    getScreenPoint2DFromMouseEvent(event))
            );
        });
        canvas.setOnMouseEntered((MouseEvent event) -> {
            eventBus.post(new MouseEnteredEvent(
                    getLocalPoint2DFromMouseEvent(event),
                    getScreenPoint2DFromMouseEvent(event))
            );
        });
        canvas.setOnMouseExited((MouseEvent event) -> {
            eventBus.post(new MouseExitedEvent(
                    getLocalPoint2DFromMouseEvent(event),
                    getScreenPoint2DFromMouseEvent(event))
            );
        });
//        canvas.setOnMouseMoved((MouseEvent event) -> {
//            eventBus.post(new MouseMovedEvent());
//        });
        canvas.setOnMousePressed((MouseEvent event) -> {
            eventBus.post(new MousePressedEvent(
                    getLocalPoint2DFromMouseEvent(event),
                    getScreenPoint2DFromMouseEvent(event))
            );
        });
        canvas.setOnMouseReleased((MouseEvent event) -> {
            eventBus.post(new MouseReleasedEvent(
                    getLocalPoint2DFromMouseEvent(event),
                    getScreenPoint2DFromMouseEvent(event))
            );
        });

        EventStream<MouseEvent> mouseEvents = EventStreams.eventsOf(canvas, MouseEvent.ANY);
        EventStream<Point2D> stationaryPositions = mouseEvents
                .successionEnds(Duration.ofSeconds(1))
                .filter(e -> e.getEventType() == MouseEvent.MOUSE_MOVED)
                .map(e -> {
                    return new Point2D(e.getScreenX(), e.getScreenY());
                });

        EventStream<Void> stoppers = mouseEvents.supply((Void) null);

        EventStream<Either<Point2D, Void>> stationaryEvents
                = stationaryPositions.or(stoppers)
                .distinct();

        stationaryEvents.<MouseStationaryEvent>map(either -> either.unify(pos -> new MouseStationaryStartEvent(pos, pos),
                stop -> new MouseStationaryEndEvent()))
                .subscribe(evt -> eventBus.post(evt));
    }

    public void resetZoomStripe() {
        this.zoomStripeCoordinate = -1;
        eventBus.post(new ZoomStripeEvent(zoomStripeCoordinate));
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        final double width = getWidth();
        final double height = getHeight();
        final Insets insets = getInsets();
        final double contentX = insets.getLeft();
        final double contentY = insets.getTop();
        final double contentWith = Math.max(0, width - (insets.getLeft() + insets.getRight()));
        final double contentHeight = Math.max(0, height - (insets.getTop() + insets.getBottom()));
        canvas.relocate(contentX, contentY);
        canvas.setWidth(contentWith);
        canvas.setHeight(contentHeight);
    }

    private void draw() {
        final double width = canvas.getWidth();
        final double height = canvas.getHeight();
        final GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, width, height);
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public double getModelWidth() {
        return modelWidth;
    }

    private void drawZoomCoordinateLine(MouseEvent event) {
        zoomStripeCoordinate = (event.getX() / xFactor) + xOffset;
        drawZoomCoordinateLine();

    }

    public void drawZoomCoordinateLine() {
        if (zoomStripeCoordinate >= 0) {
            eventBus.post(new ZoomStripeEvent(zoomStripeCoordinate));
            GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.save();
            gc.setStroke(Color.rgb(0, 0, 0, .3));
            gc.scale(xFactor, 1);
            double x = Math.floor(zoomStripeCoordinate) - xOffset;
            double width = canvas.getWidth() / xFactor;
            if (width > 500) {
                gc.setLineWidth(width * 0.002);
            }
            if (x >= 0 && x <= width) {
                gc.strokeLine(x + .5, 0, x + .5, canvas.getHeight());
            }
            gc.restore();
        }
    }

    double getXFactor() {
        return xFactor;
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    @Subscribe
    private void handleScaleEvent(ScaleEvent scaleEvent) {
        xFactor = CanvasUtils.exponentialScaleTransform(this, scaleEvent.getScaleX());
        visibleVirtualCoordinatesX = Math.floor(canvas.getWidth() / xFactor);
        xOffset = Math.round((scaleEvent.getScrollX() / 100) * (modelWidth - visibleVirtualCoordinatesX));
    }

    @Reference(optional = true)
    public void setMainViewController(GenoVixFxController controller) {
        this.controller = controller;
//        controller.getHSliderValue().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
//            xFactor = CanvasUtils.exponentialScaleTransform(this, newValue.doubleValue());
//            visibleVirtualCoordinatesX = Math.floor(canvas.getWidth() / xFactor);
//            xOffset = Math.round((controller.getXScrollPosition().doubleValue() / 100) * (modelWidth - visibleVirtualCoordinatesX));
//        });
    }

    @Reference
    public void setRefrenceSequenceProvider(RefrenceSequenceProvider refrenceSequenceProvider) {
        this.refrenceSequenceProvider = refrenceSequenceProvider;
    }

    @Reference
    public void setEventBusService(EventBusService eventBusService) {
        this.eventBusService = eventBusService;
    }
}
