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
public class ScrollScaleEvent {

    private final Direction direction;

    public ScrollScaleEvent(Direction direction) {
        this.direction = direction;
    }

    public Direction getDirection() {
        return direction;
    }

    public enum Direction {
        INCREMENT, DECREMENT;
    }
}
