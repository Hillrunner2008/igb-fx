package org.lorainelab.igb.visualization.ui;

import aQute.bnd.annotation.component.Component;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Region;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true, provide = OverlayRegion.class)
public class OverlayRegion extends Region {

    private final Canvas canvas;

    public OverlayRegion() {
        canvas = new Canvas();
        canvas.setMouseTransparent(true);
        setMouseTransparent(true);
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

    public void clear() {
        canvas.getGraphicsContext2D().clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    public Canvas getCanvas() {
        return canvas;
    }

}
