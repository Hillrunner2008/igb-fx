/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.visualization.model;

/**
 * = TrackRenderer
 *
 */
public interface TrackRenderer {

    final int MAX_ZOOM_MODEL_COORDINATES_X = 85;
    final int MAX_ZOOM_MODEL_COORDINATES_Y = 50;

    String getTrackLabel();

    void render();

    void scaleCanvas(double xFactor, double scrollX, double scrollY);

    void updateView(double scrollX, double scrollY);

    CanvasContext getCanvasContext();

    View getView();

    int getModelWidth();

    double getModelHeight();

    /**
     *@return the weight that will be used for sorting tracks. Lower weights will be
     */
    int getWeight();

}
