package org.lorainelab.igb.visualization.model;

import com.google.common.collect.Range;
import java.util.Optional;
import java.util.Set;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import static org.lorainelab.igb.visualization.model.TrackRenderer.SORT_BY_WEIGHT;

/**
 *
 * @author dcnorris
 */
public class ViewPortManager {

    private static double MIN_TRACK_HEIGHT = 100;
    private static final int MAX_TRACK_HEIGHT = 1000;
    private static final int COORDINATE_TRACK_HEIGHT = 50;
    private final Set<TrackRenderer> trackRenderers;
    private double canvasWidth;
    private double canvasHeight;
    private double viewPortOffset;
    private double totalTrackSize;
    private double trackSize;
    private final Canvas canvas;

    public ViewPortManager(Canvas canvas, Set<TrackRenderer> trackRenderers, double vSliderValue, double yScrollerPosition) {
        this.canvas = canvas;
        this.trackRenderers = trackRenderers;
        refresh(vSliderValue, yScrollerPosition);
    }

    public final void refresh(double vSliderValue, double yScrollerPosition) {
        canvasWidth = canvas.getWidth();
        canvasHeight = canvas.getHeight();
        int trackCountExcludingCoordinates = (int) trackRenderers.stream().filter(track -> !(track instanceof CoordinateTrackRenderer)).count();
        trackSize = MIN_TRACK_HEIGHT + (MAX_TRACK_HEIGHT - MIN_TRACK_HEIGHT) * vSliderValue / 100;
        totalTrackSize = trackSize * trackCountExcludingCoordinates + COORDINATE_TRACK_HEIGHT;
        if (totalTrackSize < canvasHeight) {
            double availableTrackSpace = canvas.getHeight() - COORDINATE_TRACK_HEIGHT;
            MIN_TRACK_HEIGHT = availableTrackSpace / trackCountExcludingCoordinates;
            if (!Range.closedOpen((double) 0, canvasHeight).contains(MIN_TRACK_HEIGHT)) {
                MIN_TRACK_HEIGHT = 100;
            }
            trackSize = MIN_TRACK_HEIGHT + (MAX_TRACK_HEIGHT - MIN_TRACK_HEIGHT) * vSliderValue / 100;
            totalTrackSize = trackSize * trackCountExcludingCoordinates + COORDINATE_TRACK_HEIGHT;
        }
        viewPortOffset = (totalTrackSize - canvasHeight) * (yScrollerPosition / 100);
        updateCanvasContexts();
    }

    private double calculateTrackStartPosition(int trackRendererIndex) {
        double startPosition = -1;
        final Optional<TrackRenderer> coordinateTrackRenderer = trackRenderers.stream().filter(track -> track instanceof CoordinateTrackRenderer).findFirst();
        if (coordinateTrackRenderer.isPresent()) {
            int indexOfCoordinateTrack = coordinateTrackRenderer.get().getWeight();
            if (trackRendererIndex > indexOfCoordinateTrack) {
                startPosition = (trackRendererIndex - 1) * trackSize;
                startPosition += COORDINATE_TRACK_HEIGHT;
                return startPosition;
            }
        }
        startPosition = trackRendererIndex * trackSize;
        return startPosition;
    }

    private double calculateEndPosition(int trackRendererIndex, double startPosition) {
        double endPosition = -1;
        final Optional<TrackRenderer> coordinateTrackRenderer = trackRenderers.stream().filter(track -> track instanceof CoordinateTrackRenderer).findFirst();
        if (coordinateTrackRenderer.isPresent()) {
            int indexOfCoordinateTrack = coordinateTrackRenderer.get().getWeight();
            if (trackRendererIndex == indexOfCoordinateTrack) {
                endPosition = startPosition + COORDINATE_TRACK_HEIGHT;
                return endPosition;
            }
        }
        endPosition = startPosition + trackSize;
        return endPosition;
    }

    public static boolean isNegative(double d) {
        return Double.doubleToRawLongBits(d) < 0;
    }

    public double getTotalTrackSize() {
        return totalTrackSize;
    }

    private void updateCanvasContexts() {
        int[] i = {0};
        trackRenderers.stream().sorted(SORT_BY_WEIGHT.reversed()).forEach(trackRenderer -> {
            double startPosition = calculateTrackStartPosition(i[0]);
            double endPosition = calculateEndPosition(i[0], startPosition);
            if (startPosition >= 0 && endPosition > 0 && startPosition < endPosition) {
                Range<Double> trackRange = Range.closed(startPosition, endPosition);
                Range<Double> viewPortRange = Range.closed(viewPortOffset, viewPortOffset + canvasHeight);
                if (viewPortRange.isConnected(trackRange)) {
                    Range<Double> intersection = viewPortRange.intersection(trackRange);
                    double trackOffset = intersection.lowerEndpoint() - viewPortOffset;
                    double y = intersection.lowerEndpoint() - startPosition;
                    final double y2 = intersection.upperEndpoint() - startPosition;
                    final double height = y2 - y;
                    Rectangle2D boundingRectangle = new Rectangle2D(0, trackOffset, canvasWidth, height);
                    double relativeTrackOffset = 0;
                    if (trackOffset == 0) {
                        relativeTrackOffset = viewPortOffset - startPosition;
                    }

                    if (trackRenderer instanceof CoordinateTrackRenderer) {
                        trackRenderer.getCanvasContext().update(boundingRectangle, COORDINATE_TRACK_HEIGHT, relativeTrackOffset);
                    } else {
                        trackRenderer.getCanvasContext().update(boundingRectangle, trackSize, relativeTrackOffset);
                    }
                    trackRenderer.getCanvasContext().setIsVisible(true);
                } else {
                    trackRenderer.getCanvasContext().setIsVisible(false);
                }
            }
            i[0]++;
        });

    }

}
