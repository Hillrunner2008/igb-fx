package org.lorainelab.igb.visualization.ui;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.lorainelab.igb.visualization.model.CanvasModel;
import static org.lorainelab.igb.visualization.util.CanvasUtils.exponentialScaleTransform;
import static org.lorainelab.igb.visualization.util.CanvasUtils.invertExpScaleTransform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true, provide = HorizontalZoomSlider.class)
public class HorizontalZoomSlider extends Slider {

    private static final Logger LOG = LoggerFactory.getLogger(HorizontalZoomSlider.class);
    private boolean ignoreHSliderEvent;
    private double lastHSliderFire;
    private CanvasModel canvasModel;
    private CanvasRegion canvasRegion;
    private double xFactor;

    public HorizontalZoomSlider() {
        HBox.setHgrow(this, Priority.ALWAYS);
        setValue(0);
        setMin(0);
        setMax(100);
        setBlockIncrement(2);
        setMajorTickUnit(1);
        setMinorTickCount(0);
        setSnapToTicks(true);
        ignoreHSliderEvent = false;
        lastHSliderFire = -1;
        xFactor = 1;
    }

    @Activate
    public void activate() {
        valueProperty().bindBidirectional(canvasModel.gethSlider());
        valuePropertyChangeListener = (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            if (!ignoreHSliderEvent) {
                final boolean isSnapEvent = newValue.doubleValue() % getMajorTickUnit() == 0;
                if (lastHSliderFire < 0 || Math.abs(lastHSliderFire - newValue.doubleValue()) > 1 || isSnapEvent) {
                    xFactor = exponentialScaleTransform(canvasRegion.getWidth(), canvasModel.getModelWidth().get(), newValue.doubleValue());
                    lastHSliderFire = newValue.doubleValue();
                    canvasModel.setxFactor(xFactor);
                }
            }
        };
        valueProperty().addListener(new WeakChangeListener<>(valuePropertyChangeListener));
        xFactorChangeListener = (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            if (xFactor != newValue.doubleValue()) {
                double updatedHsliderPosition = invertExpScaleTransform(canvasRegion.getWidth(), canvasModel.getModelWidth().get(), newValue.doubleValue());
                ignoreHSliderEvent = true;
                setValue(updatedHsliderPosition);
                ignoreHSliderEvent = false;
                xFactor = newValue.doubleValue();
            }
        };
        canvasModel.getxFactor().addListener(new WeakChangeListener<>(xFactorChangeListener));
        widthPropertyChangeListener = (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            xFactor = exponentialScaleTransform(canvasRegion.getWidth(), canvasModel.getModelWidth().get(), valueProperty().doubleValue());
            canvasModel.setxFactor(xFactor);
        };
        canvasRegion.widthProperty().addListener(new WeakChangeListener<>(widthPropertyChangeListener));
    }
    private ChangeListener<Number> widthPropertyChangeListener;
    private ChangeListener<Number> xFactorChangeListener;
    private ChangeListener<Number> valuePropertyChangeListener;

    @Reference
    public void setCanvasModel(CanvasModel canvasModel) {
        this.canvasModel = canvasModel;
    }

    @Reference
    public void setCanvasRegion(CanvasRegion canvasRegion) {
        this.canvasRegion = canvasRegion;
    }

}
