package org.lorainelab.igb.visualization.model;

import com.google.common.collect.Range;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.sun.javafx.tk.FontMetrics;
import com.sun.javafx.tk.Toolkit;
import java.text.DecimalFormat;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.lorainelab.igb.data.model.CanvasContext;
import org.lorainelab.igb.data.model.Chromosome;
import org.lorainelab.igb.data.model.View;
import static org.lorainelab.igb.data.model.sequence.BasePairColorReference.getBaseColor;
import org.lorainelab.igb.visualization.CanvasPane;
import org.lorainelab.igb.visualization.event.ClickDragCancelEvent;
import org.lorainelab.igb.visualization.event.ClickDragEndEvent;
import org.lorainelab.igb.visualization.event.ClickDragStartEvent;
import org.lorainelab.igb.visualization.event.ClickDragZoomEvent;
import org.lorainelab.igb.visualization.event.ClickDraggingEvent;
import org.lorainelab.igb.visualization.event.RefreshTrackEvent;
import org.lorainelab.igb.visualization.event.ZoomStripeEvent;
import static org.lorainelab.igb.visualization.util.BoundsUtil.enforceRangeBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
public class CoordinateTrackRenderer implements TrackRenderer {

    private static final Logger LOG = LoggerFactory.getLogger(CoordinateTrackRenderer.class);
    private static final String COORDINATES_TRACK_LABEL = "Coordinates";
    private static final int COORDINATE_CENTER_LINE = 20;

    final int modelWidth;
    final double modelHeight;
    private Rectangle2D viewBoundingRectangle;
    private Range<Double> viewYcoordinateRange;
    private double xfactor = 1;
    double zoomStripeCoordinate = -1;
    //protected EventBus eventBus;
    private final CanvasContext canvasContext;
    private final GraphicsContext gc;
    private int weight;
    private TrackLabel trackLabel;
    private final Chromosome chromosome;
    private final Range<Integer> validViewRange;

    public CoordinateTrackRenderer(CanvasPane canvasPane, Chromosome chromosome) {
        weight = 0;
//        this.eventBus = canvasPane.getEventBus();
        //eventBus.register(this);
        this.chromosome = chromosome;
        this.modelWidth = chromosome.getLength();
        this.modelHeight = 50;
        validViewRange = Range.closedOpen(0, modelWidth);
        viewBoundingRectangle = new Rectangle2D(0, 0, modelWidth, modelHeight);
        canvasContext = new CanvasContext(canvasPane.getCanvas(), Rectangle2D.EMPTY, 0, 0);
        trackLabel = new TrackLabel(this, COORDINATES_TRACK_LABEL);
        gc = canvasPane.getCanvas().getGraphicsContext2D();
    }

    void draw() {
        if (canvasContext.isVisible()) {
            gc.save();
            gc.scale(xfactor, 1);
            drawCoordinateBasePairs();
            drawCoordinateLine();
            drawClickDrag();
            gc.restore();
        }
    }

    private void drawClickDrag() {
        if (lastMouseClickX >= 0 && lastMouseDragX >= 0) {
            gc.save();
            gc.setFill(Color.rgb(33, 150, 243, .3));
            if (lastMouseClickX < lastMouseDragX) {
                gc.fillRect(lastMouseClickX, viewBoundingRectangle.getMinY(), lastMouseDragX - lastMouseClickX, COORDINATE_CENTER_LINE + 2);
            } else {
                gc.fillRect(lastMouseDragX, viewBoundingRectangle.getMinY(), lastMouseClickX - lastMouseDragX, COORDINATE_CENTER_LINE + 2);
            }
            gc.restore();
        }
    }

    double round(double num, int multipleOf) {
        return Math.floor((num + multipleOf / 2) / multipleOf) * multipleOf;
    }

    public double findLarget(double[] numbers) {

        double largest = Double.MIN_VALUE;

        for (int i = 0; i < numbers.length; i++) {
            if (numbers[i] > largest) {
                largest = numbers[i];
            }
        }

        return largest;
    }

    public double findSmallest(double[] numbers) {
        double smallest = Double.MAX_VALUE;

        for (int i = 0; i < numbers.length; i++) {
            if (smallest > numbers[i]) {
                smallest = numbers[i];
            }
        }
        return smallest;
    }

