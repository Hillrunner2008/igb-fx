package org.lorainelab.igb.visualization;

import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;

//@Component(immediate = true)
public class ZoomSliderMiniMapWidget {

    @FXML
    private Pane xSliderPane;
    @FXML
    private Rectangle slider;
    @FXML
    private Rectangle leftSliderThumb;
    @FXML
    private Rectangle rightSliderThumb;

    public ZoomSliderMiniMapWidget() {
    }

    private void syncWidgetSlider() {
        double[] scaledPercentage = {0};

//        double minScaleX = canvasPane.getModelWidth();
//        double maxScaleX = MAX_ZOOM_MODEL_COORDINATES_X - 1;
//        final double scaleRange = maxScaleX - minScaleX;

//        final double current = trackRenderer.getView().getBoundingRect().getWidth();
//        scaledPercentage[0] = (current - minScaleX) / scaleRange;
//        double oldWidth = slider.getWidth();
//        double oldX = slider.getX();
//        double width = ((1 - scaledPercentage[0]) * (xSliderPane.getWidth() - 50)) + 50;
//        double x = ((scrollX.getValue() / 100)) * (xSliderPane.getWidth() - width);
//        slider.setX(x);
//        leftSliderThumb.setX(x);
//        slider.setWidth(width);
//        rightSliderThumb.setX(rightSliderThumb.getX() - (oldWidth + oldX - width - x));
    }

    public Pane getContent() {
        return xSliderPane;
    }

}
