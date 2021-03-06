/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.data.model;

import com.google.common.base.Preconditions;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
public class CanvasContext {

    private static final Logger LOG = LoggerFactory.getLogger(CanvasContext.class);
    private final Canvas canvas;
    private volatile Rectangle2D boundingRectangle;
    private double relativeTrackOffset;
    private boolean isVisible;
    private double trackHeight;

    public CanvasContext(Canvas canvas, double trackHeight, double relativeTrackOffset) {
        this.canvas = canvas;
        this.boundingRectangle = new Rectangle2D(0, relativeTrackOffset, canvas.getWidth(), trackHeight);
        this.trackHeight = trackHeight;
        this.relativeTrackOffset = relativeTrackOffset;
        isVisible = false;
    }

    public Rectangle2D getBoundingRect() {
        return boundingRectangle;
    }

    public GraphicsContext getGraphicsContext() {
        return canvas.getGraphicsContext2D();
    }

    public double getRelativeTrackOffset() {
        return relativeTrackOffset;
    }

    public void setIsVisible(boolean isVisible) {
        this.isVisible = isVisible;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public double getTrackHeight() {
        return trackHeight;
    }

    public void update(Rectangle2D boundingRectangle, double trackHeight, double relativeTrackOffset) {
        Preconditions.checkArgument(trackHeight >= 50, "Invalid Track Height detected");
        this.boundingRectangle = boundingRectangle;
        this.relativeTrackOffset = relativeTrackOffset;
        this.trackHeight = trackHeight;
    }
}
