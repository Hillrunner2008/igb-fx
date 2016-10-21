package org.lorainelab.igb.data.model.glyph;

import cern.colt.list.DoubleArrayList;
import static com.google.common.base.Preconditions.*;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import com.vividsolutions.jts.geom.Coordinate;
import java.awt.Rectangle;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.lorainelab.igb.data.model.View;
import org.lorainelab.igb.data.model.chart.ChartData;
import org.lorainelab.igb.data.model.util.FontReference;
import static org.lorainelab.igb.data.model.util.FontUtils.AXIS_LABEL_FONT;
import org.lorainelab.igb.data.model.util.Palette;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
public class GraphGlyph implements Glyph {

    private static final Logger LOG = LoggerFactory.getLogger(GraphGlyph.class);
    private final Rectangle2D boundingRect;
    private GlyphAlignment glyphAlignment;
    private ChartData data;

    public GraphGlyph(ChartData data) {
        checkNotNull(data);
        this.data = data;
        glyphAlignment = GlyphAlignment.BOTTOM;
        this.boundingRect = new Rectangle2D(data.getDataBounds().x, data.getDataBounds().y, data.getDataBounds().width, data.getDataBounds().height);
        majorTickPositionValueReference = Maps.newTreeMap();
    }

    @Override
    public Color getFill() {
        return Palette.DEFAULT_GLYPH_FILL;
    }

    @Override
    public Color getStrokeColor() {
        return Palette.DEFAULT_GLYPH_FILL;
    }

    @Override
    public Rectangle2D getBoundingRect() {
        return boundingRect;
    }

    @Override
    public void draw(GraphicsContext gc, View view, Rectangle2D slotBoundingViewRect) {
        try {
            gc.save();
            updateYbounds(view);
            drawXAxisGridLines(gc, view);
            drawYAxisGridLines(gc, view);
            drawChartData(gc, view);
            drawYAxis(gc, view);
        } finally {
            gc.restore();
        }
    }

    private Font tickLabelFont = Font.font("System", 8);
    private DecimalFormat df = new DecimalFormat();

    //TODO create test case to report bug report for tick path width being incorrectly calculated due to scaling requiring use of immediate close/stroke for each tick
    private void drawYAxis(GraphicsContext gc, View view) {
        final Rectangle2D canvasCoordRect = view.getCanvasContext().getBoundingRect();
        java.awt.geom.Rectangle2D.Double modelCoordRect = view.getMutableCoordRect();
        try {
            gc.save();
            gc.setGlobalAlpha(.70);
            gc.setFill(Color.WHITESMOKE);
            gc.setStroke(Color.BLACK);
            final double yAxisMaxX = yAxisWidth / view.getXfactor();
            gc.fillRect(0, canvasCoordRect.getMinY() / view.getXfactor(), yAxisMaxX, canvasCoordRect.getHeight() / view.getYfactor());
            double majorTickInterval = AxisUtil.getMajorTick(modelCoordRect.height);
            long tickStartPosition;
            double minVisibleY = modelCoordRect.getMinY();
            double maxVisibleY = modelCoordRect.getMaxY();
            if ((minVisibleY % majorTickInterval) == 0) {
                tickStartPosition = (long) majorTickInterval;
            } else {
                tickStartPosition = (long) (minVisibleY + majorTickInterval - (minVisibleY % majorTickInterval));
            }
            FontReference labelFont = AXIS_LABEL_FONT;
            gc.setFont(labelFont.getFont());
            gc.setFill(Color.BLACK);
            gc.setGlobalAlpha(1);
            final double majorTickLenth = 10 / view.getXfactor();
            for (long i = tickStartPosition; i < maxVisibleY; i += majorTickInterval) {
                double y = i - minVisibleY;
                gc.fillRect(yAxisMaxX - majorTickLenth, y, majorTickLenth, 1 / view.getYfactor());
                majorTickPositionValueReference.put(i, y);
            }
            List<Double> tickPositions = new ArrayList<>(majorTickPositionValueReference.values());
            List<Long> tickValues = new ArrayList<>(majorTickPositionValueReference.keySet());
            Collections.reverse(tickValues);
            gc.scale(1 / view.getXfactor(), 1 / view.getYfactor());
            for (int i = 0; i < tickPositions.size(); i++) {
                final float labelOffset = labelFont.getAscent() / 2;
                final double textYPosition = tickPositions.get(i) * view.getYfactor() + labelOffset;
                gc.fillText(df.format(tickValues.get(i)), 2, textYPosition);
            }
            gc.scale(view.getXfactor(), view.getYfactor());

            double minorTickInterval = majorTickInterval / 10;
            long startMinor = (long) (minVisibleY + minorTickInterval - (minVisibleY % minorTickInterval));
            final double minorTickLenth = majorTickLenth / 2;
            for (long i = startMinor; i < maxVisibleY; i += minorTickInterval) {
                double y = i - minVisibleY;
                if (y % majorTickInterval != 0) {
                    gc.fillRect(yAxisMaxX - minorTickLenth, y, minorTickLenth, 1 / view.getYfactor());
                }
            }
        } finally {
            gc.restore();
        }
    }
    private Map<Long, Double> majorTickPositionValueReference;

