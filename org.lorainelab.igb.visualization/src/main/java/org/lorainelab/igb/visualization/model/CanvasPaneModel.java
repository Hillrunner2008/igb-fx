package org.lorainelab.igb.visualization.model;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import com.google.common.collect.Range;
import java.util.Optional;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Point2D;
import org.lorainelab.igb.data.model.Chromosome;
import org.lorainelab.igb.selections.SelectionInfoService;
import org.lorainelab.igb.visualization.PrimaryCanvasRegion;
import static org.lorainelab.igb.visualization.util.CanvasUtils.exponentialScaleTransform;
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
    private DoubleProperty xOffset;
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
    private PrimaryCanvasRegion primaryCanvasRegion;

    public CanvasPaneModel() {
        modelWidth = new SimpleDoubleProperty(1);
        xFactor = new SimpleDoubleProperty(1);
        yFactor = new SimpleDoubleProperty(1);
        visibleVirtualCoordinatesX = new SimpleDoubleProperty();
        zoomStripeCoordinate = new SimpleDoubleProperty(-1);
        xOffset = new SimpleDoubleProperty(0);
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
            visibleVirtualCoordinatesX.setValue(Math.floor(primaryCanvasRegion.getWidth() / newValue.doubleValue()));
            updateScrollXPosition();
            xOffset.setValue(Math.round(scrollX.get() / 100) * (modelWidth.get() - visibleVirtualCoordinatesX.get()));
        });
        selectionInfoService.getSelectedChromosome().addListener((ObservableValue<? extends Optional<Chromosome>> observable, Optional<Chromosome> oldValue, Optional<Chromosome> newValue) -> {
            newValue.ifPresent(selectedChromosome -> {
                Platform.runLater(() -> {
                    modelWidth.set(selectedChromosome.getLength());
                    resetPositionalState();
                });
            });
        });

    }

    // this method corrects the scrollX position if zoom stripe centering is required
    private void updateScrollXPosition() {
        if (zoomStripeCoordinate.get() != -1) {
            double zoomStripePositionPercentage = (zoomStripeCoordinate.get() - xOffset.get()) / visibleVirtualCoordinatesX.get();
            double xOffset = Math.max(zoomStripeCoordinate.get() - (visibleVirtualCoordinatesX.get() * zoomStripePositionPercentage), 0);
            double maxXoffset = modelWidth.get() - visibleVirtualCoordinatesX.get();
            xOffset = Math.min(maxXoffset, xOffset);
            if (maxXoffset > 0) {
                scrollX.set((xOffset / (maxXoffset)) * 100);
            } else {
                scrollX.set(0);
            }
        }
    }

    private void resetPositionalState() {
        xFactor.set(exponentialScaleTransform(primaryCanvasRegion.getWidth(), modelWidth.get(), 0));
        yFactor.set(1);
        zoomStripeCoordinate.set(-1);
        scrollX.setValue(0);
        scrollY.setValue(0);
        scrollYVisibleAmount.setValue(100);
        hSlider.setValue(0);
        vSlider.setValue(0);
    }

    public Range<Integer> getCurrentModelCoordinatesInView() {
        return Range.closedOpen(xOffset.intValue(), xOffset.intValue() + visibleVirtualCoordinatesX.intValue());
    }

    public ReadOnlyDoubleProperty getModelWidth() {
        return modelWidth;
    }

    public ReadOnlyDoubleProperty getxFactor() {
        return xFactor;
    }

    public ReadOnlyDoubleProperty getyFactor() {
        return yFactor;
    }

    public ReadOnlyDoubleProperty getZoomStripeCoordinate() {
        return zoomStripeCoordinate;
    }

    public ReadOnlyDoubleProperty getScrollX() {
        return scrollX;
    }

    public ReadOnlyDoubleProperty getVisibleVirtualCoordinatesX() {
        return visibleVirtualCoordinatesX;
    }

    public void setScrollX(double updatedScrollX, boolean resetZoomStripe) {
        if (resetZoomStripe) {
            zoomStripeCoordinate.set(-1);
        }
        scrollX.set(updatedScrollX);
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

    @Reference
    public void setPrimaryCanvasRegion(PrimaryCanvasRegion primaryCanvasRegion) {
        this.primaryCanvasRegion = primaryCanvasRegion;
    }

    public void setxFactor(double xFactor) {
        this.xFactor.set(xFactor);
    }

    public void setScrollX(double scrollX) {
        this.scrollX.set(scrollX);
    }

}
