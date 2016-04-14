/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.data.model;

import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

/**
 *
 * @author dcnorris
 */
public class CanvasContext {

    private final Canvas canvas;
    private Rectangle2D boundingRectangle;
    private double trackHeight;
    private double relativeTrackOffset;
    private boolean isVisible;

    public CanvasContext(Canvas canvas, Rectangle2D boundingRectangle, double trackHeight, double relativeTrackOffset) {
        this.canvas = canvas;
        this.boundingRectangle = boundingRectangle;
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

    public double getTrackHeight() {
        return trackHeight;
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

    public void update(Rectangle2D boundingRectangle, double trackSize, double relativeTrackOffset) {
        this.boundingRectangle = boundingRectangle;
        this.trackHeight = trackSize;
        this.relativeTrackOffset = relativeTrackOffset;
    }
}
