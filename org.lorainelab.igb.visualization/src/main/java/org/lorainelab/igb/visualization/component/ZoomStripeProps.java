/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.visualization.component;

import javafx.scene.canvas.Canvas;
import org.lorainelab.igb.visualization.component.api.Props;

/**
 *
 * @author jeckstei
 */
public class ZoomStripeProps implements Props {
    private Canvas canvas;
    private double zoomStripeCoordinate;
    private double xFactor;
    private double xOffset;
    private double canvasWidth;
    private double modelWidth;
    private double modelHeight;
    private double scrollX;

    public ZoomStripeProps(Canvas canvas, double zoomStripeCoordinate,
            double xFactor, double xOffset, double canvasWidth,
            double modelWidth, double modelHeight, double scrollX) {
        this.canvas = canvas;
        this.zoomStripeCoordinate = zoomStripeCoordinate;
        this.xFactor = xFactor;
        this.xOffset = xOffset;
        this.canvasWidth = canvasWidth;
        this.modelWidth = modelWidth;
        this.modelHeight = modelHeight;
        this.scrollX = scrollX;
    }

    public double getScrollX() {
        return scrollX;
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public double getxFactor() {
        return xFactor;
    }

    public double getxOffset() {
        return xOffset;
    }
    
    public double getZoomStripeCoordinate() {
        return zoomStripeCoordinate;
    }

    public double getCanvasWidth() {
        return canvasWidth;
    }

    public double getModelWidth() {
        return modelWidth;
    }

    public double getModelHeight() {
        return modelHeight;
    }
    
    
    
}
