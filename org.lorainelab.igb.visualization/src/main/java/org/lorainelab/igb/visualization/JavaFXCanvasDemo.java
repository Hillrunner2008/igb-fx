package org.lorainelab.igb.visualization;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.stage.Stage;

public final class JavaFXCanvasDemo extends Application {

    private static final Rectangle2D MAP_RECT = new Rectangle2D(-50.0, -50.0, 100.0, 100.0);
    private static final int OBJECT_COUNT = 500;
    private static boolean PAINT_LABELS = true;  // Set this to 'false' for real speedup but then ... no labels :-(

    private Map< String, Point2D> points_;
    private Canvas canvas_;
    private double xOffset_;
    private double yOffset_;
    private double scale_ = 1.0;
    private double dragStartX_;
    private double dragStartY_;
    private Color backgroundColor_;
    private Color foregroundColor_;

    @Override
    public void init() throws Exception {
        backgroundColor_ = Color.grayRgb(0xc8);
        foregroundColor_ = Color.rgb(0x04, 0x87, 0xf9);

        points_ = new HashMap<>();
        Random random = new Random();
        for (int i = 0; i < OBJECT_COUNT; ++i) {
            double x = random.nextDouble() * MAP_RECT.getWidth() + MAP_RECT.getMinX();
            double y = random.nextDouble() * MAP_RECT.getHeight() + MAP_RECT.getMinY();
            points_.put(String.format("Point%04d", i), new Point2D(x, y));
        }
    }

    @Override
    public void start(Stage stage) throws Exception {
        Group root = new Group();
        canvas_ = new Canvas(800, 800);
        root.getChildren().add(canvas_);

        stage.setTitle("JavaFX Canvas Demo");
        stage.setScene(new Scene(root, backgroundColor_));
        stage.show();

        canvas_.widthProperty().bind(stage.widthProperty());
        canvas_.heightProperty().bind(stage.heightProperty());

        ChangeListener< Number> dimensionChangeListener = (ObservableValue< ? extends Number> arg0, Number arg1, Number arg2) -> {
            drawObjects(canvas_.getGraphicsContext2D());
        };
        canvas_.widthProperty().addListener(dimensionChangeListener);
        canvas_.heightProperty().addListener(dimensionChangeListener);

        canvas_.setOnScroll((ScrollEvent ev) -> {
            Point2D focalPoint = canvas2Physical(ev.getX(), ev.getY());
            if (ev.getDeltaY() > 0.0) {
                scale_ *= 1.1;
            } else {
                scale_ *= 0.9;
            }
            Point2D resetPoint = physical2Canvas(focalPoint.getX(), focalPoint.getY());
            xOffset_ -= (resetPoint.getX() - ev.getX());
            yOffset_ -= (resetPoint.getY() - ev.getY());
            drawObjects(canvas_.getGraphicsContext2D());
        });

        canvas_.setOnMousePressed(new EventHandler< MouseEvent>() {
            @Override
            public void handle(MouseEvent ev) {
                dragStartX_ = ev.getX();
                dragStartY_ = ev.getY();
            }
        });
        canvas_.setOnMouseDragged(new EventHandler< MouseEvent>() {
            @Override
            public void handle(MouseEvent ev) {
                xOffset_ += ev.getX() - dragStartX_;
                dragStartX_ = ev.getX();
                yOffset_ += ev.getY() - dragStartY_;
                dragStartY_ = ev.getY();
                drawObjects(canvas_.getGraphicsContext2D());
            }
        });

        fullScaleAndCenter(canvas_);
        drawObjects(canvas_.getGraphicsContext2D());
    }

    private void drawObjects(GraphicsContext gc) {
        gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
        gc.setStroke(foregroundColor_);

        for (String id : points_.keySet()) {
            Point2D point = points_.get(id);
            Point2D canvasPos = physical2Canvas(point.getX(), point.getY());
            gc.strokeArc(canvasPos.getX(), canvasPos.getY(), 10.0, 10.0, 0.0, 360.0, ArcType.OPEN);
            gc.strokeLine(canvasPos.getX(), canvasPos.getY() + 5.0, canvasPos.getX() + 10.0, canvasPos.getY() + 5.0);
            gc.strokeLine(canvasPos.getX() + 5.0, canvasPos.getY(), canvasPos.getX() + 5.0, canvasPos.getY() + 10.0);
            if (PAINT_LABELS) {
                gc.strokeText(id, canvasPos.getX() + 10.0, canvasPos.getY() - 10.0);
            }
        }
    }

    private void fullScaleAndCenter(Canvas canvas) {
        double width = MAP_RECT.getWidth();
        double height = MAP_RECT.getHeight();

        double scaleX = canvas.getWidth() / width;
        double scaleY = canvas.getHeight() / height;

        scale_ = Math.min(scaleX, scaleY);

        xOffset_ = canvas.getWidth() / 2.0;
        yOffset_ = canvas.getHeight() / 2.0;
    }

    private Point2D physical2Canvas(double x, double y) {
        return new Point2D((x * scale_ + xOffset_), (y * (-scale_) + yOffset_));
    }

    private Point2D canvas2Physical(double i, double j) {
        return new Point2D((i - xOffset_) / scale_, (j - yOffset_) / (-scale_));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
