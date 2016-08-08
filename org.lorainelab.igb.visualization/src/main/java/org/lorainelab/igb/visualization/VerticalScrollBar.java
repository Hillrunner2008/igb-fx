package org.lorainelab.igb.visualization;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import javafx.geometry.Orientation;
import javafx.scene.control.ScrollBar;
import org.lorainelab.igb.visualization.model.CanvasPaneModel;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true, provide = VerticalScrollBar.class)
public class VerticalScrollBar extends ScrollBar {

    private CanvasPaneModel canvasPaneModel;

    public VerticalScrollBar() {
        setOrientation(Orientation.VERTICAL);
        setBlockIncrement(.01);
        setMin(0);
        setMax(100);
        setVisibleAmount(100);
    }

    @Activate
    public void activate() {
        valueProperty().bindBidirectional(canvasPaneModel.getScrollY());
        visibleAmountProperty().bindBidirectional(canvasPaneModel.getScrollYVisibleAmount());
    }

    @Reference
    public void setCanvasPaneModel(CanvasPaneModel canvasPaneModel) {
        this.canvasPaneModel = canvasPaneModel;
    }

}
