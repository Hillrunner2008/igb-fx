/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.visualization.component;

import javafx.beans.property.DoubleProperty;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.Slider;
import org.lorainelab.igb.data.model.Chromosome;
import org.lorainelab.igb.data.model.GenomeVersion;
import org.lorainelab.igb.selections.SelectionInfoService;
import org.lorainelab.igb.visualization.CanvasPane;
import org.lorainelab.igb.visualization.component.api.Props;

/**
 *
 * @author jeckstei
 */
public class AppProps implements Props {

    private Slider hSlider;
    private DoubleProperty scrollX;
    private ScrollBar scrollY;
    private double zoomStripeCoordinate;
    private CanvasPane canvasPane;
    private SelectionInfoService selectionInfoService;
    private Chromosome selectedChromosome;
    private GenomeVersion selectedGenomeVersion;
    private Slider vSlider;
    private double totalTrackHeight;

    public AppProps(Slider hslider, DoubleProperty scrollX, ScrollBar scrollY,
            double zoomStripeCoordinate, CanvasPane canvasPane,
            SelectionInfoService selectionInfoService, Chromosome selectedChromosome,
            GenomeVersion selectedGenomeVersion, Slider vslider, double totalTrackHeight) {
        this.hSlider = hslider;
        this.scrollX = scrollX;
        this.scrollY = scrollY;
        this.zoomStripeCoordinate = zoomStripeCoordinate;
        this.canvasPane = canvasPane;
        this.selectionInfoService = selectionInfoService;
        this.selectedChromosome = selectedChromosome;
        this.selectedGenomeVersion = selectedGenomeVersion;
        this.vSlider = vslider;
        this.totalTrackHeight = totalTrackHeight;
    }

    public double getTotalTrackHeight() {
        return totalTrackHeight;
    }

    public void setTotalTrackHeight(double totalTrackHeight) {
        this.totalTrackHeight = totalTrackHeight;
    }

    public Slider getvSlider() {
        return vSlider;
    }

    public void setvSlider(Slider vSlider) {
        this.vSlider = vSlider;
    }

    public GenomeVersion getSelectedGenomeVersion() {
        return selectedGenomeVersion;
    }

    public void setSelectedGenomeVersion(GenomeVersion selectedGenomeVersion) {
        this.selectedGenomeVersion = selectedGenomeVersion;
    }

    public Chromosome getSelectedChromosome() {
        return selectedChromosome;
    }

    public void setSelectedChromosome(Chromosome selectedChromosome) {
        this.selectedChromosome = selectedChromosome;
    }

    public SelectionInfoService getSelectionInfoService() {
        return selectionInfoService;
    }

    public void setSelectionInfoService(SelectionInfoService selectionInfoService) {
        this.selectionInfoService = selectionInfoService;
    }

    public CanvasPane getCanvasPane() {
        return canvasPane;
    }

    public void setCanvasPane(CanvasPane canvasPane) {
        this.canvasPane = canvasPane;
    }

    public double getZoomStripeCoordinate() {
        return zoomStripeCoordinate;
    }

    public void setZoomStripeCoordinate(double zoomStripeCoordinate) {
        this.zoomStripeCoordinate = zoomStripeCoordinate;
    }

    public Slider gethSlider() {
        return hSlider;
    }

    public void sethSlider(Slider hSlider) {
        this.hSlider = hSlider;
    }

    public DoubleProperty getScrollX() {
        return scrollX;
    }

    public void setScrollX(DoubleProperty scrollX) {
        this.scrollX = scrollX;
    }

    public ScrollBar getScrollY() {
        return scrollY;
    }

    public void setScrollY(ScrollBar scrollY) {
        this.scrollY = scrollY;
    }

}
