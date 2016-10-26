package org.lorainelab.igb.data.model.glyph;

import static com.google.common.base.Preconditions.*;
import com.google.common.collect.Maps;
import com.vividsolutions.jts.geom.Coordinate;
import java.awt.Rectangle;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.lorainelab.igb.data.model.Chromosome;
import org.lorainelab.igb.data.model.View;
import org.lorainelab.igb.data.model.chart.IntervalChart;
import org.lorainelab.igb.data.model.util.FontReference;
import static org.lorainelab.igb.data.model.util.FontUtils.AXIS_LABEL_FONT;
import org.lorainelab.igb.data.model.util.Palette;
import static org.lorainelab.igb.data.model.util.Palette.LOADED_REGION_BG;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
public class GraphGlyph implements Glyph {

    private static final Logger LOG = LoggerFactory.getLogger(GraphGlyph.class);
    private final Rectangle2D boundingRect;
    private IntervalChart data;
    private Font tickLabelFont = Font.font("System", 8);
    private DecimalFormat df = new DecimalFormat();
    private Map<Long, Double> majorTickPositionValueReference;
    private final Chromosome chromosome;
    private static final int yAxisWidth = 45;

    public GraphGlyph(IntervalChart data, Chromosome chromosome) {
        checkNotNull(data);
        checkNotNull(chromosome);
        this.chromosome = chromosome;
        this.data = data;
        this.boundingRect = new Rectangle2D(0, data.getDataBounds().y, chromosome.getLength(), data.getDataBounds().height);
        majorTickPositionValueReference = Maps.newTreeMap();
        //setupExperimentalNodeChart(chromosome, data);
    }

