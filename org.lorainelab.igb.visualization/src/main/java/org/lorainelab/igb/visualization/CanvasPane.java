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
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import static javafx.scene.input.KeyCode.SHIFT;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import org.lorainelab.igb.selections.SelectionInfoService;
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
import org.lorainelab.igb.visualization.event.SelectionChangeEvent;
import org.lorainelab.igb.visualization.event.ZoomStripeEvent;
import org.lorainelab.igb.visualization.util.BoundsUtil;
import org.lorainelab.igb.visualization.util.CanvasUtils;
import org.reactfx.EventStream;
import org.reactfx.EventStreams;
import org.reactfx.util.Either;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true, provide = CanvasPane.class)
public class CanvasPane extends Region {

    private static final Logger LOG = LoggerFactory.getLogger(CanvasPane.class);
    private Canvas canvas;
    private double modelWidth;
    private double xFactor;
    private double zoomStripeCoordinate;
    private double xOffset;
    private double visibleVirtualCoordinatesX;
    private EventBus eventBus;
    private MainController controller;
    private EventBusService eventBusService;
    private List<MouseEvent> mouseEvents;
    private SelectionInfoService selectionInfoService;
    private boolean multiSelectModeActive;
    private Point2D clickStartPosition;

    public CanvasPane() {
    }

    @Activate
    public void activate() {
        eventBus = eventBusService.getEventBus();
        eventBus.register(this);
        mouseEvents = new ArrayList<>();
        this.modelWidth = 1;
        canvas = new Canvas();
        canvas.setFocusTraversable(true);
        canvas.addEventFilter(MouseEvent.ANY, (e) -> canvas.requestFocus());
        getChildren().add(canvas);
        canvas.widthProperty().addListener(observable -> {
            clear();
            xFactor = canvas.getWidth() / modelWidth;
        });
        canvas.heightProperty().addListener(observable -> clear());
        zoomStripeCoordinate = -1;
        initailizeKeyListener();
//        initializeMouseEventHandlers();
        selectionInfoService.getSelectedChromosome().addListener((observable, oldValue, newValue) -> {
            if (newValue.isPresent()) {
                modelWidth = newValue.get().getLength();
                xFactor = canvas.getWidth() / modelWidth;
            }
        });
        clickStartPosition = new Point2D(0, 0);
    }

    private Point2D getLocalPoint2DFromMouseEvent(MouseEvent event) {
        return new Point2D(event.getX(), event.getY());
    }

    private Point2D getScreenPoint2DFromMouseEvent(MouseEvent event) {
        return new Point2D(event.getScreenX(), event.getScreenY());
    }

