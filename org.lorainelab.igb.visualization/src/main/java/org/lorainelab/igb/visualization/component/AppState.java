/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.visualization.component;

import com.google.common.collect.Sets;
import java.util.Set;
import javafx.geometry.Point2D;
import org.lorainelab.igb.data.model.Chromosome;
import org.lorainelab.igb.data.model.DataSet;
import org.lorainelab.igb.data.model.GenomeVersion;
import org.lorainelab.igb.visualization.component.api.State;
import org.lorainelab.igb.visualization.model.TrackRenderer;

/**
 *
 * @author jeckstei
 */
public class AppState implements State {

    private double totalTrackHeight;
    private Set<TrackRenderer> trackRenderers;
    private double scrollX;
    private double scrollY;
    private double scrollYVisibleAmount;
    private double hSlider;
    private double vSlider;
    private GenomeVersion selectedGenomeVersion;
    private Set<DataSet> loadedDataSets;
    private Chromosome selectedChromosome;
    private double zoomStripeCoordinates;
    private Point2D mouseClickLocation;
    private Point2D localPoint;
    private Point2D screenPoint;
    private boolean mouseDragging;
    private double xFactor;
    private double yFactor;

    private static AppState instance;

    public AppState() {
        this.trackRenderers = Sets.newConcurrentHashSet();
        this.loadedDataSets = Sets.newConcurrentHashSet();
        zoomStripeCoordinates = -1;
        mouseDragging = false;
        this.xFactor = 1;
        this.yFactor = 1;
    }

    public double getxFactor() {
        return xFactor;
    }

    public AppState setxFactor(double xFactor) {
        this.xFactor = xFactor;
        return this;
    }

    public double getyFactor() {
        return yFactor;
    }

    public AppState setyFactor(double yFactor) {
        this.yFactor = yFactor;
        return this;
    }

    public Point2D getMouseClickLocation() {
        return mouseClickLocation;
    }

    public AppState setMouseClickLocation(Point2D mouseClickLocation) {
        this.mouseClickLocation = mouseClickLocation;
        return this;
    }

    public Point2D getLocalPoint() {
        return localPoint;
    }

    public AppState setLocalPoint(Point2D localPoint) {
        this.localPoint = localPoint;
        return this;
    }

    public Point2D getScreenPoint() {
        return screenPoint;
    }

    public AppState setScreenPoint(Point2D screenPoint) {
        this.screenPoint = screenPoint;
        return this;
    }

    public boolean isMouseDragging() {
        return mouseDragging;
    }

    public AppState setMouseDragging(boolean mouseDragging) {
        this.mouseDragging = mouseDragging;
        return this;
    }

    public double getZoomStripeCoordinates() {
        return zoomStripeCoordinates;
    }

    public AppState setZoomStripeCoordinates(double zoomStripeCoordinates) {
        this.zoomStripeCoordinates = zoomStripeCoordinates;
        return this;
    }

    public Chromosome getSelectedChromosome() {
        return selectedChromosome;
    }

    public AppState setSelectedChromosome(Chromosome selectedChromosome) {
        this.selectedChromosome = selectedChromosome;
        return this;
    }

    public Set<DataSet> getLoadedDataSets() {
        return loadedDataSets;
    }

    public AppState setLoadedDataSets(Set<DataSet> loadedDataSets) {
        this.loadedDataSets.clear();
        this.loadedDataSets.addAll(loadedDataSets);
        return this;
    }

    public GenomeVersion getSelectedGenomeVersion() {
        return selectedGenomeVersion;
    }

    public AppState setSelectedGenomeVersion(GenomeVersion selectedGenomeVersion) {
        this.selectedGenomeVersion = selectedGenomeVersion;
        return this;
    }

    public double getScrollX() {
        return scrollX;
    }

    public AppState setScrollX(double scrollX) {
        this.scrollX = scrollX;
        return this;
    }

    public double getScrollY() {
        return scrollY;
    }

    public AppState setScrollY(double scrollY) {
        this.scrollY = scrollY;
        return this;
    }

    public double getScrollYVisibleAmount() {
        return scrollYVisibleAmount;
    }

    public AppState setScrollYVisibleAmount(double scrollYVisibleAmount) {
        this.scrollYVisibleAmount = scrollYVisibleAmount;
        return this;
    }

    public double gethSlider() {
        return hSlider;
    }

    public AppState sethSlider(double hSlider) {
        this.hSlider = hSlider;
        return this;
    }

    public double getvSlider() {
        return vSlider;
    }

    public AppState setvSlider(double vSlider) {
        this.vSlider = vSlider;
        return this;
    }

    public double getTotalTrackHeight() {
        return totalTrackHeight;
    }

    public AppState setTotalTrackHeight(double totalTrackHeight) {
        this.totalTrackHeight = totalTrackHeight;
        return this;
    }

    public Set<TrackRenderer> getTrackRenderers() {
        return trackRenderers;
    }

    public AppState setTrackRenderers(Set<TrackRenderer> trackRenderers) {
        this.trackRenderers.clear();
        this.trackRenderers.addAll(trackRenderers);
        return this;
    }

}
