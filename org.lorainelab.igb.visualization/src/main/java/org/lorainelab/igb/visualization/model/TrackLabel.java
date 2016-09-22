package org.lorainelab.igb.visualization.model;

import static com.google.common.base.Preconditions.checkNotNull;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.io.IOException;
import java.net.URL;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import static org.lorainelab.igb.utils.FXUtilities.runAndWait;
import org.lorainelab.igb.visualization.widget.CoordinateTrackRenderer;
import org.lorainelab.igb.visualization.widget.TrackRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
public class TrackLabel {

    private static final Logger LOG = LoggerFactory.getLogger(TrackLabel.class);
    @FXML
    private VBox leftSideColorIndicator;
    @FXML
    private Label trackLabel;
    @FXML
    private FontAwesomeIconView unLockIcon;
    @FXML
    private StackPane root;
    @FXML
    private FontAwesomeIconView dragGrip;
    @FXML
    private VBox bottomDragGrip;
    @FXML
    private VBox lockIconContainer;
    @FXML
    private BorderPane resizeHandleContainer;

    private final TrackRenderer trackRenderer;
    private String trackLabelText;
    private BooleanProperty isHeightLocked;
    private FontAwesomeIconView lockIcon;

    public TrackLabel(TrackRenderer trackRenderer, String trackLabelText, BooleanProperty isHeightLocked) {
        checkNotNull(trackRenderer);
        checkNotNull(trackLabelText);
        checkNotNull(isHeightLocked);
        this.isHeightLocked = isHeightLocked;
        this.trackRenderer = trackRenderer;
        this.trackLabelText = trackLabelText;
        final URL resource = TrackLabel.class.getClassLoader().getResource("trackLabelAlt.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(resource);
        fxmlLoader.setClassLoader(this.getClass().getClassLoader());
        fxmlLoader.setController(this);
        runAndWait(() -> {
            try {
                fxmlLoader.load();
                lockIcon = new FontAwesomeIconView(FontAwesomeIcon.LOCK);
                lockIcon.setFill(unLockIcon.getFill());
                lockIcon.setSize(unLockIcon.getSize());
                if (trackRenderer instanceof CoordinateTrackRenderer) {
                    lockIconContainer.getChildren().remove(unLockIcon);
                    leftSideColorIndicator.setStyle("-fx-background-color: #141414");
                    resizeHandleContainer.setVisible(false);
                }
            } catch (IOException ex) {
                LOG.error(ex.getMessage(), ex);
            }
        });
        initComponenets();
    }

    public void setDimensions(VBox labelContainer) {
        checkNotNull(labelContainer);
        double height = trackRenderer.getCanvasContext().getBoundingRect().getHeight();
        root.setPrefSize(labelContainer.getParent().getBoundsInLocal().getWidth(), height);
        labelContainer.widthProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            root.setPrefWidth(newValue.doubleValue());
        });
    }

    private void initComponenets() {
        trackLabel.setText(trackLabelText);
        trackLabel.setWrapText(true);
        dragGrip.setOnMouseEntered(event -> root.getScene().setCursor(Cursor.HAND));
        dragGrip.setOnMouseExited(event -> root.getScene().setCursor(Cursor.DEFAULT));
        bottomDragGrip.setOnMouseEntered(event -> root.getScene().setCursor(Cursor.S_RESIZE));
//        bottomDragGrip.setOnMouseExited(event -> root.getScene().setCursor(Cursor.DEFAULT));
        root.setOnMouseExited(event -> root.getScene().setCursor(Cursor.DEFAULT));
        unLockIcon.setOnMouseClicked(click -> {
            isHeightLocked.set(true);
            lockIconContainer.getChildren().remove(unLockIcon);
            lockIconContainer.getChildren().add(lockIcon);
        });
        lockIcon.setOnMouseClicked(click -> {
            isHeightLocked.set(false);
            lockIconContainer.getChildren().remove(lockIcon);
            lockIconContainer.getChildren().add(unLockIcon);
        });
    }

    public StackPane getContent() {
        return root;
    }

    public FontAwesomeIconView getDragGrip() {
        return dragGrip;
    }

    public FontAwesomeIconView getUnLockIcon() {
        return unLockIcon;
    }

    public FontAwesomeIconView getLockIcon() {
        return lockIcon;
    }

    public VBox getResizeDragGrip() {
        return bottomDragGrip;
    }

    public TrackRenderer getTrackRenderer() {
        return trackRenderer;
    }

    public String getTrackLabelText() {
        return trackLabelText;
    }

    public ReadOnlyBooleanProperty getIsHeightLocked() {
        return isHeightLocked;
    }

    public void setTrackLabelText(String trackLabelText) {
        this.trackLabelText = trackLabelText;
    }

}
