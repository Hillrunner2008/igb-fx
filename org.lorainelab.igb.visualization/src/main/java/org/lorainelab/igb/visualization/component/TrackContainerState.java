/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.visualization.component;

import com.google.common.collect.Sets;
import java.util.Set;
import org.lorainelab.igb.data.model.Chromosome;
import org.lorainelab.igb.data.model.DataSet;
import org.lorainelab.igb.visualization.component.api.State;
import org.lorainelab.igb.visualization.model.TrackRenderer;

/**
 *
 * @author jeckstei
 */
public class TrackContainerState implements State {
    private TrackRenderer trackRenderer;
    private double scrollX;
    private double scrollY;
    private double hSlider;
    private double vSlider;
    private Set<DataSet> loadedDataSets;
    private Chromosome selectedChromosome;
    private static TrackContainerState instance;

    public TrackContainerState() {
        loadedDataSets = Sets.newConcurrentHashSet();
    }
    
    public static TrackContainerState factory() {
        return new TrackContainerState();
    }

    public Chromosome getSelectedChromosome() {
        return selectedChromosome;
    }

    public TrackContainerState setSelectedChromosome(Chromosome selectedChromosome) {
        this.selectedChromosome = selectedChromosome;
        return this;
    }
    
    public TrackRenderer getTrackRenderer() {
        return trackRenderer;
    }

    public TrackContainerState setTrackRenderer(TrackRenderer trackRenderer) {
        this.trackRenderer = trackRenderer;
        return this;
    }

    public double getScrollX() {
        return scrollX;
    }

    public TrackContainerState setScrollX(double scrollX) {
        this.scrollX = scrollX;
        return this;
    }

    public double getScrollY() {
        return scrollY;
    }

    public TrackContainerState setScrollY(double scrollY) {
        this.scrollY = scrollY;
        return this;
    }

    public double gethSlider() {
        return hSlider;
    }

    public TrackContainerState sethSlider(double hSlider) {
        this.hSlider = hSlider;
        return this;
    }

    public double getvSlider() {
        return vSlider;
    }

    public TrackContainerState setvSlider(double vSlider) {
        this.vSlider = vSlider;
        return this;
    }

    public Set<DataSet> getLoadedDataSets() {
        return loadedDataSets;
    }

    public TrackContainerState setLoadedDataSets(Set<DataSet> loadedDataSets) {
        this.loadedDataSets.clear();
        this.loadedDataSets.addAll(loadedDataSets);
        return this;
    }

    
    
    public static TrackContainerState getInstance() {
        return instance;
    }

    
    public static void setInstance(TrackContainerState instance) {
        TrackContainerState.instance = instance;
    }
    
    
    
}
