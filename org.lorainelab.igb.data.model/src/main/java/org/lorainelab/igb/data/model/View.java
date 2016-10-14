package org.lorainelab.igb.data.model;

import com.google.common.collect.Range;
import java.awt.Rectangle;
import javafx.geometry.Rectangle2D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
public class View {

    private static final Logger LOG = LoggerFactory.getLogger(View.class);
    private Chromosome chromosome;
    private Rectangle2D modelCoordRect;
    private Rectangle.Double mutableCoordRect;
    private Range<Double> xRange;
    private double xfactor = 1;
    private double yfactor = 1;
    double xPixelsPerCoordinate;
    private double scrollYOffset;
    private final CanvasContext canvasContext;
    private final boolean isNegative;

    public View(Rectangle2D modelCoordRect, CanvasContext canvasContext, Chromosome chromosome, boolean isNegative) {
        this.modelCoordRect = modelCoordRect;
        this.mutableCoordRect = new Rectangle.Double(modelCoordRect.getMinX(), modelCoordRect.getMinY(), modelCoordRect.getWidth(), modelCoordRect.getHeight());
        this.chromosome = chromosome;
        this.canvasContext = canvasContext;
        this.isNegative = isNegative;
        xRange = Range.closed(modelCoordRect.getMinX(), modelCoordRect.getMaxX());
        xPixelsPerCoordinate = modelCoordRect.getWidth() / canvasContext.getBoundingRect().getWidth();
        scrollYOffset = canvasContext.getRelativeTrackOffset() / xfactor;
    }

    public Rectangle2D modelCoordRect() {
        return modelCoordRect;
    }

    public java.awt.geom.Rectangle2D.Double getMutableCoordRect() {
        return mutableCoordRect;
    }

    public Range<Double> getXrange() {
        return xRange;
    }

    public void setModelCoordRect(Rectangle2D boundingRect) {
        this.modelCoordRect = boundingRect;
        mutableCoordRect = new Rectangle.Double(boundingRect.getMinX(), boundingRect.getMinY(), boundingRect.getWidth(), boundingRect.getHeight());
        xRange = Range.closed(boundingRect.getMinX(), boundingRect.getMaxX());
        xPixelsPerCoordinate = boundingRect.getWidth() / canvasContext.getBoundingRect().getWidth();
        scrollYOffset = boundingRect.getMinY();
    }

    public CanvasContext getCanvasContext() {
        return canvasContext;
    }

    public double getXpixelsPerCoordinate() {
        return xPixelsPerCoordinate;
    }

    public double getScrollYOffset() {
        return scrollYOffset;
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

    public boolean isIsNegative() {
        return isNegative;
    }

    public void setYfactor(double yfactor) {
        if (Double.isFinite(yfactor)) {
            this.yfactor = yfactor;
        } else {
            LOG.info("shouldn't happen");
        }
    }

    public Chromosome getChromosome() {
        return chromosome;
    }

}
