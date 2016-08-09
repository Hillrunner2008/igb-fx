package org.lorainelab.igb.visualization.widget;

import org.lorainelab.igb.visualization.widget.TrackRenderer;
import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.collect.TreeMultimap;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.SetChangeListener;
import javafx.geometry.Point2D;
import org.lorainelab.igb.data.model.GenomeVersion;
import org.lorainelab.igb.selections.SelectionInfoService;
import org.lorainelab.igb.visualization.ui.CanvasRegion;
import org.lorainelab.igb.visualization.model.CanvasPaneModel;
import org.lorainelab.igb.visualization.model.TracksModel;
import org.lorainelab.igb.visualization.model.ViewPortManager;
import org.lorainelab.igb.visualization.widget.Widget;
import org.reactfx.EventSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true, provide = WidgetManager.class)
public class WidgetManager {

    private static final Logger LOG = LoggerFactory.getLogger(WidgetManager.class);
    private EventSource<RenderAction> refreshViewStream;
    private List<Widget> widgets;
    private CanvasPaneModel canvasPaneModel;
    private TracksModel tracksModel;
    private ViewPortManager viewPortManager;
    private ChangeListener<Number> refreshViewListener;
    private CanvasRegion canvasRegionRegion;
    private SelectionInfoService selectionInfoService;

    public WidgetManager() {
        refreshViewStream = new EventSource<>();
        widgets = Lists.newArrayList();
        refreshViewListener = (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            refreshViewStream.emit(new RenderAction());
        };
    }

    @Activate
    public void activate() {
        refreshViewStream.successionEnds(Duration.ofMillis(5)).subscribe(e -> {
            renderWidgets();
        });
        canvasPaneModel.getxFactor().addListener(refreshViewListener);
        canvasPaneModel.getScrollX().addListener(refreshViewListener);
        canvasPaneModel.getModelWidth().addListener(refreshViewListener);
        canvasPaneModel.getZoomStripeCoordinate().addListener(refreshViewListener);
        canvasPaneModel.getyFactor().addListener(refreshViewListener);
        canvasPaneModel.getScrollY().addListener(refreshViewListener);
        canvasPaneModel.getScrollYVisibleAmount().addListener(refreshViewListener);
        canvasPaneModel.getVisibleVirtualCoordinatesX().addListener(refreshViewListener);
        canvasPaneModel.getvSlider().addListener(refreshViewListener);
        canvasPaneModel.getMouseClickLocation().addListener((ObservableValue<? extends Optional<Point2D>> observable, Optional<Point2D> oldValue, Optional<Point2D> newValue) -> {
            refreshViewStream.emit(new RenderAction());
        });
        canvasPaneModel.getLocalPoint().addListener((ObservableValue<? extends Optional<Point2D>> observable, Optional<Point2D> oldValue, Optional<Point2D> newValue) -> {
            refreshViewStream.emit(new RenderAction());
        });
        canvasPaneModel.getScreenPoint().addListener((ObservableValue<? extends Optional<Point2D>> observable, Optional<Point2D> oldValue, Optional<Point2D> newValue) -> {
            refreshViewStream.emit(new RenderAction());
        });
        tracksModel.getTrackRenderers().addListener((SetChangeListener.Change<? extends TrackRenderer> change) -> {
            refreshViewStream.emit(new RenderAction());
        });
        canvasRegionRegion.widthProperty().addListener(refreshViewListener);
        canvasRegionRegion.heightProperty().addListener(refreshViewListener);
        selectionInfoService.getSelectedGenomeVersion().addListener((ObservableValue<? extends Optional<GenomeVersion>> observable, Optional<GenomeVersion> oldValue, Optional<GenomeVersion> newValue) -> {
            refreshViewStream.emit(new RenderAction());
        });
    }

    @Reference(multiple = true, unbind = "removeWidget", dynamic = true, optional = true)
    public void addWidget(Widget widget) {
        widgets.add(widget);
        refreshViewStream.emit(new RenderAction());
    }

    public void removeWidget(Widget widget) {
        widgets.remove(widget);
        refreshViewStream.emit(new RenderAction());
    }

    public void renderWidgets() {
        TreeMultimap<Integer, Widget> sortedWidgets = TreeMultimap.create(Ordering.natural(), Ordering.arbitrary());
        widgets.stream().forEach(widget -> sortedWidgets.put(widget.getZindex(), widget));
        tracksModel.getTrackRenderers().stream().forEach(widget -> sortedWidgets.put(widget.getZindex(), widget));
        viewPortManager.refresh();
        sortedWidgets.entries().forEach(entry -> entry.getValue().render(canvasPaneModel));
    }

    @Reference
    public void setCanvasRegionRegion(CanvasRegion canvasRegionRegion) {
        this.canvasRegionRegion = canvasRegionRegion;
    }

    @Reference
    public void setCanvasPaneModel(CanvasPaneModel canvasPaneModel) {
        this.canvasPaneModel = canvasPaneModel;
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

    private class RenderAction {
    }

}
