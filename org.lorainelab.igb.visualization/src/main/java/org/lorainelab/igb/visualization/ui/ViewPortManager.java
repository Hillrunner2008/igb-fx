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
    static final double MAX_MODEL_COORDINATES_IN_VIEW = 50;//full zoom //TODO unit test depend on this not changing due to hard coded validation numbers... need to update them to be flexible
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
        if (canvasHeight < 1 || canvasWidth < 1) {
            return;
        }

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
        double yFactor = getYFactor();

        //pass through as if unscaled
        double remainingPixels = totalPixelsInScrollPane;
        double maxY = 0;
        for (TrackRenderer tr : sortedTrackRenderers) {
            final CanvasContext canvasContext = tr.getCanvasContext();
            double trackPixelHeight = getPixelHeight(tr, pixelToCoordRatio);
            double trackOffset = totalPixelsInScrollPane - remainingPixels;
            final Rectangle2D updatedBoundingRect = new Rectangle2D(0, trackOffset, canvasWidth, trackPixelHeight);
            canvasContext.update(updatedBoundingRect, trackPixelHeight, 0);
            remainingPixels -= trackPixelHeight;
            canvasContext.setIsVisible(false);//set all to false until next pass to determine intersections with viewports
            maxY = updatedBoundingRect.getMaxY();
        }

        double updatedMax = totalLockedPixelHeight + (stretchedToFitModelHeight * yFactor);
        double viewPortOffset = updatedMax * (verticalScrollBar.getValue() / 100);
        viewPortOffset = Math.min(updatedMax - canvasHeight, viewPortOffset);
        final double viewPortMax = viewPortOffset + canvasHeight;
        //now correct for scaling etc...
        Range<Double> viewPortRange = Range.closed(viewPortOffset, viewPortMax);
        double previousLockedTrackMaxY = -1;
        double previousIntersectionMax = 0;
        for (TrackRenderer tr : sortedTrackRenderers) {
            final CanvasContext canvasContext = tr.getCanvasContext();
            Rectangle2D canvasContextRect = canvasContext.getBoundingRect();
            double trackMinY = previousIntersectionMax > 0 ? previousIntersectionMax : canvasContextRect.getMinY() * yFactor;
            double trackHeight = tr.isHeightLocked() ? canvasContextRect.getHeight() : canvasContextRect.getHeight() * yFactor;
            Range<Double> trackRange = Range.closed(trackMinY, trackMinY + trackHeight);
            if (viewPortRange.isConnected(trackRange)) {
                Range<Double> intersection = viewPortRange.intersection(trackRange);
                if (intersection.upperEndpoint() - intersection.lowerEndpoint() > 0) {
                    double startPosition = trackMinY;
                    double y = intersection.lowerEndpoint() - startPosition;
                    final double y2 = intersection.upperEndpoint() - startPosition;
                    double height = y2 - y;
                    double trackOffset = intersection.lowerEndpoint() - viewPortRange.lowerEndpoint();
                    Rectangle2D boundingRectangle;
                    if (tr.isHeightLocked()) {
                        boundingRectangle = new Rectangle2D(0, trackOffset, canvasWidth, height);
                        previousLockedTrackMaxY = boundingRectangle.getMaxY();
                    } else {
                        final double minY = previousLockedTrackMaxY > -1 ? previousLockedTrackMaxY : trackOffset;
                        double diff = trackOffset - minY;
                        height += diff;
                        boundingRectangle = new Rectangle2D(0, minY, canvasWidth, height);
                        previousLockedTrackMaxY = -1;
                    }
                    double relativeTrackOffset = 0;
                    if (trackOffset == 0) {
                            relativeTrackOffset = intersection.lowerEndpoint() - startPosition;
                    }
                    double originalHeight = canvasContextRect.getHeight() * yFactor;
                    canvasContext.update(boundingRectangle, originalHeight, relativeTrackOffset);
                    canvasContext.setIsVisible(true);
                    previousIntersectionMax = intersection.upperEndpoint();
                }
            } else {
                //locked tracks height adjustment still needed if it is out of view above a track
                if (tr.isHeightLocked()) {
                    previousLockedTrackMaxY = 0;
                }
                previousIntersectionMax = trackRange.upperEndpoint();
            }
        }

    }

    public double getYFactor() {
        final double vSliderValue = canvasModel.getvSlider().get();
        final double vSliderPercentage = 1 - (vSliderValue / 100);
        double minScaleY = canvas.getHeight();
        double maxScaleY = MAX_MODEL_COORDINATES_IN_VIEW;
        double yFactor = canvas.getHeight() / (maxScaleY + (minScaleY - maxScaleY) * vSliderPercentage);
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
