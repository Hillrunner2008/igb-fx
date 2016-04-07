/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.visualization.event;

import javafx.geometry.Point2D;

/**
 *
 * @author jeckstei
 */
public class MouseStationaryEvent {
    private final Point2D location;

    public MouseStationaryEvent(Point2D location) {
        this.location = location;
    }

    public Point2D getLocation() {
        return location;
    }
    
    
}
