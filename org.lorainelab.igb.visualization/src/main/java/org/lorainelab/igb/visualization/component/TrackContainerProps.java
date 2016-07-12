/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.visualization.component;

import java.util.Set;
import javafx.scene.layout.Pane;
import org.lorainelab.igb.data.model.Chromosome;
import org.lorainelab.igb.data.model.DataSet;
import org.lorainelab.igb.visualization.CanvasPane;
import org.lorainelab.igb.visualization.component.api.Props;
import org.lorainelab.igb.visualization.model.TrackRenderer;

/**
 *
 * @author jeckstei
 */
public class TrackContainerProps implements Props {

    private TrackRenderer trackRenderer;
    private double scrollX;
    private double scrollY;
    private double hSlider;
    private double vSlider;
    private CanvasPane canvasPane;
    private Set<DataSet> loadedDataSets;
    private Chromosome selectedChromosome;
    private Pane labelPane;
    private double zoomStripeCoordinate;

    public TrackContainerProps(TrackRenderer trackRenderer, double scrollX,
            double scrollY, double hSlider, double vSlider,
            CanvasPane canvasPane, Set<DataSet> loadedDataSets, 
            Chromosome selectedChromosome, Pane labelPane,
            double zoomStripeCoordinate) {
        this.trackRenderer = trackRenderer;
        this.scrollX = scrollX;
        this.scrollY = scrollY;
        this.hSlider = hSlider;
        this.vSlider = vSlider;
        this.canvasPane = canvasPane;
        this.loadedDataSets = loadedDataSets;
        this.selectedChromosome = selectedChromosome;
        this.labelPane = labelPane;
        this.zoomStripeCoordinate = zoomStripeCoordinate;
    }

    public double getZoomStripeCoordinate() {
        return zoomStripeCoordinate;
    }

    public Pane getLabelPane() {
        return labelPane;
    }

    public TrackRenderer getTrackRenderer() {
        return trackRenderer;
    }

    public Set<DataSet> getLoadedDataSets() {
        return loadedDataSets;
    }

    public double getScrollX() {
        return scrollX;
    }

    public double getScrollY() {
        return scrollY;
    }

    public double gethSlider() {
        return hSlider;
    }

    public CanvasPane getCanvasPane() {
        return canvasPane;
    }

    public double getvSlider() {
        return vSlider;
    }

    public Chromosome getSelectedChromosome() {
        return selectedChromosome;
    }


}
