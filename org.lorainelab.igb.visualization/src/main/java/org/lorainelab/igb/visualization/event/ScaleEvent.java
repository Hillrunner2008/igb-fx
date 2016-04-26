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
public class ScaleEvent {

    private final double scaleX;
    private final double scaleY;
    private final double scrollX;
    private final double scrollY;

    public ScaleEvent(double scaleX, double scaleY, double scrollX, double scrollY) {
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.scrollX = scrollX;
        this.scrollY = scrollY;
    }

    public double getScaleX() {
        return scaleX;
    }

    public double getScaleY() {
        return scaleY;
    }

    public double getScrollX() {
        return scrollX;
    }

    public double getScrollY() {
        return scrollY;
    }

}
