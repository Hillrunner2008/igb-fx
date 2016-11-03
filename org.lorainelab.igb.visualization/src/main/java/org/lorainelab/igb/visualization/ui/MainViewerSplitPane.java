package org.lorainelab.igb.visualization.ui;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import javafx.application.Platform;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.controlsfx.control.action.Action;
import org.lorainelab.igb.visualization.widget.LabelPane;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true, provide = MainViewerSplitPane.class)
public class MainViewerSplitPane extends SplitPane {

    private HBox leftSide;
    private HBox rightSide;
    private VBox rightSideVbox;
    private VerticalZoomSlider verticalZoomSlider;
    private HorizontalPlusMinusSlider horizontalPlusMinusSlider;
    private VerticalScrollBar verticalScrollBar;
    private LabelPane labelPane;
    private StackPane canvasStackPane;
    private CanvasRegion canvasRegion;
    private OverlayRegion overlayRegion;

    public MainViewerSplitPane() {
            Action action;
        canvasStackPane = new StackPane();
        setDividerPositions(0.25);
        leftSide = new HBox();
        leftSide.setMinWidth(50);
        leftSide.setMaxWidth(600);
        SplitPane.setResizableWithParent(leftSide, Boolean.FALSE);
        rightSide = new HBox();
        rightSide.setMinWidth(350);
        rightSideVbox = new VBox();
        getItems().add(leftSide);
        getItems().add(rightSide);
        rightSide.getChildren().add(rightSideVbox);
    }

    @Activate
    public void activate() {
        canvasStackPane.getChildren().add(canvasRegion);
        canvasStackPane.getChildren().add(overlayRegion);
        HBox.setHgrow(rightSideVbox, Priority.ALWAYS);
        VBox.setVgrow(canvasStackPane, Priority.ALWAYS);
        HBox.setHgrow(labelPane, Priority.ALWAYS);
        Platform.runLater(() -> {
            leftSide.getChildren().add(verticalZoomSlider);
            leftSide.getChildren().add(labelPane);
            rightSideVbox.getChildren().add(canvasStackPane);
            rightSideVbox.getChildren().add(horizontalPlusMinusSlider);
            rightSide.getChildren().add(verticalScrollBar);

        });
    }

    @Reference
    public void setVerticalZoomSlider(VerticalZoomSlider verticalZoomSlider) {
        this.verticalZoomSlider = verticalZoomSlider;
    }

    @Reference
    public void setCanvasRegion(CanvasRegion canvasRegion) {
        this.canvasRegion = canvasRegion;
    }

    @Reference
    public void setOverlayRegion(OverlayRegion overlayRegion) {
        this.overlayRegion = overlayRegion;
    }

    @Reference
    public void setHorizontalPlusMinusSlider(HorizontalPlusMinusSlider horizontalPlusMinusSlider) {
        this.horizontalPlusMinusSlider = horizontalPlusMinusSlider;
    }

    @Reference
    public void setVerticalScrollBar(VerticalScrollBar verticalScrollBar) {
        this.verticalScrollBar = verticalScrollBar;
    }

    @Reference
    public void setLabelPane(LabelPane labelPane) {
        this.labelPane = labelPane;
    }

}
