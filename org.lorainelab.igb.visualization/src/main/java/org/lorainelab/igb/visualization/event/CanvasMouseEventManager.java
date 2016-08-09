package org.lorainelab.igb.visualization.event;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import java.time.Duration;
import java.util.Optional;
import java.util.stream.Collectors;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import org.lorainelab.igb.data.model.View;
import org.lorainelab.igb.selections.SelectionInfoService;
import org.lorainelab.igb.visualization.model.CanvasModel;
import static org.lorainelab.igb.visualization.model.CanvasModel.MAX_ZOOM_MODEL_COORDINATES_X;
import org.lorainelab.igb.visualization.model.TracksModel;
import org.lorainelab.igb.visualization.ui.CanvasRegion;
import org.lorainelab.igb.visualization.util.BoundsUtil;
import static org.lorainelab.igb.visualization.util.BoundsUtil.enforceRangeBounds;
import static org.lorainelab.igb.visualization.util.CanvasUtils.exponentialScaleTransform;
import static org.lorainelab.igb.visualization.util.CanvasUtils.invertExpScaleTransform;
import org.lorainelab.igb.visualization.widget.TrackRenderer;
import org.lorainelab.igb.visualization.widget.ZoomableTrackRenderer;
import org.reactfx.EventStream;
import org.reactfx.EventStreams;
import org.reactfx.util.Either;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true, provide = CanvasMouseEventManager.class)
public class CanvasMouseEventManager {

    private static final Logger LOG = LoggerFactory.getLogger(CanvasMouseEventManager.class);
    private CanvasRegion canvasRegion;
    private CanvasModel canvasModel;
    private SelectionInfoService selectionInfoService;
    private TracksModel tracksModel;
    private Canvas canvas;

    @Activate
    public void activate() {
        canvas = canvasRegion.getCanvas();
        initailizeKeyListener();
        initializeMouseEventHandlers();
    }

    private void initializeMouseEventHandlers() {
        addCanvasScrollEventHandler();
        addHoverEventHandler();
        canvas.setOnMouseDragEntered(e -> canvasModel.resetZoomStripe());

        canvas.setOnMouseDragged(event -> {
            canvasModel.setLocalPoint(getPoint2dFromMouseEvent(event));
            canvasModel.setScreenPoint(getScreenPoint2DFromMouseEvent(event));
            canvasModel.setMouseDragging(true);
        });
        canvas.setOnMousePressed(event -> {
            canvasModel.setMouseClickLocation(getPoint2dFromMouseEvent(event));
        });
        canvas.setOnMouseReleased((MouseEvent event) -> {
            if (canvasModel.isMouseDragging()) {
                processMouseDragReleased(event);
            } else {
                processMouseClicked(event);
            }
            canvasModel.setLocalPoint(null);
            canvasModel.setScreenPoint(null);
            canvasModel.setMouseDragging(false);
        });

    }

    private void addHoverEventHandler() {
        EventStream<MouseEvent> mouseEventsStream = EventStreams.eventsOf(canvas, MouseEvent.ANY);
        EventStream<MouseHoverEvent> stationaryPositions = mouseEventsStream
                .successionEnds(Duration.ofSeconds(1))
                .filter(e -> e.getEventType() == MouseEvent.MOUSE_MOVED)
                .map(e -> {
                    return new MouseHoverEvent(
                            new Point2D(e.getX(), e.getY()),
                            new Point2D(e.getScreenX(), e.getScreenY())
                    );
                });
        EventStream<MouseHoverEvent> stoppers = mouseEventsStream.supply((MouseHoverEvent) null);

        EventStream<Either<MouseHoverEvent, MouseHoverEvent>> stationaryEvents
                = stationaryPositions.or(stoppers)
                        .distinct();

        stationaryEvents.<MouseHoverEvent>map(either -> {
            return either.unify(
                    pos -> pos,
                    stop -> null
            );
        }).subscribe(hoverEvent -> {
            if (hoverEvent != null) {
                tracksModel.getTrackRenderers().stream()
                        .filter(tr -> tr instanceof ZoomableTrackRenderer)
                        .map(tr -> ZoomableTrackRenderer.class.cast(tr))
                        .filter(tr -> tr.isContained(hoverEvent.getLocal()))
                        .findFirst()
                        .ifPresent(tr -> tr.showToolTip(hoverEvent));
            } else {
                tracksModel.getTrackRenderers().stream()
                        .filter(tr -> tr instanceof ZoomableTrackRenderer)
                        .map(tr -> ZoomableTrackRenderer.class.cast(tr))
                        .forEach(tr -> {
                            tr.hideTooltip();
                        });
            }
        });

    }

