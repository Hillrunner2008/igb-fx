package org.lorainelab.igb.visualization.event;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import java.time.Duration;
import java.util.Optional;
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
        canvas.setOnDragDetected(event -> {
            canvasModel.setClickDragStartPosition(getPoint2dFromMouseEvent(event));
            canvasModel.resetZoomStripe();
        });

        canvas.setOnMouseDragged(event -> {
            canvasModel.setLastDragPosition(getPoint2dFromMouseEvent(event));
            canvasModel.setMouseDragging(true);
        });
        canvas.setOnMouseExited(event -> {
            canvasModel.setLastDragPosition(getRangeBoundedDragEventLocation(event));
        });
        canvas.setOnMousePressed(event -> {
            canvasModel.setMouseClickLocation(getPoint2dFromMouseEvent(event));
        });
        canvas.setOnMouseReleased((MouseEvent event) -> {
            if (canvasModel.isMouseDragging()) {
                processMouseDragReleased(event);
                canvasModel.setClickDragStartPosition(null);
                canvasModel.setClickDragStartPosition(null);
                canvasModel.setMouseDragging(false);
            } else {
                processMouseClicked(event);
            }
        });

    }

    private void addHoverEventHandler() {
        EventStream<MouseEvent> allMouseEvents = EventStreams.eventsOf(canvas, MouseEvent.ANY);
        EventStream<MouseHoverEvent> stationaryPositions = allMouseEvents
                .successionEnds(Duration.ofSeconds(1))
                .filter(e -> e.getEventType() == MouseEvent.MOUSE_MOVED)
                .map(e -> {
                    return new MouseHoverEvent(
                            new Point2D(e.getX(), e.getY()),
                            new Point2D(e.getScreenX(), e.getScreenY())
                    );
                });
        stationaryPositions.or(allMouseEvents).distinct()
                .map(either -> either.asLeft())
                .subscribe(hoverEvent -> {
                    if (hoverEvent.isPresent()) {
                        getTrackRendererContainingPoint(hoverEvent.get().getLocal())
                                .ifPresent(tr -> tr.showToolTip(hoverEvent.get()));
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
        canvasModel.forceRefresh();
        Point2D mousePoint = getPoint2dFromMouseEvent(event);
        if (event.getClickCount() >= 2) {
            selectionInfoService.getSelectedGlyphs().clear();
            getTrackRendererContainingPoint(mousePoint).ifPresent(tr -> {

                tr.getTrack().getSelectedGlyphs().stream()
                        .findFirst().ifPresent(glyphToJumpZoom -> {
                            jumpZoom(glyphToJumpZoom.getBoundingRect(), tr, event);
                            selectionInfoService.getSelectedGlyphs().add(glyphToJumpZoom);
                        });
            });
        } else {
            selectionInfoService.getSelectedGlyphs().clear();
            getTrackRendererContainingPoint(mousePoint).ifPresent(tr -> {
                selectionInfoService.getSelectedGlyphs().addAll(
                        tr.getTrack().getSelectedGlyphs()
                );
            });
        }
        updateZoomStripe(event);
    }

    private Optional<ZoomableTrackRenderer> getTrackRendererContainingPoint(Point2D mousePoint) {
        return tracksModel.getTrackRenderers().stream()
                .filter(tr -> tr instanceof ZoomableTrackRenderer)
                .map(tr -> ZoomableTrackRenderer.class.cast(tr))
                .filter(tr -> tr.isContained(mousePoint))
                .findFirst();
    }

    private void processMouseDragReleased(MouseEvent event) {
        canvasModel.getClickDragStartPosition().get().ifPresent(dragStartPoint -> {
            tracksModel.getCoordinateTrackRenderer().ifPresent(tr -> {
                Rectangle2D boundingRect = tr.getCanvasContext().getBoundingRect();
                if (boundingRect.contains(dragStartPoint)) {
                    Point2D lastMouseDragLocation = getPoint2dFromMouseEvent(event);
                    double xfactor = canvasModel.getxFactor().doubleValue();
                    double lastMouseDragX = lastMouseDragLocation.getX() / xfactor;
                    double lastMouseClickX = dragStartPoint.getX() / xfactor;
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
        tracksModel.getCoordinateTrackRenderer().ifPresent(coordinateTrackRenderer -> {
            canvasModel.getClickDragStartPosition().get().ifPresent(clickDragStartPoint -> {
                if (!coordinateTrackRenderer.getCanvasContext().getBoundingRect().contains(clickDragStartPoint)) {
                    canvasModel.getLastDragPosition().get().ifPresent(lastDragPoint -> {
                        double minX;
                        double maxX;
                        double minY;
                        double maxY;
                        Point2D rangeBoundedclickDragStartPoint = getRangeBoundedDragEventLocation(clickDragStartPoint);
                        Point2D rangeBoundedlastDragPoint = getRangeBoundedDragEventLocation(lastDragPoint);
                        if (rangeBoundedlastDragPoint.getX() < rangeBoundedclickDragStartPoint.getX()) {
                            minX = rangeBoundedlastDragPoint.getX();
                            maxX = rangeBoundedclickDragStartPoint.getX();
                        } else {
                            minX = rangeBoundedclickDragStartPoint.getX();
                            maxX = rangeBoundedlastDragPoint.getX();
                        }
                        if (rangeBoundedlastDragPoint.getY() < rangeBoundedclickDragStartPoint.getY()) {
                            minY = rangeBoundedlastDragPoint.getY();
                            maxY = rangeBoundedclickDragStartPoint.getY();
                        } else {
                            minY = rangeBoundedclickDragStartPoint.getY();
                            maxY = rangeBoundedlastDragPoint.getY();
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
