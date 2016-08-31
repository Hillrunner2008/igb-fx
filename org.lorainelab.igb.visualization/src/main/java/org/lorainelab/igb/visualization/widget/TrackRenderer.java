/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.visualization.widget;

import java.util.Comparator;
import org.lorainelab.igb.data.model.CanvasContext;
import org.lorainelab.igb.data.model.View;
import org.lorainelab.igb.visualization.model.TrackLabel;

/**
 * = TrackRenderer
 *
 */
public interface TrackRenderer extends Widget {

    final Comparator<TrackRenderer> SORT_BY_WEIGHT = (TrackRenderer o1, TrackRenderer o2) -> Double.compare(o1.getWeight(), o2.getWeight());

    String getTrackLabelText();

    TrackLabel getTrackLabel();

    CanvasContext getCanvasContext();

    View getView();

    int getModelWidth();

    double getModelHeight();

    /**
     * @return the weight that will be used for sorting tracks. Lower weights
     * will be
     */
    int getWeight();

    void setWeight(int weight);
    
    boolean isHeightLocked();

}
