package org.lorainelab.igb.visualization.ui;

import aQute.bnd.annotation.component.Component;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true, provide = CanvasRegion.class)
public class CanvasRegion extends Region {

    private final Canvas canvas;
    static final Color DEFAULT_BG = Color.web("#323232");

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

    public void clear() {
        final GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.save();
        gc.setFill(DEFAULT_BG);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.restore();
    }

}
