package org.lorainelab.igb.visualization.model;

import javafx.geometry.Rectangle2D;

/**
 *
 * @author dcnorris
 */
public class JumpZoomEvent {

    private final Rectangle2D rect;
    private final TrackRenderer trackRenderer;

    public JumpZoomEvent(Rectangle2D rect, TrackRenderer trackRenderer) {
        this.rect = rect;
        this.trackRenderer = trackRenderer;
    }

    public Rectangle2D getRect() {
        return rect;
    }

    public TrackRenderer getTrackRenderer() {
        return trackRenderer;
    }

}
