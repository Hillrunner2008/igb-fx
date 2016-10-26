package org.lorainelab.igb.data.model.glyph;

import static com.google.common.base.Preconditions.*;
import com.vividsolutions.jts.geom.Coordinate;
import java.awt.Rectangle;
import java.util.List;
import java.util.Optional;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.lorainelab.igb.data.model.Chromosome;
import org.lorainelab.igb.data.model.View;
import org.lorainelab.igb.data.model.chart.IntervalChart;
import static org.lorainelab.igb.data.model.glyph.GraphXAxis.drawXAxisGridLines;
import org.lorainelab.igb.data.model.util.Palette;
import static org.lorainelab.igb.data.model.util.Palette.GRAPH_FILL;
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
    private final Chromosome chromosome;
    private GraphYAxis yAxis;

    public GraphGlyph(IntervalChart data, Chromosome chromosome) {
        checkNotNull(data);
        checkNotNull(chromosome);
        this.chromosome = chromosome;
        this.data = data;
        this.boundingRect = new Rectangle2D(0, data.getDataBounds().y, chromosome.getLength(), data.getDataBounds().height);
        yAxis = new GraphYAxis();
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
            updateBounds(view);
            drawXAxisGridLines(gc, view);
            yAxis.drawYAxisGridLines(gc, view);
            drawChartData(gc, view);
            yAxis.drawYAxis(gc, view);
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
    }

   

    @Override
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
        gc.setFill(GRAPH_FILL);
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
        gc.setStroke(GRAPH_FILL);
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

}