    private double getMajorTick(double value) {
        int approxIntervals = 10;
        int incr1 = 10;
        int incr2 = 20;
        int incr3 = 50;
        int lastSmallestIncr = 0;
        double lastSmallest = value;
        while (lastSmallest > approxIntervals) {
            double value1 = value / incr1;
            double value2 = value / incr2;
            double value3 = value / incr3;
            if (value1 > approxIntervals && value2 > approxIntervals && value3 > approxIntervals) {
                lastSmallest = findSmallest(new double[]{value1, value2, value3});
                if (lastSmallest == value1) {
                    lastSmallestIncr = incr1;
                } else if (lastSmallest == value2) {
                    lastSmallestIncr = incr2;
                } else {
                    lastSmallestIncr = incr3;
                }

                incr1 *= 10;
                incr2 *= 10;
                incr3 *= 10;
            } else {

                double value1Diff = Math.abs(approxIntervals - value1);
                double value2Diff = Math.abs(approxIntervals - value2);
                double value3Diff = Math.abs(approxIntervals - value3);
                double lastSmallestDiff = Math.abs(approxIntervals - lastSmallest);
                double smallestDiff = findSmallest(new double[]{value1Diff, value2Diff, value3Diff, lastSmallestDiff});
                if (smallestDiff == value1Diff) {
                    return incr1;
                } else if (smallestDiff == value2Diff) {
                    return incr2;
                } else if (smallestDiff == value3Diff) {
                    return incr3;
                } else {
                    return lastSmallestIncr;
                }
            }
        }
        return 10;
    }

    private void drawCoordinateLine() {
        gc.save();
        gc.scale(1 / xfactor, 1);
        gc.setFill(Color.BLACK);
        double majorTickInterval = getMajorTick(viewBoundingRectangle.getWidth());

        double minorTickInterval = majorTickInterval / 10;
        DecimalFormat formatter = new DecimalFormat("#,###");
        double textScale = .8;
        double yOffset = (modelHeight - viewBoundingRectangle.getHeight()) / textScale;
        double y = (14 / textScale) + viewBoundingRectangle.getMinY() / textScale;
        if (viewBoundingRectangle.getMinY() + modelHeight < gc.getCanvas().getHeight()) {
            y -= yOffset;
        }
        if (viewYcoordinateRange.contains(y * textScale)) {
            long startMajor;
            if ((viewBoundingRectangle.getMinX() % majorTickInterval) == 0) {
                startMajor = (long) viewBoundingRectangle.getMinX();
            } else {
                startMajor = (long) (viewBoundingRectangle.getMinX() + majorTickInterval - (viewBoundingRectangle.getMinX() % majorTickInterval));
            }
            for (long i = startMajor; i < (viewBoundingRectangle.getMaxX() + 1); i += majorTickInterval) {
                gc.scale(textScale, textScale);
                double x = (i - viewBoundingRectangle.getMinX()) * xfactor;
                gc.fillText(formatter.format(i), x / textScale, y);
                gc.scale(1 / textScale, 1 / textScale);
                double y1 = (COORDINATE_CENTER_LINE - 4) + viewBoundingRectangle.getMinY();
                double y2 = (COORDINATE_CENTER_LINE + 4) + viewBoundingRectangle.getMinY();
                if (viewBoundingRectangle.getMinY() + modelHeight < gc.getCanvas().getHeight()) {
                    y1 -= yOffset;
                    y2 -= yOffset;
                }
                gc.strokeLine(x, y1, x, y2);
            }

            long startMinor = (long) (viewBoundingRectangle.getMinX() + minorTickInterval - (viewBoundingRectangle.getMinX() % minorTickInterval));
            for (long i = startMinor; i < (viewBoundingRectangle.getMaxX() + 1); i += minorTickInterval) {
                double x = (i - viewBoundingRectangle.getMinX()) * xfactor;
                double y1 = (COORDINATE_CENTER_LINE - 2) + viewBoundingRectangle.getMinY();
                double y2 = (COORDINATE_CENTER_LINE + 2) + viewBoundingRectangle.getMinY();
                if (viewBoundingRectangle.getMinY() + modelHeight < gc.getCanvas().getHeight()) {
                    y1 -= yOffset;
                    y2 -= yOffset;
                }
                gc.strokeLine(x, y1, x, y2);
            }
            yOffset = modelHeight - viewBoundingRectangle.getHeight();
            y = COORDINATE_CENTER_LINE + canvasContext.getBoundingRect().getMinY();
            if (viewBoundingRectangle.getMinY() + modelHeight < gc.getCanvas().getHeight()) {
                y -= yOffset;
            }
            gc.restore();
            gc.strokeLine(0, y, viewBoundingRectangle.getWidth(), y);
            return;
        }
        gc.restore();
    }

