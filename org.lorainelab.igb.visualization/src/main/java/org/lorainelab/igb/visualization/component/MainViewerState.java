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
public class MainViewerState implements State {

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


    public MainViewerState() {
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

    public MainViewerState setxFactor(double xFactor) {
        this.xFactor = xFactor;
        return this;
    }

    public double getyFactor() {
        return yFactor;
    }

    public MainViewerState setyFactor(double yFactor) {
        this.yFactor = yFactor;
        return this;
    }

    public Point2D getMouseClickLocation() {
        return mouseClickLocation;
    }

    public MainViewerState setMouseClickLocation(Point2D mouseClickLocation) {
        this.mouseClickLocation = mouseClickLocation;
        return this;
    }

    public Point2D getLocalPoint() {
        return localPoint;
    }

    public MainViewerState setLocalPoint(Point2D localPoint) {
        this.localPoint = localPoint;
        return this;
    }

    public Point2D getScreenPoint() {
        return screenPoint;
    }

    public MainViewerState setScreenPoint(Point2D screenPoint) {
        this.screenPoint = screenPoint;
        return this;
    }

    public boolean isMouseDragging() {
        return mouseDragging;
    }

    public MainViewerState setMouseDragging(boolean mouseDragging) {
        this.mouseDragging = mouseDragging;
        return this;
    }

    public double getZoomStripeCoordinates() {
        return zoomStripeCoordinates;
    }

    public MainViewerState setZoomStripeCoordinates(double zoomStripeCoordinates) {
        this.zoomStripeCoordinates = zoomStripeCoordinates;
        return this;
    }

    public Chromosome getSelectedChromosome() {
        return selectedChromosome;
    }

    public MainViewerState setSelectedChromosome(Chromosome selectedChromosome) {
        this.selectedChromosome = selectedChromosome;
        return this;
    }

    public Set<DataSet> getLoadedDataSets() {
        return loadedDataSets;
    }

    public MainViewerState setLoadedDataSets(Set<DataSet> loadedDataSets) {
        this.loadedDataSets.clear();
        this.loadedDataSets.addAll(loadedDataSets);
        return this;
    }

    public GenomeVersion getSelectedGenomeVersion() {
        return selectedGenomeVersion;
    }

    public MainViewerState setSelectedGenomeVersion(GenomeVersion selectedGenomeVersion) {
        this.selectedGenomeVersion = selectedGenomeVersion;
        return this;
    }

    public double getScrollX() {
        return scrollX;
    }

    public MainViewerState setScrollX(double scrollX) {
        this.scrollX = scrollX;
        return this;
    }

    public double getScrollY() {
        return scrollY;
    }

    public MainViewerState setScrollY(double scrollY) {
        this.scrollY = scrollY;
        return this;
    }

    public double getScrollYVisibleAmount() {
        return scrollYVisibleAmount;
    }

    public MainViewerState setScrollYVisibleAmount(double scrollYVisibleAmount) {
        this.scrollYVisibleAmount = scrollYVisibleAmount;
        return this;
    }

    public double gethSlider() {
        return hSlider;
    }

    public MainViewerState sethSlider(double hSlider) {
        this.hSlider = hSlider;
        return this;
    }

    public double getvSlider() {
        return vSlider;
    }

    public MainViewerState setvSlider(double vSlider) {
        this.vSlider = vSlider;
        return this;
    }

    public double getTotalTrackHeight() {
        return totalTrackHeight;
    }

    public MainViewerState setTotalTrackHeight(double totalTrackHeight) {
        this.totalTrackHeight = totalTrackHeight;
        return this;
    }

    public Set<TrackRenderer> getTrackRenderers() {
        return trackRenderers;
    }

    public MainViewerState setTrackRenderers(Set<TrackRenderer> trackRenderers) {
        this.trackRenderers.clear();
        this.trackRenderers.addAll(trackRenderers);
        return this;
    }

}
