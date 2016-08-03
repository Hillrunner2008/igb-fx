package org.lorainelab.igb.visualization;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.lorainelab.igb.visualization.model.CanvasPaneModel;
import static org.lorainelab.igb.visualization.util.CanvasUtils.exponentialScaleTransform;
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
    private CanvasPaneModel canvasPaneModel;
    private PrimaryCanvasRegion primaryCanvasRegion;

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
    }

    @Activate
    public void activate() {
        valueProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            if (ignoreHSliderEvent) {
                ignoreHSliderEvent = false;
                return;
            }
            final boolean isSnapEvent = newValue.doubleValue() % getMajorTickUnit() == 0;
            if (lastHSliderFire < 0 || Math.abs(lastHSliderFire - newValue.doubleValue()) > 1 || isSnapEvent) {
                double xFactor = exponentialScaleTransform(primaryCanvasRegion.getWidth(), canvasPaneModel.getModelWidth().get(), newValue.doubleValue());
                lastHSliderFire = newValue.doubleValue();
                canvasPaneModel.getxFactor().set(xFactor);
            }
        });
    }

    @Reference
    public void setCanvasPaneModel(CanvasPaneModel canvasPaneModel) {
        this.canvasPaneModel = canvasPaneModel;
    }

    @Reference
    public void setPrimaryCanvasRegion(PrimaryCanvasRegion primaryCanvasRegion) {
        this.primaryCanvasRegion = primaryCanvasRegion;
    }

}
