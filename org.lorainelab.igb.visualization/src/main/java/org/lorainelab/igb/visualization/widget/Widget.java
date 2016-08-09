package org.lorainelab.igb.visualization.widget;

import org.lorainelab.igb.visualization.model.CanvasModel;

/**
 *
 * @author dcnorris
 */
public interface Widget {
    
    int getZindex();

    void render(CanvasModel canvasModel);

}
