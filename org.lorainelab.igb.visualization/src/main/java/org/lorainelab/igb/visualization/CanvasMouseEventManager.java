package org.lorainelab.igb.visualization;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import com.google.common.collect.Lists;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import org.lorainelab.igb.data.model.View;
import org.lorainelab.igb.selections.SelectionInfoService;
import org.lorainelab.igb.visualization.model.CanvasPaneModel;
import static org.lorainelab.igb.visualization.model.CanvasPaneModel.MAX_ZOOM_MODEL_COORDINATES_X;
import org.lorainelab.igb.visualization.model.CoordinateTrackRenderer;
import org.lorainelab.igb.visualization.model.TrackRenderer;
import org.lorainelab.igb.visualization.model.TracksModel;
import org.lorainelab.igb.visualization.model.ZoomableTrackRenderer;
import org.lorainelab.igb.visualization.util.BoundsUtil;
import static org.lorainelab.igb.visualization.util.BoundsUtil.enforceRangeBounds;
import static org.lorainelab.igb.visualization.util.CanvasUtils.exponentialScaleTransform;
import static org.lorainelab.igb.visualization.util.CanvasUtils.invertExpScaleTransform;
import org.reactfx.EventStream;
import org.reactfx.EventStreams;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true, provide = CanvasMouseEventManager.class)
public class CanvasMouseEventManager {

    private PrimaryCanvasRegion primaryCanvas;
    private CanvasPaneModel canvasPaneModel;
    private List<MouseEvent> mouseEvents;
    private SelectionInfoService selectionInfoService;
    private TracksModel tracksModel;

    public CanvasMouseEventManager() {
        mouseEvents = Lists.newArrayList();
    }

