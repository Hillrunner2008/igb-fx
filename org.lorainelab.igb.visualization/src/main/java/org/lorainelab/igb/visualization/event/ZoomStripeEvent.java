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
public class ZoomStripeEvent {

    double zoomStripeCoordinate;

    public ZoomStripeEvent(double zoomStripeCoordinate) {
        this.zoomStripeCoordinate = zoomStripeCoordinate;
    }

    public double getZoomStripeCoordinate() {
        return zoomStripeCoordinate;
    }

}
