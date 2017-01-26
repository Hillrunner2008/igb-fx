package org.lorainelab.igb.visualization.model;

import static com.google.common.base.Preconditions.checkNotNull;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.io.IOException;
import java.net.URL;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import static org.lorainelab.igb.data.model.util.ColorUtils.toHex;
import org.lorainelab.igb.data.model.util.Palette;
import static org.lorainelab.igb.data.model.util.Palette.DEFAULT_GLYPH_FILL;
import static org.lorainelab.igb.data.model.util.Palette.DEFAULT_LABEL_COLOR;
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
//    @FXML
//    private VBox leftSideColorIndicator;
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
    
    public TrackLabel(TrackRenderer trackRenderer, String trackLabelText, BooleanProperty isHeightLocked, boolean isNegative) {
        checkNotNull(trackRenderer);
        checkNotNull(trackLabelText);
        checkNotNull(isHeightLocked);
        this.isHeightLocked = isHeightLocked;
        this.trackRenderer = trackRenderer;
        this.trackLabelText = trackLabelText;
        final URL resource = TrackLabel.class.getClassLoader().getResource("trackLabel.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(resource);
        fxmlLoader.setClassLoader(this.getClass().getClassLoader());
        fxmlLoader.setController(this);
        runAndWait(() -> {
            try {
                fxmlLoader.load();
                trackLabel.textFillProperty().bind(Palette.DEFAULT_LABEL_COLOR);
                root.setBackground(TRACK_LABEL_BG.get());
                root.setStyle("-fx-border-color:" + toHex(DEFAULT_LABEL_COLOR.get()) + "; -fx-border-width: 0 0 1 0;");
//                leftSideColorIndicator.setStyle("-fx-background-color:" + toHex(DEFAULT_GLYPH_FILL.get()));
                trackLabelBackgroundChangeListener = (ObservableValue<? extends Background> observable, Background oldValue, Background updatedBg) -> {
                    root.setBackground(updatedBg);
                    root.setStyle("-fx-border-color:" + toHex(DEFAULT_LABEL_COLOR.get()) + "; -fx-border-width: 0 0 1 0;");
//                    leftSideColorIndicator.setStyle("-fx-background-color:" + toHex(DEFAULT_GLYPH_FILL.get()));
                };
                TRACK_LABEL_BG.addListener(new WeakChangeListener<>(trackLabelBackgroundChangeListener));
                trackLabelBackgroundInvalidationListener = new InvalidationListener() {
                    @Override
                    public void invalidated(Observable observable) {
                        root.setStyle("-fx-border-color:" + toHex(DEFAULT_LABEL_COLOR.get()) + "; -fx-border-width: 0 0 1 0;");
//                        leftSideColorIndicator.setStyle("-fx-background-color:" + toHex(DEFAULT_GLYPH_FILL.get()));
                    }
                };
                TRACK_LABEL_BG.addListener(new WeakInvalidationListener(trackLabelBackgroundInvalidationListener));
                lockIcon = new FontAwesomeIconView(FontAwesomeIcon.LOCK);
                unLockIcon.fillProperty().bind(DEFAULT_LABEL_COLOR);
                lockIcon.fillProperty().bind(DEFAULT_LABEL_COLOR);
                lockIcon.setSize(unLockIcon.getSize());
                if (trackRenderer.hideLockToggle().get()) {
                    lockIconContainer.getChildren().remove(unLockIcon);
                }
                if (isNegative) {
                    resizeHandleContainer.setAlignment(dragGrip, Pos.BOTTOM_LEFT);
                }
                if (trackRenderer instanceof CoordinateTrackRenderer) {
                    resizeHandleContainer.setAlignment(dragGrip, Pos.CENTER);
                    bottomDragGrip.setVisible(false);
                }
            } catch (IOException ex) {
                LOG.error(ex.getMessage(), ex);
            }
        });
        initComponenets();
    }
    private InvalidationListener trackLabelBackgroundInvalidationListener;
    private ChangeListener<Background> trackLabelBackgroundChangeListener;
    
    public void refreshSize(VBox labelContainer, double yFactor) {
        root.setPrefSize(labelContainer.getParent().getBoundsInLocal().getWidth(), trackRenderer.getLabelHeight(yFactor));
    }
    
    private void initComponenets() {
        trackLabel.setText(trackLabelText);
        trackLabel.setWrapText(true);
        dragGrip.setOnMouseEntered(event -> root.getScene().setCursor(Cursor.HAND));
        dragGrip.setOnMouseExited(event -> root.getScene().setCursor(Cursor.DEFAULT));
        bottomDragGrip.setOnMouseEntered(event -> root.getScene().setCursor(Cursor.S_RESIZE));
        bottomDragGrip.setOnMouseExited(event -> root.getScene().setCursor(Cursor.DEFAULT));
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
    
    private static final ObjectProperty<Background> TRACK_LABEL_BG = new SimpleObjectProperty<>(new Background(new BackgroundFill(Palette.DEFAULT_CANVAS_BG.get(), CornerRadii.EMPTY, Insets.EMPTY)));
    private static ChangeListener<Color> DEFAULT_CANVAS_BG_CHANGE_LISTENER;
    private static ChangeListener<Color> DEFAULT_LABEL_COLOR_CHANGE_LISTENER;
    private static ChangeListener<Color> DEFAULT_GLYPH_FILL_COLOR_CHANGE_LISTENER;
    
    static {
        DEFAULT_CANVAS_BG_CHANGE_LISTENER = new ChangeListener<Color>() {
            @Override
            public void changed(ObservableValue<? extends Color> observable, Color oldValue, Color newValue) {
                TRACK_LABEL_BG.set(new Background(new BackgroundFill(newValue, CornerRadii.EMPTY, Insets.EMPTY)));
            }
        };
        Palette.DEFAULT_CANVAS_BG.addListener(new WeakChangeListener<Color>(DEFAULT_CANVAS_BG_CHANGE_LISTENER));
        DEFAULT_LABEL_COLOR_CHANGE_LISTENER = new ChangeListener<Color>() {
            @Override
            public void changed(ObservableValue<? extends Color> observable, Color oldValue, Color newValue) {
                TRACK_LABEL_BG.set(new Background(new BackgroundFill(Palette.DEFAULT_CANVAS_BG.get(), CornerRadii.EMPTY, Insets.EMPTY)));
            }
        };
        Palette.DEFAULT_LABEL_COLOR.addListener(new WeakChangeListener<Color>(DEFAULT_LABEL_COLOR_CHANGE_LISTENER));
        DEFAULT_GLYPH_FILL_COLOR_CHANGE_LISTENER = new ChangeListener<Color>() {
            @Override
            public void changed(ObservableValue<? extends Color> observable, Color oldValue, Color newValue) {
                TRACK_LABEL_BG.set(new Background(new BackgroundFill(Palette.DEFAULT_CANVAS_BG.get(), CornerRadii.EMPTY, Insets.EMPTY)));
            }
        };
        Palette.DEFAULT_GLYPH_FILL.addListener(new WeakChangeListener<Color>(DEFAULT_GLYPH_FILL_COLOR_CHANGE_LISTENER));
    }
}
