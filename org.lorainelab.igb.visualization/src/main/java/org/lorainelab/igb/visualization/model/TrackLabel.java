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
import static org.lorainelab.igb.visualization.util.ColorUtils.colorToWebStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
public class TrackLabel {

    private static final Logger LOG = LoggerFactory.getLogger(TrackLabel.class);
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

    public TrackLabel(TrackRenderer trackRenderer, String trackLabelText) {
        this.trackRenderer = trackRenderer;
        this.trackLabelText = trackLabelText;
        final URL resource = TrackLabel.class.getClassLoader().getResource("trackLabel.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(resource);
        fxmlLoader.setClassLoader(this.getClass().getClassLoader());
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public void setDimensions(Pane parent) {
        double y = trackRenderer.getCanvasContext().getBoundingRect().getMinY();
        double height = trackRenderer.getCanvasContext().getBoundingRect().getHeight();
        LOG.info(trackLabelText + " : height : " + height);
        root.setLayoutY(y - root.getLayoutBounds().getMinY());
        root.setPrefSize(parent.getWidth(), height);
        if (height < 80) {
            hideOptionalWidgets();
        } else {
            showOptionalWidgets();
        }
        Rectangle clipRect = new Rectangle(root.getLayoutX(), root.getLayoutY(), root.getWidth(), root.getHeight());
        System.out.println(clipRect);
    }

    @FXML
    private void initialize() {
        colorChooserRect.setFill(Color.DODGERBLUE.brighter());
        leftSideColorIndicator.setStyle(colorToWebStyle(Color.DODGERBLUE));
        trackLabel.setText(trackLabelText);
        trackLabel.setWrapText(true);
        dragGrip.setOnMouseEntered(event -> root.getScene().setCursor(Cursor.HAND));
        dragGrip.setOnMouseExited(event -> root.getScene().setCursor(Cursor.DEFAULT));
    }

    public StackPane getContent() {
        return root;
    }

    private void hideOptionalWidgets() {
        lockIcon.setVisible(false);
        leftSideColorIndicator.setStyle(colorToWebStyle(Color.GRAY));
        colorChooserRect.setVisible(false);
        GridPane.setRowIndex(dragGrip, 0);
        GridPane.setRowSpan(dragGrip, 3);
    }

    public String getTrackLabelText() {
        return trackLabelText;
    }

    public void setTrackLabelText(String trackLabelText) {
        this.trackLabelText = trackLabelText;
    }

    private void showOptionalWidgets() {
        lockIcon.setVisible(true);
        colorChooserRect.setFill(Color.DODGERBLUE.brighter());
        leftSideColorIndicator.setStyle(colorToWebStyle(Color.DODGERBLUE));
        colorChooserRect.setVisible(true);
        GridPane.setRowIndex(dragGrip, 0);
        GridPane.setRowSpan(dragGrip, 3);
    }
}
