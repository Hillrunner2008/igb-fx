package org.lorainelab.igb.visualization.model;

import com.google.common.base.CharMatcher;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.io.IOException;
import java.net.URL;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import net.miginfocom.layout.CC;
import static org.lorainelab.igb.data.model.util.ColorUtils.colorToWebStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tbee.javafx.scene.layout.MigPane;

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
        root.setLayoutY(y - root.getLayoutBounds().getMinY());
        root.setPrefSize(parent.getWidth(), height);
        if (height < 80) {
            hideOptionalWidgets();
        } else {
            showOptionalWidgets();
        }
        Rectangle clipRect = new Rectangle(0, 0, parent.getWidth(), height);
        root.setClip(clipRect);
    }

    @FXML
    private void initialize() {
        colorChooserRect.setFill(Color.DODGERBLUE.brighter());
        leftSideColorIndicator.setStyle(colorToWebStyle(Color.DODGERBLUE));
        trackLabel.setText(trackLabelText);
        trackLabel.setWrapText(true);
        dragGrip.setOnMouseEntered(event -> root.getScene().setCursor(Cursor.HAND));
        dragGrip.setOnMouseExited(event -> root.getScene().setCursor(Cursor.DEFAULT));
        addContextMenu();
        root.setStyle("-fx-border-width: .5 0 .5 0; -fx-border-color: BLACK;");
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
        GridPane.setRowSpan(dragGrip, 1);
    }

    private void addContextMenu() {
        if (trackRenderer instanceof ZoomableTrackRenderer) {
            ZoomableTrackRenderer zoomableTrackRenderer = (ZoomableTrackRenderer) trackRenderer;
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
                        zoomableTrackRenderer.getTrack().setMaxStackHeight(Integer.parseInt(stackHeightEntryField.getText()));
                        zoomableTrackRenderer.render();
                        stage.hide();
                    });
                }
            });
            Button okBtn = new Button("Ok");
            okBtn.setOnAction(event -> {
                Platform.runLater(() -> {
                    zoomableTrackRenderer.getTrack().setMaxStackHeight(Integer.parseInt(stackHeightEntryField.getText()));
                    zoomableTrackRenderer.render();
                    stage.hide();
                });
            });
            Button cancelBtn = new Button("Cancel");
            cancelBtn.setOnAction(event -> {
                stage.hide();
            });
            stage.setWidth(220);
            
            stage.setHeight(155);
            
            stage.setTitle("Set Track Stack Height");
            migPane.add(label, "growx, wrap");
            migPane.add(stackHeightEntryField, "growx, wrap");
            migPane.add(okBtn, new CC().gap("rel").x("container.x+55").y("container.y+90").span(3).tag("ok").split());
            migPane.add(cancelBtn, new CC().x("container.x+105").y("container.y+90").tag("ok"));
            stage.initModality(Modality.APPLICATION_MODAL);
            
            stage.setResizable(false);
            Scene scene = new Scene(migPane);
            stage.setScene(scene);
            adjustStackHeightMenuItem.setOnAction(action -> {
                stackHeightEntryField.setText("" + zoomableTrackRenderer.getTrack().getStackHeight());
                stage.show();
            });
            contextMenu.getItems().add(adjustStackHeightMenuItem);
            root.setOnMouseClicked(event -> {
                if ((event.getButton() == MouseButton.SECONDARY) || (event.getButton() == MouseButton.PRIMARY && event.isControlDown())) {
                    root.setStyle("-fx-border-color: red;-fx-border-width:2.0;");
                    contextMenu.show(root.getScene().getWindow(), event.getScreenX(), event.getScreenY());
                    contextMenu.setOnHiding(windowEvent -> {
                        root.setStyle("-fx-border-width: .5 0 .5 0; -fx-border-color: BLACK;");
                    });
                }

            });
        }
    }

}
