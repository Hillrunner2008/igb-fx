package org.lorainelab.igb.visualization.model;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Reference;
import com.google.common.collect.Range;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.sun.javafx.tk.FontMetrics;
import com.sun.javafx.tk.Toolkit;
import java.text.DecimalFormat;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.lorainelab.igb.visualization.CanvasPane;
import org.lorainelab.igb.visualization.EventBusService;
import org.lorainelab.igb.visualization.event.ClickDragZoomEvent;
import org.lorainelab.igb.visualization.event.MouseDraggedEvent;
import org.lorainelab.igb.visualization.event.MousePressedEvent;
import org.lorainelab.igb.visualization.event.MouseReleasedEvent;
import org.lorainelab.igb.visualization.event.ZoomStripeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
@Component
public class CoordinateTrackRenderer implements TrackRenderer {

    private static final Logger LOG = LoggerFactory.getLogger(CoordinateTrackRenderer.class);
    private static final String COORDINATES_TRACK_LABEL = "Coordinates";
    private static final Color A_COLOR = Color.rgb(151, 255, 179);
    private static final Color T_COLOR = Color.rgb(102, 211, 255);
    private static final Color G_COLOR = Color.rgb(255, 210, 0);
    private static final Color C_COLOR = Color.rgb(255, 176, 102);
    private static final int COORDINATE_CENTER_LINE = 20;

    final int modelWidth;
    final double modelHeight;
    private Rectangle2D viewBoundingRectangle;
    private Range<Double> viewYcoordinateRange;
    private double xfactor = 1;
    private final RefrenceSequenceProvider refrenceSequenceProvider;
    double zoomStripeCoordinate = -1;
    protected EventBus eventBus;
    private final CanvasContext canvasContext;
    private final GraphicsContext gc;
    private int weight;

    public CoordinateTrackRenderer(CanvasPane canvasPane, RefrenceSequenceProvider refrenceSequenceProvider) {
        weight = 0;
        this.eventBus = canvasPane.getEventBus();
        eventBus.register(this);
        this.refrenceSequenceProvider = refrenceSequenceProvider;
        this.modelWidth = refrenceSequenceProvider.getReferenceDna().length();
        this.modelHeight = 50;
        viewBoundingRectangle = new Rectangle2D(0, 0, modelWidth, modelHeight);
        canvasContext = new CanvasContext(canvasPane.getCanvas(), Rectangle2D.EMPTY, 0, 0);
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
                int endDna = startDna + (int) Math.ceil(viewBoundingRectangle.getWidth());
                char[] dna = refrenceSequenceProvider.getReferenceDna().substring(startDna, endDna).toCharArray();

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
                        char outOfviewChar = refrenceSequenceProvider.getReferenceDna().charAt(i - 1);
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

    private Color getBaseColor(char base) {
        switch (base) {
            case 'a':
            case 'A':
                return A_COLOR;
            case 't':
            case 'T':
                return T_COLOR;
            case 'g':
            case 'G':
                return G_COLOR;
            case 'c':
            case 'C':
                return C_COLOR;
            default:
                return Color.GRAY;
        }
    }

    @Override
    public void updateView(double scrollX, double scrollY) {
        if (canvasContext.isVisible()) {
            final double visibleVirtualCoordinatesX = Math.floor(canvasContext.getBoundingRect().getWidth() / xfactor);
            double xOffset = Math.round((scrollX / 100) * (modelWidth - visibleVirtualCoordinatesX));
            if (zoomStripeCoordinate != -1) {
                double zoomStripePositionPercentage = (zoomStripeCoordinate - viewBoundingRectangle.getMinX()) / viewBoundingRectangle.getWidth();
                xOffset = Math.max(zoomStripeCoordinate - (visibleVirtualCoordinatesX * zoomStripePositionPercentage), 0);
            }
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

    private void clearCanvas() {
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
    public void handleMouseReleased(MouseReleasedEvent mouseEvent) {
        ClickDragZoomEvent event;
        double x1 = viewBoundingRectangle.getMinX() + lastMouseClickX;
        double x2 = viewBoundingRectangle.getMinX() + lastMouseDragX;
        if (lastMouseDragX > lastMouseClickX) {
            event = new ClickDragZoomEvent(x1, x2);
        } else {
            event = new ClickDragZoomEvent(x2, x1);
        }
        eventBus.post(event);
        lastMouseClickX = -1;
        lastMouseDragX = -1;
        render();
    }

    @Subscribe
    public void handleMouseDraggedEvent(MouseDraggedEvent event) {
        lastMouseDragX = Math.floor(event.getLocal().getX() / xfactor);
        render();
    }

    @Subscribe
    public void handleMousePressedEvent(MousePressedEvent event) {
        lastMouseClickX = Math.floor(event.getLocal().getX() / xfactor);
    }

    @Override
    public void render() {
        if (canvasContext.isVisible()) {
            clearCanvas();
            draw();
        }
    }

    @Subscribe
    private void zoomStripeListener(ZoomStripeEvent event) {
        zoomStripeCoordinate = event.getZoomStripeCoordinate();
    }

    @Override
    public CanvasContext getCanvasContext() {
        return canvasContext;
    }

    @Override
    public View getView() {
        View toReturn = new View(viewBoundingRectangle);
        toReturn.setXfactor(xfactor);
        return toReturn;
    }

    @Override
    public String getTrackLabel() {
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

}
