/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.visualization.component;

import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.eventbus.Subscribe;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.SetChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import org.controlsfx.control.PlusMinusSlider.PlusMinusEvent;
import org.lorainelab.igb.data.model.CanvasContext;
import org.lorainelab.igb.data.model.Chromosome;
import org.lorainelab.igb.data.model.DataSet;
import org.lorainelab.igb.data.model.GenomeVersion;
import org.lorainelab.igb.data.model.Track;
import org.lorainelab.igb.data.model.View;
import org.lorainelab.igb.visualization.CanvasPane;
import org.lorainelab.igb.visualization.component.api.Component;
import org.lorainelab.igb.visualization.model.CoordinateTrackRenderer;
import org.lorainelab.igb.visualization.model.TrackRenderer;
import static org.lorainelab.igb.visualization.model.TrackRenderer.MAX_ZOOM_MODEL_COORDINATES_X;
import org.lorainelab.igb.visualization.model.ViewPortManager;
import org.lorainelab.igb.visualization.model.ZoomableTrackRenderer;
import org.lorainelab.igb.visualization.store.AppStore;
import org.lorainelab.igb.visualization.store.AppStoreEvent;
import org.lorainelab.igb.visualization.util.BoundsUtil;
import static org.lorainelab.igb.visualization.util.BoundsUtil.enforceRangeBounds;
import static org.lorainelab.igb.visualization.util.CanvasUtils.exponentialScaleTransform;
import static org.lorainelab.igb.visualization.util.CanvasUtils.invertExpScaleTransform;
import static org.lorainelab.igb.visualization.util.CanvasUtils.linearScaleTransform;
import static org.lorainelab.igb.visualization.util.FXUtilities.runAndWait;
import org.reactfx.EventStream;
import org.reactfx.EventStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jeckstei
 */
public class App extends Component<AppProps, AppState> {

    private static final Logger LOG = LoggerFactory.getLogger(App.class);

    private double lastHSliderFire = -1;
    private ViewPortManager viewPortManager;
    private static final int TOTAL_SLIDER_THUMB_WIDTH = 30;
    private boolean ignoreHSliderEvent = false;
    private boolean ignoreScrollXEvent = false;
    private List<MouseEvent> mouseEvents;

    public App(AppProps appProps) {
        this.props = appProps;
        mouseEvents = Lists.newArrayList();
        this.state = new AppState();
        initializeCanvas();
        initializeChromosomeSelectionListener();
        initializeGenomeVersionSelectionListener();
        initializeMouseEvents();
        initializePlusMinusSlider();
        initializeZoomScrollBar();
        Platform.runLater(() -> {
            refreshSliderWidget();
            updateCanvasContexts();
        });
        AppStore.getStore().subscribe(this);

    }

    @Override
    public App beforeComponentReady() {
        return this;
    }

    @Subscribe
    private void subscribeToAppStore(AppStoreEvent event) {
        double hSliderValue = AppStore.getStore().gethSlider();
        double xScrollValue = AppStore.getStore().getScrollX();
        if (hSliderValue != this.getProps().gethSlider().getValue()) {
            ignoreHSliderEvent = true;
            this.getProps().gethSlider().setValue(hSliderValue);
        }
        if (xScrollValue != this.getProps().getScrollX().getValue()) {
            ignoreScrollXEvent = true;
            this.getProps().getScrollX().set(xScrollValue);
        }

        AppState state = this.getState()
                .setxFactor(AppStore.getStore().getxFactor())
                .setyFactor(AppStore.getStore().getyFactor())
                .setScrollX(AppStore.getStore().getScrollX())
                .setScrollY(AppStore.getStore().getScrollY())
                .setScrollYVisibleAmount(AppStore.getStore().getScrollYVisibleAmount())
                .sethSlider(AppStore.getStore().gethSlider())
                .setvSlider(AppStore.getStore().getvSlider())
                .setTrackRenderers(AppStore.getStore().getTrackRenderers())
                .setLoadedDataSets(AppStore.getStore().getLoadedDataSets())
                .setSelectedGenomeVersion(AppStore.getStore().getSelectedGenomeVersion())
                .setSelectedChromosome(AppStore.getStore().getSelectedChromosome())
                .setZoomStripeCoordinates(AppStore.getStore().getZoomStripeCoordinate())
                .setLocalPoint(AppStore.getStore().getLocalPoint())
                .setMouseClickLocation(AppStore.getStore().getMouseClickLocation())
                .setMouseDragging(AppStore.getStore().isMouseDragging())
                .setScreenPoint(AppStore.getStore().getScreenPoint());
        updateCanvasContexts();
        this.setState(state);

    }

