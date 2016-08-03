package org.lorainelab.igb.visualization.model;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import java.util.Optional;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Point2D;
import org.lorainelab.igb.data.model.Chromosome;
import org.lorainelab.igb.selections.SelectionInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true, provide = CanvasPaneModel.class)
public class CanvasPaneModel {

    private static final Logger LOG = LoggerFactory.getLogger(CanvasPaneModel.class);
    public static final int MAX_ZOOM_MODEL_COORDINATES_X = 120;
    public static final int MAX_ZOOM_MODEL_COORDINATES_Y = 50;
    private DoubleProperty modelWidth;
    private DoubleProperty xFactor;
    private DoubleProperty yFactor;
    private DoubleProperty visibleVirtualCoordinatesX;
    private DoubleProperty zoomStripeCoordinate;
    private DoubleProperty scrollX;
    private DoubleProperty scrollY;
    private DoubleProperty scrollYVisibleAmount;
    private DoubleProperty hSlider;
    private DoubleProperty vSlider;
    private Point2D mouseClickLocation;
    private Point2D localPoint;
    private Point2D screenPoint;
    private boolean mouseDragging;
    private SelectionInfoService selectionInfoService;

    public CanvasPaneModel() {
        modelWidth = new SimpleDoubleProperty(1);
        xFactor = new SimpleDoubleProperty(1);
        yFactor = new SimpleDoubleProperty(1);
        visibleVirtualCoordinatesX = new SimpleDoubleProperty();
        zoomStripeCoordinate = new SimpleDoubleProperty(-1);
        scrollX = new SimpleDoubleProperty(0);
        scrollY = new SimpleDoubleProperty(0);
        scrollYVisibleAmount = new SimpleDoubleProperty(100);
        hSlider = new SimpleDoubleProperty(0);
        vSlider = new SimpleDoubleProperty(0);
        mouseDragging = false;
    }

    @Activate
    public void activate() {
        xFactor.addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            LOG.info("Zooming {}", newValue.doubleValue());
        });
        selectionInfoService.getSelectedChromosome().addListener((ObservableValue<? extends Optional<Chromosome>> observable, Optional<Chromosome> oldValue, Optional<Chromosome> newValue) -> {
            newValue.ifPresent(selectedChromosome -> {
                modelWidth.set(selectedChromosome.getLength());
                resetPositionalState();
            });
        });
    }

    private void resetPositionalState() {
        xFactor.set(1);
        yFactor.set(1);
        zoomStripeCoordinate.set(-1);
        scrollX.setValue(0);
        scrollY.setValue(0);
        scrollYVisibleAmount.setValue(100);
        hSlider.setValue(0);
        vSlider.setValue(0);
    }

    public DoubleProperty getModelWidth() {
        return modelWidth;
    }

    public DoubleProperty getxFactor() {
        return xFactor;
    }

    public DoubleProperty getyFactor() {
        return yFactor;
    }

    public DoubleProperty getZoomStripeCoordinate() {
        return zoomStripeCoordinate;
    }

    public DoubleProperty getScrollX() {
        return scrollX;
    }

    public DoubleProperty getScrollY() {
        return scrollY;
    }

    public DoubleProperty getScrollYVisibleAmount() {
        return scrollYVisibleAmount;
    }

    public DoubleProperty gethSlider() {
        return hSlider;
    }

    public DoubleProperty getvSlider() {
        return vSlider;
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

    @Reference
    public void setSelectionInfoService(SelectionInfoService selectionInfoService) {
        this.selectionInfoService = selectionInfoService;
    }

}
