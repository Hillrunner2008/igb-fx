package org.lorainelab.igb.visualization.ui;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import javafx.event.EventHandler;
import org.controlsfx.control.PlusMinusSlider;
import org.lorainelab.igb.visualization.model.CanvasModel;
import static org.lorainelab.igb.visualization.util.BoundsUtil.enforceRangeBounds;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true, provide = HorizontalPlusMinusSlider.class)
public class HorizontalPlusMinusSlider extends PlusMinusSlider {

    private CanvasModel canvasModel;

    public HorizontalPlusMinusSlider() {
    }

    @Activate
    public void activate() {
        setOnValueChanged(new EventHandler<PlusMinusEvent>() {
            @Override
            public void handle(PlusMinusEvent event) {
                if (canvasModel != null) {
                    final double updatedScrollXValue = getUpdatedScrollxValue(event.getValue());
                    double scrollX = canvasModel.getScrollX().get();
                    if (updatedScrollXValue != scrollX) {
                        canvasModel.setScrollX(updatedScrollXValue, true);
                    }
                }
            }
        });
    }

    private double getUpdatedScrollxValue(double eventValue) {
        double updatedScrollXValue = canvasModel.getScrollX().get() + getAdjustedScrollValue(eventValue);
        updatedScrollXValue = enforceRangeBounds(updatedScrollXValue, 0, 100);
        return updatedScrollXValue;
    }

    private double getAdjustedScrollValue(double value) {
        if (value < -0.8 || value > 0.8) {
            return value;
        } else if ((value < 0 && value > -0.1) || value < .1) {
            return value / 10000;
        } else if ((value < 0 && value > -0.2) || value < .2) {
            return value / 2000;
        }
        return value / 50;
    }

    @Reference(optional = true, unbind = "removeCanvasModel", dynamic = true)
    public void setCanvasModel(CanvasModel canvasModel) {
        this.canvasModel = canvasModel;
    }

    public void removeCanvasModel(CanvasModel canvasModel) {
        this.canvasModel = null;
    }

}
