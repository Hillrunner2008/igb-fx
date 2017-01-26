package org.lorainelab.igb.visualization.widget;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.collect.TreeMultimap;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.SetChangeListener;
import javafx.collections.WeakSetChangeListener;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import org.lorainelab.igb.data.model.GenomeVersion;
import org.lorainelab.igb.selections.SelectionInfoService;
import org.lorainelab.igb.visualization.model.CanvasModel;
import org.lorainelab.igb.visualization.model.TracksModel;
import org.lorainelab.igb.visualization.ui.CanvasRegion;
import org.lorainelab.igb.visualization.ui.OverlayRegion;
import org.lorainelab.igb.visualization.ui.ViewPortManager;
import org.reactfx.EventSource;
import org.reactfx.EventStream;
import org.reactfx.util.AccumulatorSize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true, provide = WidgetManager.class)
public class WidgetManager {

    private static final Logger LOG = LoggerFactory.getLogger(WidgetManager.class);
    private EventSource<String> refreshViewStream;
    private EventSource<Void> overlayRefreshStream;
    private List<Widget> widgets;
    private CanvasModel canvasModel;
    private TracksModel tracksModel;
    private ViewPortManager viewPortManager;
    private CanvasRegion canvasRegion;
    private SelectionInfoService selectionInfoService;
    private BooleanProperty isRefreshing;
    private OverlayRegion overlayRegion;

    public WidgetManager() {
        isRefreshing = new SimpleBooleanProperty(false);
        refreshViewStream = new EventSource<>();
        overlayRefreshStream = new EventSource<>();
        widgets = Lists.newArrayList();
    }

    @Activate
    public void activate() {
        overlayRefreshStream.accumulateWhen(isRefreshing, Function.identity(), (a, b) -> b, a -> AccumulatorSize.ONE, Function.identity(), Function.identity()).subscribe(renderEvent -> {
            renderOverayWidgets();
        });

        EventStream<String> debouncedRefreshAwareStream = refreshViewStream.successionEnds(Duration.ofMillis(4))
                .accumulateWhen(isRefreshing, Function.identity(), (a, b) -> b, a -> AccumulatorSize.ONE, Function.identity(), Function.identity());

        debouncedRefreshAwareStream.subscribe(renderEvent -> {
            isRefreshing.setValue(Boolean.TRUE);
            LOG.debug(renderEvent);
            renderWidgets();
            isRefreshing.setValue(Boolean.FALSE);
        });
        xFactorListener = (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            refreshViewStream.emit("xFactor");
        };
        canvasModel.getxFactor().addListener(new WeakChangeListener<>(xFactorListener));
        scrollXListener = (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            refreshViewStream.emit("getScrollX");
        };
        canvasModel.getScrollX().addListener(new WeakChangeListener<>(scrollXListener));
        modelWidthListener = (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            refreshViewStream.emit("getModelWidth");
        };
        canvasModel.getModelWidth().addListener(new WeakChangeListener<>(modelWidthListener));
        zoomStripCoordinateListener = (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            if (newValue.intValue() != -1) {
                overlayRefreshStream.emit(null);
            }
        };
        canvasModel.getZoomStripeCoordinate().addListener(new WeakChangeListener<>(zoomStripCoordinateListener));
        yFactorListener = (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            refreshViewStream.emit("getyFactor");
        };
        canvasModel.getyFactor().addListener(new WeakChangeListener<>(yFactorListener));
        scrollYListener = (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            refreshViewStream.emit("getScrollY");
        };
        canvasModel.getScrollY().addListener(new WeakChangeListener<>(scrollYListener));
        visibleVirtualCoordinatesXListener = (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            refreshViewStream.emit("getVisibleVirtualCoordinatesX");
        };
        canvasModel.getVisibleVirtualCoordinatesX().addListener(new WeakChangeListener<>(visibleVirtualCoordinatesXListener));
        vSliderChangeListener = (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            refreshViewStream.emit("canvasModel.getvSlider");
        };
        canvasModel.getvSlider().addListener(new WeakChangeListener<>(vSliderChangeListener));
        clickDragStartPositionListener = (ObservableValue<? extends Optional<Point2D>> observable, Optional<Point2D> oldValue, Optional<Point2D> newValue) -> {
            overlayRefreshStream.emit(null);
        };
        canvasModel.getClickDragStartPosition().addListener(new WeakChangeListener<>(clickDragStartPositionListener));
        lastDragPositionListener = (ObservableValue<? extends Optional<Point2D>> observable, Optional<Point2D> oldValue, Optional<Point2D> newValue) -> {
            overlayRefreshStream.emit(null);
        };
        canvasModel.getLastDragPosition().addListener(new WeakChangeListener<>(lastDragPositionListener));
        trackRendererSetChangeListener = (SetChangeListener.Change<? extends TrackRenderer> change) -> {
            refreshViewStream.emit("trackRenderers change");
        };
        tracksModel.getTrackRenderers().addListener(new WeakSetChangeListener<>(trackRendererSetChangeListener));
        canvasRegionWidthListener = (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            refreshViewStream.emit("canvasRegion.widthProperty");
        };
        canvasRegion.widthProperty().addListener(new WeakChangeListener<>(canvasRegionWidthListener));
        canvasRegionHeightListener = (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            if (!canvasModel.getLabelResizingActive().get()) {
                refreshViewStream.emit("canvasRegionHeightListener");
            }
        };
        canvasRegion.heightProperty().addListener(new WeakChangeListener<>(canvasRegionHeightListener));
        selectedGenomeVersionListener = (ObservableValue<? extends Optional<GenomeVersion>> observable, Optional<GenomeVersion> oldValue, Optional<GenomeVersion> newValue) -> {
            refreshViewStream.emit("selectedGenomeVersion change");
        };
        selectionInfoService.getSelectedGenomeVersion().addListener(new WeakChangeListener<>(selectedGenomeVersionListener));
        forceRefreshListener = (ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            refreshViewStream.emit("forceRefresh");
        };
        selectionRectangleChangeListener = (ObservableValue<? extends Optional<Rectangle2D>> observable, Optional<Rectangle2D> oldValue, Optional<Rectangle2D> newValue) -> {
           refreshViewStream.emit("selectionRectangleChangeListener");
        };
        canvasModel.getSelectionRectangle().addListener(new WeakChangeListener<>(selectionRectangleChangeListener));

        canvasModel.isforceRefresh().addListener(new WeakChangeListener<>(forceRefreshListener));

    }
    private ChangeListener<Optional<Rectangle2D>> selectionRectangleChangeListener;
    private ChangeListener<Number> xFactorListener;
    private ChangeListener<Number> scrollXListener;
    private ChangeListener<Number> modelWidthListener;
    private ChangeListener<Number> zoomStripCoordinateListener;
    private ChangeListener<Number> yFactorListener;
    private ChangeListener<Number> scrollYListener;
    private ChangeListener<Number> visibleVirtualCoordinatesXListener;
    private ChangeListener<Number> vSliderChangeListener;
    private ChangeListener<Optional<Point2D>> clickDragStartPositionListener;
    private ChangeListener<Optional<Point2D>> lastDragPositionListener;
    private SetChangeListener<TrackRenderer> trackRendererSetChangeListener;
    private ChangeListener<Number> canvasRegionHeightListener;
    private ChangeListener<Number> canvasRegionWidthListener;
    private ChangeListener<Optional<GenomeVersion>> selectedGenomeVersionListener;
    private ChangeListener<Boolean> forceRefreshListener;