    private void updateYbounds(View view) {
        final Rectangle2D canvasCoordRect = view.getCanvasContext().getBoundingRect();
        java.awt.geom.Rectangle2D.Double modelCoordRect = view.getMutableCoordRect();
        final double modelCoordMinY = modelCoordRect.getMinY();
        double visPercY = canvasCoordRect.getHeight() / view.getCanvasContext().getTrackHeight();
        double maxUpperBounds = boundingRect.getHeight() - modelCoordMinY;
        double minVisibleY;
        double maxVisibleY;
        if (visPercY < 1) {
            if (modelCoordMinY > 0) {
                //cut off from top
                double topCutOff = maxUpperBounds * visPercY;
                minVisibleY = topCutOff - modelCoordRect.getHeight();
                maxVisibleY = topCutOff;
            } else {
                //cut off from bottom
                double bottomCutOff = maxUpperBounds - (maxUpperBounds * visPercY);
                minVisibleY = bottomCutOff;
                maxVisibleY = bottomCutOff + modelCoordRect.getHeight();
            }
        } else {
            minVisibleY = boundingRect.getMinY();
            maxVisibleY = boundingRect.getHeight();
        }
        modelCoordRect.setRect(modelCoordRect.getMinX(), minVisibleY, modelCoordRect.getWidth(), maxVisibleY);
    }
    private static final int yAxisWidth = 45;

    private void drawXAxisGridLines(GraphicsContext gc, View view) {
        java.awt.geom.Rectangle2D.Double modelCoordRect = view.getMutableCoordRect();
        try {
            gc.save();
            gc.setGlobalAlpha(.7);
            gc.setFill(Color.web("#616060"));
            gc.setStroke(Color.web("#616060"));
            final double maxY = (view.getCanvasContext().getBoundingRect().getMaxY());
            double majorTickInterval = AxisUtil.getMajorTick(modelCoordRect.getWidth());
            long startMajor;
            if ((modelCoordRect.getMinX() % majorTickInterval) == 0) {
                startMajor = (long) modelCoordRect.getMinX();
            } else {
                startMajor = (long) (modelCoordRect.getMinX() + majorTickInterval - (modelCoordRect.getMinX() % majorTickInterval));
            }
            for (long i = startMajor; i < (modelCoordRect.getMaxX() + 1); i += majorTickInterval) {
                double x = i - modelCoordRect.getMinX();
                gc.fillRect(x, modelCoordRect.getMinY(), 1 / view.getXfactor(), modelCoordRect.getHeight());
            }
        } finally {
            gc.restore();
        }
    }

    private void drawYAxisGridLines(GraphicsContext gc, View view) {
        try {
            gc.save();
            final Rectangle2D canvasCoordRect = view.getCanvasContext().getBoundingRect();
            gc.setGlobalAlpha(.7);
            gc.setFill(Color.web("#616060"));
            final double minX = yAxisWidth / view.getXfactor();
            final double width = (canvasCoordRect.getWidth() / view.getXfactor()) - minX;
            final double height = 1 / view.getYfactor();
            majorTickPositionValueReference.entrySet().forEach(entry -> {
                gc.fillRect(minX, entry.getValue(), width, height);
            });

        } finally {
            gc.restore();
        }
    }

