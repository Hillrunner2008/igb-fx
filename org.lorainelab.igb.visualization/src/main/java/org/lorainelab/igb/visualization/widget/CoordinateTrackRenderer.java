package org.lorainelab.igb.visualization.widget;

import com.google.common.collect.Range;
import java.text.DecimalFormat;
import java.util.Optional;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.lorainelab.igb.data.model.CanvasContext;
import org.lorainelab.igb.data.model.Chromosome;
import org.lorainelab.igb.data.model.Track;
import org.lorainelab.igb.data.model.View;
import static org.lorainelab.igb.data.model.util.FontUtils.BASE_PAIR_FONT;
import org.lorainelab.igb.data.model.util.Palette;
import static org.lorainelab.igb.data.model.util.Palette.CLICK_DRAG_HIGHLIGHT;
import static org.lorainelab.igb.data.model.util.Palette.getBaseColor;
import org.lorainelab.igb.visualization.model.CanvasModel;
import org.lorainelab.igb.visualization.model.TrackLabel;
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
    private final CanvasContext canvasContext;
    private final GraphicsContext gc;
    private int weight;
    private TrackLabel trackLabel;
    private final Chromosome chromosome;
    private DoubleProperty trackHeight;

    public CoordinateTrackRenderer(Canvas canvas, Chromosome chromosome) {
        this.chromosome = chromosome;
        this.modelWidth = chromosome.getLength();
        this.modelHeight = MIN_HEIGHT;
        trackHeight = new SimpleDoubleProperty(MIN_HEIGHT);
        weight = 0;
        viewBoundingRectangle = new Rectangle2D(0, 0, modelWidth, modelHeight);
        canvasContext = new CanvasContext(canvas, modelHeight, 0);
        trackLabel = new TrackLabel(this, COORDINATES_TRACK_LABEL, new SimpleBooleanProperty(true), false);
        gc = canvas.getGraphicsContext2D();
    }

    void draw(CanvasModel canvasModel) {
        if (canvasContext.isVisible()) {
            gc.save();
            gc.scale(xfactor, 1);
            drawCoordinateBasePairs(canvasModel);
            drawCoordinateLine();
            drawClickDrag(canvasModel);
            gc.restore();
        }
    }

    private void drawClickDrag(CanvasModel canvasModel) {
        canvasModel.getClickDragStartPosition().get().ifPresent(clickDragStartPosition -> {
            canvasModel.getLastDragPosition().get().ifPresent(lastMouseDrag -> {
                if (canvasContext.getBoundingRect().contains(clickDragStartPosition)) {
                    final double lastMouseClickX = Math.floor(clickDragStartPosition.getX() / xfactor);
                    final double lastMouseDragX = Math.floor(lastMouseDrag.getX() / xfactor);
                    if (lastMouseClickX >= 0 && lastMouseDragX >= 0) {
                        gc.save();
                        gc.setFill(CLICK_DRAG_HIGHLIGHT.get());
                        if (lastMouseClickX < lastMouseDragX) {
                            gc.fillRect(lastMouseClickX, viewBoundingRectangle.getMinY(), lastMouseDragX - lastMouseClickX, COORDINATE_CENTER_LINE + 2);
                        } else {
                            gc.fillRect(lastMouseDragX, viewBoundingRectangle.getMinY(), lastMouseClickX - lastMouseDragX, COORDINATE_CENTER_LINE + 2);
                        }
                        gc.restore();
                    }
                }
            });
        });

    }

    double round(double num, int multipleOf) {
        return Math.floor((num + multipleOf / 2) / multipleOf) * multipleOf;
    }

    public double findLargest(double[] numbers) {

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
            gc.save();
            gc.scale(1 / xfactor, 1);
            gc.setFill(Palette.DEFAULT_LABEL_COLOR.get());
            gc.setStroke(Palette.DEFAULT_LABEL_COLOR.get());
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
            gc.restore();
            yOffset = modelHeight - viewBoundingRectangle.getHeight();
            y = COORDINATE_CENTER_LINE + canvasContext.getBoundingRect().getMinY();
            if (viewBoundingRectangle.getMinY() + modelHeight < gc.getCanvas().getHeight()) {
                y -= yOffset;
            }
            gc.setStroke(Palette.DEFAULT_LABEL_COLOR.get());
            gc.strokeLine(0, y, viewBoundingRectangle.getWidth(), y);
            return;
        }
    }

    private void drawCoordinateBasePairs(CanvasModel canvasModel) {
        gc.save();
        final int baseRectOffset = 30;
        final int BASE_HEIGHT = 13;
        double yOffset = (modelHeight - viewBoundingRectangle.getHeight());
        double y = (viewBoundingRectangle.getMinY()) + baseRectOffset;
        if (viewBoundingRectangle.getMinY() + modelHeight < gc.getCanvas().getHeight()) {
            y -= yOffset;
        }
        if (viewBoundingRectangle.getWidth() < 1500) {
//            final double pixelsPerBasePair = Math.floor(canvasContext.getBoundingRect().getWidth() / viewBoundingRectangle.getWidth());

            gc.setFont(BASE_PAIR_FONT.getFont());
            if (viewYcoordinateRange.contains(y)) {
                int startDna = (int) Math.ceil(viewBoundingRectangle.getMinX());
                int length = (int) Math.ceil(viewBoundingRectangle.getWidth());
                char[] dna = chromosome.getSequence(startDna, length);
                int start = (int) Math.ceil(viewBoundingRectangle.getMinX());
                for (int i = start; i < (dna.length + viewBoundingRectangle.getMinX()); i++) {
                    double index = i - viewBoundingRectangle.getMinX();
                    char base = dna[i - start];
                    gc.setFill(getBaseColor(base));
                    gc.fillRect(index, y, 1, BASE_HEIGHT);
                    if (index < 1 && (i - 1) >= 0) {
                        char outOfviewChar = chromosome.getSequence(i - 1, 1)[0];
                        gc.setFill(getBaseColor(outOfviewChar));
                        gc.fillRect(0, y, index, BASE_HEIGHT);
                    }
                    double textHeight = BASE_PAIR_FONT.getAscent() * .85;
                    double y2 = y + textHeight;
                    if (viewBoundingRectangle.getWidth() < 250) {
                        gc.setFill(Color.BLACK);
                        gc.fillText("" + base, index + .25, y2, .5);
                    }
                }
            }
        } else {
            gc.setFill(Color.GRAY);
            if (viewYcoordinateRange.contains(y)) {
                gc.fillRect(0, y, viewBoundingRectangle.getWidth(), BASE_HEIGHT);
            }
        }
        gc.restore();
    }

    public void updateView(CanvasModel canvasModel) {
        if (canvasContext.isVisible()) {
            double scrollX = canvasModel.getScrollX().doubleValue();
            final double visibleVirtualCoordinatesX = Math.floor(canvasContext.getBoundingRect().getWidth() / xfactor);
            double xOffset = Math.round((scrollX / 100) * (modelWidth - visibleVirtualCoordinatesX));
            xOffset = enforceRangeBounds(xOffset, 0, modelWidth);
            viewBoundingRectangle = new Rectangle2D(xOffset, canvasContext.getBoundingRect().getMinY(), visibleVirtualCoordinatesX, canvasContext.getBoundingRect().getHeight());
            viewYcoordinateRange = Range.<Double>closed(viewBoundingRectangle.getMinY(), viewBoundingRectangle.getMaxY());
            if (canvasContext.isVisible()) {
                clearCanvas();
                draw(canvasModel);
            }
        }
    }

    private void scaleCanvas(CanvasModel canvasModel) {
        this.xfactor = canvasModel.getxFactor().doubleValue();
        if (canvasContext.isVisible()) {
            gc.save();
            gc.scale(xfactor, 1);
            gc.restore();
            updateView(canvasModel);
        }
    }

    public void clearCanvas() {
        gc.save();
        double y = canvasContext.getBoundingRect().getMinY();
        final double height = canvasContext.getBoundingRect().getHeight();
        gc.clearRect(0, y, canvasContext.getBoundingRect().getWidth(), height);
        gc.setFill(Palette.DEFAULT_CANVAS_BG.get());
        gc.fillRect(0, y, canvasContext.getBoundingRect().getWidth(), height);
        gc.restore();
    }

