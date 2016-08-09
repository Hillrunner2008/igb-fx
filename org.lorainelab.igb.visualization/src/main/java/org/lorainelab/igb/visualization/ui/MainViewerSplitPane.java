package org.lorainelab.igb.visualization.ui;

import org.lorainelab.igb.visualization.widget.LabelPane;
import org.lorainelab.igb.visualization.widget.ZoomSliderWidget;
import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import javafx.application.Platform;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

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
    private ZoomSliderWidget zoomSliderWidget;
    private VerticalScrollBar verticalScrollBar;
    private LabelPane labelPane;
    private CanvasRegion canvasRegion;

    public MainViewerSplitPane() {
        setDividerPositions(0.1);
        leftSide = new HBox();
        rightSide = new HBox();
        rightSideVbox = new VBox();
        getItems().add(leftSide);
        getItems().add(rightSide);
        rightSide.getChildren().add(rightSideVbox);
    }

    @Activate
    public void activate() {
        HBox.setHgrow(rightSideVbox, Priority.ALWAYS);
        VBox.setVgrow(canvasRegion, Priority.ALWAYS);
        HBox.setHgrow(labelPane, Priority.ALWAYS);
        Platform.runLater(() -> {
            leftSide.getChildren().add(verticalZoomSlider);
            leftSide.getChildren().add(labelPane);
            rightSideVbox.getChildren().add(canvasRegion);
            rightSideVbox.getChildren().add(zoomSliderWidget);
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
    public void setHorizontalPlusMinusSlider(HorizontalPlusMinusSlider horizontalPlusMinusSlider) {
        this.horizontalPlusMinusSlider = horizontalPlusMinusSlider;
    }

    @Reference
    public void setZoomSliderWidget(ZoomSliderWidget zoomSliderWidget) {
        this.zoomSliderWidget = zoomSliderWidget;
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
