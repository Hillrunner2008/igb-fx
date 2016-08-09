package org.lorainelab.igb.visualization.component;

import org.lorainelab.igb.visualization.model.CanvasPaneModel;

/**
 *
 * @author dcnorris
 */
public interface Widget {
    
    int getZindex();

    void render(CanvasPaneModel canvasPaneModel);

}
