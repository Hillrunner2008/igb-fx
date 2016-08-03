package org.lorainelab.igb.visualization;

import aQute.bnd.annotation.component.Component;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.Slider;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true, provide = VerticalZoomSlider.class)
public class VerticalZoomSlider extends Slider {

    public VerticalZoomSlider() {
        setOrientation(Orientation.VERTICAL);
        setPrefWidth(15);
        setRotate(180);
        setPadding(new Insets(15, 0, 15, 0));
        setMin(0);
        setValue(0);
        setMax(100);
    }

}