    private void addCanvasScrollEventHandler() {
        canvas.setOnScroll(scrollEvent -> {
            final boolean isForwardScroll = scrollEvent.getDeltaY() > 0.0;
            if (isForwardScroll) {
                canvasModel.gethSlider().add(1);
            } else {
                canvasModel.gethSlider().subtract(1);
            }
        });
    }

    private void processMouseClicked(MouseEvent event) {
        if (event.getClickCount() >= 2) {
            selectionInfoService.getSelectedGlyphs().clear();
            tracksModel.getTrackRenderers().stream()
                    .filter(tr -> tr instanceof ZoomableTrackRenderer)
                    .map(tr -> ZoomableTrackRenderer.class.cast(tr))
                    .filter(tr -> tr.isContained(getPoint2dFromMouseEvent(event)))
                    .findFirst().ifPresent(tr
                            -> tr.getTrack()
                            .getGlyphs()
                            .stream()
                            .filter(glyph -> glyph.isSelected())
                            .findFirst().ifPresent(glyphToJumpZoom -> {
                                jumpZoom(glyphToJumpZoom.getRenderBoundingRect(), tr, event);
                                selectionInfoService.getSelectedGlyphs().add(glyphToJumpZoom);
                            }));
        } else {
            selectionInfoService.getSelectedGlyphs().clear();
            selectionInfoService.getSelectedGlyphs().addAll(
                    tracksModel.getTrackRenderers().stream()
                            .filter(tr -> tr instanceof ZoomableTrackRenderer)
                            .map(tr -> ZoomableTrackRenderer.class.cast(tr)).flatMap(tr
                            -> tr.getTrack()
                                    .getGlyphs()
                                    .stream()
                                    .filter(glyph -> glyph.isSelected()))
                            .collect(Collectors.toList())
            );

        }
        updateZoomStripe(event);
    }

    private void processMouseDragReleased(MouseEvent event) {
        canvasModel.getMouseClickLocation().get().ifPresent(mouseClickLocation -> {
            tracksModel.getCoordinateTrackRenderer().ifPresent(tr -> {
                Rectangle2D boundingRect = tr.getCanvasContext().getBoundingRect();
                if (boundingRect.contains(mouseClickLocation)) {
                    Point2D lastMouseDragLocation = getPoint2dFromMouseEvent(event);
                    double xfactor = canvasModel.getxFactor().doubleValue();
                    double lastMouseDragX = lastMouseDragLocation.getX() / xfactor;
                    double lastMouseClickX = mouseClickLocation.getX() / xfactor;
                    double minX = tr.getView().getBoundingRect().getMinX();
                    double x1 = minX + lastMouseClickX;
                    double x2 = minX + lastMouseDragX;
                    if (x1 > x2) {
                        double x1old = x1;
                        x1 = x2;
                        x2 = x1old;
                    }
                    final Rectangle2D zoomFocus = new Rectangle2D(x1, 0, x2 - x1, Double.MAX_VALUE);
                    jumpZoom(zoomFocus, tr, event);
                }
            });

        });
        canvasModel.setSelectionRectangle(getSelectionRectangle().orElse(null));
    }

    private Optional<Rectangle2D> getSelectionRectangle() {
        Rectangle2D[] selectionRectangle = new Rectangle2D[1];
        canvasModel.getMouseClickLocation().get().ifPresent(clickStartPosition -> {
            tracksModel.getCoordinateTrackRenderer().ifPresent(coordinateTrackRenderer -> {
                if (!coordinateTrackRenderer.getCanvasContext().getBoundingRect().contains(clickStartPosition)) {
                    canvasModel.getLocalPoint().get().ifPresent(localPoint -> {
                        double minX;
                        double maxX;
                        double minY;
                        double maxY;
                        Point2D rangeBoundedEventLocation = getRangeBoundedDragEventLocation(localPoint);
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
                        selectionRectangle[0] = new Rectangle2D(minX, minY, maxX - minX, maxY - minY);
                    });
                }
            });
        });
        return Optional.ofNullable(selectionRectangle[0]);
    }