    private void initializePlusMinusSlider() {
        this.getProps().getPlusMinusSlider().setOnValueChanged((PlusMinusEvent event) -> {
            final double updatedScrollXValue = getUpdatedScrollxValue(event.getValue());
            double scrollX = this.getState().getScrollX();
            if (updatedScrollXValue != scrollX) {
                if (Platform.isFxApplicationThread()) {
                    AppStore.getStore().updatePlusMinusSlider(updatedScrollXValue);
                    syncWidgetSlider();
                } else {
                    Platform.runLater(() -> {
                        AppStore.getStore().updatePlusMinusSlider(updatedScrollXValue);
                        syncWidgetSlider();
                    });
                }
            }
        });
    }

    private double getUpdatedScrollxValue(double eventValue) {
        double updatedScrollXValue = this.getState().getScrollX() + getAdjustedScrollValue(eventValue);
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

    private Point2D getLocalPoint2DFromMouseEvent(MouseEvent event) {
        return new Point2D(event.getX(), event.getY());
    }

    private Point2D getScreenPoint2DFromMouseEvent(MouseEvent event) {
        return new Point2D(event.getScreenX(), event.getScreenY());
    }

    private void initializeMouseEvents() {
        this.getProps().getCanvasPane().getCanvas().setOnScroll(scrollEvent -> {
            final boolean isForwardScroll = scrollEvent.getDeltaY() > 0.0;
            if (isForwardScroll) {
                this.getProps().gethSlider().increment();
            } else {
                this.getProps().gethSlider().decrement();
            }
        });
        Canvas canvas = this.getProps().getCanvasPane().getCanvas();
        canvas.setOnMouseClicked((MouseEvent event) -> {
            mouseEvents.add(event);
        });
        canvas.setOnMouseDragEntered((MouseEvent event) -> {
            resetZoomStripe();
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
            AppStore.getStore().updateMouseDraggedLocation(
                    getLocalPoint2DFromMouseEvent(event),
                    getScreenPoint2DFromMouseEvent(event),
                    true
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
            Point2D localPoint2DFromMouseEvent = getLocalPoint2DFromMouseEvent(event);
            AppStore.getStore().updateMouseClickedLocation(localPoint2DFromMouseEvent);
        });
        canvas.setOnMouseReleased((MouseEvent event) -> {
            resetZoomStripe();
            List<EventType<? extends MouseEvent>> types = mouseEvents.stream().map(e -> e.getEventType()).collect(Collectors.toList());
            Point2D rangeBoundedDragEventLocation = getRangeBoundedDragEventLocation(event);
            final Point2D screenPoint2DFromMouseEvent = getScreenPoint2DFromMouseEvent(event);
            if (types.contains(MouseEvent.MOUSE_DRAGGED)) {
                //Rectangle2D selectionRectangle = getSelectionRectangle(event);
                this.getState().getTrackRenderers().stream().filter(tr -> tr instanceof CoordinateTrackRenderer).findFirst().ifPresent(tr -> {
                    Point2D mouseClickLocation = this.getState().getMouseClickLocation();
                    Point2D point = getLocalPoint2DFromMouseEvent(event);
                    Rectangle2D boundingRect = tr.getCanvasContext().getBoundingRect();
                    if (boundingRect.contains(mouseClickLocation)) {

                        Point2D lastMouseClickLocation = this.getState().getMouseClickLocation();
                        Point2D lastMouseDragLocation = getLocalPoint2DFromMouseEvent(event);
                        double xfactor = this.getState().getxFactor();
                        double lastMouseDragX = lastMouseDragLocation.getX() / xfactor;
                        double lastMouseClickX = lastMouseClickLocation.getX() / xfactor;
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

//                if (types.contains(MouseEvent.MOUSE_EXITED)) {
//                    eventBus.post(new ClickDragEndEvent(rangeBoundedDragEventLocation, screenPoint2DFromMouseEvent, selectionRectangle));
//                } else {
//                    eventBus.post(new ClickDragEndEvent(rangeBoundedDragEventLocation, screenPoint2DFromMouseEvent, selectionRectangle));
//                }
            } else {
                if (event.getClickCount() >= 2) {
                    this.getProps().getSelectionInfoService().getSelectedGlyphs().clear();
                    this.getState().getTrackRenderers().stream()
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
                                        this.getProps().getSelectionInfoService().getSelectedGlyphs().add(glyphToJumpZoom);
                                    }));
                } else {
                    this.getProps().getSelectionInfoService().getSelectedGlyphs().clear();
                    this.getProps().getSelectionInfoService().getSelectedGlyphs().addAll(
                            this.getState().getTrackRenderers().stream()
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

//            eventBus.post(new SelectionChangeEvent());
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

//        EventStream<Void> stoppers = mouseEventsStream.supply((Void) null);
//
//        EventStream<Either<org.lorainelab.igb.visualization.event.MouseEvent, Void>> stationaryEvents
//                = stationaryPositions.or(stoppers)
//                        .distinct();
//
//        stationaryEvents.<MouseStationaryEvent>map(either -> either.unify(
//                pos -> new MouseStationaryStartEvent(pos),
//                stop -> new MouseStationaryEndEvent()))
//                .subscribe(evt -> eventBus.post(evt));
    }

    private void clearMouseEventState() {
        AppStore.getStore().updateMouseDraggedLocation(null, null, false);
        mouseEvents.clear();
    }

    private void updateZoomStripe(MouseEvent event) {
        this.getState().getTrackRenderers().stream().filter(tr -> tr instanceof CoordinateTrackRenderer).findFirst().ifPresent(tr -> {
            double xFactor = this.getState().getxFactor();
            final double visibleVirtualCoordinatesX = Math.floor(tr.getCanvasContext().getBoundingRect().getWidth() / xFactor);
            double xOffset = Math.round((tr.getModelWidth() - visibleVirtualCoordinatesX) * (this.getState().getScrollX() / 100));
            double zoomStripeCoordinate = Math.floor((event.getX() / xFactor)
                    + xOffset);
            AppStore.getStore().updateZoomStripe(zoomStripeCoordinate);
        });
    }

//    private Rectangle2D getSelectionRectangle(MouseEvent event) {
//        double minX;
//        double maxX;
//        double minY;
//        double maxY;
//        Point2D rangeBoundedEventLocation = getRangeBoundedDragEventLocation(event);
//        if (clickStartPosition.getX() < rangeBoundedEventLocation.getX()) {
//            minX = clickStartPosition.getX();
//            maxX = rangeBoundedEventLocation.getX();
//        } else {
//            minX = rangeBoundedEventLocation.getX();
//            maxX = clickStartPosition.getX();
//        }
//        if (clickStartPosition.getY() < rangeBoundedEventLocation.getY()) {
//            minY = clickStartPosition.getY();
//            maxY = rangeBoundedEventLocation.getY();
//        } else {
//            minY = rangeBoundedEventLocation.getY();
//            maxY = clickStartPosition.getY();
//        }
//        return new Rectangle2D(minX, minY, maxX - minX, maxY - minY);
//    }
    private void jumpZoom(Rectangle2D focusRect, TrackRenderer eventLocationReference, MouseEvent event) {
        View view = eventLocationReference.getView();
        CanvasPane canvasPane = this.getProps().getCanvasPane();
        double modelWidth = canvasPane.getModelWidth();//eventLocationReference.getModelWidth();
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

        double newHSlider = invertExpScaleTransform(canvasPane, scaleXalt);
        double xFactor = exponentialScaleTransform(
                this.getProps().getCanvasPane(),
                newHSlider
        );
        AppStore.getStore().updateJumpZoom(
                newHSlider,
                scrollXValue,
                getLocalPoint2DFromMouseEvent(event),
                getScreenPoint2DFromMouseEvent(event),
                false,
                xFactor,
                1
        );
        syncWidgetSlider();
    }

    double lastDragX = 0;

    private void initializeZoomScrollBar() {
        Rectangle slider = this.getProps().getSlider();
        Rectangle leftSliderThumb = this.getProps().getLeftSliderThumb();
        Rectangle rightSliderThumb = this.getProps().getRightSliderThumb();
        Pane xSliderPane = this.getProps().getxSliderPane();
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
                double hSlider = (1 - (current / max)) * 100;
                double xFactor = linearScaleTransform(
                        this.getProps().getCanvasPane(),
                        hSlider
                );
                double exphSlider = invertExpScaleTransform(this.getProps().getCanvasPane(), xFactor);
                AppStore.getStore().updateHSlider(exphSlider, newScrollX, xFactor, 1);
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
                double hSlider = (1 - (current / max)) * 100;
                double xFactor = linearScaleTransform(
                        this.getProps().getCanvasPane(),
                        hSlider
                );
                double exphSlider = invertExpScaleTransform(this.getProps().getCanvasPane(), xFactor);
                AppStore.getStore().updateHSlider(exphSlider, newScrollX, xFactor, 1);
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
            AppStore.getStore().updatePlusMinusSlider((current / max) * 100);
            lastDragX = event.getX();
        });
    }

    private Point2D getRangeBoundedDragEventLocation(MouseEvent event) {
        double boundedEventX = BoundsUtil.enforceRangeBounds(event.getX(), 0, this.getProps().getCanvasPane().getWidth());
        double boundedEventY = BoundsUtil.enforceRangeBounds(event.getY(), 0, this.getProps().getCanvasPane().getHeight());
        return new Point2D(boundedEventX, boundedEventY);
    }

    public void resetZoomStripe() {
        AppStore.getStore().updateZoomStripe(-1);
    }

    private void updateCanvasContexts() {
        viewPortManager.refresh(this.getState().getvSlider(), this.getState().getScrollY());

        //TODO: MOve into other setstates
        //this.setState(this.getState().setTotalTrackHeight(viewPortManager.getTotalTrackSize()));
        updateScrollY();
        //updateTrackLabels();
    }

    private void updateScrollY() {
        double sum = this.getState().getTrackRenderers().stream()
                .map(trackRenderer -> trackRenderer.getCanvasContext())
                .filter(canvasContext -> canvasContext.isVisible())
                .mapToDouble(canvasContext -> canvasContext.getBoundingRect().getHeight())
                .sum();
        double scrollYVisibleAmount = (sum / viewPortManager.getTotalTrackSize()) * 100;
        this.getProps().getScrollY().setVisibleAmount(scrollYVisibleAmount);
        //this.setState(this.getState().setScrollYVisibleAmount((sum / this.getState().getTotalTrackHeight()) * 100));
    }

    private double calcScrollXWithZoomStripe(double hSlider) {
        double[] calculatedScrollXPosition = new double[]{this.getState().getScrollX()};
        this.getState().getTrackRenderers().stream().findFirst().ifPresent(tr -> {
            double zoomStripeCoordinate = this.getState().getZoomStripeCoordinates();
            if (zoomStripeCoordinate != -1) {

                //TODO: move into state
                double xFactor = exponentialScaleTransform(
                        this.getProps().getCanvasPane(),
                        hSlider
                );

                View view = tr.getView();
                int modelWidth = tr.getModelWidth();
                CanvasContext canvasContext = tr.getCanvasContext();
                final double visibleVirtualCoordinatesX = Math.floor(canvasContext.getBoundingRect().getWidth() / xFactor);
                final double visibleVirtualCoordinatesY = Math.floor(canvasContext.getBoundingRect().getHeight() / view.getYfactor());
                double zoomStripePositionPercentage = (zoomStripeCoordinate - view.getBoundingRect().getMinX()) / view.getBoundingRect().getWidth();
                double xOffset = Math.max(zoomStripeCoordinate - (visibleVirtualCoordinatesX * zoomStripePositionPercentage), 0);
                double maxXoffset = modelWidth - visibleVirtualCoordinatesX;
                xOffset = Math.min(maxXoffset, xOffset);
                if (maxXoffset > 0) {
                    calculatedScrollXPosition[0] = (xOffset / (maxXoffset)) * 100;
                } else {
                    calculatedScrollXPosition[0] = 0;
                }
            }
        });
        return calculatedScrollXPosition[0];
    }

    final ChangeListener<Number> vSliderListener = (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
        AppStore.getStore().updateVSlider(newValue.doubleValue());
    };
    final ChangeListener<Number> hSliderListener = (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {

        if (ignoreHSliderEvent) {
            ignoreHSliderEvent = false;
            return;
        }
        final boolean isSnapEvent = newValue.doubleValue() % this.getProps().gethSlider().getMajorTickUnit() == 0;
        if (lastHSliderFire < 0 || Math.abs(lastHSliderFire - newValue.doubleValue()) > 1 || isSnapEvent) {
            double scrollX = calcScrollXWithZoomStripe(newValue.doubleValue());
            double xFactor = exponentialScaleTransform(
                    this.getProps().getCanvasPane(),
                    newValue.doubleValue()
            );
            AppStore.getStore().updateHSlider(newValue.doubleValue(), scrollX, xFactor, 1);
            syncWidgetSlider();
            lastHSliderFire = newValue.doubleValue();

        }
    };
    final ChangeListener<Number> hSliderWidgetListener = (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
        boolean isNearMaxZoom = newValue.doubleValue() > 98;
        if (lastHSliderFire < 0 || Math.abs(lastHSliderFire - newValue.doubleValue()) > 1 || isNearMaxZoom) {
            final double xFactor = linearScaleTransform(this.getProps().getCanvasPane(), newValue.doubleValue());
            syncHSlider(xFactor);
            lastHSliderFire = newValue.doubleValue();
        }
    };
    final ChangeListener<Number> widthPropertyListener = (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
        updateCanvasContexts();
        double hSlider = this.getState().gethSlider();
        double scrollX = calcScrollXWithZoomStripe(hSlider);
        double xFactor = exponentialScaleTransform(
                this.getProps().getCanvasPane(),
                hSlider
        );
        AppStore.getStore().updateHSlider(hSlider, scrollX, xFactor, 1);
        Platform.runLater(() -> {
            refreshSliderWidget();
        });
    };
    final ChangeListener<Number> heightPropertyListener = (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
        updateCanvasContexts();
        AppStore.getStore().noop();
    };
    final ChangeListener<Number> scrollXPropertyListener = (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
        final double boundedScrollValue = enforceRangeBounds(newValue.doubleValue(), 0, 100);
        if (boundedScrollValue != newValue.doubleValue()) {
            this.getProps().getScrollX().setValue(boundedScrollValue);
            return;
        }
        if (ignoreScrollXEvent) {
            ignoreScrollXEvent = false;
        } else {
            //updateCanvasContexts();
        }
    };
    final ChangeListener<Number> scrollYPositionListener = (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
        AppStore.getStore().updateScrollY(newValue.doubleValue());
    };
    final EventHandler<ActionEvent> loadDataActionListener = action -> {
        Chromosome selectedChromosome = this.getState().getSelectedChromosome();
        GenomeVersion selectedGenomeVersion = this.getState().getSelectedGenomeVersion();
        Optional.ofNullable(selectedGenomeVersion).ifPresent(genomeVersion -> {
            Optional.ofNullable(selectedChromosome).ifPresent(chr -> {
                genomeVersion.getLoadedDataSets().forEach(dataSet -> {
                    CompletableFuture.supplyAsync(() -> {
                        dataSet.loadRegion(chr.getName(), getCurrentRange());
                        return null;
                    }).thenRun(() -> {
                        Platform.runLater(() -> {
                            AppStore.getStore().noop();
                        });
                    });
                });
            });
        });
    };
    final EventHandler<ActionEvent> loadSequenceActionListener = action -> {
        Chromosome selectedChromosome = this.getState().getSelectedChromosome();
        Optional.ofNullable(selectedChromosome).ifPresent(chr -> {
            CompletableFuture.supplyAsync(() -> {
                chr.loadRegion(getCurrentRange());
                return null;
            }).thenRun(() -> {
                Platform.runLater(() -> {
                    //TODO: hack for refresh
                    AppStore.getStore().noop();
                });
            }).exceptionally(ex -> {
                LOG.error(ex.getMessage(), ex);
                return null;
            });
        });
    };

    private void initializeCanvas() {
        Canvas canvas = this.getProps().getCanvasPane().getCanvas();
        viewPortManager = new ViewPortManager(canvas, this.getState().getTrackRenderers(), 0, 0);
        this.getProps().getvSlider().valueProperty().addListener(vSliderListener);
        this.getProps().gethSlider().valueProperty().addListener(hSliderListener);
        //this.getProps().gethSliderWidget().addListener(hSliderWidgetListener);
        canvas.widthProperty().addListener(widthPropertyListener);
        canvas.heightProperty().addListener(heightPropertyListener);
        this.getProps().getScrollX().addListener(scrollXPropertyListener);
        this.getProps().getScrollY().valueProperty().addListener(scrollYPositionListener);
        this.getProps().getLoadDataButton().setOnAction(loadDataActionListener);
        this.getProps().getLoadSequenceButton().setOnAction(loadSequenceActionListener);
    }

    private Range<Integer> getCurrentRange() {
        CanvasPane canvasPane = this.getProps().getCanvasPane();
        double hSlider = this.getState().gethSlider();
        double scrollX = this.getState().getScrollX();
        final double xFactor = this.getState().getxFactor();
        final double visibleVirtualCoordinatesX = Math.floor(canvasPane.getWidth() / xFactor);
        double xOffset = Math.round((scrollX / 100) * (canvasPane.getModelWidth() - visibleVirtualCoordinatesX));
        return Range.closedOpen((int) xOffset, (int) xOffset + (int) visibleVirtualCoordinatesX);
    }

    private void syncWidgetSlider() {
        double minScaleX = this.getProps().getCanvasPane().getModelWidth();
        double maxScaleX = MAX_ZOOM_MODEL_COORDINATES_X - 1;
        final double scaleRange = maxScaleX - minScaleX;
        final double xFactor = this.getState().getxFactor();
        final double current = Math.floor(this.getProps().getCanvasPane().getWidth() / xFactor);
        double scaledPercentage = (current - minScaleX) / scaleRange;

        Rectangle slider = this.getProps().getSlider();
        Pane xSliderPane = this.getProps().getxSliderPane();
        DoubleProperty scrollX = this.getProps().getScrollX();
        Rectangle leftSliderThumb = this.getProps().getLeftSliderThumb();
        Rectangle rightSliderThumb = this.getProps().getRightSliderThumb();
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
        AppStore.getStore().updateHSlider(
                invertExpScaleTransform(this.getProps().getCanvasPane(), xFactor),
                this.getState().getScrollX(),
                xFactor,
                1
        );
    }

    public DoubleProperty getHSliderValue() {
        return this.getProps().gethSlider().valueProperty();
    }

    public DoubleProperty getXScrollPosition() {
        return this.getProps().getScrollX();
    }

    private ChangeListener<Optional<Chromosome>> chromosomeSelectionListener = (observable, oldValue, newValue) -> {
        newValue.ifPresent(newChromosomeSelection -> {
            Chromosome selectedChromosome = this.getProps().getSelectedChromosome();
            if (selectedChromosome != newChromosomeSelection) {
                this.getProps().getSelectionInfoService().getSelectedGenomeVersion().get().ifPresent(gv -> {
                    Optional[] coordinateTrackRenderer = new Optional[]{Optional.empty()};
                    final Chromosome chromosome = newChromosomeSelection;
                    coordinateTrackRenderer[0] = Optional.of(new CoordinateTrackRenderer(this.getProps().getCanvasPane(), chromosome));
                    ((CoordinateTrackRenderer) coordinateTrackRenderer[0].get()).setWeight(getMinWeight());
                    loadDataSets(gv, chromosome);
                    //TODO: handle this comp
                    this.getProps().getLabelPane().getChildren().clear();
                    double xFactor = exponentialScaleTransform(
                            this.getProps().getCanvasPane(),
                            this.getState().gethSlider()
                    );

                    AppStore.getStore().update(
                            this.getState().getScrollX(),
                            0,
                            0,
                            0,
                            0,
                            true,
                            this.getState().getSelectedGenomeVersion(),
                            newChromosomeSelection,
                            coordinateTrackRenderer[0],
                            xFactor,
                            1
                    );
                });
            }
        });
    };

    private void initializeChromosomeSelectionListener() {
        this.getProps().getSelectionInfoService().getSelectedChromosome().addListener(chromosomeSelectionListener);
    }

    private void initializeGenomeVersionSelectionListener() {
        this.getProps().getSelectionInfoService().getSelectedGenomeVersion().addListener(genomeVersionSelectionListener);
    }
    private ChangeListener<Optional<GenomeVersion>> genomeVersionSelectionListener = (observable, oldValue, newValue) -> {
        LOG.info("initializeGenomeVersionSelectionListener event triggered");
        LOG.info(App.this.toString());
        newValue.ifPresent(genomeVersion -> {
            try {
                runAndWait(() -> {
                    this.getProps().getLabelPane().getChildren().clear();
                });
            } catch (InterruptedException | ExecutionException ex) {
                LOG.error(ex.getMessage(), ex);
            }

            double xFactor = exponentialScaleTransform(
                    this.getProps().getCanvasPane(),
                    this.getState().gethSlider()
            );
            AppStore.getStore().update(
                    this.getState().getScrollX(),
                    0,
                    0,
                    0,
                    0,
                    true,
                    genomeVersion,
                    this.getState().getSelectedChromosome(),
                    Optional.empty(),
                    xFactor,
                    1
            );
            initializeDataSetListener(genomeVersion);
        });
    };

    ;

    private boolean selectedChromosomeNotNull() {
        return this.getState().getSelectedChromosome() != null;
    }

    private boolean coordinateTrackRendererExists() {
        return this.getState().getTrackRenderers().stream()
                .anyMatch(renderer -> renderer instanceof CoordinateTrackRenderer);
    }

//    private void updateTrackRenderers(GenomeVersion gv) {
//        if (gv.getSelectedChromosomeProperty().get().isPresent()) {
//            if (!this.getState().getTrackRenderers().stream().anyMatch(renderer -> renderer instanceof CoordinateTrackRenderer)) {
//                final Chromosome chromosome = gv.getSelectedChromosomeProperty().get().get();
//                final CoordinateTrackRenderer coordinateTrackRenderer = new CoordinateTrackRenderer(this.getProps().getCanvasPane(), chromosome);
//                coordinateTrackRenderer.setWeight(getMinWeight());
//                AppStore.getStore().addTrackRenderer(coordinateTrackRenderer);
//                loadDataSets(gv, chromosome);
//            }
//        }
//    }
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
    private void loadDataSets(GenomeVersion gv, final Chromosome chromosome) {
        gv.getLoadedDataSets().forEach(dataSet -> {
            Track positiveStrandTrack = dataSet.getPositiveStrandTrack(chromosome.getName());
            Track negativeStrandTrack = dataSet.getNegativeStrandTrack(gv.getSelectedChromosomeProperty().get().get().getName());
            CanvasPane canvasPane = this.getProps().getCanvasPane();
            final ZoomableTrackRenderer positiveStrandTrackRenderer = new ZoomableTrackRenderer(canvasPane, positiveStrandTrack, chromosome);
            positiveStrandTrackRenderer.setWeight(getMinWeight());
            final ZoomableTrackRenderer negativeStrandTrackRenderer = new ZoomableTrackRenderer(canvasPane, negativeStrandTrack, chromosome);
            negativeStrandTrackRenderer.setWeight(getMaxWeight());
            Platform.runLater(() -> {
                AppStore.getStore().addTrackRenderer(positiveStrandTrackRenderer, negativeStrandTrackRenderer);
            });
        });

        if (gv.getLoadedDataSets().isEmpty()) {
            //updateCanvasContexts();
        }
    }

    private int getMinWeight() {
        int[] min = {0};
        this.getState().getTrackRenderers().stream().mapToInt(t -> t.getWeight()).min().ifPresent(currentMin -> {
            min[0] = currentMin - 1;
        });
        return min[0];
    }

    private int getMaxWeight() {
        int[] max = {0};
        this.getState().getTrackRenderers().stream().mapToInt(t -> t.getWeight()).max().ifPresent(currentMax -> {
            max[0] = currentMax + 1;
        });
        return max[0];
    }

    private void refreshSliderWidget() {
        Pane xSliderPane = this.getProps().getxSliderPane();
        Rectangle slider = this.getProps().getSlider();
        DoubleProperty scrollX = this.getProps().getScrollX();
        Rectangle rightSliderThumb = this.getProps().getRightSliderThumb();
        Rectangle leftSliderThumb = this.getProps().getLeftSliderThumb();
        double hSliderWidget = this.getState().gethSlider();
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
                double newSliderWidth = -maxPaneWidth * ((hSliderWidget / 100) - 1) + TOTAL_SLIDER_THUMB_WIDTH;
                LOG.info("sliderw: {}", hSliderWidget);
                double rightThumbX = rightSliderThumb.getX() + newXValue - current - slider.getWidth() + newSliderWidth;
                rightSliderThumb.setX(rightThumbX);
                slider.setWidth(newSliderWidth);

            }
        }
    }

    @Override
    public List<Component> render() {
        this.getProps().getLabelPane().getChildren().clear();
        List<Component> children = Lists.newArrayList();
        this.getState().getTrackRenderers().stream().map(trackRendererToTrackContainer).forEach(children::add);
        children.add(getSelectionRectangleComponent());
        children.add(getZoomStripeComponent());
        return children;
    }

    private ZoomStripe getZoomStripeComponent() {
        CanvasPane canvasPane = this.getProps().getCanvasPane();
        ZoomStripe zoomStripe = new ZoomStripe();
        zoomStripe.withAttributes(new ZoomStripeProps(
                canvasPane.getCanvas(),
                this.getState().getZoomStripeCoordinates(),
                this.getState().getxFactor(),
                canvasPane.getXOffset(),
                canvasPane.getWidth(),
                canvasPane.getModelWidth(),
                50,
                this.getState().getScrollX()
        ));
        return zoomStripe;
    }

    private SelectionRectangle getSelectionRectangleComponent() {
        CanvasPane canvasPane = this.getProps().getCanvasPane();
        SelectionRectangle selectionRectangle = new SelectionRectangle(
                new SelectionRectangleProps(
                        canvasPane,
                        this.getState().getMouseClickLocation(),
                        this.getState().getLocalPoint()
                ));
        return selectionRectangle;
    }

    private Function<TrackRenderer, TrackContainer> trackRendererToTrackContainer = tr -> {
        final TrackContainerProps trackContainerProps = new TrackContainerProps(
                tr,
                this.getState().getScrollX(),
                this.getState().getScrollY(),
                this.getState().gethSlider(),
                this.getState().getvSlider(),
                this.getProps().getCanvasPane(),
                this.getState().getLoadedDataSets(),
                this.getState().getSelectedChromosome(),
                this.getProps().getLabelPane(),
                this.getState().getZoomStripeCoordinates(),
                this.getState().getMouseClickLocation(),
                this.getState().getLocalPoint(),
                this.getState().getScreenPoint(),
                this.getState().isMouseDragging(),
                this.getState().getxFactor(),
                this.getState().getyFactor()
        );
        TrackContainer trackContainer = new TrackContainer(trackContainerProps);
        trackContainer.beforeComponentReady();
        return trackContainer;
    };

    private void initializeDataSetListener(GenomeVersion gv) {
        gv.getLoadedDataSets().removeListener(selectedGenomeVersionDataSetListener);
        gv.getLoadedDataSets().addListener(selectedGenomeVersionDataSetListener);
    }
    private SetChangeListener<DataSet> selectedGenomeVersionDataSetListener = (SetChangeListener.Change<? extends DataSet> change) -> {
        this.getProps().getSelectionInfoService().getSelectedGenomeVersion().get().ifPresent(gv -> {
            if (change.wasAdded()) {
                if (gv.getSelectedChromosomeProperty().get().isPresent()) {
                    Chromosome selectedChromosome = gv.getSelectedChromosomeProperty().get().get();
                    final DataSet loadedDataSet = change.getElementAdded();
                    if (!this.getState().getLoadedDataSets().contains(loadedDataSet)) {
                        CanvasPane canvasPane = this.getProps().getCanvasPane();
                        Track positiveStrandTrack = loadedDataSet.getPositiveStrandTrack(selectedChromosome.getName());
                        Track negativeStrandTrack = change.getElementAdded().getNegativeStrandTrack(gv.getSelectedChromosomeProperty().get().get().getName());
                        final ZoomableTrackRenderer positiveStrandTrackRenderer = new ZoomableTrackRenderer(canvasPane, positiveStrandTrack, selectedChromosome);
                        positiveStrandTrackRenderer.setWeight(getMinWeight());
                        final ZoomableTrackRenderer negativeStrandTrackRenderer = new ZoomableTrackRenderer(canvasPane, negativeStrandTrack, selectedChromosome);
                        negativeStrandTrackRenderer.setWeight(getMaxWeight());
                        try {
                            runAndWait(() -> {
                                AppStore.getStore().updateTrackRenderer(Arrays.asList(loadedDataSet),
                                        Arrays.asList(positiveStrandTrackRenderer, negativeStrandTrackRenderer));
                            });
                        } catch (InterruptedException | ExecutionException ex) {
                            LOG.error(ex.getMessage(), ex);
                        }
                    }
                }
            } else {
                //todo implement remove
            }
        });
    };

    @Override
    public void close() {
        LOG.info("closing app: " + genomeVersionSelectionListener.toString());
        this.getProps().getSelectionInfoService().getSelectedChromosome().removeListener(chromosomeSelectionListener);
        this.getProps().getSelectionInfoService().getSelectedGenomeVersion().removeListener(genomeVersionSelectionListener);
    }
}
