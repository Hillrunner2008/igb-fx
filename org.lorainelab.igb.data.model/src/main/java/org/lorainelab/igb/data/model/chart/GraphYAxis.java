package org.lorainelab.igb.data.model.chart;

import com.google.common.collect.Maps;
import java.text.DecimalFormat;
import java.util.Map;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.lorainelab.igb.data.model.View;
import static org.lorainelab.igb.data.model.util.FontUtils.AXIS_LABEL_FONT;
import static org.lorainelab.igb.data.model.util.Palette.GRAPH_GRID_FILL;

/**
 *
 * @author dcnorris
 */
public class GraphYAxis {

    private static Font tickLabelFont = Font.font("System", 8);
    private static DecimalFormat df = new DecimalFormat();
    private Map<Double, Double> majorTickPositionValueReference;
    private MinimalNumberAxis yAxisTest;

    public static final int yAxisWidth = 45;

    public GraphYAxis() {
        majorTickPositionValueReference = Maps.newTreeMap();
        labelOffset = AXIS_LABEL_FONT.getAscent() / 2;
    }

    public void drawYAxis(GraphicsContext gc, View view, Rectangle2D graphBounds) {
        final Rectangle2D canvasCoordRect = view.getCanvasContext().getBoundingRect();
        java.awt.geom.Rectangle2D.Double modelCoordRect = view.getMutableCoordRect();
        try {
            gc.save();
            gc.setGlobalAlpha(.70);
            gc.setFill(Color.WHITESMOKE);
            gc.setStroke(Color.BLACK);

            double minTrackY = 0; //already translated to canvas rect minY
            double maxTrackY = canvasCoordRect.getHeight() / view.getYfactor();
            double trackHeightInModelCoords = maxTrackY;
            double cutOffAmount = graphBounds.getHeight() - modelCoordRect.getHeight();
            double topCutOff = modelCoordRect.getMinY();
            double bottomCutOff = cutOffAmount - topCutOff;

            double minGraphY = graphBounds.getMinY() + bottomCutOff;
            double maxGraphY = graphBounds.getMaxY() - topCutOff;

            final double yAxisMaxX = yAxisWidth / view.getXfactor();
            double majorTickInterval = AxisUtil.getMajorTick(graphBounds.getHeight());

            double tickStartPosition;
            gc.fillRect(0, minTrackY, yAxisMaxX, maxTrackY);
            final double majorTickLenth = 10 / view.getXfactor();
            gc.setFont(AXIS_LABEL_FONT.getFont());
            gc.setFill(Color.BLACK);
            gc.setGlobalAlpha(1);

            majorTickPositionValueReference.clear();
            final double tickHeight = 1 / view.getYfactor();

            tickStartPosition = (long) majorTickInterval;
            for (double i = tickStartPosition; i < maxGraphY; i += majorTickInterval) {
                final double y = maxGraphY - i;
                gc.fillRect(yAxisMaxX - majorTickLenth, y, majorTickLenth, tickHeight);
                majorTickPositionValueReference.put(i, y);
            }
            gc.scale(1 / view.getXfactor(), 1 / view.getYfactor());

            majorTickPositionValueReference.entrySet().forEach(entry -> {
                double textYPosition = entry.getValue() * view.getYfactor() + labelOffset;
                gc.fillText(df.format(entry.getKey()), 2, textYPosition);
            });
            gc.scale(view.getXfactor(), view.getYfactor());

            double minorTickInterval = majorTickInterval / 10;
            long startMinor = (long) (minGraphY + minorTickInterval - (minGraphY % minorTickInterval));
            final double minorTickLenth = majorTickLenth / 2;
            for (long i = startMinor; i < maxGraphY; i += minorTickInterval) {
                double y = maxGraphY - i;
                if (y % majorTickInterval != 0) {
                    gc.fillRect(yAxisMaxX - minorTickLenth, y, minorTickLenth, tickHeight);
                }
            }
        } finally {
            gc.restore();
        }
    }

    private float labelOffset;

    public void drawYAxisGridLines(GraphicsContext gc, View view, Rectangle2D boundingRect) {
        try {
            gc.save();
            final Rectangle2D canvasCoordRect = view.getCanvasContext().getBoundingRect();
            gc.setGlobalAlpha(.7);
            gc.setFill(GRAPH_GRID_FILL);
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
}
