/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.visualization.component;

import javafx.beans.property.DoubleProperty;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.Slider;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import org.controlsfx.control.PlusMinusSlider;
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
    private DoubleProperty hSliderWidget;
    private Rectangle slider;
    private Pane xSliderPane;
    private Rectangle leftSliderThumb;
    private Rectangle rightSliderThumb;
    private Pane labelPane;
    private Button loadDataButton;
    private Button loadSequenceButton;
    private PlusMinusSlider plusMinusSlider;

    public AppProps(Slider hslider, DoubleProperty scrollX, ScrollBar scrollY,
            double zoomStripeCoordinate, CanvasPane canvasPane,
            SelectionInfoService selectionInfoService, Chromosome selectedChromosome,
            GenomeVersion selectedGenomeVersion, Slider vslider, double totalTrackHeight,
            DoubleProperty hSliderWidget, Rectangle slider, Pane xSliderPane,
            Rectangle leftSliderThumb, Rectangle rightSliderThumb, Pane labelPane,
            Button loadDataButton, Button loadSequenceButton, PlusMinusSlider plusMinusSlider) {
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
        this.hSliderWidget = hSliderWidget;
        this.slider = slider;
        this.xSliderPane = xSliderPane;
        this.leftSliderThumb = leftSliderThumb;
        this.rightSliderThumb = rightSliderThumb;
        this.labelPane = labelPane;
        this.loadDataButton = loadDataButton;
        this.loadSequenceButton = loadSequenceButton;
        this.plusMinusSlider = plusMinusSlider;
    }

    public PlusMinusSlider getPlusMinusSlider() {
        return plusMinusSlider;
    }
    
    public Button getLoadDataButton() {
        return loadDataButton;
    }

    public Button getLoadSequenceButton() {
        return loadSequenceButton;
    }

    public Pane getLabelPane() {
        return labelPane;
    }

    public Pane getxSliderPane() {
        return xSliderPane;
    }

    public Rectangle getLeftSliderThumb() {
        return leftSliderThumb;
    }

    public Rectangle getRightSliderThumb() {
        return rightSliderThumb;
    }

    public Rectangle getSlider() {
        return slider;
    }

    public DoubleProperty gethSliderWidget() {
        return hSliderWidget;
    }

    public Slider gethSlider() {
        return hSlider;
    }

    public DoubleProperty getScrollX() {
        return scrollX;
    }

    public ScrollBar getScrollY() {
        return scrollY;
    }

    public double getZoomStripeCoordinate() {
        return zoomStripeCoordinate;
    }

    public CanvasPane getCanvasPane() {
        return canvasPane;
    }

    public SelectionInfoService getSelectionInfoService() {
        return selectionInfoService;
    }

    public Chromosome getSelectedChromosome() {
        return selectedChromosome;
    }

    public GenomeVersion getSelectedGenomeVersion() {
        return selectedGenomeVersion;
    }

    public Slider getvSlider() {
        return vSlider;
    }

    public double getTotalTrackHeight() {
        return totalTrackHeight;
    }

}
