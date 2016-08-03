package org.lorainelab.igb.visualization;

import aQute.bnd.annotation.component.Component;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true, provide = PrimaryCanvasRegion.class)
public class PrimaryCanvasRegion extends Region {

    private final Canvas canvas;
    private boolean multiSelectModeActive;

    public PrimaryCanvasRegion() {
        canvas = new Canvas();
        canvas.setFocusTraversable(true);
        canvas.addEventFilter(MouseEvent.ANY, (e) -> canvas.requestFocus());
        initailizeKeyListener();
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

    public boolean isMultiSelectModeActive() {
        return multiSelectModeActive;
    }

    private void initailizeKeyListener() {
        canvas.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                switch (event.getCode()) {
                    case CONTROL:
                    case SHIFT:
                        multiSelectModeActive = true;
                        break;
                }
            }
        });

        canvas.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                switch (event.getCode()) {
                    case CONTROL:
                    case SHIFT:
                        multiSelectModeActive = false;
                        break;
                }
            }
        });
    }

}