    private void drawCoordinateBasePairs() {
        gc.save();
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 25));
        final int basePairPadding = 50;
        if (viewBoundingRectangle.getWidth() < 1500) {
            double textScale = .5;
            gc.scale(textScale, textScale);
            FontMetrics fm = Toolkit.getToolkit().getFontLoader().getFontMetrics(gc.getFont());
            double textHeight = fm.getLineHeight();
            gc.scale(1 / textScale, 1 / textScale);
            double yOffset = (modelHeight - viewBoundingRectangle.getHeight()) / textScale;
            double y = (viewBoundingRectangle.getMinY() / textScale) + textHeight + basePairPadding - 4;
            if (viewBoundingRectangle.getMinY() + modelHeight < gc.getCanvas().getHeight()) {
                y -= yOffset;
            }
            if (viewYcoordinateRange.contains(y * textScale)) {
                int startDna = (int) Math.ceil(viewBoundingRectangle.getMinX());
                int length = (int) Math.ceil(viewBoundingRectangle.getWidth());
                char[] dna = chromosome.getSequence(startDna, length);

                gc.scale(textScale, textScale);
                int start = (int) Math.ceil(viewBoundingRectangle.getMinX());
                for (int i = start; i < (dna.length + viewBoundingRectangle.getMinX()); i++) {
                    double index = i - viewBoundingRectangle.getMinX();
                    char base = dna[i - start];
                    gc.setFill(getBaseColor(base));
                    gc.scale(1 / textScale, 1 / textScale);
                    double y1 = 2 + viewBoundingRectangle.getMinY() + basePairPadding / 2;
                    if (viewBoundingRectangle.getMinY() + modelHeight < gc.getCanvas().getHeight()) {
                        y1 -= (modelHeight - viewBoundingRectangle.getHeight());
                    }
                    gc.fillRect(index, y1, 1, 12);
                    if (index < 1 && (i - 1) >= 0) {
                        char outOfviewChar = chromosome.getSequence(i - 1, 1)[0];
                        gc.setFill(getBaseColor(outOfviewChar));
                        gc.fillRect(0, y1, index, 12);
                    }
                    double y2 = (viewBoundingRectangle.getMinY() / textScale) + textHeight + basePairPadding - 4;
                    if (viewBoundingRectangle.getMinY() + modelHeight < gc.getCanvas().getHeight()) {
                        y2 -= yOffset;
                    }
                    gc.scale(textScale, textScale);
                    if (viewBoundingRectangle.getWidth() < 100) {
                        gc.setFill(Color.BLACK);
                        gc.fillText("" + base, index * 2 + .5, y2, 1);
                    }
                }
            }
        } else {
            gc.setFill(Color.GRAY);
            double yOffset = (modelHeight - viewBoundingRectangle.getHeight());
            double y = 2 + viewBoundingRectangle.getMinY() + basePairPadding / 2;
            if (viewBoundingRectangle.getMinY() + modelHeight < gc.getCanvas().getHeight()) {
                y -= yOffset;
            }
            final int height = 12;
            if (viewYcoordinateRange.contains(y)) {
                gc.fillRect(0, y, viewBoundingRectangle.getWidth(), height);
            }
        }
        gc.restore();
    }

    @Override
    public void updateView(double scrollX, double scrollY) {
        if (canvasContext.isVisible()) {
            final double visibleVirtualCoordinatesX = Math.floor(canvasContext.getBoundingRect().getWidth() / xfactor);
            double xOffset = Math.round((scrollX / 100) * (modelWidth - visibleVirtualCoordinatesX));
//            if (zoomStripeCoordinate != -1) {
//                double zoomStripePositionPercentage = (zoomStripeCoordinate - viewBoundingRectangle.getMinX()) / viewBoundingRectangle.getWidth();
//                xOffset = Math.max(zoomStripeCoordinate - (visibleVirtualCoordinatesX * zoomStripePositionPercentage), 0);
//                double maxXoffset = modelWidth - visibleVirtualCoordinatesX;
//                xOffset = Math.min(maxXoffset, xOffset);
//            }
            xOffset = enforceRangeBounds(xOffset, 0, modelWidth);
            viewBoundingRectangle = new Rectangle2D(xOffset, canvasContext.getBoundingRect().getMinY(), visibleVirtualCoordinatesX, canvasContext.getBoundingRect().getHeight());
            viewYcoordinateRange = Range.<Double>closed(viewBoundingRectangle.getMinY(), viewBoundingRectangle.getMaxY());
            render();
        }
    }

    @Override
    public void scaleCanvas(double xFactor, double scrollX, double scrollY) {
        if (canvasContext.isVisible()) {
            gc.save();
            gc.scale(xFactor, 1);
            xfactor = xFactor;
            gc.restore();
            updateView(scrollX, scrollY);
        }
    }

    @Override
    public void clearCanvas() {
        gc.save();
        double y = canvasContext.getBoundingRect().getMinY();
        final double height = canvasContext.getBoundingRect().getHeight();
        gc.clearRect(0, y, canvasContext.getBoundingRect().getWidth(), height);
        gc.setFill(Color.WHITE);
        gc.fillRect(0, y, canvasContext.getBoundingRect().getWidth(), height);
        gc.restore();
    }

    private double lastMouseClickX = -1;
    private double lastMouseDragX = -1;

    @Subscribe
    private void handleClickDragCancelEvent(ClickDragCancelEvent event) {
        lastMouseClickX = -1;
        lastMouseDragX = -1;
        render();
    }

    @Subscribe
    public void handleClickDragEndEvent(ClickDragEndEvent mouseEvent) {
        LOG.info("handleClickDragEndEvent method called");
        if (lastMouseClickX == -1
                || !canvasContext.getBoundingRect().contains(new Point2D(mouseEvent.getLocal().getX(), canvasContext.getBoundingRect().getMinY()))) {
            render();
            return;
        }
        final double visibleVirtualCoordinatesX = viewBoundingRectangle.getWidth();
        double xOffset = viewBoundingRectangle.getMinX();
        Range<Double> currentRange = Range.closedOpen(xOffset, xOffset + visibleVirtualCoordinatesX);

        lastMouseDragX = Math.floor(mouseEvent.getLocal().getX() / xfactor);
        ClickDragZoomEvent event;
        double x1 = viewBoundingRectangle.getMinX() + lastMouseClickX;
        double x2 = viewBoundingRectangle.getMinX() + lastMouseDragX;
        if (lastMouseDragX > lastMouseClickX) {
            event = new ClickDragZoomEvent(x1, x2);
        } else {
            event = new ClickDragZoomEvent(x2, x1);
        }
//        eventBus.post(event);
        lastMouseClickX = -1;
        lastMouseDragX = -1;
        render();
    }

