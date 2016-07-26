/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.visualization.model;

import java.util.Comparator;
import javafx.geometry.Point2D;
import org.lorainelab.igb.data.model.CanvasContext;
import org.lorainelab.igb.data.model.View;

/**
 * = TrackRenderer
 *
 */
public interface TrackRenderer {

    final Comparator<TrackRenderer> SORT_BY_WEIGHT = (TrackRenderer o1, TrackRenderer o2) -> Double.compare(o1.getWeight(), o2.getWeight());

    final int MAX_ZOOM_MODEL_COORDINATES_X = 120;
    final int MAX_ZOOM_MODEL_COORDINATES_Y = 50;

    String getTrackLabelText();

    TrackLabel getTrackLabel();

    void scaleCanvas(double xFactor, double scrollX, double scrollY);

    void updateView(double scrollX, double scrollY);

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

    void clearCanvas();

    void setZoomStripeCoordinate(double zoomStripeCoordinate);

    void setLastMouseClickedPoint(Point2D point);
    void setIsMultiSelectModeActive(boolean multiSelectModeActive);

    void setLastMouseDragPoint(Point2D point);
    
    void setMouseDragging(boolean isMouseDragging);

}
