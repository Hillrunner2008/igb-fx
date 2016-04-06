package org.lorainelab.igb.visualization;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import com.google.common.eventbus.EventBus;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import org.lorainelab.igb.visualization.model.RefrenceSequenceProvider;
import org.lorainelab.igb.visualization.util.CanvasUtils;

@Component(immediate = true, provide = CanvasPane.class)
public class CanvasPane extends Region {

    private Canvas canvas;
    private double modelWidth;
    private double xFactor;
    private double zoomStripeCoordinate;
    private double xOffset;
    private double visibleVirtualCoordinatesX;
    private EventBus eventBus;
    private GenoVixFxController controller;
    private RefrenceSequenceProvider refrenceSequenceProvider;

    public CanvasPane() {
        eventBus = new EventBus();
    }

    @Activate
    public void activate() {
        this.modelWidth = refrenceSequenceProvider.getReferenceDna().length();
        canvas = new Canvas();
        getChildren().add(canvas);
        canvas.widthProperty().addListener(observable -> {
            draw();
            xFactor = canvas.getWidth() / modelWidth;
        });
        canvas.heightProperty().addListener(observable -> draw());
        zoomStripeCoordinate = -1;
        initializeMouseEventHandlers();
    }

    private void initializeMouseEventHandlers() {
        canvas.setOnMouseClicked((MouseEvent event) -> {
            drawZoomCoordinateLine(event);
        });

    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        final double width = getWidth();
        final double height = getHeight();
        final Insets insets = getInsets();
        final double contentX = insets.getLeft();
        final double contentY = insets.getTop();
        final double contentWith = Math.max(0, width - (insets.getLeft() + insets.getRight()));
        final double contentHeight = Math.max(0, height - (insets.getTop() + insets.getBottom()));
        canvas.relocate(contentX, contentY);
        canvas.setWidth(contentWith);
        canvas.setHeight(contentHeight);
    }

    private void draw() {
        final double width = canvas.getWidth();
        final double height = canvas.getHeight();
        final GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, width, height);
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public double getModelWidth() {
        return modelWidth;
    }

    public void drawZoomCoordinateLine(MouseEvent event) {
        zoomStripeCoordinate = (event.getX() / xFactor) + xOffset;
        if (zoomStripeCoordinate >= 0) {
            GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.save();
            gc.setStroke(Color.rgb(0, 0, 0, .3));
            gc.scale(xFactor, 1);
            double x = Math.floor(zoomStripeCoordinate) - xOffset;
            double width = canvas.getWidth() / xFactor;
            if (width > 500) {
                gc.setLineWidth(width * 0.002);
            }
            if (x >= 0 && x <= width) {
                gc.strokeLine(x + .5, 0, x + .5, canvas.getHeight());
            }
            gc.restore();
        }
    }

    double getXFactor() {
        return xFactor;
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    @Reference(optional = true)
    public void setMainViewController(GenoVixFxController controller) {
        this.controller = controller;
        controller.getHSliderValue().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            xFactor = CanvasUtils.exponentialScaleTransform(this, newValue.doubleValue());
            visibleVirtualCoordinatesX = Math.floor(canvas.getWidth() / xFactor);
            xOffset = Math.round((controller.getXScrollPosition().doubleValue() / 100) * (modelWidth - visibleVirtualCoordinatesX));
        });
    }

    @Reference
    public void setRefrenceSequenceProvider(RefrenceSequenceProvider refrenceSequenceProvider) {
        this.refrenceSequenceProvider = refrenceSequenceProvider;
    }
}
