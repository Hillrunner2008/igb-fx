package org.lorainelab.igb.visualization;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.lorainelab.igb.visualization.model.CanvasPaneModel;
import static org.lorainelab.igb.visualization.model.CanvasPaneModel.MAX_ZOOM_MODEL_COORDINATES_X;
import static org.lorainelab.igb.visualization.util.CanvasUtils.linearScaleTransform;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true, provide = ZoomSliderWidget.class)
public class ZoomSliderWidget extends HBox {

    private static final int TOTAL_SLIDER_THUMB_WIDTH = 30;
    private static final Color SLIDER_COLOR = Color.web("#0397c9");
    private static final Color THUMB_COLOR = Color.web("#dddddd");
    private Pane xSliderPane;
    private Rectangle leftThumb;
    private Rectangle slider;
    private Rectangle rightThumb;
    double lastDragX;
    private DoubleProperty scrollX;
    private CanvasPaneModel canvasPaneModel;
    private PrimaryCanvasRegion primaryCanvasRegion;
    double xFactor;

    public ZoomSliderWidget() {
        scrollX = new SimpleDoubleProperty(0);
        lastDragX = 0;
        initializeGuiComponents();
    }

    private void initializeGuiComponents() {
        setMinHeight(20);
        setMaxHeight(20);
        setPrefHeight(20);
        xSliderPane = new Pane();
        xSliderPane.setMaxHeight(15);
        xSliderPane.setMinHeight(15);
        xSliderPane.setPrefHeight(15);
        xSliderPane.setPrefWidth(1194);
        leftThumb = new Rectangle(15, 15);
        slider = new Rectangle(150, 10);
        rightThumb = new Rectangle(15, 15);
        leftThumb.setFill(THUMB_COLOR);
        slider.setFill(SLIDER_COLOR);
        rightThumb.setFill(THUMB_COLOR);
        leftThumb.setStroke(Color.BLACK);
        slider.setStroke(Color.BLACK);
        rightThumb.setStroke(Color.BLACK);
        leftThumb.setArcWidth(5);
        leftThumb.setArcHeight(5);
        slider.setLayoutY(3);
        slider.setWidth(832);
        rightThumb.setArcWidth(5);
        rightThumb.setArcHeight(5);
        rightThumb.setLayoutX(818);
        setHgrow(xSliderPane, Priority.ALWAYS);
        xSliderPane.getChildren().addAll(slider, rightThumb, leftThumb);
        getChildren().add(xSliderPane);
    }

    @Activate
    public void activate() {
        initializeZoomScrollBar();
        setupModelListeners();
    }

