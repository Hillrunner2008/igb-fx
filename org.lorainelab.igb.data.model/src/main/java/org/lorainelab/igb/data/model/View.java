package org.lorainelab.igb.data.model;

import javafx.geometry.Rectangle2D;

/**
 *
 * @author dcnorris
 */
public class View {

    private Rectangle2D boundingRect;
    private double xfactor = 1;
    private double yfactor = 1;

    public View(Rectangle2D boundingRect) {
        this.boundingRect = boundingRect;
    }

    public Rectangle2D getBoundingRect() {
        return boundingRect;
    }

    public void setBoundingRect(Rectangle2D boundingRect) {
        this.boundingRect = boundingRect;
    }

    public double getXfactor() {
        return xfactor;
    }

    public void setXfactor(double xfactor) {
        this.xfactor = xfactor;
    }

    public double getYfactor() {
        return yfactor;
    }

    public void setYfactor(double yfactor) {
        this.yfactor = yfactor;
    }

}
