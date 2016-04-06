package org.lorainelab.igb.visualization.model;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.io.IOException;
import java.net.URL;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.lorainelab.igb.visualization.MainApp;
import static org.lorainelab.igb.visualization.util.ColorUtils.colorToWebStyle;

/**
 *
 * @author dcnorris
 */
public class TrackLabel {

    @FXML
    private StackPane leftSideColorIndicator;
    @FXML
    private Label trackLabel;
    @FXML
    private ColorPicker colorPicker;
    @FXML
    private Rectangle colorChooserRect;
    @FXML
    private FontAwesomeIconView lockIcon;
    @FXML
    private StackPane root;
    @FXML
    private StackPane dragGrip;
    private final TrackRenderer trackRenderer;
    private String trackLabelText;
    private final Pane parent;

    public TrackLabel(TrackRenderer trackRenderer, Pane parent) {
        this.trackRenderer = trackRenderer;
        this.trackLabelText = trackRenderer.getTrackLabel();
        this.parent = parent;
        final URL resource = MainApp.class.getClassLoader().getResource("trackLabel.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(resource);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
            setDimensions();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    private void setDimensions() {
        double y = trackRenderer.getCanvasContext().getBoundingRect().getMinY();
        double height = trackRenderer.getCanvasContext().getBoundingRect().getHeight();
        root.setLayoutY(y - root.getLayoutBounds().getMinY());
        root.setPrefSize(parent.getWidth(), height);
        if (height < 75) {
            hideOptionalWidgets();
        }
    }

    @FXML
    private void initialize() {
        if (isCanvasTrack()) {
            hideOptionalWidgets();
        } else {
            colorChooserRect.setFill(Color.DODGERBLUE.brighter());
            leftSideColorIndicator.setStyle(colorToWebStyle(Color.DODGERBLUE));
        }
        trackLabel.setText(trackLabelText);
        dragGrip.setOnMouseEntered(event -> root.getScene().setCursor(Cursor.HAND));
        dragGrip.setOnMouseExited(event -> root.getScene().setCursor(Cursor.DEFAULT));
    }

    public StackPane getRoot() {
        return root;
    }

    private boolean isCanvasTrack() {
        return trackRenderer instanceof CoordinateTrackRenderer;
    }

    private void hideOptionalWidgets() {
        lockIcon.setVisible(false);
        leftSideColorIndicator.setStyle(colorToWebStyle(Color.GRAY));
        colorChooserRect.setVisible(false);
        GridPane.setRowIndex(dragGrip, 0);
        GridPane.setRowSpan(dragGrip, 3);
    }

}
