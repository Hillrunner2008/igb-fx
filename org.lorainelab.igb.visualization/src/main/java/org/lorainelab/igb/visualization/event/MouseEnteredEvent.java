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
public class MouseEnteredEvent extends MouseEvent {

    public MouseEnteredEvent(Point2D local, Point2D screen) {
        super(local, screen);
    }
    
}
