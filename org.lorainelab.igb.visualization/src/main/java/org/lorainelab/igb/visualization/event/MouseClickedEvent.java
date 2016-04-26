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
public class MouseClickedEvent extends MouseEvent {

    private final boolean multiSelectModeActive;

    public MouseClickedEvent(Point2D local, Point2D screen, boolean multiSelectModeActive) {
        super(local, screen);
        this.multiSelectModeActive = multiSelectModeActive;
    }

    public boolean isMultiSelectModeActive() {
        return multiSelectModeActive;
    }

}