//    @Subscribe
//    private void handleClickDragCancelEvent(ClickDragCancelEvent event) {
//        lastMouseClickX = -1;
//        lastMouseDragX = -1;
//        render();
//    }
//    @Subscribe
//    public void handleClickDragEndEvent(ClickDragEndEvent mouseEvent) {
//        LOG.info("handleClickDragEndEvent method called");
//        if (lastMouseClickX == -1
//                || !canvasContext.getBoundingRect().contains(new Point2D(mouseEvent.getLocal().getX(), canvasContext.getBoundingRect().getMinY()))) {
//            render();
//            return;
//        }
//        final double visibleVirtualCoordinatesX = viewBoundingRectangle.getWidth();
//        double xOffset = viewBoundingRectangle.getMinX();
//        Range<Double> currentRange = Range.closedOpen(xOffset, xOffset + visibleVirtualCoordinatesX);
//
//        lastMouseDragX = Math.floor(mouseEvent.getLocal().getX() / xfactor);
//        ClickDragZoomEvent event;
//        double x1 = viewBoundingRectangle.getMinX() + lastMouseClickX;
//        double x2 = viewBoundingRectangle.getMinX() + lastMouseDragX;
//        if (lastMouseDragX > lastMouseClickX) {
//            event = new ClickDragZoomEvent(x1, x2);
//        } else {
//            event = new ClickDragZoomEvent(x2, x1);
//        }
////        eventBus.post(event);
//        lastMouseClickX = -1;
//        lastMouseDragX = -1;
//        render();
//    }
//    @Subscribe
//    public void handleClickDraggingEvent(ClickDraggingEvent event) {
//        if (!canvasContext.getBoundingRect().contains(new Point2D(event.getLocal().getX(), event.getLocal().getY()))) {
//            return;
//        }
//        lastMouseDragX = Math.floor(event.getLocal().getX() / xfactor);
//        render();
//    }
//    @Subscribe
//    public void handleClickDragStartEvent(ClickDragStartEvent event) {
//        if (!canvasContext.getBoundingRect().contains(event.getLocal())) {
//            return;
//        }
//        lastMouseClickX = Math.floor(event.getLocal().getX() / xfactor);
//    }
    public void render(CanvasModel canvasModel) {
        if (canvasContext.isVisible()) {
            clearCanvas();
            scaleCanvas(canvasModel);
        }
    }

    public void setZoomStripeCoordinate(double zoomStripeCoordinate) {
    }

    @Override
    public CanvasContext getCanvasContext() {
        return canvasContext;
    }

    @Override
    public View getView() {
        View toReturn = new View(viewBoundingRectangle, canvasContext, chromosome, false);
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

    @Override
    public int getZindex() {
        return 2;
    }

    @Override
    public boolean isOverlayWidget() {
        return true;
    }

    @Override
    public DoubleProperty stretchDelta() {
        return new SimpleDoubleProperty(0);
    }

    @Override
    public DoubleProperty activeStretchDelta() {
        return new SimpleDoubleProperty(0);
    }

    @Override
    public ReadOnlyBooleanProperty heightLocked() {
        return new SimpleBooleanProperty(true);
    }

    @Override
    public Optional<Track> getTrack() {
        return Optional.empty();
    }

    @Override
    public BooleanProperty hideLockToggle() {
        return new SimpleBooleanProperty(true);
    }

}
