package org.lorainelab.igb.visualization.ui;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import javafx.geometry.Orientation;
import javafx.scene.control.ScrollBar;
import org.lorainelab.igb.visualization.model.CanvasModel;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true, provide = VerticalScrollBar.class)
public class VerticalScrollBar extends ScrollBar {

    private CanvasModel canvasModel;
    private CanvasRegion canvasRegion;

    public VerticalScrollBar() {
        setOrientation(Orientation.VERTICAL);
        setBlockIncrement(.01);
        setMin(0);
        setMax(100);
        setVisibleAmount(100);
    }

    @Activate
    public void activate() {
//        visibleAmountProperty().bind(canvasRegion.heightProperty().multiply(canvasModel.getyFactor()));
        valueProperty().bindBidirectional(canvasModel.getScrollY());
//        canvasModel.getvSlider().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
//            final double visiblePercentage = 1 - (newValue.doubleValue() / 100);
//            double visibleAmount = visiblePercentage * 100;
//            visibleAmount = Math.round(visibleAmount * 10) / 10.0;
//            setVisibleAmount(visibleAmount);
//            double previousValuePercentage = getValue() / oldValue.doubleValue();
//            double updatedValuePercentage = getValue() / newValue.doubleValue();
//        });
    }
    
      @Reference
    public void setCanvasRegion(CanvasRegion canvasRegion) {
        this.canvasRegion = canvasRegion;
    }

    @Reference
    public void setCanvasModel(CanvasModel canvasModel) {
        this.canvasModel = canvasModel;
    }

}
