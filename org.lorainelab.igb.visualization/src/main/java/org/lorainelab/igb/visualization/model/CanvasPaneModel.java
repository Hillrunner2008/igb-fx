package org.lorainelab.igb.visualization.model;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import com.google.common.collect.Range;
import java.util.Optional;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import org.lorainelab.igb.data.model.Chromosome;
import org.lorainelab.igb.selections.SelectionInfoService;
import org.lorainelab.igb.visualization.ui.CanvasRegion;
import static org.lorainelab.igb.visualization.util.BoundsUtil.enforceRangeBounds;
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
    private SelectionInfoService selectionInfoService;
    private CanvasRegion canvasRegion;
    //TODO consider moving mouse related content to separate model
    private ObjectProperty<Optional<Point2D>> mouseClickLocation;
    private ObjectProperty<Optional<Point2D>> localPoint;
    private ObjectProperty<Optional<Point2D>> screenPoint;
    private ObjectProperty<Optional<Rectangle2D>> selectionRectangle;
    private boolean mouseDragging;
    private BooleanProperty multiSelectModeActive;

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
        mouseClickLocation = new SimpleObjectProperty<>(Optional.empty());
        localPoint = new SimpleObjectProperty<>(Optional.empty());
        screenPoint = new SimpleObjectProperty<>(Optional.empty());
        selectionRectangle = new SimpleObjectProperty<>(Optional.empty());
        mouseDragging = false;
        multiSelectModeActive = new SimpleBooleanProperty(false);

    }

    @Activate
    public void activate() {
        xFactor.addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            final double previousVisibleCoordinatesX = Math.floor(canvasRegion.getWidth() / oldValue.doubleValue());
            final double updatedVisibleCoordinatesX = Math.floor(canvasRegion.getWidth() / newValue.doubleValue());
            visibleVirtualCoordinatesX.setValue(updatedVisibleCoordinatesX);
            updateScrollXPosition(previousVisibleCoordinatesX);
        });
        selectionInfoService.getSelectedChromosome().addListener((ObservableValue<? extends Optional<Chromosome>> observable, Optional<Chromosome> oldValue, Optional<Chromosome> newValue) -> {
            newValue.ifPresent(selectedChromosome -> {
                Platform.runLater(() -> {
                    modelWidth.set(selectedChromosome.getLength());
                    resetPositionalState();
                });
            });
        });
        scrollX.addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            double xOffset = Math.round((scrollX.get() / 100) * (modelWidth.get() - visibleVirtualCoordinatesX.get()));
            xOffset = enforceRangeBounds(xOffset, 0, modelWidth.get());
            this.xOffset.set(xOffset);
        });
    }

    // this method corrects the scrollX position if zoom stripe centering is required
    private void updateScrollXPosition(final double previousVisibleCoordinatesX) {
        double zoomStripeCoordinate = this.zoomStripeCoordinate.doubleValue();
        if (zoomStripeCoordinate != -1) {
            double zoomStripePositionPercentage = (zoomStripeCoordinate - xOffset.doubleValue()) / previousVisibleCoordinatesX;
            double xOffset = Math.max(zoomStripeCoordinate - (visibleVirtualCoordinatesX.doubleValue() * zoomStripePositionPercentage), 0);
            double maxXoffset = modelWidth.doubleValue() - visibleVirtualCoordinatesX.doubleValue();
            xOffset = Math.min(maxXoffset, xOffset);
            if (maxXoffset > 0) {
                this.xOffset.set(xOffset);
                scrollX.set((xOffset / (maxXoffset)) * 100);
            } else {
                this.xOffset.set(0);
                scrollX.set(0);
            }
        } else {
            xOffset.setValue(Math.round(scrollX.get() / 100) * (modelWidth.get() - visibleVirtualCoordinatesX.get()));
        }
    }

    private void resetPositionalState() {
        xFactor.set(exponentialScaleTransform(canvasRegion.getWidth(), modelWidth.get(), 0));
        yFactor.set(1);
        resetZoomStripe();
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

    public void setZoomStripeCoordinate(double zoomStripeCoordinate) {
        this.zoomStripeCoordinate.set(zoomStripeCoordinate);
    }

    public ReadOnlyDoubleProperty getScrollX() {
        return scrollX;
    }

    public ReadOnlyDoubleProperty getVisibleVirtualCoordinatesX() {
        return visibleVirtualCoordinatesX;
    }

    public void setScrollX(double updatedScrollX, boolean resetZoomStripe) {
        if (resetZoomStripe) {
            resetZoomStripe();
        }
        scrollX.set(updatedScrollX);
    }

    public void resetZoomStripe() {
        zoomStripeCoordinate.set(-1);
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

    public ReadOnlyBooleanProperty isMultiSelectModeActive() {
        return multiSelectModeActive;
    }

    public void setMultiSelectModeActive(boolean multiSelectModeActive) {
        this.multiSelectModeActive.set(multiSelectModeActive);
    }

    public ObjectProperty<Optional<Point2D>> getMouseClickLocation() {
        return mouseClickLocation;
    }

    public ObjectProperty<Optional<Point2D>> getLocalPoint() {
        return localPoint;
    }

    public ObjectProperty<Optional<Point2D>> getScreenPoint() {
        return screenPoint;
    }

    public ObjectProperty<Optional<Rectangle2D>> getSelectionRectangle() {
        return selectionRectangle;
    }

    public void setSelectionRectangle(Rectangle2D selectionRectangle) {
        this.selectionRectangle.set(Optional.ofNullable(selectionRectangle));
    }

    public void setMouseClickLocation(Point2D mouseClickLocation) {
        this.mouseClickLocation.set(Optional.ofNullable(mouseClickLocation));
    }

    public void setLocalPoint(Point2D localPoint) {
        this.localPoint.set(Optional.ofNullable(localPoint));
    }

    public void setScreenPoint(Point2D screenPoint) {
        this.screenPoint.set(Optional.ofNullable(screenPoint));
    }

    public void setMouseDragging(boolean mouseDragging) {
        this.mouseDragging = mouseDragging;
    }

    public boolean isMouseDragging() {
        return mouseDragging;
    }

    @Reference
    public void setSelectionInfoService(SelectionInfoService selectionInfoService) {
        this.selectionInfoService = selectionInfoService;
    }

    @Reference
    public void setCanvasRegion(CanvasRegion canvasRegion) {
        this.canvasRegion = canvasRegion;
    }

    public void setxFactor(double xFactor) {
        this.xFactor.set(xFactor);
    }

    public void setScrollX(double scrollX) {
        this.scrollX.set(scrollX);
    }

}
