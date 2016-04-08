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
public class MouseEvent {
    
    protected Point2D local;
    protected Point2D screen;

    public MouseEvent(Point2D local, Point2D screen) {
        this.local = local;
        this.screen = screen;
    }

    public Point2D getLocal() {
        return local;
    }

    public Point2D getScreen() {
        return screen;
    }
    
    
    
}
