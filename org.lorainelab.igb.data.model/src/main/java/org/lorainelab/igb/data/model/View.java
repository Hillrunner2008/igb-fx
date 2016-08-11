package org.lorainelab.igb.data.model;

import com.google.common.collect.Range;
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
    private Rectangle2D boundingRect;
    private Range<Double> xRange;
    private double xfactor = 1;
    private double yfactor = 1;

    public View(Rectangle2D boundingRect, Chromosome chromosome) {
        this.boundingRect = boundingRect;
        this.chromosome = chromosome;
        xRange = Range.closed(boundingRect.getMinX(), boundingRect.getMaxX());
    }

    public Rectangle2D getBoundingRect() {
        return boundingRect;
    }

    public Range<Double> getXrange() {
        return xRange;
    }
    
    public void setBoundingRect(Rectangle2D boundingRect) {
        this.boundingRect = boundingRect;
        xRange = Range.closed(boundingRect.getMinX(), boundingRect.getMaxX());
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