    @Reference(multiple = true, unbind = "removeWidget", dynamic = true, optional = true)
    public void addWidget(Widget widget) {
        widgets.add(widget);
        refreshViewStream.emit(null);
    }

    public void removeWidget(Widget widget) {
        widgets.remove(widget);
        refreshViewStream.emit(null);
    }

    public void renderWidgets() {
        TreeMultimap<Integer, Widget> sortedWidgets = getSortedWidgets();
        viewPortManager.refresh();
        canvasRegion.clear();
        overlayRegion.clear();
        sortedWidgets.entries().forEach(entry -> entry.getValue().render(canvasModel));
        //TODO consider other ways to handle the need to rest this value
        canvasModel.setMouseClickLocation(null);
    }

    private void renderOverayWidgets() {
        overlayRegion.clear();
        TreeMultimap<Integer, Widget> sortedWidgets = getSortedWidgets();
        sortedWidgets.entries().stream().filter(entry -> entry.getValue().isOverlayWidget()).forEach(entry -> entry.getValue().render(canvasModel));
    }

    private TreeMultimap<Integer, Widget> getSortedWidgets() {
        TreeMultimap<Integer, Widget> sortedWidgets = TreeMultimap.create(Ordering.natural(), Ordering.arbitrary());
        widgets.stream().forEach(widget -> sortedWidgets.put(widget.getZindex(), widget));
        tracksModel.getTrackRenderers().stream().forEach(widget -> sortedWidgets.put(widget.getZindex(), widget));
        return sortedWidgets;
    }

    @Reference
    public void setCanvasRegion(CanvasRegion canvasRegion) {
        this.canvasRegion = canvasRegion;
    }

    @Reference
    public void setOverlayRegion(OverlayRegion overlayRegion) {
        this.overlayRegion = overlayRegion;
    }

    @Reference
    public void setCanvasModel(CanvasModel canvasModel) {
        this.canvasModel = canvasModel;
    }

    @Reference
    public void setTracksModel(TracksModel tracksModel) {
        this.tracksModel = tracksModel;
    }

    @Reference
    public void setViewPortManager(ViewPortManager viewPortManager) {
        this.viewPortManager = viewPortManager;
    }

    @Reference
    public void setSelectionInfoService(SelectionInfoService selectionInfoService) {
        this.selectionInfoService = selectionInfoService;
    }

}
