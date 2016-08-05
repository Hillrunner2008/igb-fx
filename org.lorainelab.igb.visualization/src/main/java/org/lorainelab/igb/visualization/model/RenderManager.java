package org.lorainelab.igb.visualization.model;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javafx.beans.value.ObservableValue;
import org.lorainelab.igb.visualization.component.Widget;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true, provide = RenderManager.class)
public class RenderManager {

    private List<Widget> widgets;
    private CanvasPaneModel canvasPaneModel;
    private TracksModel tracksModel;
    private ViewPortManager viewPortManager;

    public RenderManager() {
        widgets = Lists.newArrayList();
    }

    @Activate
    public void activate() {
        canvasPaneModel.getxFactor().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            renderWidgets();
        });
    }

    @Reference(multiple = true, unbind = "removeWidget", dynamic = true, optional = true)
    public void addWidget(Widget widget) {
        widgets.add(widget);
        renderWidgets();
    }

    public void removeWidget(Widget widget) {
        widgets.remove(widget);
        renderWidgets();
    }

    public void renderWidgets() {
        List<Widget> sorWidgets = Lists.newArrayList();
        sorWidgets.addAll(widgets);
        sorWidgets.addAll(tracksModel.getTrackRenderers());
        Collections.sort(sorWidgets, Comparator.<Widget>comparingInt(w -> w.getZindex()).reversed());
        viewPortManager.refresh();
        sorWidgets.stream().forEach(w -> w.render(canvasPaneModel));
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

}
