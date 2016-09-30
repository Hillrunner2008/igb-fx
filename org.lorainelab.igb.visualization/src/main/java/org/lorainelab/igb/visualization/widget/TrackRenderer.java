/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.visualization.widget;

import java.util.Comparator;
import java.util.Optional;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import org.lorainelab.igb.data.model.CanvasContext;
import org.lorainelab.igb.data.model.Track;
import org.lorainelab.igb.data.model.View;
import org.lorainelab.igb.visualization.model.TrackLabel;

/**
 * = TrackRenderer
 *
 */
public interface TrackRenderer extends Widget {

    static final int DEFAULT_HEIGHT = 150;

    final Comparator<TrackRenderer> SORT_BY_WEIGHT = (TrackRenderer o1, TrackRenderer o2) -> Double.compare(o1.getWeight(), o2.getWeight());

    String getTrackLabelText();

    TrackLabel getTrackLabel();

    CanvasContext getCanvasContext();
    
    Optional<Track> getTrack();

    View getView();

    int getModelWidth();

    double getModelHeight();

    /**
     * @return the weight that will be used for sorting tracks. Lower weights
     * will be
     */
    int getWeight();

    void setWeight(int weight);

    ReadOnlyBooleanProperty heightLocked();

    DoubleProperty stretchDelta();

    DoubleProperty activeStretchDelta();

    default int getLabelHeight(double yFactor) {
        double height;
        if (heightLocked().get()) {
            height = getCanvasContext().getBoundingRect().getHeight();
        } else {
            height = (DEFAULT_HEIGHT + stretchDelta().doubleValue()) * yFactor;
            height = height + activeStretchDelta().doubleValue();
        }
        return (int) Math.max(50, height);
    }

}