    @Activate
    public void activate() {
        initailizeKeyListener();
        Canvas canvas = primaryCanvas.getCanvas();
        canvas.setOnScroll(scrollEvent -> {
            final boolean isForwardScroll = scrollEvent.getDeltaY() > 0.0;
            if (isForwardScroll) {
                canvasPaneModel.gethSlider().add(1);
            } else {
                canvasPaneModel.gethSlider().subtract(1);
            }
        });
        canvas.setOnMouseClicked((MouseEvent event) -> {
            mouseEvents.add(event);
        });
        canvas.setOnMouseDragEntered((MouseEvent event) -> {
            canvasPaneModel.resetZoomStripe();
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
            canvasPaneModel.setLocalPoint(getLocalPoint2DFromMouseEvent(event));
            canvasPaneModel.setScreenPoint(getScreenPoint2DFromMouseEvent(event));
            canvasPaneModel.setMouseDragging(true);
        });
        canvas.setOnMouseEntered((MouseEvent event) -> {
            mouseEvents.add(event);
        });
        canvas.setOnMouseExited((MouseEvent event) -> {
            mouseEvents.add(event);
        });
        canvas.setOnMousePressed((MouseEvent event) -> {
            mouseEvents.add(event);
            canvasPaneModel.setMouseClickLocation(getLocalPoint2DFromMouseEvent(event));
        });
        canvas.setOnMouseReleased((MouseEvent event) -> {
            canvasPaneModel.resetZoomStripe();
            List<EventType<? extends MouseEvent>> types = mouseEvents.stream().map(e -> e.getEventType()).collect(Collectors.toList());
            Point2D rangeBoundedDragEventLocation = getRangeBoundedDragEventLocation(event);
            final Point2D screenPoint2DFromMouseEvent = getScreenPoint2DFromMouseEvent(event);
            if (types.contains(MouseEvent.MOUSE_DRAGGED)) {
                //Rectangle2D selectionRectangle = getSelectionRectangle(event);
                tracksModel.getTrackRenderers().stream().filter(tr -> tr instanceof CoordinateTrackRenderer).findFirst().ifPresent(tr -> {
                    canvasPaneModel.getMouseClickLocation().get().ifPresent(mouseClickLocation -> {
                        Point2D point = getLocalPoint2DFromMouseEvent(event);
                        Rectangle2D boundingRect = tr.getCanvasContext().getBoundingRect();
                        if (boundingRect.contains(mouseClickLocation)) {
                            Point2D lastMouseDragLocation = getLocalPoint2DFromMouseEvent(event);
                            double xfactor = canvasPaneModel.getxFactor().doubleValue();
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

//                if (types.contains(MouseEvent.MOUSE_EXITED)) {
//                    eventBus.post(new ClickDragEndEvent(rangeBoundedDragEventLocation, screenPoint2DFromMouseEvent, selectionRectangle));
//                } else {
//                    eventBus.post(new ClickDragEndEvent(rangeBoundedDragEventLocation, screenPoint2DFromMouseEvent, selectionRectangle));
//                }
            } else {
                if (event.getClickCount() >= 2) {
                    selectionInfoService.getSelectedGlyphs().clear();
                    tracksModel.getTrackRenderers().stream()
                            .filter(tr -> tr instanceof ZoomableTrackRenderer)
                            .map(tr -> ZoomableTrackRenderer.class.cast(tr))
                            .filter(tr -> tr.isContained(getLocalPoint2DFromMouseEvent(event)))
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
            clearMouseEventState();

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
    }

    private void updateZoomStripe(MouseEvent event) {
        ReadOnlyDoubleProperty scrollX = canvasPaneModel.getScrollX();
        ReadOnlyDoubleProperty modelWidth = canvasPaneModel.getModelWidth();
        ReadOnlyDoubleProperty visibleVirtualCoordinatesX = canvasPaneModel.getVisibleVirtualCoordinatesX();
        double xOffset = Math.round((scrollX.get() / 100) * (modelWidth.get() - visibleVirtualCoordinatesX.get()));
        xOffset = enforceRangeBounds(xOffset, 0, modelWidth.get());
        double zoomStripeCoordinate = Math.floor((event.getX() / canvasPaneModel.getxFactor().doubleValue()) + xOffset);
        canvasPaneModel.setZoomStripeCoordinate(zoomStripeCoordinate);
    }

    private Point2D getRangeBoundedDragEventLocation(MouseEvent event) {
        double boundedEventX = BoundsUtil.enforceRangeBounds(event.getX(), 0, primaryCanvas.getCanvas().getWidth());
        double boundedEventY = BoundsUtil.enforceRangeBounds(event.getY(), 0, primaryCanvas.getCanvas().getHeight());
        return new Point2D(boundedEventX, boundedEventY);
    }

    private Point2D getLocalPoint2DFromMouseEvent(MouseEvent event) {
        return new Point2D(event.getX(), event.getY());
    }

    private Point2D getScreenPoint2DFromMouseEvent(MouseEvent event) {
        return new Point2D(event.getScreenX(), event.getScreenY());
    }

    private void jumpZoom(Rectangle2D focusRect, TrackRenderer eventLocationReference, MouseEvent event) {
        View view = eventLocationReference.getView();
        double modelWidth = canvasPaneModel.getModelWidth().doubleValue();
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
        double newHSlider = invertExpScaleTransform(primaryCanvas.getCanvas().getWidth(), canvasPaneModel.getModelWidth().get(), scaleXalt);
        double xFactor = exponentialScaleTransform(primaryCanvas.getCanvas().getWidth(), canvasPaneModel.getModelWidth().get(), newHSlider);
        canvasPaneModel.resetZoomStripe();
        canvasPaneModel.setxFactor(xFactor);
        canvasPaneModel.setScrollX(scrollXValue);
    }

    @Reference
    public void setCanvasPaneModel(CanvasPaneModel canvasPaneModel) {
        this.canvasPaneModel = canvasPaneModel;
    }

    @Reference
    public void setPrimaryCanvas(PrimaryCanvasRegion primaryCanvas) {
        this.primaryCanvas = primaryCanvas;
    }

    @Reference
    public void setSelectionInfoService(SelectionInfoService selectionInfoService) {
        this.selectionInfoService = selectionInfoService;
    }

    @Reference
    public void setTracksModel(TracksModel tracksModel) {
        this.tracksModel = tracksModel;
    }

    private void clearMouseEventState() {
        mouseEvents.clear();
        canvasPaneModel.setLocalPoint(null);
        canvasPaneModel.setMouseDragging(false);
        canvasPaneModel.setScreenPoint(null);
    }

    private void initailizeKeyListener() {
        Canvas canvas = primaryCanvas.getCanvas();
        canvas.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                switch (event.getCode()) {
                    case CONTROL:
                    case SHIFT:
                        canvasPaneModel.setMultiSelectModeActive(true);
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
                        canvasPaneModel.setMultiSelectModeActive(false);
                        break;
                }
            }
        });
    }
}
