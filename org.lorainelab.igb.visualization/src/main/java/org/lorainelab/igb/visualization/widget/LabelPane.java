package org.lorainelab.igb.visualization.widget;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import com.google.common.base.CharMatcher;
import com.google.common.collect.Lists;
import com.sun.glass.ui.Application;
import com.sun.glass.ui.Robot;
import java.util.Collections;
import java.util.List;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.collections.SetChangeListener;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Transform;
import javafx.stage.Modality;
import javafx.stage.Stage;
import net.miginfocom.layout.CC;
import org.lorainelab.igb.data.model.CanvasContext;
import org.lorainelab.igb.data.model.StackedGlyphTrack;
import org.lorainelab.igb.data.model.Track;
import static org.lorainelab.igb.data.model.Track.MIN_TRACK_HEIGHT;
import org.lorainelab.igb.visualization.model.CanvasModel;
import org.lorainelab.igb.visualization.model.TrackLabel;
import org.lorainelab.igb.visualization.model.TracksModel;
import org.lorainelab.igb.visualization.ui.VerticalScrollBar;
import static org.lorainelab.igb.visualization.widget.TrackRenderer.SORT_BY_WEIGHT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tbee.javafx.scene.layout.MigPane;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true, provide = {LabelPane.class, Widget.class})
public class LabelPane extends ScrollPane implements Widget {

    private static final Logger LOG = LoggerFactory.getLogger(LabelPane.class);
    private TracksModel tracksModel;
    private CanvasModel canvasModel;
    private AnimationTimer animationTimer;
    private Robot robot;
    private TrackLabel activeLabel;
    private VBox labelContainer;
    private VerticalScrollBar verticalScrollBar;

    public LabelPane() {
        labelContainer = new VBox();
        setContent(labelContainer);
        addEventFilter(ScrollEvent.ANY, (ScrollEvent event) -> event.consume());
        setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        setFitToWidth(true);
        setPannable(false);
        Platform.runLater(() -> {
            robot = Application.GetApplication().createRobot();
            animationTimer = new AnimationTimer() {
                @Override
                public void handle(long now) {
                    int mouseY = robot.getMouseY();
                    if (activeLabel != null) {
                        Bounds screenBounds = activeLabel.getContent().localToScreen(activeLabel.getContent().getBoundsInLocal());
                        double delta = (int) (mouseY - screenBounds.getMaxY());
                        double initialContextHeight = activeLabel.getContent().getLayoutBounds().getHeight();
                        double updatedContextHeight = getUpdatedContextHeight(activeLabel, delta);
                        final TrackRenderer tr = activeLabel.getTrackRenderer();
                        double currentHeightDelta = updatedContextHeight - initialContextHeight;
                        final double currentStretchDelta = tr.stretchDelta().doubleValue();
                        tr.stretchDelta().set(currentStretchDelta + currentHeightDelta);
                        activeLabel.refreshSize(labelContainer, canvasModel.getyFactor().get());
                        canvasModel.forceRefresh();
                    }
                }
            };
        });
    }

    @Activate
    public void activate() {
        refreshLabelPaneContent();
        tracksModel.getTrackRenderers().addListener((SetChangeListener.Change<? extends TrackRenderer> change) -> {
            initializeScrollBarBinding();
            refreshLabelPaneContent();
        });
    }

    public void render(CanvasModel canvasModel) {
        tracksModel.getTrackRenderers().forEach(tr -> tr.getTrackLabel().refreshSize(labelContainer, canvasModel.getyFactor().get()));
    }

    private void refreshLabelPaneContent() {
        labelContainer.getChildren().clear();
        List<TrackRenderer> sortedTrackRenderers = Lists.newArrayList(tracksModel.getTrackRenderers());
        Collections.sort(sortedTrackRenderers, SORT_BY_WEIGHT);
        sortedTrackRenderers.stream()
                .forEach(trackRenderer -> {
                    addTrackLabel(trackRenderer);
                });
        setupDragDropSorting();
    }

