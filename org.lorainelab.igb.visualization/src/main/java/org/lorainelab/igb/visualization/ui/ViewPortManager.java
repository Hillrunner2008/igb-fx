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

        double totalLockedPixelHeight = sortedTrackRenderers.stream().filter(t -> t.isHeightLocked()).mapToDouble(t -> t.getLockedHeight()).sum();
        double totalUnlockedPixels = Math.max(1, sortedTrackRenderers.stream().filter(t -> !t.isHeightLocked()).mapToDouble(t -> t.getModelHeight()).sum());//min 1 to avoid division by zero
        double totalPixels = totalLockedPixelHeight + totalUnlockedPixels;
        double pixelToCoordRatio = 1;
        if (totalPixels < canvasHeight) {
            //scaled the unlocked pixels to minimally fit the view
            double stretchPixelsNeeded = canvasHeight - totalPixels;
            pixelToCoordRatio = (stretchPixelsNeeded + totalUnlockedPixels) / totalUnlockedPixels;
        }
        //fit into this space if there is enough room

        final double stretchedToFitModelHeight = totalUnlockedPixels * pixelToCoordRatio;
        double totalPixelsInScrollPane = totalLockedPixelHeight + (stretchedToFitModelHeight);
        updateYFactor(totalPixelsInScrollPane);

        double yFactor = canvasModel.getyFactor().doubleValue();
        double value = verticalScrollBar.getValue() / verticalScrollBar.getMax();
        verticalScrollBar.setMax(totalLockedPixelHeight + (stretchedToFitModelHeight * yFactor));
        verticalScrollBar.setValue(Math.min(verticalScrollBar.getMax() * value, verticalScrollBar.getMax() - canvasHeight));
//        if (canvasHeight >= verticalScrollBar.getMax()) {
//            verticalScrollBar.setValue(0);///shouldn't be needed... and doesn't work well
//        }
        verticalScrollBar.setVisibleAmount(canvasHeight);

        //pass through as if unscaled
        double remainingPixels = totalPixelsInScrollPane;
        for (TrackRenderer tr : sortedTrackRenderers) {
            final CanvasContext canvasContext = tr.getCanvasContext();
            double trackPixelHeight = getPixelHeight(tr, pixelToCoordRatio);
            double trackOffset = totalPixelsInScrollPane - remainingPixels;
            canvasContext.update(new Rectangle2D(0, trackOffset, canvasWidth, trackPixelHeight), trackPixelHeight, 0);
            remainingPixels -= trackPixelHeight;
            canvasContext.setIsVisible(false);//set all to false until next pass to determine intersections with viewports
        }
        final double viewPortOffset = verticalScrollBar.getValue();
        final double viewPortMax = verticalScrollBar.getValue() + verticalScrollBar.getVisibleAmount();
        //now correct for scaling etc...
        Range<Double> viewPortRange = Range.closed(viewPortOffset, viewPortMax);
        double previousLockedTrackMaxY = -1;
        for (TrackRenderer tr : sortedTrackRenderers) {
            final CanvasContext canvasContext = tr.getCanvasContext();
            Rectangle2D canvasContextRect = canvasContext.getBoundingRect();
            final double trackMinY = tr.isHeightLocked() ? canvasContextRect.getMinY() * yFactor : canvasContextRect.getMinY() * yFactor;
            final double trackHeight = tr.isHeightLocked() ? canvasContextRect.getHeight() : canvasContextRect.getHeight() * yFactor;
            Range<Double> trackRange = Range.closed(trackMinY, trackMinY + trackHeight);
            if (viewPortRange.isConnected(trackRange)) {
                Range<Double> intersection = viewPortRange.intersection(trackRange);
                double startPosition = trackMinY;
                double y = intersection.lowerEndpoint() - startPosition;
                final double y2 = intersection.upperEndpoint() - startPosition;
                double height = y2 - y;
                double trackOffset = intersection.lowerEndpoint() - viewPortRange.lowerEndpoint();
                Rectangle2D boundingRectangle;
                if (tr.isHeightLocked()) {
                    boundingRectangle = new Rectangle2D(0, trackOffset, canvasWidth, height);
                    previousLockedTrackMaxY += boundingRectangle.getMaxY();
                } else {
                    final double minY = previousLockedTrackMaxY > -1 ? previousLockedTrackMaxY : trackOffset;
                    double diff = trackOffset - minY;
                    height += diff;
                    boundingRectangle = new Rectangle2D(0, minY, canvasWidth, height);
                    previousLockedTrackMaxY = -1;
                }
                double relativeTrackOffset = 0;
                if (trackOffset == 0) {
                    relativeTrackOffset = viewPortRange.lowerEndpoint() - canvasContextRect.getMinY();
                }
                double originalHeight = canvasContextRect.getHeight() * yFactor;
                canvasContext.update(boundingRectangle, originalHeight, relativeTrackOffset);
                canvasContext.setIsVisible(true);

            } else {
                //locked tracks height adjustment still needed if it is out of view above a track
                if (tr.isHeightLocked()) {
                    previousLockedTrackMaxY = 0;
                }
            }
        }

    }

    private void updateYFactor(double totalPixelsInScrollPane) {
        double yFactor = getYFactor(totalPixelsInScrollPane);
        canvasModel.getyFactor().setValue(yFactor);
    }

    private double getYFactor(double totalPixelsInScrollPane) {
        final double vSliderValue = canvasModel.getvSlider().get();
        final double vSliderPercentage = 1 - (vSliderValue / 100);
        double minScaleY = canvas.getHeight();
        double maxScaleY = MAX_MODEL_COORDINATES_IN_VIEW;
        double yFactor = totalPixelsInScrollPane / (maxScaleY + (minScaleY - maxScaleY) * vSliderPercentage);
        return yFactor;
    }

    private double getPixelHeight(final TrackRenderer tr, double pixelToCoordRatio) {
        if (tr.isHeightLocked()) {
            return tr.getLockedHeight();
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
