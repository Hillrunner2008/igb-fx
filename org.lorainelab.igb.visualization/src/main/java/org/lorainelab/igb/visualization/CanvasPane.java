package org.lorainelab.igb.visualization;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javafx.event.EventType;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import org.lorainelab.igb.visualization.event.ClickDragCancelEvent;
import org.lorainelab.igb.visualization.event.ClickDragEndEvent;
import org.lorainelab.igb.visualization.event.ClickDragStartEvent;
import org.lorainelab.igb.visualization.event.ClickDraggingEvent;
import org.lorainelab.igb.visualization.event.MouseClickedEvent;
import org.lorainelab.igb.visualization.event.MouseDoubleClickEvent;
import org.lorainelab.igb.visualization.event.MouseStationaryEndEvent;
import org.lorainelab.igb.visualization.event.MouseStationaryEvent;
import org.lorainelab.igb.visualization.event.MouseStationaryStartEvent;
import org.lorainelab.igb.visualization.event.RefreshTrackEvent;
import org.lorainelab.igb.visualization.event.ScaleEvent;
import org.lorainelab.igb.visualization.event.ScrollScaleEvent;
import org.lorainelab.igb.visualization.event.ScrollScaleEvent.Direction;
import org.lorainelab.igb.visualization.event.ZoomStripeEvent;
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
    private EventBusService eventBusService;
    private List<MouseEvent> mouseEvents;

    public CanvasPane() {
    }

    @Activate
    public void activate() {
        eventBus = eventBusService.getEventBus();
        eventBus.register(this);
        mouseEvents = new ArrayList<>();
        this.modelWidth = 1;
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
            mouseEvents.add(event);
        });
        canvas.setOnMouseDragEntered((MouseEvent event) -> {
            mouseEvents.add(event);
        });
        canvas.setOnMouseDragExited((MouseEvent event) -> {
            mouseEvents.add(event);
        });
        canvas.setOnMouseDragOver((MouseEvent event) -> {
            mouseEvents.add(event);
        });
        canvas.setOnMouseDragReleased((MouseEvent event) -> {
            mouseEvents.add(event);
        });
        canvas.setOnMouseDragged((MouseEvent event) -> {
            mouseEvents.add(event);
            eventBus.post(new ClickDraggingEvent(
                    getLocalPoint2DFromMouseEvent(event),
                    getScreenPoint2DFromMouseEvent(event))
            );
        });
        canvas.setOnMouseEntered((MouseEvent event) -> {
            mouseEvents.add(event);
        });
        canvas.setOnMouseExited((MouseEvent event) -> {
            mouseEvents.add(event);
        });
        canvas.setOnMousePressed((MouseEvent event) -> {
            mouseEvents.add(event);
            resetZoomStripe();
            eventBus.post(new ClickDragStartEvent(
                    getLocalPoint2DFromMouseEvent(event),
                    getScreenPoint2DFromMouseEvent(event))
            );
        });
        canvas.setOnMouseReleased((MouseEvent event) -> {
            resetZoomStripe();
            List<EventType<? extends MouseEvent>> types = mouseEvents.stream().map(e -> e.getEventType()).collect(Collectors.toList());
            if (types.contains(MouseEvent.MOUSE_DRAGGED)) {
                eventBus.post(new ClickDragEndEvent(
                        getLocalPoint2DFromMouseEvent(event),
                        getScreenPoint2DFromMouseEvent(event))
                );
            } else {
                eventBus.post(new ClickDragCancelEvent());
                if (event.getClickCount() >= 2) {
                    
                    eventBus.post(new MouseDoubleClickEvent(
                            getLocalPoint2DFromMouseEvent(event),
                            getScreenPoint2DFromMouseEvent(event))
                    );
                } else {

                    eventBus.post(new MouseClickedEvent(
                            getLocalPoint2DFromMouseEvent(event),
                            getScreenPoint2DFromMouseEvent(event))
                    );
                    drawZoomCoordinateLine(event);
                }
            }
            mouseEvents.clear();
            
        });

        EventStream<MouseEvent> mouseEventsStream = EventStreams.eventsOf(canvas, MouseEvent.ANY);
        EventStream<org.lorainelab.igb.visualization.event.MouseEvent> stationaryPositions = mouseEventsStream
                .successionEnds(Duration.ofSeconds(1))
                .filter(e -> e.getEventType() == MouseEvent.MOUSE_MOVED)
                .map(e -> {
                    return new org.lorainelab.igb.visualization.event.MouseEvent(
                            new Point2D(e.getX(), e.getY()),
                            new Point2D(e.getScreenX(), e.getScreenY())
                    );
                });

        EventStream<Void> stoppers = mouseEventsStream.supply((Void) null);

        EventStream<Either<org.lorainelab.igb.visualization.event.MouseEvent, Void>> stationaryEvents
                = stationaryPositions.or(stoppers)
                .distinct();

        stationaryEvents.<MouseStationaryEvent>map(either -> either.unify(
                pos -> new MouseStationaryStartEvent(pos),
                stop -> new MouseStationaryEndEvent()))
                .subscribe(evt -> eventBus.post(evt));
        
        canvas.setOnScroll(scrollEvent -> {
            final boolean isForwardScroll = scrollEvent.getDeltaY() > 0.0;
            if (isForwardScroll) {
                eventBus.post(new ScrollScaleEvent(Direction.INCREMENT));
            } else {
                eventBus.post(new ScrollScaleEvent(Direction.DECREMENT));
            }
        });
    }

    public void resetZoomStripe() {
        this.zoomStripeCoordinate = -1;
        eventBus.post(new ZoomStripeEvent(zoomStripeCoordinate));
        eventBus.post(new RefreshTrackEvent());
        
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
        zoomStripeCoordinate = Math.floor((event.getX() / xFactor) + xOffset);
        drawZoomCoordinateLine();

    }

    public void drawZoomCoordinateLine() {
        if (zoomStripeCoordinate >= 0) {
            eventBus.post(new ZoomStripeEvent(zoomStripeCoordinate));
            GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.save();
            gc.setStroke(Color.rgb(0, 0, 0, .3));
            gc.scale(xFactor, 1);
            double x = (zoomStripeCoordinate) - xOffset;
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
        visibleVirtualCoordinatesX = (canvas.getWidth() / xFactor);
        xOffset = ((scaleEvent.getScrollX() / 100) * (modelWidth - visibleVirtualCoordinatesX));
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
    public void setEventBusService(EventBusService eventBusService) {
        this.eventBusService = eventBusService;
    }
}