    //TODO there are jump zooming bugs that appear to be related to too mouse event selection rectangles not matching coordinate range selected..
    private void initializeMouseEventHandlers() {

        canvas.setOnMouseClicked((MouseEvent event) -> {
            mouseEvents.add(event);
        });
        canvas.setOnMouseDragEntered((MouseEvent event) -> {
            zoomStripeCoordinate = -1;
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
            drawSelectionRectangle(event);
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
            eventBus.post(new ClickDraggingEvent(
                    getRangeBoundedDragEventLocation(event),
                    getScreenPoint2DFromMouseEvent(event))
            );
            mouseEvents.add(event);
        });
        canvas.setOnMousePressed((MouseEvent event) -> {
            clickStartPosition = getLocalPoint2DFromMouseEvent(event);
            mouseEvents.add(event);
            eventBus.post(new ClickDragStartEvent(
                    getLocalPoint2DFromMouseEvent(event),
                    getScreenPoint2DFromMouseEvent(event))
            );
        });
        canvas.setOnMouseReleased((MouseEvent event) -> {
            resetZoomStripe();
            List<EventType<? extends MouseEvent>> types = mouseEvents.stream().map(e -> e.getEventType()).collect(Collectors.toList());
            Point2D rangeBoundedDragEventLocation = getRangeBoundedDragEventLocation(event);
            final Point2D screenPoint2DFromMouseEvent = getScreenPoint2DFromMouseEvent(event);
            if (types.contains(MouseEvent.MOUSE_DRAGGED)) {
                Rectangle2D selectionRectangle = getSelectionRectangle(event);
                if (types.contains(MouseEvent.MOUSE_EXITED)) {
                    eventBus.post(new ClickDragEndEvent(rangeBoundedDragEventLocation, screenPoint2DFromMouseEvent, selectionRectangle));
                } else {
                    eventBus.post(new ClickDragEndEvent(rangeBoundedDragEventLocation, screenPoint2DFromMouseEvent, selectionRectangle));
                }
            } else {
                eventBus.post(new ClickDragCancelEvent());
                if (event.getClickCount() >= 2) {
                    eventBus.post(new MouseDoubleClickEvent(rangeBoundedDragEventLocation, screenPoint2DFromMouseEvent));
                    drawZoomCoordinateLine();
                } else {
                    eventBus.post(new MouseClickedEvent(rangeBoundedDragEventLocation, screenPoint2DFromMouseEvent, multiSelectModeActive));
                    drawZoomCoordinateLine(event);
                }
            }
            mouseEvents.clear();
            eventBus.post(new SelectionChangeEvent());
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

    private Rectangle2D getSelectionRectangle(MouseEvent event) {
        double minX;
        double maxX;
        double minY;
        double maxY;
        Point2D rangeBoundedEventLocation = getRangeBoundedDragEventLocation(event);
        if (clickStartPosition.getX() < rangeBoundedEventLocation.getX()) {
            minX = clickStartPosition.getX();
            maxX = rangeBoundedEventLocation.getX();
        } else {
            minX = rangeBoundedEventLocation.getX();
            maxX = clickStartPosition.getX();
        }
        if (clickStartPosition.getY() < rangeBoundedEventLocation.getY()) {
            minY = clickStartPosition.getY();
            maxY = rangeBoundedEventLocation.getY();
        } else {
            minY = rangeBoundedEventLocation.getY();
            maxY = clickStartPosition.getY();
        }
        return new Rectangle2D(minX, minY, maxX - minX, maxY - minY);
    }

    private Point2D getRangeBoundedDragEventLocation(MouseEvent event) {
        double boundedEventX = BoundsUtil.enforceRangeBounds(event.getX(), 0, getWidth());
        double boundedEventY = BoundsUtil.enforceRangeBounds(event.getY(), 0, getHeight());
        return new Point2D(boundedEventX, boundedEventY);
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

    public void clear() {
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
            clear();
            eventBus.post(new ZoomStripeEvent(zoomStripeCoordinate));
            eventBus.post(new RefreshTrackEvent());
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

    private void drawSelectionRectangle(MouseEvent event) {
        clear();
        eventBus.post(new RefreshTrackEvent());
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.save();
        gc.setStroke(Color.RED);
        Rectangle2D selectionRectangle = getSelectionRectangle(event);
        gc.strokeRect(selectionRectangle.getMinX(), selectionRectangle.getMinY(), selectionRectangle.getWidth(), selectionRectangle.getHeight());
        gc.restore();
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
    public void setMainViewController(MainController controller) {
        this.controller = controller;
    }

    @Reference
    public void setEventBusService(EventBusService eventBusService) {
        this.eventBusService = eventBusService;
    }

    @Reference
    public void setSelectionInfoService(SelectionInfoService selectionInfoService) {
        this.selectionInfoService = selectionInfoService;
    }

    private void initailizeKeyListener() {
        canvas.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                switch (event.getCode()) {
                    case CONTROL:
                    case SHIFT:
                        multiSelectModeActive = true;
                        break;
                }
            }
        });

        canvas.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                switch (event.getCode()) {
                    case CONTROL:
                    case SHIFT:
                        multiSelectModeActive = false;
                        break;
                }
            }
        });
    }
}
