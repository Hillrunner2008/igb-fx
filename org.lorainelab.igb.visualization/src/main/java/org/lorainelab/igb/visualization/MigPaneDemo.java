package org.lorainelab.igb.visualization;

import com.google.common.collect.Lists;
import com.sun.javafx.geom.RectBounds;
import java.util.List;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import net.miginfocom.layout.LC;
import org.lorainelab.igb.visualization.util.ShapeConverter;
import org.tbee.javafx.scene.layout.MigPane;

/**
 * Tip 1: A canvas resizing itself to the size of the parent pane.
 */
public class MigPaneDemo extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("Drawing Operations Test");
        ResizableCanvas canvas = new ResizableCanvas();
        MigPane pane = new MigPane(new LC().fill().insetsAll("0"));
        pane.setStyle("-fx-background-color: #009688;");
        pane.add(canvas, "grow");
        canvas.widthProperty().bind(
                pane.widthProperty());
        canvas.heightProperty().bind(
                pane.heightProperty());
//        root.setStyle("-fx-background-color: #009688;");
//
//        root.getChildren().add(pane);
        Scene scene = new Scene(pane, 300, 250);
        stage.setWidth(600);
        stage.setHeight(600);
        stage.setScene(scene);
        stage.show();
//        Pane root = new Pane();
//
//        Canvas canvas = new Canvas(300, 300);
//        GraphicsContext gc = canvas.getGraphicsContext2D();
//        drawOnCanvas(canvas);
//
//        root.getChildren().add(canvas);
//
//        Scene scene = new Scene(root, 300, 250, Color.WHITESMOKE);
//
//        stage.setTitle("Lines");
//        stage.setScene(scene);
//        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    class ResizableCanvas extends Canvas {

        private List<FxGlyph> glyphs;

        //
        public ResizableCanvas() {
            glyphs = Lists.newArrayList();
            // Redraw canvas when size changes.
            widthProperty().addListener(evt -> draw());
            heightProperty().addListener(evt -> draw());
        }

        private void draw() {
            double width = getWidth();
            double height = getHeight();
            GraphicsContext gc = getGraphicsContext2D();
            gc.setFill(Color.web("#00BCD4"));
            gc.setStroke(Color.web("#F9F9F9"));
            gc.clearRect(0, 0, width, height);
            addRectToCanvas(gc);
        }

        private void addRectToCanvas(GraphicsContext gc) {
            double size = getWidth() * .03;
            Rectangle rect = new Rectangle(0, getHeight() - size, size, size);
            SVGPath svgPath = ShapeConverter.shapeToSvgPath(rect);
            gc.beginPath();
            gc.appendSVGPath(svgPath.getContent());
            gc.closePath();
            gc.fill();
            gc.stroke();
            gc.setFill(Color.web("#212121"));
            gc.strokeText("A", 5, getHeight() - 5);
        }

        @Override
        public boolean isResizable() {
            return true;
        }

        @Override
        public double prefWidth(double height) {
            return getWidth();
        }

        @Override
        public double prefHeight(double width) {
            return getHeight();
        }
    }

    class FxGlyph {

        private RectBounds rectBounds;

        public FxGlyph(RectBounds rectBounds) {
            this.rectBounds = rectBounds;
        }

        public RectBounds getBBox() {
            return rectBounds;
        }
    }
}
