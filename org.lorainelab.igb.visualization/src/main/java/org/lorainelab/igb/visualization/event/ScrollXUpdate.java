package org.lorainelab.igb.visualization.event;

/**
 *
 * @author dcnorris
 */
public class ScrollXUpdate {

    private final double scrollX;

    public ScrollXUpdate(double scrollX) {
        this.scrollX = scrollX;
    }

    public double getScrollX() {
        return scrollX;
    }

}
