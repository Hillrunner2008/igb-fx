/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.visualization.component;

import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import java.util.List;
import javafx.application.Platform;
import javafx.scene.layout.StackPane;
import org.lorainelab.igb.visualization.CanvasPane;
import org.lorainelab.igb.visualization.component.api.Component;
import org.lorainelab.igb.visualization.event.ScaleEvent;
import org.lorainelab.igb.visualization.model.TrackLabel;
import static org.lorainelab.igb.visualization.util.CanvasUtils.exponentialScaleTransform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jeckstei
 */
public class TrackContainer extends Component<TrackContainerProps, TrackContainerState> {

    private static final Logger LOG = LoggerFactory.getLogger(TrackContainer.class);

    public TrackContainer() {
        this.state = TrackContainerState.factory();

    }

    @Override
    public TrackContainer beforeComponentReady() {
        this.state = this.getState().setTrackRenderer(
                this.getProps().getTrackRenderer()
        ).setScrollX(
                this.getProps().getScrollX()
        ).setScrollY(
                this.getProps().getScrollY()
        ).sethSlider(
                this.getProps().gethSlider()
        ).setvSlider(
                this.getProps().getvSlider()
        ).setLoadedDataSets(
                this.getProps().getLoadedDataSets()
        ).setSelectedChromosome(
                this.getProps().getSelectedChromosome()
        );
        this.state.getTrackRenderer().setZoomStripeCoordinate(this.getProps().getZoomStripeCoordinate());
        return this;
    }

    private Range<Integer> getCurrentRange() {
        CanvasPane canvasPane = this.getProps().getCanvasPane();
        double hSlider = this.getState().gethSlider();
        double scrollX = this.getState().getScrollX();
        final double xFactor = exponentialScaleTransform(canvasPane, hSlider);
        final double visibleVirtualCoordinatesX = Math.floor(canvasPane.getWidth() / xFactor);
        double xOffset = Math.round((scrollX / 100) * (canvasPane.getModelWidth() - visibleVirtualCoordinatesX));
        return Range.closedOpen((int) xOffset, (int) xOffset + (int) visibleVirtualCoordinatesX);
    }

    @Override
    public List<Component> render() {
        //LOG.info("render track container");

        this.getState().getTrackRenderer().clearCanvas();
        this.getState().getTrackRenderer().setLastMouseClickedPoint(this.getProps().getMouseClickLocation());
        this.getState().getTrackRenderer().setLastMouseDragPoint(this.getProps().getLocalPoint());
        this.getState().getTrackRenderer().setMouseDragging(this.getProps().isMouseDragging());
        
        
        scaleCanvas();
        ScaleEvent scaleEvent = new ScaleEvent(this.getState().gethSlider(), this.getState().getvSlider(), this.getState().getScrollX(), this.getState().getScrollY());
        this.getProps().getCanvasPane().handleScaleEvent(scaleEvent);

        TrackLabel trackLabel = this.getState().getTrackRenderer().getTrackLabel();
        trackLabel.setDimensions(this.getProps().getLabelPane());
        StackPane content = trackLabel.getContent();
        //TODO: fix issues with multiple labels
        try {
            this.getProps().getLabelPane().getChildren().add(content);
        } catch (Exception ex) {
            //
        }

        return Lists.newArrayList();
    }

    private void scaleCanvas() {
        this.getState().getTrackRenderer().scaleCanvas(
                exponentialScaleTransform(
                        this.getProps().getCanvasPane(),
                        this.getState().gethSlider()
                ),
                this.getState().getScrollX(),
                this.getState().getScrollY()
        );
    }

}
