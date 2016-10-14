package org.lorainelab.igb.data.model.glyph;

import static com.google.common.base.Preconditions.*;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import com.vividsolutions.jts.simplify.DouglasPeuckerSimplifier;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
import org.lorainelab.igb.data.model.View;
import static org.lorainelab.igb.data.model.glyph.AxisUtil.getMajorTick;
import static org.lorainelab.igb.utils.FXUtilities.runAndWait;
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
    private final Rectangle.Double bounds;
    private final Rectangle2D boundingRect;
    private final RangeMap<Double, Double> graphIntervals;
    private GlyphAlignment glyphAlignment;
    private final NumberAxis xAxis;
    private final GraphAxis yAxis;
    private AreaChart<Number, Number> ac;
    private GeometryFactory gf;

    public GraphGlyph(RangeMap<Double, Double> graphIntervals) {
        checkNotNull(graphIntervals);
        gf = new GeometryFactory();
        this.graphIntervals = graphIntervals;
        glyphAlignment = GlyphAlignment.BOTTOM;
        bounds = new Rectangle.Double();

        for (Map.Entry<Range<Double>, Double> entry : graphIntervals.asMapOfRanges().entrySet()) {
            Range<Double> r = entry.getKey();
            bounds.add(new Rectangle.Double(r.lowerEndpoint(), 0, r.upperEndpoint() - r.lowerEndpoint(), entry.getValue()));
        }
        this.boundingRect = new Rectangle2D(bounds.x, bounds.y, bounds.width, bounds.height);

//        yAxis = new GraphYAxis(bounds.y, bounds.y + bounds.height);
        xAxis = new NumberAxis(bounds.x, bounds.x + bounds.width, bounds.x + bounds.width); //
        yAxis = new GraphAxis();
        xAxis.setAutoRanging(false);
        yAxis.setAutoRanging(false);
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
        runAndWait(() -> {
            scene = new Scene(ac, 800, 600);
            BundleContext bc = FrameworkUtil.getBundle(GraphGlyph.class).getBundleContext();
            ac.getStylesheets().add(bc.getBundle().getEntry("chartStyle.css").toExternalForm());
           Pane chartPane = (Pane) ac.lookup(".chart-content");
//            Region graphArea = (Region) ac.lookup(".chart-plot-background");
            graph = chartPane.getChildren().stream().filter(child -> child instanceof Group).map(child -> Group.class.cast(child)).findFirst().get();
        });
    }
    private Group graph;
//    private Pane chartPane;
    private Scene scene;

    @Override
    public Color getFill() {
        return Color.BLUEVIOLET;
    }

    @Override
    public Color getStrokeColor() {
        return Color.BLUEVIOLET;
    }

    @Override
    public Rectangle2D getBoundingRect() {
        return boundingRect;
    }

    @Override
    public void draw(GraphicsContext gc, View view, Rectangle2D slotBoundingViewRect) {
        try {
            gc.save();
            final Rectangle2D canvasCoordRect = view.getCanvasContext().getBoundingRect();
            final java.awt.geom.Rectangle2D.Double modelCoordRect = view.getMutableCoordRect();
            double modelCoordinatesPerScreenXPixel = modelCoordRect.getWidth() / canvasCoordRect.getWidth();
//            double modelCoordinatesPerScreenYPixel = modelCoordRect.getHeight() / canvasCoordRect.getHeight();

            final double modelCoordMinY = modelCoordRect.getMinY();

            double visPercY = canvasCoordRect.getHeight() / view.getCanvasContext().getTrackHeight();
            double maxUpperBounds = bounds.height - modelCoordMinY;
            if (visPercY < 1) {
                if (modelCoordMinY > 0) {
                    //cut off from top
                    double topCutOff = maxUpperBounds * visPercY;
                    yAxis.updateUpperBound(topCutOff);
                } else {
                    //cut off from bottom
                    double bottomCutOff = maxUpperBounds - (maxUpperBounds * visPercY);
                    yAxis.updateLowerBound(bottomCutOff);
                }
            } else {
                yAxis.updateLowerBound(bounds.y);
                yAxis.updateUpperBound(maxUpperBounds);
            }
               yAxis.updateUpperBound(15_000);
            yAxis.setTickUnit(getMajorTick(yAxis.getUpperBound() - yAxis.getLowerBound()));
            xAxis.setLowerBound(modelCoordRect.x);
            xAxis.setUpperBound(modelCoordRect.getMaxX());

            XYChart.Series intervalData = new XYChart.Series();

            final Set<Map.Entry<Range<Double>, Double>> entrySet = graphIntervals.subRangeMap(view.getXrange()).asMapOfRanges().entrySet();

            List<Coordinate> coordinates = new ArrayList<>();
            for (Map.Entry<Range<Double>, Double> entry : entrySet) {
                Range<Double> xRange = entry.getKey();
                final double x = xRange.lowerEndpoint();
                double maxX = xRange.upperEndpoint() - 1;
                final Double y = entry.getValue();
                coordinates.add(new Coordinate(x, y));
                coordinates.add(new Coordinate(maxX, y));
            }
            Coordinate[] coordArr = coordinates.toArray(new Coordinate[coordinates.size()]);
            Geometry geom = new LineString(new CoordinateArraySequence(coordArr), gf);
            Geometry simplified = DouglasPeuckerSimplifier.simplify(geom, modelCoordinatesPerScreenXPixel);
            List<XYChart.Data<Number, Number>> update = new ArrayList<XYChart.Data<Number, Number>>();
            for (Coordinate each : simplified.getCoordinates()) {
                update.add(new XYChart.Data<>(each.x, each.y));
            }
//            System.out.println(String.format("Reduces points from %d to %d", coordinates.size(), update.size()));
            ObservableList<XYChart.Data<Number, Number>> list = FXCollections.observableArrayList(update);
            intervalData.setData(list);
            ac.getData().clear();
            ac.getData().add(intervalData);

            gc.scale(1 / view.getXfactor(), 1 / view.getYfactor());
            ac.setPrefSize(canvasCoordRect.getWidth(), canvasCoordRect.getHeight());
            SnapshotParameters snapshotParams = new SnapshotParameters();
            WritableImage yAxisImage = yAxis.snapshot(snapshotParams, null);
            gc.setGlobalAlpha(1);

            WritableImage snapshot = graph.snapshot(snapshotParams, null);
            gc.drawImage(snapshot, 0, canvasCoordRect.getMinY(), canvasCoordRect.getWidth(), canvasCoordRect.getHeight());
            gc.setGlobalAlpha(.70);
            gc.drawImage(yAxisImage, 0, canvasCoordRect.getMinY(), 45, canvasCoordRect.getHeight());
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

}
