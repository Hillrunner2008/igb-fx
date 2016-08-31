package org.lorainelab.igb.visualization.ui;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import java.util.Collections;
import java.util.List;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import org.lorainelab.igb.data.model.CanvasContext;
import org.lorainelab.igb.visualization.model.CanvasModel;
import org.lorainelab.igb.visualization.model.TracksModel;
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
    private static final double MAX_MODEL_COORDINATES_IN_VIEW = 75;//full zoom
    private Canvas canvas;
    private CanvasRegion canvasRegion;
    private TracksModel tracksModel;
    private VerticalScrollBar verticalScrollBar;
    private CanvasModel canvasModel;

    @Activate
    public void activate() {
        this.canvas = canvasRegion.getCanvas();
        refresh();
    }

    public final void refresh() {
        List<TrackRenderer> sortedTrackRenderers = Lists.newArrayList(tracksModel.getTrackRenderers());
        Collections.sort(sortedTrackRenderers, SORT_BY_WEIGHT);
        double canvasWidth = canvas.getWidth();
        double canvasHeight = canvas.getHeight();

        double totalLockedPixelHeight = sortedTrackRenderers.stream().filter(t -> t.isHeightLocked()).mapToDouble(t -> t.getModelHeight()).sum();
        double totalModelHeight = Math.max(1, sortedTrackRenderers.stream().filter(t -> !t.isHeightLocked()).mapToDouble(t -> t.getModelHeight()).sum());//min 1 to avoid division by zero
        double unlockedPixels = Math.max(1, canvas.getHeight() - totalLockedPixelHeight);//min 1 to avoid division by zero
        //fit into this space if there is enough room
        double pixelToCoordRatio = Math.max(unlockedPixels / totalModelHeight, 1);//adjust as needed to set min track height to smaller pixel value
        double totalPixelsInScrollPane = totalLockedPixelHeight + (totalModelHeight * pixelToCoordRatio);
        updateYFactor(totalPixelsInScrollPane);

        //pass through as if unscaled
        double remainingPixels = totalPixelsInScrollPane;
        for (TrackRenderer tr : sortedTrackRenderers) {
            final CanvasContext canvasContext = tr.getCanvasContext();
            double trackPixelHeight = getPixelHeight(tr, pixelToCoordRatio);
            double trackOffset = totalPixelsInScrollPane - remainingPixels;
            canvasContext.update(new Rectangle2D(0, trackOffset, canvasWidth, trackPixelHeight), 0);
            remainingPixels -= trackPixelHeight;
            canvasContext.setIsVisible(false);//set all to false until next pass to determine intersections with viewports
        }
        //now worry about scaling etc...
//        final double visiblePercentage = 1 - (canvasModel.getvSlider().get() / 100);
        double yFactor = Math.round(canvasModel.getyFactor().doubleValue() * 100) / 100.0;
        double viewPortOffset = (totalPixelsInScrollPane * (verticalScrollBar.getValue() / 100)) * yFactor;
        double scaledCanvasHeight = canvasHeight * yFactor;
        Range<Double> viewPortRange = Range.closed(viewPortOffset, viewPortOffset + scaledCanvasHeight);
        double unscaledPixelsAdjustment = 0;
        for (TrackRenderer tr : sortedTrackRenderers) {
            final CanvasContext canvasContext = tr.getCanvasContext();
            Rectangle2D canvasContextRect = canvasContext.getBoundingRect();
            final double trackMinY = canvasContextRect.getMinY() * yFactor;
            final double trackHeight = tr.isHeightLocked() ? canvasContextRect.getHeight() : canvasContextRect.getHeight() * yFactor;
            Range<Double> trackRange = Range.closed(trackMinY, trackMinY + trackHeight);
            if (viewPortRange.isConnected(trackRange)) {
                Range<Double> intersection = viewPortRange.intersection(trackRange);
                double startPosition = trackMinY;
                double y = intersection.lowerEndpoint() - startPosition;
                final double y2 = intersection.upperEndpoint() - startPosition;
                double height = y2 - y;
                double trackOffset = intersection.lowerEndpoint() - viewPortOffset;
                Rectangle2D boundingRectangle;
                if (tr.isHeightLocked()) {
                    unscaledPixelsAdjustment += (yFactor * height) - height;
                    boundingRectangle = new Rectangle2D(0, trackOffset, canvasWidth, height);
                } else {
                    double minY = trackOffset - unscaledPixelsAdjustment;
                    height += unscaledPixelsAdjustment;
                    boundingRectangle = new Rectangle2D(0, minY, canvasWidth, height);
                    unscaledPixelsAdjustment = 0;
                }
                double relativeTrackOffset = 0;
                if (trackOffset == 0) {
                    relativeTrackOffset = viewPortOffset - canvasContextRect.getMinY();
                }
                canvasContext.update(boundingRectangle, relativeTrackOffset);
                canvasContext.setIsVisible(true);

            }
        }

    }

    private void updateYFactor(double totalPixels) {
        final double vSliderValue = canvasModel.getvSlider().get();
        final double visiblePercentage = 1 - (vSliderValue / 100);
        double minScaleY = canvas.getHeight();
        double maxScaleY = MAX_MODEL_COORDINATES_IN_VIEW;
        double yFactor = totalPixels / (maxScaleY + (minScaleY - maxScaleY) * visiblePercentage);
        canvasModel.getyFactor().setValue(yFactor);
    }

    private double getPixelHeight(final TrackRenderer tr, double pixelToCoordRatio) {
        if (tr.isHeightLocked()) {
            return tr.getCanvasContext().getBoundingRect().getHeight();
        }
        return tr.getModelHeight() * pixelToCoordRatio;
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

}
