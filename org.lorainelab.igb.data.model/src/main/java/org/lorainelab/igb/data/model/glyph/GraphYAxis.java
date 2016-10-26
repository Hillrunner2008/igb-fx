package org.lorainelab.igb.data.model.glyph;

import com.google.common.collect.Maps;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.lorainelab.igb.data.model.View;
import org.lorainelab.igb.data.model.util.FontReference;
import static org.lorainelab.igb.data.model.util.FontUtils.AXIS_LABEL_FONT;

/**
 *
 * @author dcnorris
 */
public class GraphYAxis {

    private static Font tickLabelFont = Font.font("System", 8);
    private static DecimalFormat df = new DecimalFormat();
    private Map<Long, Double> majorTickPositionValueReference;

    public static final int yAxisWidth = 45;

    public GraphYAxis() {
        majorTickPositionValueReference = Maps.newTreeMap();
    }

    public void drawYAxis(GraphicsContext gc, View view) {
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
            majorTickPositionValueReference.clear();
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

    public void drawYAxisGridLines(GraphicsContext gc, View view) {
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
}