    public Optional<Rectangle.Double> calculateDrawRect(View view, Rectangle2D slotBoundingViewRect) {
        SHARED_RECT.setRect(view.getMutableCoordRect());
        return Optional.of(SHARED_RECT);
    }

    @Override
    public GlyphAlignment getGlyphAlignment() {
        return glyphAlignment;
    }

    @Override
    public void setGlyphAlignment(GlyphAlignment alignment) {
        this.glyphAlignment = alignment;
    }

    private double getDisplayPosition(double value, View view) {
        return (value - view.getMutableCoordRect().getMinY()) + view.getMutableCoordRect().getHeight();
    }

    private void drawChartData(GraphicsContext gc, View view) {
        gc.save();
        java.awt.geom.Rectangle2D.Double modelCoordRect = view.getMutableCoordRect();
        gc.setFill(Color.web("#375257"));
        gc.setStroke(Color.web("#6ED0E0"));
        gc.setLineWidth(Math.max(.75, 1 / view.getXfactor()));
        DoubleArrayList x = new DoubleArrayList();
        DoubleArrayList y = new DoubleArrayList();
        double lastX = 0;

        final Range<Double> viewXRange = Range.closed(modelCoordRect.x, modelCoordRect.getMaxX());
        final List<Coordinate> dataInRange = data.getDataInRange(viewXRange, modelCoordRect.y, modelCoordRect.getMaxY(), view.getXpixelsPerCoordinate());

        gc.beginPath();
        gc.moveTo(0, modelCoordRect.getMaxY());
        final double zeroPosition = getDisplayPosition(0, view);
        for (Coordinate c : dataInRange) {
            double width = c.z;
            final double minX = c.x - 0.5 - modelCoordRect.x;
            final double maxY = modelCoordRect.getMaxY() - c.y;
            gc.moveTo(minX, zeroPosition);
            gc.lineTo(minX, maxY);
            gc.lineTo(minX + width, maxY);
            gc.lineTo(minX + width, zeroPosition);//maybe not needed
        }
//        List<Coordinate> constructedPath = new ArrayList<>(dataInRange.size());
//        for (Iterator<Coordinate> it = dataInRange.iterator(); it.hasNext();) {
//            Coordinate c = it.next();
//            constructedPath.add(c);
//            lastX = c.x;
//        }
//        if (!constructedPath.isEmpty()) {
//            Collections.sort(constructedPath, (e1, e2) -> Double.compare(e1.x, e2.x));
//            Coordinate first = constructedPath.get(0);
//
//            final double displayYPos = modelCoordRect.getMaxY() - first.y;
////            final double numericYPos = getYAxis().toNumericValue(getYAxis().getValueForDisplay(displayYPos));
////
////            final double yAxisZeroPos = maxVisibleY;
////            final boolean isYAxisZeroPosVisible = minVisibleY == 0;
////            final double yAxisHeight = maxVisibleY - minVisibleY;
////            final double yFillPos = isYAxisZeroPosVisible ? yAxisZeroPos : numericYPos < 0 ? numericYPos - yAxisHeight : yAxisHeight;
//            final double yFillPos = maxVisibleY;
//            //draw data border
//            gc.beginPath();
//            gc.moveTo(first.x - 0.5 - modelCoordRect.x, displayYPos);
//            gc.setLineWidth(.1);
//            constructedPath.stream().forEach(coord -> {
//                gc.lineTo(coord.x - 0.5 - modelCoordRect.x, modelCoordRect.getMaxY() - coord.y);
//            });
//            gc.stroke();
//
//            gc.beginPath();
//            gc.moveTo(first.x - 0.5 - modelCoordRect.x, yFillPos);
//            for (Coordinate coord : constructedPath) {
//                gc.lineTo(coord.x - 0.5 - modelCoordRect.x, maxVisibleY - coord.y);
//            }
//            gc.lineTo((float) lastX, (float) yFillPos);
//            gc.closePath();
//            gc.fill();
////            gc.stroke();
//        }
        gc.stroke();
        gc.fill();

//        gc.setGlobalAlpha(.8);
//        gc.fill();
//        gc.strokePolyline(x.elements(), y.elements(), x.size());
//        gc.fill();
        gc.restore();

    }

}
