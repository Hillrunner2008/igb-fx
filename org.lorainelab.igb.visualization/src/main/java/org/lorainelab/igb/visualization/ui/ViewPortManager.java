package org.lorainelab.igb.visualization.ui;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import java.util.Collections;
import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.StackPane;
import org.lorainelab.igb.data.model.CanvasContext;
import org.lorainelab.igb.visualization.model.CanvasModel;
import org.lorainelab.igb.visualization.model.TracksModel;
import org.lorainelab.igb.visualization.widget.LabelPane;
import org.lorainelab.igb.visualization.widget.TrackRenderer;
import static org.lorainelab.igb.visualization.widget.TrackRenderer.SORT_BY_WEIGHT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true, provide = ViewPortManager.class)
public class ViewPortManager {

    private static final Logger LOG = LoggerFactory.getLogger(ViewPortManager.class);
    private Canvas canvas;
    private CanvasRegion canvasRegion;
    private TracksModel tracksModel;
    private VerticalScrollBar verticalScrollBar;
    private CanvasModel canvasModel;
    private LabelPane labelPane;
    private HorizontalPlusMinusSlider horizontalPlusMinusSlider;

    @Activate
    public void activate() {
        this.canvas = canvasRegion.getCanvas();
        refresh();
        canvasModel.getyFactor().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            tracksModel.getTrackRenderers().forEach(tr -> {
                tr.stretchDelta().setValue(tr.stretchDelta().doubleValue() + (tr.activeStretchDelta().get() / canvasModel.getyFactor().doubleValue()));
                tr.activeStretchDelta().setValue(0);
            });
        });
    }

    public final void refresh() {
        labelPane.updatedLabelBounds(canvasModel);
        double canvasWidth = canvas.getWidth();
        double canvasHeight = canvas.getHeight();
        if (canvasHeight < 1 || canvasWidth < 1) {
            return;
        }
        List<TrackRenderer> sortedTrackRenderers = Lists.newArrayList(tracksModel.getTrackRenderers());
        Collections.sort(sortedTrackRenderers, SORT_BY_WEIGHT);

        Bounds bounds = labelPane.getViewportBounds();
//        double plusMinuxSliderOffset = bounds.getHeight() - canvasHeight;
        double labelPaneHeight = labelPane.getContent().getBoundsInLocal().getHeight();
        double viewPortOffset = (labelPaneHeight - bounds.getHeight()) * labelPane.getVvalue();
        double viewPortMax = viewPortOffset + bounds.getHeight();
        Range<Double> viewPortRange = Range.closed(viewPortOffset, viewPortMax);
        for (int i = 0; i < sortedTrackRenderers.size(); i++) {
            TrackRenderer tr = sortedTrackRenderers.get(i);
            final CanvasContext canvasContext = tr.getCanvasContext();
            final StackPane labelNode = tr.getTrackLabel().getContent();
            double labelMinY = labelNode.getBoundsInParent().getMinY();
            double labelMaxY = labelNode.getBoundsInParent().getMaxY();
            if (labelMinY < 0 || labelMaxY < 0) {
                return;
            }

            Range<Double> labelRange = Range.closed(labelMinY, labelMaxY);
            if (viewPortRange.isConnected(labelRange)) {
                Range<Double> intersection = viewPortRange.intersection(labelRange);
                if (intersection.upperEndpoint() - intersection.lowerEndpoint() > 0) {
                    canvasContext.setIsVisible(true);
                    double trackOffset = intersection.lowerEndpoint() - viewPortRange.lowerEndpoint();
                    double relativeTrackOffset = 0;
                    if (trackOffset == 0) {
                        relativeTrackOffset = intersection.lowerEndpoint() - labelMinY;
                    }
                    double y = intersection.lowerEndpoint() - labelMinY;
                    final double y2 = intersection.upperEndpoint() - labelMinY;
                    double height = y2 - y;
                    canvasContext.update(new Rectangle2D(0, trackOffset, canvasWidth, height), labelNode.getBoundsInParent().getHeight(), relativeTrackOffset);
                    canvasContext.setIsVisible(true);
                }
            } else {
                canvasContext.setIsVisible(false);
            }
        }
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
    public void setTracksModel(TracksModel tracksModel) {
        this.tracksModel = tracksModel;
    }

    @Reference
    public void setVerticalScrollBar(VerticalScrollBar verticalScrollBar) {
        this.verticalScrollBar = verticalScrollBar;
    }

    @Reference
    public void setLabelPane(LabelPane labelPane) {
        this.labelPane = labelPane;
    }

    @Reference
    public void setHorizontalPlusMinusSlider(HorizontalPlusMinusSlider horizontalPlusMinusSlider) {
        this.horizontalPlusMinusSlider = horizontalPlusMinusSlider;
    }
}
