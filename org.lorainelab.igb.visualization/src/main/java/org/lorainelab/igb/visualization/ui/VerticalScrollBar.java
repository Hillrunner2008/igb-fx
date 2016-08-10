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

    public VerticalScrollBar() {
        setOrientation(Orientation.VERTICAL);
        setBlockIncrement(.01);
        setMin(0);
        setMax(100);
        setVisibleAmount(100);
    }

    @Activate
    public void activate() {
        valueProperty().bindBidirectional(canvasModel.getScrollY());
        visibleAmountProperty().bindBidirectional(canvasModel.getScrollYVisibleAmount());
    }

    @Reference
    public void setCanvasModel(CanvasModel canvasModel) {
        this.canvasModel = canvasModel;
    }

}
