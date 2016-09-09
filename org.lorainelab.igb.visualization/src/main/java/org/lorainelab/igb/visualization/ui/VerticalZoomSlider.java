package org.lorainelab.igb.visualization.ui;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.Slider;
import org.lorainelab.igb.visualization.model.CanvasModel;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true, provide = VerticalZoomSlider.class)
public class VerticalZoomSlider extends Slider {

    private CanvasModel canvasModel;

    public VerticalZoomSlider() {
        setOrientation(Orientation.VERTICAL);
        setPrefWidth(15);
        setRotate(180);
        setPadding(new Insets(15, 0, 15, 0));
        setMin(0.1);
        setValue(0);
        setMax(100);
    }
    
    @Activate
    public void activate() {
        valueProperty().bindBidirectional(canvasModel.getvSlider());
    }

    @Reference
    public void setCanvasModel(CanvasModel canvasModel) {
        this.canvasModel = canvasModel;
    }

}
