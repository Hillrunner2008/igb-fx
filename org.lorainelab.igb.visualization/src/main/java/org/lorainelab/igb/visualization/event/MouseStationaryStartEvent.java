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
public class MouseStationaryStartEvent extends MouseEvent implements MouseStationaryEvent  {

    public MouseStationaryStartEvent(Point2D local, Point2D screen) {
        super(local, screen);
    }
    
    public MouseStationaryStartEvent(MouseEvent mouseEvent) {
        super(mouseEvent.getLocal(), mouseEvent.getScreen());
    }
    
    
}
