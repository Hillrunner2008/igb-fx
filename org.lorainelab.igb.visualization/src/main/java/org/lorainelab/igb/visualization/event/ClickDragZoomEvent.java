/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.visualization.event;

/**
 *
 * @author jeckstei
 */
public class ClickDragZoomEvent {

    private final double startX;
    private final double endX;

    public ClickDragZoomEvent(double startX, double endX) {
        this.startX = startX;
        this.endX = endX;
    }

    public double getStartX() {
        return startX;
    }

    public double getEndX() {
        return endX;
    }

}
