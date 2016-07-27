/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.visualization.component;

import com.google.common.collect.Lists;
import java.util.List;
import javafx.scene.layout.StackPane;
import org.lorainelab.igb.visualization.component.api.Component;
import org.lorainelab.igb.visualization.event.ScaleEvent;
import org.lorainelab.igb.visualization.model.TrackLabel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jeckstei
 */
public class TrackContainer extends Component<TrackContainerProps, TrackContainerState> {

    private static final Logger LOG = LoggerFactory.getLogger(TrackContainer.class);

    public TrackContainer(TrackContainerProps props) {
        this.props = props;
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

    @Override
    public List<Component> render() {
        //LOG.info("render track container");

        this.getState().getTrackRenderer().clearCanvas();
        this.getState().getTrackRenderer().setIsMultiSelectModeActive(this.getProps().getCanvasPane().isMultiSelectModeActive());
        this.getState().getTrackRenderer().setLastMouseClickedPoint(this.getProps().getMouseClickLocation());
        this.getState().getTrackRenderer().setLastMouseDragPoint(this.getProps().getLocalPoint());
        this.getState().getTrackRenderer().setMouseDragging(this.getProps().isMouseDragging());

        scaleCanvas();
        ScaleEvent scaleEvent = new ScaleEvent(this.getState().gethSlider(), this.getState().getvSlider(), this.getState().getScrollX(), this.getState().getScrollY());
        this.getProps().getCanvasPane().handleScaleEvent(scaleEvent);

        TrackLabel trackLabel = this.getState().getTrackRenderer().getTrackLabel();
        trackLabel.setDimensions(this.getProps().getLabelPane());
        StackPane content = trackLabel.getContent();
        this.getProps().getLabelPane().getChildren().add(content);

        return Lists.newArrayList();
    }

    private void scaleCanvas() {
        this.getState().getTrackRenderer().scaleCanvas(
                this.getProps().getxFactor(),
                this.getState().getScrollX(),
                this.getState().getScrollY()
        );
    }

}