    private void initializeZoomScrollBar() {
        slider.setOnMousePressed((MouseEvent event) -> {
            lastDragX = event.getX();
        });
        leftThumb.setOnMousePressed((MouseEvent event) -> {
            lastDragX = event.getX();
        });
        rightThumb.setOnMousePressed((MouseEvent event) -> {
            lastDragX = event.getX();
        });

        rightThumb.setOnMouseDragged((MouseEvent event) -> {
            double increment = Math.round(event.getX() - lastDragX);
            double newSliderValue = slider.getWidth() + increment;
            double newRightThumbValue = rightThumb.getX() + increment;

            if (newSliderValue < TOTAL_SLIDER_THUMB_WIDTH) {
                newSliderValue = TOTAL_SLIDER_THUMB_WIDTH;
                newRightThumbValue = rightThumb.getX() - slider.getWidth() + newSliderValue;
            }
            if (newSliderValue > xSliderPane.getWidth()) {
                double tmp = slider.getWidth() + (xSliderPane.getWidth() - slider.getWidth() - slider.getX());
                double diff = Math.abs(newSliderValue - tmp);
                newRightThumbValue -= diff;
                newSliderValue = tmp;
            }
            if (newSliderValue >= 0 && newSliderValue <= (xSliderPane.getWidth() - slider.getX())) {
                slider.setWidth(newSliderValue);
                rightThumb.setX(newRightThumbValue);
                double max = xSliderPane.getWidth() - TOTAL_SLIDER_THUMB_WIDTH;
                double current = slider.getWidth() - TOTAL_SLIDER_THUMB_WIDTH;

                double maxSlider = xSliderPane.getWidth() - slider.getWidth();
                double currentSlider = slider.getX();
                if (maxSlider < 0) {
                    canvasPaneModel.getScrollX().set(0);
                } else {
                    double newScrollX = (currentSlider / maxSlider) * 100;
                    canvasPaneModel.getScrollX().set(Double.isNaN(newScrollX) ? 0 : newScrollX);
                }
                double hSlider = (1 - (current / max)) * 100;
                xFactor = linearScaleTransform(primaryCanvasRegion.getWidth(), canvasPaneModel.getModelWidth().get(), hSlider);

                canvasPaneModel.getxFactor().set(xFactor);
            }
            lastDragX = event.getX();
        });

        leftThumb.setOnMouseDragged((MouseEvent event) -> {
            double increment = Math.round(event.getX() - lastDragX);
            double newSliderValue = slider.getX() + increment;
            double newLeftThumbValue = leftThumb.getX() + increment;
            double newSliderWidth = (slider.getWidth() - increment);
            if (newSliderWidth < TOTAL_SLIDER_THUMB_WIDTH) {
                newSliderWidth = TOTAL_SLIDER_THUMB_WIDTH;
                newSliderValue = slider.getX() + slider.getWidth() - newSliderWidth;
                newLeftThumbValue = leftThumb.getX() + slider.getWidth() - newSliderWidth;
            }
            if (newSliderValue < 0) {
                newSliderValue = 0;
                newLeftThumbValue = 0;
                newSliderWidth = slider.getWidth() + slider.getX();
            }
            if (newSliderValue > xSliderPane.getWidth()) {
                newSliderValue = xSliderPane.getWidth();
            }
            if (newSliderValue >= 0 && newSliderValue <= xSliderPane.getWidth()) {
                slider.setX(newSliderValue);
                slider.setWidth(newSliderWidth);
                leftThumb.setX(newLeftThumbValue);
                double max = xSliderPane.getWidth() - TOTAL_SLIDER_THUMB_WIDTH;
                double current = slider.getWidth() - TOTAL_SLIDER_THUMB_WIDTH;

                double maxSlider = xSliderPane.getWidth() - slider.getWidth();
                double currentSlider = slider.getX();
                if (maxSlider < 0) {
                    canvasPaneModel.getScrollX().set(0);
                } else {
                    double newScrollX = (currentSlider / maxSlider) * 100;
                    canvasPaneModel.getScrollX().set(Double.isNaN(newScrollX) ? 0 : newScrollX);
                }
                double hSlider = (1 - (current / max)) * 100;
                xFactor = linearScaleTransform(primaryCanvasRegion.getWidth(), canvasPaneModel.getModelWidth().get(), hSlider);
                canvasPaneModel.getxFactor().set(xFactor);
            }
            lastDragX = event.getX();
        });

        slider.setOnMouseDragged((MouseEvent event) -> {
            double increment = Math.round(event.getX() - lastDragX);
            double newSliderValue = slider.getX() + increment;
            double newRightThumbValue = rightThumb.getX() + increment;
            double newLeftThumbValue = leftThumb.getX() + increment;
            if (newSliderValue < 0) {
                newSliderValue = 0;
                newLeftThumbValue = 0;
                newRightThumbValue = rightThumb.getX() - slider.getX();
            } else if (newSliderValue > (xSliderPane.getWidth() - slider.getWidth())) {
                newSliderValue = (xSliderPane.getWidth() - slider.getWidth());
                newLeftThumbValue = newSliderValue;
                newRightThumbValue = rightThumb.getX() + xSliderPane.getWidth() - slider.getX() - slider.getWidth();

            }
            slider.setX(newSliderValue);
            leftThumb.setX(newLeftThumbValue);
            rightThumb.setX(newRightThumbValue);
            double max = xSliderPane.getWidth() - slider.getWidth();
            double current = slider.getX();
            double newScrollX = (current / max) * 100;
            canvasPaneModel.setScrollX(Double.isNaN(newScrollX) ? 0 : newScrollX, true);
            lastDragX = event.getX();
        });
    }

    private void syncWidgetSlider() {
        double minScaleX = canvasPaneModel.getModelWidth().get();
        double maxScaleX = MAX_ZOOM_MODEL_COORDINATES_X - 1;
        final double scaleRange = maxScaleX - minScaleX;
        final double xFactor = canvasPaneModel.getxFactor().get();
        final double current = Math.floor(primaryCanvasRegion.getWidth() / xFactor);
        double scaledPercentage = (current - minScaleX) / scaleRange;

        DoubleProperty scrollX = canvasPaneModel.getScrollX();
        double oldWidth = slider.getWidth();
        double oldX = slider.getX();
        double width = ((1 - scaledPercentage) * (xSliderPane.getWidth() - TOTAL_SLIDER_THUMB_WIDTH)) + TOTAL_SLIDER_THUMB_WIDTH;
        double x = ((scrollX.getValue() / 100)) * (xSliderPane.getWidth() - width);
        slider.setX(x);
        leftThumb.setX(x);
        slider.setWidth(width);
        rightThumb.setX(rightThumb.getX() - (oldWidth + oldX - width - x));
    }

    @Reference
    public void setCanvasPaneModel(CanvasPaneModel canvasPaneModel) {
        this.canvasPaneModel = canvasPaneModel;
    }

    @Reference
    public void setPrimaryCanvasRegion(PrimaryCanvasRegion primaryCanvasRegion) {
        this.primaryCanvasRegion = primaryCanvasRegion;
    }

    private void setupModelListeners() {
        canvasPaneModel.getxFactor().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            if (xFactor != newValue.doubleValue()) {
                syncWidgetSlider();
            }
        });
    }

}
