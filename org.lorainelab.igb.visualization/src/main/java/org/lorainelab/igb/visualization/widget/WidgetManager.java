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
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.SetChangeListener;
import javafx.geometry.Point2D;
import org.lorainelab.igb.data.model.GenomeVersion;
import org.lorainelab.igb.selections.SelectionInfoService;
import org.lorainelab.igb.visualization.model.CanvasModel;
import org.lorainelab.igb.visualization.model.TracksModel;
import org.lorainelab.igb.visualization.ui.CanvasRegion;
import org.lorainelab.igb.visualization.ui.ViewPortManager;
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
    private CanvasModel canvasModel;
    private TracksModel tracksModel;
    private ViewPortManager viewPortManager;
    private ChangeListener<Number> refreshViewListener;
    private CanvasRegion canvasRegion;
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
        refreshViewStream.successionEnds(Duration.ofMillis(2)).subscribe(e -> {
            renderWidgets();
        });
        canvasModel.getxFactor().addListener(refreshViewListener);
        canvasModel.getScrollX().addListener(refreshViewListener);
        canvasModel.getModelWidth().addListener(refreshViewListener);
        canvasModel.getZoomStripeCoordinate().addListener(refreshViewListener);
        canvasModel.getyFactor().addListener(refreshViewListener);
        canvasModel.getScrollY().addListener(refreshViewListener);
        canvasModel.getScrollYVisibleAmount().addListener(refreshViewListener);
        canvasModel.getVisibleVirtualCoordinatesX().addListener(refreshViewListener);
        canvasModel.getvSlider().addListener(refreshViewListener);
        canvasModel.getMouseClickLocation().addListener((ObservableValue<? extends Optional<Point2D>> observable, Optional<Point2D> oldValue, Optional<Point2D> newValue) -> {
            refreshViewStream.emit(new RenderAction());
        });
        canvasModel.getClickDragStartPosition().addListener((ObservableValue<? extends Optional<Point2D>> observable, Optional<Point2D> oldValue, Optional<Point2D> newValue) -> {
            refreshViewStream.emit(new RenderAction());
        });
        canvasModel.getScreenPoint().addListener((ObservableValue<? extends Optional<Point2D>> observable, Optional<Point2D> oldValue, Optional<Point2D> newValue) -> {
            refreshViewStream.emit(new RenderAction());
        });
        tracksModel.getTrackRenderers().addListener((SetChangeListener.Change<? extends TrackRenderer> change) -> {
            refreshViewStream.emit(new RenderAction());
        });
        canvasRegion.widthProperty().addListener(refreshViewListener);
        canvasRegion.heightProperty().addListener(refreshViewListener);
        selectionInfoService.getSelectedGenomeVersion().addListener((ObservableValue<? extends Optional<GenomeVersion>> observable, Optional<GenomeVersion> oldValue, Optional<GenomeVersion> newValue) -> {
            refreshViewStream.emit(new RenderAction());
        });
        canvasModel.isforceRefresh().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
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
        sortedWidgets.entries().forEach(entry -> entry.getValue().render(canvasModel));
        canvasModel.setMouseClickLocation(null);
    }

    @Reference
    public void setCanvasRegion(CanvasRegion canvasRegion) {
        this.canvasRegion = canvasRegion;
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

    private class RenderAction {
    }

}
