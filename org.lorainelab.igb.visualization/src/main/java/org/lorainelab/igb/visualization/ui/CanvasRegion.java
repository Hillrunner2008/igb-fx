package org.lorainelab.igb.visualization.ui;

import aQute.bnd.annotation.component.Component;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true, provide = CanvasRegion.class)
public class CanvasRegion extends Region {

    private final Canvas canvas;

    public CanvasRegion() {
        canvas = new Canvas();
        canvas.setFocusTraversable(true);
        canvas.addEventFilter(MouseEvent.ANY, (e) -> canvas.requestFocus());
        getChildren().add(canvas);
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        final double width = getWidth();
        final double height = getHeight();
        final Insets insets = getInsets();
        final double contentX = insets.getLeft();
        final double contentY = insets.getTop();
        final double contentWith = Math.max(0, width - (insets.getLeft() + insets.getRight()));
        final double contentHeight = Math.max(0, height - (insets.getTop() + insets.getBottom()));
        canvas.relocate(contentX, contentY);
        canvas.setWidth(contentWith);
        canvas.setHeight(contentHeight);
    }

    public Canvas getCanvas() {
        return canvas;
    }

}
