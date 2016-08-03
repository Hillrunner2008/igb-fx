package org.lorainelab.igb.visualization;

import aQute.bnd.annotation.component.Component;
import javafx.geometry.Orientation;
import javafx.scene.control.ScrollBar;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true, provide = VerticalScrollBar.class)
public class VerticalScrollBar extends ScrollBar {

    public VerticalScrollBar() {
        setOrientation(Orientation.VERTICAL);
        setBlockIncrement(.01);
        setMin(0);
        setMax(100);
        setVisibleAmount(100);
    }

}
