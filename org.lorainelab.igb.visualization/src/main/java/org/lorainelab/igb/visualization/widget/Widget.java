package org.lorainelab.igb.visualization.widget;

import org.lorainelab.igb.visualization.model.CanvasModel;

/**
 *
 * @author dcnorris
 */
public interface Widget {

    int getZindex();

    // will participate is overlay rendering lifecyle (e.g. selection rectangle rendering, coordinate track drag events)
    default boolean isOverlayWidget() {
        return false;
    }

    void render(CanvasModel canvasModel);

}
