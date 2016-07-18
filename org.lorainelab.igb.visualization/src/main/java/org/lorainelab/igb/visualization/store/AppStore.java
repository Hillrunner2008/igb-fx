/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.visualization.store;

import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javafx.geometry.Point2D;
import org.lorainelab.igb.data.model.Chromosome;
import org.lorainelab.igb.data.model.DataSet;
import org.lorainelab.igb.data.model.GenomeVersion;
import org.lorainelab.igb.visualization.model.TrackRenderer;

/**
 *
 * @author jeckstei
 */
public class AppStore {

    private final EventBus bus;
    private double scrollX;
    private double scrollY;
    private double scrollYVisibleAmount;
    private double hSlider;
    private double vSlider;
    private final Set<TrackRenderer> trackRenderers;
    private final Set<DataSet> loadedDataSets;
    private GenomeVersion selectedGenomeVersion;
    private Chromosome selectedChromosome;
    private double zoomStripeCoordinate;
    private Point2D mouseClickLocation;
    private Point2D localPoint;
    private Point2D screenPoint;
    private boolean mouseDragging;

    public AppStore() {
        mouseDragging = false;
        zoomStripeCoordinate = -1;
        trackRenderers = Sets.newConcurrentHashSet();
        loadedDataSets = Sets.newConcurrentHashSet();
        bus = new EventBus();
    }

    private static AppStore instance;
    
    public void subscribe(Object object) {
        this.bus.register(object);
    }
    
    public void unsubscribe(Object object) {
        this.bus.unregister(object);
    }
    
    public void emit() {
        this.bus.post(new AppStoreEvent());
    }

    public static AppStore getStore() {
        if (instance == null) {
            instance = new AppStore();
        }
        return instance;
    }

    public void setSelectedGenomeVersion(GenomeVersion selectedGenomeVersion) {
        this.selectedGenomeVersion = selectedGenomeVersion;
        emit();
    }

    public Point2D getMouseClickLocation() {
        return mouseClickLocation;
    }

    public Point2D getLocalPoint() {
        return localPoint;
    }

    public Point2D getScreenPoint() {
        return screenPoint;
    }

    public boolean isMouseDragging() {
        return mouseDragging;
    }

    public void setSelectedChromosome(Chromosome selectedChromosome) {
        this.selectedChromosome = selectedChromosome;
        emit();
    }

    public void clearTrackRenderers() {
        trackRenderers.clear();
        emit();
    }

    public void clearDataSets() {
        loadedDataSets.clear();
        emit();
    }

    public void addDataSet(DataSet... dataSets) {
        this.loadedDataSets.addAll(Arrays.asList(dataSets));
        emit();
    }

    public void addTrackRenderer(TrackRenderer... trackRenderers) {
        this.trackRenderers.addAll(Arrays.asList(trackRenderers));
        emit();
    }
    
    public void updateMouseDraggedLocation(Point2D localPoint, Point2D screenPoint, boolean mouseDragging) {
        this.localPoint = localPoint; 
        this.screenPoint = screenPoint;
        this.mouseDragging = mouseDragging;
        emit();
    }
    
    public void updateMouseClickedLocation(Point2D mouseClickLocation) {
        this.mouseClickLocation = mouseClickLocation;
        emit();
    }
    
    public void updateTrackRenderer(List<DataSet> dataSets, List<TrackRenderer> trackRenderers) {
        this.loadedDataSets.addAll(dataSets);
        this.trackRenderers.addAll(trackRenderers);
        emit();
    }

    public void update(double scrollX, double scrollY, double hSlider, 
            double vSlider, double scrollYVisibleAmount, 
            boolean clearTrackRenderers, GenomeVersion selectedGenomeVersion,
            Chromosome selectedChromosome, Optional<TrackRenderer> trackRenderer) {
        if (clearTrackRenderers) {
            trackRenderers.clear();
        }
        this.scrollX = scrollX;
        this.scrollY = scrollY;
        this.hSlider = hSlider;
        this.vSlider = vSlider;
        this.scrollYVisibleAmount = scrollYVisibleAmount;
        this.selectedGenomeVersion = selectedGenomeVersion;
        this.selectedChromosome = selectedChromosome;
        trackRenderer.ifPresent(tr ->{
            this.trackRenderers.add(tr);
        });
        emit();
    }
    
    public void updateHSlider(double hSlider) {
        this.hSlider = hSlider;
        emit();
    }
    
    //TODO: This is bad practice
    public void noop() {
        emit();
    }

    public void update(double scrollYVisibleAmount) {
        this.scrollYVisibleAmount = scrollYVisibleAmount;
        emit();
    }
    
    public void updateZoomStripe(double zoomStripeCoordinate) {
        this.zoomStripeCoordinate = zoomStripeCoordinate;
        emit();
    }
    
    public void updateJumpZoom(double hSlider, double scrollX, Point2D localPoint, Point2D screenPoint, boolean mouseDragging) {
        this.hSlider = hSlider;
        this.scrollX = scrollX;
        this.zoomStripeCoordinate = -1;
        this.localPoint = localPoint; 
        this.screenPoint = screenPoint;
        this.mouseDragging = mouseDragging;
        emit();
    }
    
    // Getters

    public double getZoomStripeCoordinate() {
        return zoomStripeCoordinate;
    }

    public void setZoomStripeCoordinate(double zoomStripeCoordinate) {
        this.zoomStripeCoordinate = zoomStripeCoordinate;
    }
    
    public EventBus getBus() {
        return bus;
    }

    public double getScrollX() {
        return scrollX;
    }

    public double getScrollY() {
        return scrollY;
    }

    public double getScrollYVisibleAmount() {
        return scrollYVisibleAmount;
    }

    public double gethSlider() {
        return hSlider;
    }

    public double getvSlider() {
        return vSlider;
    }

    public Set<TrackRenderer> getTrackRenderers() {
        return trackRenderers;
    }

    public Set<DataSet> getLoadedDataSets() {
        return loadedDataSets;
    }

    public GenomeVersion getSelectedGenomeVersion() {
        return selectedGenomeVersion;
    }

    public Chromosome getSelectedChromosome() {
        return selectedChromosome;
    }

  
}
