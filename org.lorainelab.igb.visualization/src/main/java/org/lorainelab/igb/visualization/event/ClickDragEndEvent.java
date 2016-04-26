/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.visualization.event;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;

/**
 *
 * @author jeckstei
 */
public class ClickDragEndEvent extends MouseEvent {

    private final Rectangle2D selectionRectangle;

    public ClickDragEndEvent(Point2D local, Point2D screen, Rectangle2D selectionRectangle) {
        super(local, screen);
        this.selectionRectangle = selectionRectangle;
    }

    public Rectangle2D getSelectionRectangle() {
        return selectionRectangle;
    }

}
