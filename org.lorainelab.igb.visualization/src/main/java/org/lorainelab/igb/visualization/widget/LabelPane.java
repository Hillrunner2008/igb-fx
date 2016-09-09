package org.lorainelab.igb.visualization.widget;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import com.google.common.collect.Lists;
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
import org.lorainelab.igb.visualization.widget.Widget;
import org.lorainelab.igb.visualization.model.CanvasModel;
import org.lorainelab.igb.visualization.model.TrackLabel;
import org.lorainelab.igb.visualization.model.TracksModel;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true, provide = {LabelPane.class, Widget.class})
public class LabelPane extends Pane implements Widget {

    private TracksModel tracksModel;

    public LabelPane() {
    }

    public void render(CanvasModel canvasModel) {
        getChildren().clear();
        tracksModel.getTrackRenderers().stream()
                .filter(trackRenderer -> trackRenderer.getCanvasContext().isVisible())
                .forEach(trackRenderer -> {
                    TrackLabel trackLabel = trackRenderer.getTrackLabel();
                    trackLabel.setDimensions(this);
                    StackPane content = trackLabel.getContent();
                    getChildren().add(content);
                });
        getChildren().stream()
                .filter(node -> node instanceof StackPane)
                .map(node -> (StackPane) node)
                .forEach((StackPane trackLabelNode) -> {
                    trackLabelNode.setOnDragDetected(
                            (MouseEvent event) -> {
                                if (event.getSource() instanceof StackPane) {
                                    Pane rootPane = (Pane) trackLabelNode.getScene().getRoot();
                                    rootPane.setOnDragOver(dragEvent -> {
                                        dragEvent.acceptTransferModes(TransferMode.ANY);
                                        dragEvent.consume();
                                    });
                                    SnapshotParameters snapshotParams = new SnapshotParameters();
                                    snapshotParams.setTransform(Transform.scale(0.75, 0.75));
                                    WritableImage snapshot = trackLabelNode.snapshot(snapshotParams, null);
                                    Dragboard db = trackLabelNode.startDragAndDrop(TransferMode.MOVE);
                                    ClipboardContent clipboardContent = new ClipboardContent();
                                    clipboardContent.put(DataFormat.PLAIN_TEXT, trackLabelNode.getBoundsInParent().getMinY());
                                    db.setDragView(snapshot, 5, 5);
                                    db.setContent(clipboardContent);
                                }
                                event.consume();
                            }
                    );
                    trackLabelNode.setOnDragDropped((DragEvent event) -> {
                        if (event.getSource() instanceof StackPane) {
                            StackPane dropLocationLabelNode = StackPane.class.cast(event.getSource());
                            boolean droppedAbove = event.getY() < (dropLocationLabelNode.getHeight() / 2);
                            double dropLocationMinY = dropLocationLabelNode.getBoundsInParent().getMinY();
                            Object dragboardContent = event.getDragboard().getContent(DataFormat.PLAIN_TEXT);
                            if (dragboardContent instanceof Double) {
                                double eventTriggerMinY = (Double) dragboardContent;
                                if (dropLocationMinY != eventTriggerMinY) {
                                    Lists.newArrayList(tracksModel.getTrackRenderers()).stream()
                                            .filter(trackRenderer -> trackRenderer.getCanvasContext().isVisible())
                                            .filter(draggedTrackRenderer -> draggedTrackRenderer.getTrackLabel().getContent().getBoundsInParent().getMinY() == eventTriggerMinY)
                                            .findFirst()
                                            .ifPresent(draggedTrackRenderer -> {
                                                Lists.newArrayList(tracksModel.getTrackRenderers()).stream()
                                                        .filter(trackRenderer -> trackRenderer.getTrackLabel().getContent() == dropLocationLabelNode)
                                                        .findFirst()
                                                        .ifPresent(droppedTrackRenderer -> {
                                                            int droppedIndex = droppedTrackRenderer.getWeight();
                                                            if (droppedAbove) {
                                                                tracksModel.getTrackRenderers().remove(draggedTrackRenderer);
                                                                draggedTrackRenderer.setWeight(droppedIndex - 1);
                                                                tracksModel.getTrackRenderers().add(draggedTrackRenderer);
                                                            } else {
                                                                tracksModel.getTrackRenderers().remove(draggedTrackRenderer);
                                                                draggedTrackRenderer.setWeight(droppedIndex + 1);
                                                                tracksModel.getTrackRenderers().add(draggedTrackRenderer);
                                                            }
                                                        });
                                            });
                                }
                            }

                        }
                        event.consume();
                    });
                });
    }

    @Reference
    public void setTracksModel(TracksModel tracksModel) {
        this.tracksModel = tracksModel;
    }

    @Override
    public int getZindex() {
        return 1;
    }

}
