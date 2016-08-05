/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.visualization.component;

import com.google.common.collect.Lists;
import java.util.List;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.transform.Transform;
import org.lorainelab.igb.visualization.component.api.Component;
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
//        this.getState().getTrackRenderer().setIsMultiSelectModeActive(this.getProps().getCanvasPane().isMultiSelectModeActive());
        this.getState().getTrackRenderer().setLastMouseClickedPoint(this.getProps().getMouseClickLocation());
        this.getState().getTrackRenderer().setLastMouseDragPoint(this.getProps().getLocalPoint());
        this.getState().getTrackRenderer().setMouseDragging(this.getProps().isMouseDragging());

        scaleCanvas();
        TrackLabel trackLabel = this.getState().getTrackRenderer().getTrackLabel();
        trackLabel.setDimensions(this.getProps().getLabelPane());
        StackPane content = trackLabel.getContent();

        content.setOnDragDetected((MouseEvent event) -> {
            if (event.getSource() instanceof StackPane) {
                Pane rootPane = (Pane) content.getScene().getRoot();
                rootPane.setOnDragOver(dragEvent -> {
                    dragEvent.acceptTransferModes(TransferMode.ANY);
                    dragEvent.consume();
                });
                SnapshotParameters snapshotParams = new SnapshotParameters();
                snapshotParams.setTransform(Transform.scale(0.75, 0.75));
                WritableImage snapshot = content.snapshot(snapshotParams, null);
                Dragboard db = content.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent clipboardContent = new ClipboardContent();
                clipboardContent.put(DataFormat.PLAIN_TEXT, content.getBoundsInParent().getMinY());
                db.setDragView(snapshot, 5, 5);
                db.setContent(clipboardContent);
            }
            event.consume();
        });

        content.setOnDragDropped((DragEvent event) -> {
            if (event.getSource() instanceof StackPane) {
                StackPane dropLocationLabelNode = StackPane.class.cast(event.getSource());
                boolean droppedAbove = event.getY() < (dropLocationLabelNode.getHeight() / 2);
                double dropLocationMinY = dropLocationLabelNode.getBoundsInParent().getMinY();
                Object dragboardContent = event.getDragboard().getContent(DataFormat.PLAIN_TEXT);
                if (dragboardContent instanceof Double) {
                    double eventTriggerMinY = (Double) dragboardContent;
                    if (dropLocationMinY != eventTriggerMinY) {
//                        Lists.newArrayList(AppStore.getStore().getTrackRenderers()).stream()
//                                .filter(trackRenderer -> trackRenderer.getCanvasContext().isVisible())
//                                .filter(draggedTrackRenderer -> draggedTrackRenderer.getTrackLabel().getContent().getBoundsInParent().getMinY() == eventTriggerMinY)
//                                .findFirst()
//                                .ifPresent(draggedTrackRenderer -> {
//                                    Lists.newArrayList(AppStore.getStore().getTrackRenderers()).stream()
//                                    .filter(trackRenderer -> trackRenderer.getTrackLabel().getContent() == dropLocationLabelNode)
//                                    .findFirst()
//                                    .ifPresent(droppedTrackRenderer -> {
//                                        int droppedIndex = droppedTrackRenderer.getWeight();
//                                        if (droppedAbove) {
//                                            AppStore.getStore().getTrackRenderers().remove(draggedTrackRenderer);
//                                            draggedTrackRenderer.setWeight(droppedIndex - 1);
//                                            AppStore.getStore().getTrackRenderers().add(draggedTrackRenderer);
//                                        } else {
//                                            AppStore.getStore().getTrackRenderers().remove(draggedTrackRenderer);
//                                            draggedTrackRenderer.setWeight(droppedIndex + 1);
//                                            AppStore.getStore().getTrackRenderers().add(draggedTrackRenderer);
//                                        }
//                                        AppStore.getStore().noop();
//                                   });
//                                });
                    }
                }

            }
            event.consume();
        });

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