//    @Subscribe
//    public void handleClickDraggingEvent(ClickDraggingEvent event) {
//        if (!canvasContext.getBoundingRect().contains(new Point2D(event.getLocal().getX(), event.getLocal().getY()))) {
//            return;
//        }
//        lastMouseDragX = Math.floor(event.getLocal().getX() / xfactor);
//        render();
//    }
    
    @Override
    public void setLastMouseClickedPoint(Point2D point) {
        if (!canvasContext.getBoundingRect().contains(point)) {
            return;
        }
        lastMouseClickX = Math.floor(point.getX() / xfactor);
    }

    @Override
    public void setLastMouseDragPoint(Point2D point) {
        if (!canvasContext.getBoundingRect().contains(point)) {
            return;
        }
        lastMouseDragX = Math.floor(point.getX() / xfactor);
    }

    @Override
    public void setMouseDragging(boolean isMouseDragging) {
        if (!isMouseDragging) {
            lastMouseClickX = -1;
            lastMouseDragX = -1;
        }
    }

//    @Subscribe
//    public void handleClickDragStartEvent(ClickDragStartEvent event) {
//        if (!canvasContext.getBoundingRect().contains(event.getLocal())) {
//            return;
//        }
//        lastMouseClickX = Math.floor(event.getLocal().getX() / xfactor);
//    }
    @Subscribe
    private void handleRefreshTrackEvent(RefreshTrackEvent event) {
        render();
    }

    @Override
    public void render() {
        if (canvasContext.isVisible()) {
            if (Platform.isFxApplicationThread()) {
                clearCanvas();
                draw();
            } else {
                Platform.runLater(() -> {
                    clearCanvas();
                    draw();
                });
            }
        }
    }

    public void setZoomStripeCoordinate(double zoomStripeCoordinate) {
        this.zoomStripeCoordinate = zoomStripeCoordinate;
    }

    @Override
    public CanvasContext getCanvasContext() {
        return canvasContext;
    }

    @Override
    public View getView() {
        View toReturn = new View(viewBoundingRectangle, chromosome);
        toReturn.setXfactor(xfactor);
        return toReturn;
    }

    @Override
    public String getTrackLabelText() {
        return COORDINATES_TRACK_LABEL;
    }

    public int getModelWidth() {
        return modelWidth;
    }

    public double getModelHeight() {
        return modelHeight;
    }

    @Override
    public int getWeight() {
        return weight;
    }

    @Override
    public void setWeight(int weight) {
        this.weight = weight;
    }

    public TrackLabel getTrackLabel() {
        return trackLabel;
    }

}