    private void setupDragDropSorting() {
        labelContainer.getChildren().stream()
                .filter(node -> node instanceof StackPane)
                .map(node -> (StackPane) node)
                .forEach((StackPane trackLabelNode) -> {
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

    private void addTrackLabel(TrackRenderer trackRenderer) {
        TrackLabel trackLabel = trackRenderer.getTrackLabel();
        trackLabel.refreshSize(labelContainer, canvasModel.getyFactor().get());
        addContextMenu(trackLabel);
        StackPane content = trackLabel.getContent();
        labelContainer.getChildren().add(content);
        addSortDragHandleListener(trackLabel);
        addDragResizeListener(trackLabel);
    }

    public void addSortDragHandleListener(TrackLabel trackLabel) {
        StackPane content = trackLabel.getContent();
        trackLabel.getDragGrip().onMousePressedProperty().set(event -> event.setDragDetect(true));
        trackLabel.getDragGrip().setOnDragDetected(event -> {
            content.getScene().getRoot().setOnDragOver(dragEvent -> {
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
            event.consume();
        });
    }

    private void addDragResizeListener(TrackLabel trackLabel) {
        final VBox dragHangle = trackLabel.getResizeDragGrip();
        dragHangle.onMousePressedProperty().set(event -> {
            event.setDragDetect(true);
            dragHangle.getParent().setCursor(Cursor.S_RESIZE);
        });
        dragHangle.setOnDragDetected(event -> {
            canvasModel.getLabelResizingActive().set(true);
            activeLabel = trackLabel;
            animationTimer.start();
        });
        dragHangle.setOnMouseReleased(event -> {
            animationTimer.stop();
            canvasModel.getLabelResizingActive().set(false);
            if (activeLabel != null) {
                activeLabel.getResizeDragGrip().getParent().setCursor(Cursor.DEFAULT);
                activeLabel = null;
                canvasModel.forceRefresh();
            }
        });
        //TODO it would be nice is the dragHangle.setOnMouseReleased event was a stable solution, but it appears to work unreliably 
        getScene().addEventFilter(MouseEvent.MOUSE_RELEASED, (MouseEvent mouseEvent) -> {
            animationTimer.stop();
            canvasModel.getLabelResizingActive().set(false);
            if (activeLabel != null) {
                activeLabel.getResizeDragGrip().getParent().setCursor(Cursor.DEFAULT);
                activeLabel = null;
                canvasModel.forceRefresh();
            }
        });
    }

    private Rectangle2D getUpdatedContextBoundingRect(TrackLabel trackLabel, double updatedContextHeight) {
        final CanvasContext canvasContext = trackLabel.getTrackRenderer().getCanvasContext();
        Rectangle2D boundingRect = canvasContext.getBoundingRect();
        final Rectangle2D updatedBounds = new Rectangle2D(boundingRect.getMinX(), boundingRect.getMinY(), boundingRect.getWidth(), updatedContextHeight);
        return updatedBounds;
    }

    private double getUpdatedContextHeight(TrackLabel trackLabel, double delta) {
        double initialContextHeight = trackLabel.getContent().getLayoutBounds().getHeight();
        double updatedContextHeight = initialContextHeight + (delta * canvasModel.getyFactor().doubleValue());
        if (updatedContextHeight < MIN_TRACK_HEIGHT) {
            updatedContextHeight = MIN_TRACK_HEIGHT;
        }
        return updatedContextHeight;
    }

    private double getUpdatedTrackHeight(TrackLabel trackLabel, double updatedContextHeight) {
        final CanvasContext canvasContext = trackLabel.getTrackRenderer().getCanvasContext();
        Rectangle2D boundingRect = canvasContext.getBoundingRect();
        double maxY = boundingRect.getMinY() + updatedContextHeight;
        double trackHeight = canvasContext.getTrackHeight();
        if (maxY > getBoundsInLocal().getMaxY()) {
            double outOfViewDelta = (boundingRect.getMinY() + trackHeight) - getBoundsInLocal().getMaxY();
            maxY = getBoundsInLocal().getMaxY();
            trackHeight += outOfViewDelta;
        }
        return trackHeight;
    }

    private void addContextMenu(TrackLabel trackLabel) {
        StackPane root = trackLabel.getContent();
        TrackRenderer trackRenderer = trackLabel.getTrackRenderer();
        if (trackRenderer instanceof ZoomableTrackRenderer) {
            ZoomableTrackRenderer zoomableTrackRenderer = (ZoomableTrackRenderer) trackRenderer;
            final Track track = zoomableTrackRenderer.getTrack();
            if (track instanceof StackedGlyphTrack) {
                StackedGlyphTrack stackedGlyphTrack = (StackedGlyphTrack) track;
                final ContextMenu contextMenu = new ContextMenu();
                MenuItem adjustStackHeightMenuItem = new MenuItem("Set Stack Height...");
                MigPane migPane = new MigPane("fillx", "[][]", "[][][]");
                Stage stage = new Stage();
                Label label = new Label("Enter new maximum track height, 0 for unlimited.");
                label.setWrapText(true);
                TextField stackHeightEntryField = new TextField();
                stackHeightEntryField.textProperty().addListener((observable, oldValue, newValue) -> {
                    stackHeightEntryField.setText(CharMatcher.inRange('0', '9').retainFrom(newValue));
                });
                stackHeightEntryField.setOnKeyPressed((KeyEvent ke) -> {
                    if (ke.getCode().equals(KeyCode.ENTER)) {
                        Platform.runLater(() -> {
                            stackedGlyphTrack.setMaxStackHeight(Integer.parseInt(stackHeightEntryField.getText()));
                            canvasModel.forceRefresh();
                            stage.hide();
                        });
                    }
                });
                Button okBtn = new Button("Ok");
                okBtn.setOnAction(event -> {
                    Platform.runLater(() -> {
                        stackedGlyphTrack.setMaxStackHeight(Integer.parseInt(stackHeightEntryField.getText()));
                        canvasModel.forceRefresh();
                        stage.hide();
                    });
                });
                Button cancelBtn = new Button("Cancel");
                cancelBtn.setOnAction(event -> {
                    stage.hide();
                });

                stage.setTitle("Set Track Stack Height");
                migPane.add(label, "growx, wrap");
                migPane.add(stackHeightEntryField, "growx, wrap");
                migPane.add(okBtn, new CC().gap("rel").x("container.x+55").y("container.y+90").span(3).tag("ok").split());
                migPane.add(cancelBtn, new CC().x("container.x+105").y("container.y+90").tag("ok"));
                stage.initModality(Modality.APPLICATION_MODAL);

                stage.setResizable(false);
                Scene scene = new Scene(migPane);
                stage.sizeToScene();
                stage.setScene(scene);
                adjustStackHeightMenuItem.setOnAction(action -> {
                    stackHeightEntryField.setText("" + stackedGlyphTrack.getStackHeight());
                    stage.show();
                });
                contextMenu.getItems().add(adjustStackHeightMenuItem);
                root.setOnMouseClicked(event -> {
                    if ((event.getButton() == MouseButton.SECONDARY) || (event.getButton() == MouseButton.PRIMARY && event.isControlDown())) {
                        contextMenu.show(root.getScene().getWindow(), event.getScreenX(), event.getScreenY());
                        contextMenu.setOnHiding(windowEvent -> {
                        });
                    }

                });

            }
        }
    }

    @Reference
    public void setTracksModel(TracksModel tracksModel) {
        this.tracksModel = tracksModel;
    }

    @Reference
    public void setCanvasModel(CanvasModel canvasModel) {
        this.canvasModel = canvasModel;
    }

    @Override
    public int getZindex() {
        return 1;
    }

    private void initializeScrollBarBinding() {
        if (vsb == null) {
            for (final Node node : lookupAll(".scroll-bar")) {
                if (node instanceof ScrollBar) {
                    ScrollBar sb = (ScrollBar) node;
                    if (sb.getOrientation() == Orientation.VERTICAL) {
                        vsb = sb;
                        verticalScrollBar.valueProperty().bindBidirectional(vsb.valueProperty());
                        verticalScrollBar.visibleAmountProperty().bindBidirectional(vsb.visibleAmountProperty());
                        verticalScrollBar.blockIncrementProperty().bindBidirectional(vsb.blockIncrementProperty());
                        verticalScrollBar.minProperty().bindBidirectional(vsb.minProperty());
                        verticalScrollBar.maxProperty().bindBidirectional(vsb.maxProperty());
                        break;
                    }
                }
            }
        }
    }
    private ScrollBar vsb;

    @Reference
    public void setVerticalScrollBar(VerticalScrollBar verticalScrollBar) {
        this.verticalScrollBar = verticalScrollBar;
    }

}
