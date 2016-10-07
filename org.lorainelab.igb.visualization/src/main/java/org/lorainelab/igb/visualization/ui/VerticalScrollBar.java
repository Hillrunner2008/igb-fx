package org.lorainelab.igb.visualization.ui;

import aQute.bnd.annotation.component.Component;
import javafx.geometry.Orientation;
import javafx.scene.control.ScrollBar;
import org.lorainelab.igb.visualization.widget.LabelPane;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true, provide = VerticalScrollBar.class)
public class VerticalScrollBar extends ScrollBar {

    private LabelPane labelPane;

    public VerticalScrollBar() {
        setOrientation(Orientation.VERTICAL);
        setBlockIncrement(1);
    }

}