    private Point2D getRangeBoundedDragEventLocation(Point2D localPoint) {
        double boundedEventX = BoundsUtil.enforceRangeBounds(localPoint.getX(), 0, canvasRegion.getCanvas().getWidth());
        double boundedEventY = BoundsUtil.enforceRangeBounds(localPoint.getY(), 0, canvasRegion.getCanvas().getHeight());
        return new Point2D(boundedEventX, boundedEventY);
    }

    private void updateZoomStripe(MouseEvent event) {
        ReadOnlyDoubleProperty scrollX = canvasModel.getScrollX();
        ReadOnlyDoubleProperty modelWidth = canvasModel.getModelWidth();
        ReadOnlyDoubleProperty visibleVirtualCoordinatesX = canvasModel.getVisibleVirtualCoordinatesX();
        double xOffset = Math.round((scrollX.get() / 100) * (modelWidth.get() - visibleVirtualCoordinatesX.get()));
        xOffset = enforceRangeBounds(xOffset, 0, modelWidth.get());
        double zoomStripeCoordinate = Math.floor((event.getX() / canvasModel.getxFactor().doubleValue()) + xOffset);
        canvasModel.setZoomStripeCoordinate(zoomStripeCoordinate);
    }

    private Point2D getRangeBoundedDragEventLocation(MouseEvent event) {
        double boundedEventX = BoundsUtil.enforceRangeBounds(event.getX(), 0, canvasRegion.getCanvas().getWidth());
        double boundedEventY = BoundsUtil.enforceRangeBounds(event.getY(), 0, canvasRegion.getCanvas().getHeight());
        return new Point2D(boundedEventX, boundedEventY);
    }

    private Point2D getPoint2dFromMouseEvent(MouseEvent event) {
        return new Point2D(event.getX(), event.getY());
    }

    private Point2D getScreenPoint2DFromMouseEvent(MouseEvent event) {
        return new Point2D(event.getScreenX(), event.getScreenY());
    }

    private void jumpZoom(Rectangle2D focusRect, TrackRenderer eventLocationReference, MouseEvent event) {
        View view = eventLocationReference.getView();
        double modelWidth = canvasModel.getModelWidth().doubleValue();
        double minX = Math.max(focusRect.getMinX(), view.getBoundingRect().getMinX());
        double maxX = Math.min(focusRect.getMaxX(), view.getBoundingRect().getMaxX());
        double width = maxX - minX;
        if (width < MAX_ZOOM_MODEL_COORDINATES_X) {
            width = Math.max(width * 1.1, MAX_ZOOM_MODEL_COORDINATES_X);
            minX = Math.max((minX + focusRect.getWidth() / 2) - (width / 2), 0);
        }
        final double scaleXalt = eventLocationReference.getCanvasContext().getBoundingRect().getWidth() / width;
        double scrollPosition = (minX / (modelWidth - width)) * 100;
        final double scrollXValue = enforceRangeBounds(scrollPosition, 0, 100);
        double newHSlider = invertExpScaleTransform(canvasRegion.getCanvas().getWidth(), canvasModel.getModelWidth().get(), scaleXalt);
        double xFactor = exponentialScaleTransform(canvasRegion.getCanvas().getWidth(), canvasModel.getModelWidth().get(), newHSlider);
        canvasModel.resetZoomStripe();
        canvasModel.setxFactor(xFactor);
        canvasModel.setScrollX(scrollXValue);
    }

    @Reference
    public void setCanvasModel(CanvasModel canvasModel) {
        this.canvasModel = canvasModel;
    }

    @Reference
    public void setCanvasRegion(CanvasRegion canvasRegion) {
        this.canvasRegion = canvasRegion;
    }

    @Reference
    public void setSelectionInfoService(SelectionInfoService selectionInfoService) {
        this.selectionInfoService = selectionInfoService;
    }

    @Reference
    public void setTracksModel(TracksModel tracksModel) {
        this.tracksModel = tracksModel;
    }

    private void initailizeKeyListener() {
        canvas.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                switch (event.getCode()) {
                    case CONTROL:
                    case SHIFT:
                        canvasModel.setMultiSelectModeActive(true);
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
                        canvasModel.setMultiSelectModeActive(false);
                        break;
                }
            }
        });
    }
}