    private void setupExperimentalNodeChart(Chromosome chromosome1, IntervalChart data1) {
        xAxis = new NumberAxis(0, chromosome1.getLength(), AxisUtil.getMajorTick(chromosome1.getLength()));
        xAxis.setAutoRanging(false);
        xAxis.setAnimated(false);
        yAxis = new NumberAxis(data1.getDataBounds().y, data1.getDataBounds().height, AxisUtil.getMajorTick(data1.getDataBounds().height));
        yAxis.setAutoRanging(false);
        yAxis.setAnimated(false);
        ac = new AreaChart<Number, Number>(xAxis, yAxis) {
            @Override
            public void setPrefSize(double prefWidth, double prefHeight) {
                super.setPrefSize(prefWidth, prefHeight);
                setWidth(prefWidth);
                setHeight(prefHeight);
            }
        };
        ac.setAnimated(false);
        ac.setCreateSymbols(false);
        ac.setLegendVisible(false);
        scene = new Scene(ac, 800, 150);
        BundleContext bc = FrameworkUtil.getBundle(GraphGlyph.class).getBundleContext();
        scene.getStylesheets().add(bc.getBundle().getEntry("chartStyle.css").toExternalForm());
        scene.setFill(LOADED_REGION_BG);
        Pane chartPane = (Pane) ac.lookup(".chart-content");
        graph = chartPane.getChildren().stream().filter(child -> child instanceof Group).map(child -> Group.class.cast(child)).findFirst().get();
    }
    private Scene scene;
    private AreaChart<Number, Number> ac;
    private Group graph;
    private NumberAxis yAxis;
    private NumberAxis xAxis;

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
            updateBounds(view);
            drawXAxisGridLines(gc, view);
            drawYAxisGridLines(gc, view);
            drawChartData(gc, view);
            drawYAxis(gc, view);
        } finally {
            gc.restore();
        }
    }

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

    private void updateBounds(View view) {
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
        double width = Math.min(chromosome.getLength() - modelCoordRect.x, Math.ceil(modelCoordRect.getMaxX() - modelCoordRect.x) + 1);
        modelCoordRect.setRect(modelCoordRect.getMinX(), minVisibleY, width, maxVisibleY);
        // updateExperimentalChartBounds(modelCoordRect);
    }

    private void updateExperimentalChartBounds(java.awt.geom.Rectangle2D.Double modelCoordRect) {
        xAxis.setLowerBound(modelCoordRect.getMinX());
        xAxis.setUpperBound(modelCoordRect.getMaxX());
        xAxis.setTickUnit(modelCoordRect.getWidth());
    }

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
        return GlyphAlignment.BOTTOM;
    }

    @Override
    public void setGlyphAlignment(GlyphAlignment alignment) {
        LOG.warn("Graphs do not support multiple glyphAlignments yet, ignoring setter call");
    }

    private double getDisplayPosition(double value, View view) {
        return (value - view.getMutableCoordRect().getMinY()) + view.getMutableCoordRect().getHeight();
    }

    private void drawChartData(GraphicsContext gc, View view) {
        try {
            gc.save();
            java.awt.geom.Rectangle2D.Double modelCoordRect = view.getMutableCoordRect();
            final List<Coordinate> dataInRange = data.getDataInRange(modelCoordRect, view.getXpixelsPerCoordinate());
            if (!dataInRange.isEmpty()) {
                drawGraphFill(gc, view, dataInRange);
                drawGrawLine(gc, view, dataInRange);
            }
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
        } finally {
            gc.restore();
        }

    }

    private void drawGraphFill(GraphicsContext gc, View view, final List<Coordinate> dataInRange) {
        java.awt.geom.Rectangle2D.Double modelCoordRect = view.getMutableCoordRect();
        final double zeroPosition = getDisplayPosition(0, view);
        gc.setFill(Color.web("#E24D42"));
        gc.beginPath();
        gc.moveTo(0, modelCoordRect.getMaxY());
        for (Coordinate c : dataInRange) {
            double width = c.z;
            final double minX = Math.max(c.x - 0.5 - modelCoordRect.x, 0);
            final double maxY = modelCoordRect.getMaxY() - c.y;
            gc.moveTo(minX, zeroPosition);
            gc.lineTo(minX, maxY);
            gc.lineTo(minX + width, maxY);
            gc.lineTo(minX + width, zeroPosition);//would not needed if there were no gaps in intervals, but I don't know if that can be assumed safely
        }
        gc.setGlobalAlpha(.4);
        gc.fill();
        gc.setGlobalAlpha(1);
    }

    private void drawGrawLine(GraphicsContext gc, View view, final List<Coordinate> dataInRange) {
        gc.setStroke(Color.web("#E24D42"));
        gc.setLineWidth(2);
        java.awt.geom.Rectangle2D.Double modelCoordRect = view.getMutableCoordRect();
        gc.beginPath();
        final double firstY = Math.floor((modelCoordRect.getMaxY() - dataInRange.get(0).y));
        final double startX = Math.max((dataInRange.get(0).x - 0.5 - modelCoordRect.x), 0);
        gc.moveTo(startX, firstY);
        for (Coordinate c : dataInRange) {
            double width = c.z;
            final double minX = c.x - 0.5 - modelCoordRect.x;
            final double maxY = modelCoordRect.getMaxY() - c.y;
            gc.lineTo(minX, maxY);
            gc.lineTo((minX + width), maxY);
        }
        gc.scale(1 / view.getXfactor(), 1 / view.getYfactor());
        gc.stroke();
    }

    //experimental chart node imageSnapshot
    private void experimentalCharNodeImageSnapshotApproach(GraphicsContext gc, View view) {
        try {
            gc.save();
            gc.scale(1 / view.getXfactor(), 1 / view.getYfactor());
            java.awt.geom.Rectangle2D.Double modelCoordRect = view.getMutableCoordRect();
            final List<Coordinate> dataInRange = data.getDataInRange(modelCoordRect, view.getXpixelsPerCoordinate());
            if (!dataInRange.isEmpty()) {
                final Rectangle2D canvasCoordRect = view.getCanvasContext().getBoundingRect();
                XYChart.Series<Number, Number> series = new XYChart.Series<>();
                List<XYChart.Data<Number, Number>> update = new ArrayList<XYChart.Data<Number, Number>>();
                for (Coordinate each : dataInRange) {
                    update.add(new XYChart.Data<>(each.x, each.y));
                    update.add(new XYChart.Data<>(each.x + each.z, each.y));
                }
                ObservableList<XYChart.Data<Number, Number>> list = FXCollections.observableArrayList(update);
                series.setData(list);
                ac.getData().clear();
                ac.getData().add(series);
                final double snapShotWidth = canvasCoordRect.getWidth();
                final double snapShotHeight = canvasCoordRect.getHeight();

                ac.setPrefSize(snapShotWidth, snapShotHeight);
                SnapshotParameters snapshotParams = new SnapshotParameters();
                snapshotParams.setFill(LOADED_REGION_BG);
                WritableImage snapshot = graph.snapshot(snapshotParams, null);
                gc.drawImage(snapshot, 0, canvasCoordRect.getMinY(), canvasCoordRect.getWidth(), canvasCoordRect.getHeight());
            }
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
        } finally {
            gc.restore();
        }

    }
}
